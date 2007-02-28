package test.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.search.MessageHandler;
import com.search.NodeState;
import com.search.SearchClient;
import com.search.SearchId;
import com.search.SearchMessage;


public class MessageHandlingTests
{
    private MockSearchClient mock;
    private MessageHandler handler;
    private NodeState targetNode;

    public class MockSearchClient implements SearchClient
    {
        
        public SearchMessage lastMessage;
        public NodeState lastDestination;
        public void close()
        {            
            
        }

        public void sendMessage(SearchMessage message, NodeState destination) throws IOException
        {
            lastMessage = message;
            lastDestination = destination;            
        }

    }
    
    @Before public void setUp() throws UnknownHostException
    {
        mock = new MockSearchClient();
        handler = new MessageHandler(new SearchId("09876543210987654321"), mock);
        targetNode = new NodeState(SearchId.fromString("12345678901234567890"), 
                        InetAddress.getByName("localhost"), 5678);
        
    }

    @Test public void respondToPing() throws IOException
    {     
        handler.respondTo(pingMessage(), targetNode.address, targetNode.port); 
        
        assertEquals("ping", mock.lastMessage.getCommand());
        assertEquals("09876543210987654321", mock.lastMessage.arguments().get("id"));
        assertEquals(targetNode, mock.lastDestination);
    }
    
    @Test public void pingUpdatesRoutingTable() throws IOException
    {            
        
        handler.respondTo(pingMessage(), targetNode.address, targetNode.port);        

        // TODO Make sure that 152 is the right kbucket
        NodeState node = handler.routingTable().getRoutingTable().get(152).get(0);

        assertEquals("12345678901234567890", node.id.toString());
        assertEquals("127.0.0.1", node.address.getHostAddress());
        assertEquals(5678, node.port);       
    }  
    
    private SearchMessage pingMessage()
    {
        SearchMessage pingMessage = new SearchMessage("ping");
        pingMessage.arguments().put("id", "12345678901234567890");
        return pingMessage;
    }   
}
