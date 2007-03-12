package test.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

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

    
    
    @Before public void setUp() throws UnknownHostException
    {
        mock = new MockSearchClient();
        handler = new MessageHandler(SearchId.fromHex("0987654321098765432109876543210987654321"), mock);
        targetNode = new NodeState(SearchId.fromHex("1234567890123456789012345678901234567890"), 
                        InetAddress.getByName("localhost"), 5678);
        
    }

    @Test public void respondToPing() throws IOException
    {     
        handler.respondTo(pingMessage(), targetNode.address, targetNode.port); 
        
        assertEquals("ping", mock.lastMessage.getCommand());
        assertEquals("0987654321098765432109876543210987654321", mock.lastMessage.arguments().get("id"));
        assertEquals(targetNode, mock.lastDestination);
    }
    
    @Test public void pingUpdatesRoutingTable() throws IOException
    {            
        
        handler.respondTo(pingMessage(), targetNode.address, targetNode.port);        

        NodeState node = handler.routingTable().getRoutingTable().get(156).get(0);

        assertEquals("1234567890123456789012345678901234567890", node.id.toString());
        assertEquals("127.0.0.1", node.address.getHostAddress());
        assertEquals(5678, node.port);       
    }  
    
    @Test public void canPingNodesOnRequest() throws IOException
    {
        handler.ping(targetNode);
        
        assertEquals("ping", mock.lastMessage.getCommand());
        assertEquals("0987654321098765432109876543210987654321", mock.lastMessage.arguments().get("id"));
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
    
    // TODO the store tests need to be refactored
    @Test public void storeCommandAddsItemToTheDatabase()
    {        
        SearchResult r = createSearchResult();
        SearchId fileNameHash = r.fileNameHash;
        
        SearchMessage storeMessage = r.createMessage("store");
        storeMessage.arguments().put("id", targetNode.id.toString());
        
        handler.respondTo(storeMessage, targetNode.address, targetNode.port);
        
        assertEquals(r, handler.database().get(fileNameHash));
    }
    
    @Test public void encounterWithANodeReplicatesDatabase() throws UnknownHostException
    {
        SearchResult r = createSearchResult();
        SearchId fileNameHash = r.fileNameHash;
        SearchMessage storeMessage = r.createMessage("store");
        storeMessage.arguments().put("id", targetNode.id.toString());
        
        handler.respondTo(storeMessage, targetNode.address, targetNode.port); 
        NodeState unknownNode = new NodeState(fileNameHash, InetAddress.getByName("localhost"), 45);
        SearchMessage unknownPing = new SearchMessage("ping");
        unknownPing.arguments().put("id", fileNameHash.toString());
        handler.respondTo(unknownPing, unknownNode.address, unknownNode.port);
        
        assertEquals(unknownNode, mock.lastDestination);
        assertEquals("store", mock.lastMessage.getCommand());
        assertEquals(fileNameHash.toString(), mock.lastMessage.arguments().get("file_name"));
    }
    
    @Test public void dontReplicateToRecentlySeenNodes() throws UnknownHostException
    {
        SearchResult r = createSearchResult();
        SearchId fileNameHash = r.fileNameHash;
        SearchMessage storeMessage = r.createMessage("store");
        storeMessage.arguments().put("id", targetNode.id.toString());
        
        handler.respondTo(storeMessage, targetNode.address, targetNode.port); 
        NodeState unknownNode = new NodeState(fileNameHash, InetAddress.getByName("localhost"), 45);
        SearchMessage unknownPing = new SearchMessage("ping");
        unknownPing.arguments().put("id", fileNameHash.toString());
        handler.respondTo(unknownPing, unknownNode.address, unknownNode.port);
        
        handler.respondTo(unknownPing, unknownNode.address, unknownNode.port);
        assertEquals(unknownNode, mock.lastDestination);
        assertEquals("ping", mock.lastMessage.getCommand());
    }
    
    @Test public void storeCommandDoesNotDuplicateDatabaseToEveryNode() throws UnknownHostException
    {
        SearchResult r = createSearchResult();       
        SearchMessage storeMessage = r.createMessage("store");
        storeMessage.arguments().put("id", targetNode.id.toString());
        
        handler.respondTo(storeMessage, targetNode.address, targetNode.port); 
        NodeState unknownNode = new NodeState(SearchId.fromHex("0000000000000000000000000000000000000000"), 
                InetAddress.getByName("localhost"), 45);
        SearchMessage unknownPing = new SearchMessage("ping");
        unknownPing.arguments().put("id", "0000000000000000000000000000000000000000");
        handler.respondTo(unknownPing, unknownNode.address, unknownNode.port);
        
        assertEquals(unknownNode, mock.lastDestination);
        assertEquals("ping", mock.lastMessage.getCommand());        
    }
    
    public SearchResult createSearchResult()
    {
        SearchId fileNameHash = SearchId.fromHex("ffffffffffffffffffffffffffffffffffffffff");
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
        
        return new SearchResult(fileNameHash, fileHash, chunkHashes, 4*512*1024-100, peers);
    }

    @Test
    public void respondToFindNode() throws IOException
    {
    	handler.findNode(targetNode, SearchId.fromHex("8765432109876543210987654321098765432109"));

    	assertEquals("find_node", mock.lastMessage.getCommand());
        assertEquals("0987654321098765432109876543210987654321", mock.lastMessage.arguments().get("id"));
        assertEquals("8765432109876543210987654321098765432109", mock.lastMessage.arguments().get("target"));
        assertEquals(targetNode, mock.lastDestination);
    }

    private SearchMessage pingMessage()
    {
        SearchMessage pingMessage = new SearchMessage("ping");
        pingMessage.arguments().put("id", "1234567890123456789012345678901234567890");
        return pingMessage;
    }   
}
