package com.search;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
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
    
    private Hashtable<SearchId, SearchResult> storedValues;
    
    public MessageHandler(SearchId myId, SearchClient client)
    {
        this.myId = myId;
        pinger = new DefaultPinger(this);
        table = new RoutingTable(myId, 20, pinger);  
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

    private SearchMessage packageNodeList(List nodes)
    {
    	SearchMessage nodeList = new SearchMessage("node_list");
    	nodeList.arguments().put("nodes", nodes.toString());
    	return nodeList;
    }

    public void respondTo(SearchMessage request, InetAddress address, int port)
    {
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
            	client.sendMessage(packageNodeList(nodes), node);
            }
            else if (request.getCommand().equals("node_list"))
            {
            	String nodeList = request.arguments().get("nodes");
            	nodeList = nodeList.replaceAll("[\\[|,\\]]", "");
            	String[] nodes = nodeList.split(", ");
            	for (String s : nodes)
            	{
            		String[] args = s.split(";");
            		NodeState n = new NodeState(args[0],
            				InetAddress.getByName(args[1]),
            				Integer.parseInt(args[2]));
            		table.addNode(n);
            	}
            }
            else if(request.getCommand().equals("store"))
            {
                SearchResult r = SearchResult.fromMessage(request);
                storedValues.put(r.fileNameHash, r);
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
        for(Entry<SearchId, SearchResult> e : storedValues.entrySet())
        {        
            BigInteger myDistance = new BigInteger(SearchId.getDistance(myId, e.getKey()));
            BigInteger guysDistance = new BigInteger(SearchId.getDistance(node.id, e.getKey()));
            
            assert myDistance.signum() == 1;
            assert guysDistance.signum() == 1;
            
            if(myDistance.compareTo(guysDistance) > 0)
            {
                SearchMessage replicateMessage = e.getValue().storeMessage();
                replicateMessage.arguments().put("id", myId.toString());
                client.sendMessage(replicateMessage, node);
            }            
        }
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

}
