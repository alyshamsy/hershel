package com.search;

import java.io.IOException;
import java.net.InetAddress;

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
                client.sendMessage(pingMessage(), node); 
            }
            catch (IOException e)
            {
                // TODO This exception should be handled some how
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

}
