package com.search;

import java.io.IOException;
import java.net.InetAddress;

public class MessageHandler implements PingCommunicator
{
    private SearchId myId;
    private RoutingTable table;
    private SearchClient client;
    
    public MessageHandler(SearchId myId, SearchClient client)
    {
        this.myId = myId;
        table = new RoutingTable(myId, 20, new DefaultPinger(this));  
        this.client = client;
    }    
    
    public void ping(NodeState targetNode)
    {
        // TODO Auto-generated method stub
        
    }
    
    public void respondTo(SearchMessage request, InetAddress address, int port) throws IOException
    {
        SearchMessage response = new SearchMessage("ping");
        response.arguments().put("id", myId.toString());
       
        NodeState node = new NodeState(request.arguments().get("id"),
                				address,
                				port);
        table.addNode(node);
        client.sendMessage(response, node);       
    }

}
