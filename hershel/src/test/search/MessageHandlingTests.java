package test.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.search.MessageHandler;
import com.search.NodeState;
import com.search.Pinger;
import com.search.RoutingTable;
import com.search.SearchClient;
import com.search.SearchId;
import com.search.SearchMessage;
import com.search.SearchResult;


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
    
    @Test public void canPingNodesOnRequest() throws IOException
    {
        handler.ping(targetNode);
        
        assertEquals("ping", mock.lastMessage.getCommand());
        assertEquals("09876543210987654321", mock.lastMessage.arguments().get("id"));
        assertEquals(targetNode, mock.lastDestination);
    }
    
    @Test public void dontReplyToExpectedPings() throws IOException
    {
        handler.setPinger(new Pinger()
        {
            public void pingReceived(SearchId id)
            {}

            public void putPingRequest(NodeState targetNode, NodeState replacementNode) throws IOException
            {}

            public void setRoutingTable(RoutingTable table)
            {}

            public void setTimeout(int millis)
            {}

            public void close()
            {}

            public boolean expected(SearchId id)
            {
                return true;
            }
            
        });
        handler.respondTo(pingMessage(), targetNode.address, targetNode.port);
        
        assertNull(mock.lastDestination);
        assertNull(mock.lastMessage);
    }
    
    /*@Test public void storeCommandAddsItemToTheDatabase()
    {        
        SearchId fileNameHash = SearchId.getRandomId();
        SearchId fileHash = SearchId.getRandomId();
        ArrayList<SearchId> chunkHashes = new ArrayList<SearchId>();
        for(int i = 0; i<4; i++)
        {
            chunkHashes.add(SearchId.getRandomId());
        }
        
        ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();
        for(int i = 0; i<4; i++)
        {
            peers.add(new InetSocketAddress("localhost", i+10));           
        }
        
        SearchResult r = new SearchResult(fileNameHash, fileHash, chunkHashes, 4*512*1024-100, peers);
        SearchMessage storeMessage = r.storeMessage();
        
        handler.respondTo(storeMessage, targetNode.address, targetNode.port);
        
        assertEquals(r, handler.database().get(fileNameHash));
    }*/

    @Test
    public void respondToFindNode() throws IOException
    {
    	handler.findNode(targetNode, new SearchId("87654321098765432109"));

    	assertEquals("find_node", mock.lastMessage.getCommand());
        assertEquals("09876543210987654321", mock.lastMessage.arguments().get("id"));
        assertEquals("87654321098765432109", mock.lastMessage.arguments().get("target"));
        assertEquals(targetNode, mock.lastDestination);
    }

    private SearchMessage pingMessage()
    {
        SearchMessage pingMessage = new SearchMessage("ping");
        pingMessage.arguments().put("id", "12345678901234567890");
        return pingMessage;
    }   
}
