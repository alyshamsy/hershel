package com.search;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;


public class MessageHandler implements PingCommunicator
{
    private SearchId myId;
    private RoutingTable table;
    private SearchClient client;
    private Pinger pinger;
    
    public MessageHandler(SearchId myId, SearchClient client)
    {
        this.myId = myId;
        pinger = new DefaultPinger(this);
        table = new RoutingTable(myId, 20, pinger);  
        this.client = client;
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
        if(pinger.expected(node.id))
        {
            pinger.pingReceived(node.id);
        }
        else
        {
            try
            {
                table.addNode(node);
                String command = request.getCommand();
                if (command.equals("ping"))
                {
                	client.sendMessage(pingMessage(), node);
                }
                else if (command.equals("find_node"))
                {
                	Map<String, String> args = request.arguments();
                	String target = args.get("target");
                	List nodes = table.findNode(new SearchId(target));
                	client.sendMessage(packageNodeList(nodes), node);
                }
                else if (command.equals("node_list"))
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
            }
            catch (IOException e)
            {
                // TODO This exception should be handled somehow
                e.printStackTrace();
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
        // TODO Auto-generated method stub
        return null;
    }  

}
