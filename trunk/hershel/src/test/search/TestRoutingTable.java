package test.search;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.search.NodeState;
import com.search.Pinger;
import com.search.RoutingTable;
import com.search.SearchId;

public class TestRoutingTable
{
    private RoutingTable t;

    private String myId = "1234567890123456789012345678901234567890";

    private String[] otherIds = { 
            "1234567890123456789012345678901234567800",
            "1234567890123456789012345678901234567810", 
            "1234567890123456789012345678901234567895", };

    private SearchId[] ids;

    private NodeState node1;
    private NodeState node2;
    private NodeState node3;
    
    private MockPinger mockPinger;

    @Before
    public void setUp() throws Exception
    {
        mockPinger = new MockPinger();
        SearchId id = SearchId.fromHex(myId);
        t = new RoutingTable(id, 2, mockPinger);

        ids = new SearchId[] { 
                SearchId.fromHex(otherIds[0]), 
                SearchId.fromHex(otherIds[1]),
                SearchId.fromHex(otherIds[2]), };
        
        node1 = new NodeState(ids[0], InetAddress.getByName("localhost"), 5670);
        t.addNode(node1);
        node2 = new NodeState(ids[1], InetAddress.getByName("localhost"), 5670);
        t.addNode(node2);
        node3 = new NodeState(ids[2], InetAddress.getByName("localhost"), 5670);
        t.addNode(node3);
        
        
    }

    @Test
    public void addToTable()
    {       

        byte[] d = SearchId.getDistance(ids[0], SearchId.fromHex(myId));
        assertEquals(new BigInteger("144"), new BigInteger(d));
        d = SearchId.getDistance(ids[1], SearchId.fromHex(myId));
        assertEquals(new BigInteger("128"), new BigInteger(d));
        d = SearchId.getDistance(ids[2], SearchId.fromHex(myId));
        assertEquals(new BigInteger("5"), new BigInteger(d));

        ArrayList<NodeState> al = (ArrayList<NodeState>) (t.getRoutingTable().get(7));
        assertEquals(node1, al.get(0));
        assertEquals(node2, al.get(1));
        al = (ArrayList<NodeState>) (t.getRoutingTable().get(2));
        assertEquals(node3, al.get(0));
    }
    
    @Test public void routingTablePingsIfKBucketIsFull() throws UnknownHostException
    {
        t.addNode(new NodeState(SearchId.fromHex("1234567890123456789012345678901234567811"), InetAddress.getByName("localhost"), 5678));
        assertEquals(node1, mockPinger.lastNodePinged);
    }
    
    @Test public void ifPingReturnsDontAddNewNode() throws UnknownHostException
    {
        t.addNode(new NodeState(SearchId.fromHex("1234567890123456789012345678901234567811"), InetAddress.getByName("localhost"), 5678));
        mockPinger.makeSuccessFullPing();
        assertEquals(node1, t.getRoutingTable().get(7).get(1));
    }
    
    @Test public void ifPingFailsAddNewNode() throws UnknownHostException
    {
        NodeState newNode = new NodeState(SearchId.fromHex("1234567890123456789012345678901234567811"), InetAddress.getByName("localhost"), 5678);
        t.addNode(newNode);
        assertEquals(node1, t.getRoutingTable().get(7).get(0));
        mockPinger.failPing();
        assertEquals(newNode, t.getRoutingTable().get(7).get(1));
    }   
    
    public class MockPinger implements Pinger
    {
        public NodeState lastNodePinged;
        public NodeState replacement;
        
        
        public void putPingRequest(NodeState targetNode, NodeState replacementNode)
        {
            lastNodePinged = targetNode;
            replacement = replacementNode;
        }


        public void makeSuccessFullPing()
        {
            t.pingResponded(lastNodePinged);            
        }
        
        public void failPing()
        {
            t.pingTimedOut(lastNodePinged, replacement);
        }


        public void setRoutingTable(RoutingTable table)
        {
                
        }


        public void pingReceived(SearchId id)
        {
            
            
        }


        public void setTimeout(int millis)
        {
                        
        }
    }
}
