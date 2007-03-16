package com.search;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class MessageHandler implements PingCommunicator
{
    private SearchId myId;
    private RoutingTable table;
    private SearchClient client;
    private Pinger pinger;
    private Searcher searcher;
    
    private Hashtable<NodeState, Long> recentlyReplicatedNodes;
    
    private Hashtable<SearchId, SearchResult> storedValues;
    
    public MessageHandler(SearchId myId, SearchClient client)
    {
    	recentlyReplicatedNodes = new Hashtable<NodeState, Long>();
        this.myId = myId;
        DefaultPinger p = new DefaultPinger(this);
        p.start();
        pinger = p;
        table = new RoutingTable(myId, 20, pinger);
        searcher = new DefaultSearcher(table, client, myId);
        searcher.start();
        this.client = client;
        storedValues = new Hashtable<SearchId, SearchResult>();
    }    
    
    public void ping(NodeState targetNode) throws IOException
    {
        client.sendMessage(pingMessage(), targetNode);
    }

    private SearchMessage pingMessage()
    {
        SearchMessage ping = new SearchMessage("ping");
        ping.arguments().put("id", myId.toString());
        return ping;
    }

    public void findNode(NodeState targetNode, SearchId targetId) throws IOException
    {
    	SearchMessage find = new SearchMessage("find_node");
    	find.arguments().put("target", targetId.toString());
    	find.arguments().put("id", myId.toString());
    	client.sendMessage(find, targetNode);
    }

    public void findValue(SearchId fileName) throws IOException
    {
    	searcher.putSearchRequest(fileName);
    }

    private SearchMessage packageNodeList(List nodes, String message)
    {
    	SearchMessage nodeList = new SearchMessage(message);
    	nodeList.arguments().put("nodes", nodes.toString());
    	nodeList.arguments().put("id", myId.toString());
    	return nodeList;
    }

    private void addNodesToTable(SearchMessage request) throws IOException
    {
    	String nodeList = request.arguments().get("nodes");
    	nodeList = nodeList.replaceAll("[\\[\\]]", "");
    	String[] nodes = nodeList.split(", ");

    	if (nodeList.length() == 0) return;

    	for (String s : nodes)
    	{
    		String[] args = s.split(";");
    		String address = args[1].substring(args[1].indexOf('/') + 1);
    		NodeState n = new NodeState(args[0],
    				InetAddress.getByName(address),
    				Integer.parseInt(args[2]));
    		table.addNode(n);
    	}
    	client.sendToUI(table.getRoutingTable().toString(), "");
    }

    public void respondTo(SearchMessage request, InetAddress address, int port)
    {
    	client.sendToUI(request.toString(), "> ");
    	if(!request.arguments().containsKey("id"))
    		System.out.println(request.getCommand());
    	
        NodeState node = new NodeState(request.arguments().get("id"),
                				address,
                				port);
        try
        {
            table.addNode(node);
            if (request.getCommand().equals("ping"))
            {
                if(pinger.expected(node.id))
                {
                    pinger.pingReceived(node.id);
                }
                else
                {
                    client.sendMessage(pingMessage(), node);
                }
            }
            else if (request.getCommand().equals("find_node"))
            {
            	Map<String, String> args = request.arguments();
            	String target = args.get("target");
            	List nodes = table.findNode(SearchId.fromHex(target));
            	client.sendMessage(packageNodeList(nodes, "node_list"), node);
            }
            else if (request.getCommand().equals("node_list"))
            {
            	addNodesToTable(request);
            }
            else if(request.getCommand().equals("store"))
            {
                SearchResult r = SearchResult.fromMessage(request);
                storedValues.put(r.fileNameHash, r);
            }
            else if(request.getCommand().equals("find_value"))
            {
            	SearchId fileName = SearchId.fromHex(request.arguments().get("file_name"));
            	SearchResult result = storedValues.get(fileName);
            	if (result != null)
            	{
            		SearchMessage m = result.createMessage("search_result");
            		m.arguments().put("id", myId.toString());
            		client.sendMessage(m, node);
            	}
            	else
            	{
                	List nodes = table.findNode(fileName);
                	SearchMessage searchFailed = packageNodeList(nodes, "search_failed");
                	searchFailed.arguments().put("file_name", fileName.toString());
                	client.sendMessage(searchFailed, node);
            	}
            }
            else if(request.getCommand().equals("search_result"))
            {
            	SearchResult r = SearchResult.fromMessage(request);
            	searcher.searchSuccessful(r.fileNameHash);
                storedValues.put(r.fileNameHash, r);
                // startDownload(r);
            }
            else if(request.getCommand().equals("search_failed"))
            {
            	addNodesToTable(request);
            	String fileName = request.arguments().get("file_name");
            	searcher.searchFailed(SearchId.fromHex(fileName));
            }

            replicateDatabaseTo(node);
        }
        catch (IOException e)
        {
            // TODO This exception should be handled somehow
            e.printStackTrace();
        }   
    }

    private void replicateDatabaseTo(NodeState node) throws IOException
    {
    	if(recentlyReplicatedNodes.containsKey(node) && 
    			System.currentTimeMillis() - recentlyReplicatedNodes.get(node) < 100000)
    		return;
    	
    	recentlyReplicatedNodes.put(node, System.currentTimeMillis());
        for(Entry<SearchId, SearchResult> e : storedValues.entrySet())
        {
            byte[] myDistance = SearchId.getDistance(myId, e.getKey());
            byte[] guysDistance = SearchId.getDistance(node.id, e.getKey());
      
            // TODO we need a distance class
            if(compareTo(myDistance, guysDistance) > 0)           
            {
                SearchMessage replicateMessage = e.getValue().createMessage("store");
                replicateMessage.arguments().put("id", myId.toString());
                client.sendMessage(replicateMessage, node);
            }
        }
    }
    
    private int compareTo(byte[] first, byte[] second)
    {
        for (int i = 19; i >= 0; i--)
        {
            if(first[i] != second[i])
            {
                return ((int)first[i] & 0x000000ff) - ((int)second[i] & 0x000000ff);
            }
        }
        return 0;
    }

    public RoutingTable routingTable()
    {
        return table;
    }

    public void setPinger(Pinger pinger)
    {
       this.pinger.close();
       this.pinger = pinger;
       table.setPinger(pinger);        
    }

    public Map<SearchId, SearchResult> database()
    {       
        return storedValues;
    }  

    public SearchId getId()
    {
    	return myId;
    }

}
