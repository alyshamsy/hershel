package test.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.search.DefaultPinger;
import com.search.NodeState;
import com.search.PingCommunicator;
import com.search.Pinger;
import com.search.RoutingTable;
import com.search.SearchId;


public class TestPinger
{    

    private MockPingCommunicator mock;
    private DefaultPinger pinger;
    private FakeRoutingTable table;
    private NodeState target;
    private NodeState replacement;

    @Before public void setUp() throws UnknownHostException
    {
        mock = new MockPingCommunicator();
        pinger = new DefaultPinger(mock);
        pinger.start();        
        table = new FakeRoutingTable(pinger);
        target = new NodeState(SearchId.fromString("12345678901234567890"), InetAddress.getByName("localhost"), 6789);
        replacement = new NodeState(SearchId.fromString("09876543210987654321"), InetAddress.getByName("localhost"), 6789);
    }

    @After public void tearDown()
    {
        pinger.close();
    }
    
    @Test public void pingerRequestsPings() throws IOException
    {        
        pinger.putPingRequest(target, replacement);        
        assertEquals(target, mock.pingedNodes.get(0));
    }
    
    @Test public void informRoutingTableOfSuccessfulPing() throws IOException
    { 
        pinger.putPingRequest(target, replacement);
        pinger.pingReceived(SearchId.fromString("12345678901234567890"));
        
        assertEquals(target, table.nodeToKeep);
    }
    
    @Test public void informRoutingTableAboutTimeOut() throws InterruptedException, IOException
    {
        pinger.setTimeout(100);
        pinger.putPingRequest(target, replacement);
        Thread.sleep(400);
        
        assertEquals(replacement, table.nodeToKeep);
        assertEquals(target, table.nodeToReplace);
    }
    
    @Test public void doNotingIfPingIsNotPending() throws IOException
    {
        pinger.putPingRequest(target, replacement);
        pinger.pingReceived(SearchId.getRandomId());
        
        assertNull(table.nodeToKeep);
        assertNull(table.nodeToReplace);
    }
    
    @Test public void onlyOnePendingPingForTheSameNode() throws InterruptedException, IOException
    {
        pinger.setTimeout(100);
        pinger.putPingRequest(target, replacement);
        pinger.putPingRequest(target, new NodeState(SearchId.getRandomId(), null, 1234));
        pinger.pingReceived(SearchId.fromString("12345678901234567890"));
        
        assertEquals(target, table.nodeToKeep);
        table.nodeToKeep = null;
        Thread.sleep(200);
        assertNull(table.nodeToKeep);
        assertNull(table.nodeToReplace);
        assertEquals(1, table.totalCalls);
    }
    
    public class MockPingCommunicator implements PingCommunicator
    {

        public ArrayList<NodeState> pingedNodes = new ArrayList<NodeState>();

        public void ping(NodeState targetNode)
        {
            pingedNodes.add(targetNode);            
        }
    }
    
    public class FakeRoutingTable extends RoutingTable
    {       
        public int totalCalls  = 0;
        public synchronized void pingTimedOut(NodeState nodePinged, NodeState replacement)
        {
            nodeToReplace = nodePinged;
            nodeToKeep = replacement;
            totalCalls+=1;
        }

        public NodeState nodeToReplace;
        public NodeState nodeToKeep;

        public FakeRoutingTable(Pinger pinger)
        {
            super(SearchId.getRandomId(), 0, pinger);           
        }
        
        public void pingResponded(NodeState node)
        {
            nodeToKeep = node;
            totalCalls+=1;
        }   
       
    }
}
