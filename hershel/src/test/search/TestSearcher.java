package test.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;

import com.search.*;

import org.junit.Before;
import org.junit.Test;

public class TestSearcher {

	private MockSearchClient client;
	private DefaultSearcher searcher;
	private RoutingTable table;

	private class MockPinger implements Pinger {

		public void close() {
		
			
		}

		public boolean expected(SearchId id) {
			
			return false;
		}

		public void pingReceived(SearchId id) {
			
			
		}

		public void putPingRequest(NodeState targetNode, NodeState replacementNode) throws IOException {
			
			
		}

		public void setRoutingTable(RoutingTable table) {
			
			
		}

		public void setTimeout(int millis) {
			
			
		}
		
	}

	@Before
	public void setUp() throws Exception {
		client = new MockSearchClient();
		SearchId id = SearchId.getRandomId();
		table = new RoutingTable(id, 10, new MockPinger());
		searcher = new DefaultSearcher(table, client, id, 2);
		searcher.start();
		String nodeId =
            "1234567890123456789012345678901234567800";
		id =  SearchId.fromHex(nodeId);
        
        NodeState node = new NodeState(id,
        		InetAddress.getByName("localhost"), 5670);
        table.addNode(node);
	}

	@Test public void findValueMessageSent() throws IOException {
		SearchId fileName =
			SearchId.fromHex("1234567890123456789012345678901234567890");
		searcher.putSearchRequest(fileName);

		SearchMessage sm =
			((MockSearchClient)client).lastMessage;
		assertEquals("find_value", sm.getCommand());
		assertEquals(fileName.toString(), sm.arguments().get("file_name"));
	}
	
	@Test public void continueSearchingAfterFirstFailure() throws IOException, InterruptedException
	{
		SearchId fileName =
			SearchId.fromHex("1234567890123456789012345678901234567890");
		searcher.putSearchRequest(fileName);
		
		NodeState newNode = new NodeState(SearchId.fromHex("1234567890123456789012345678901234567891"), 
				InetAddress.getByName("localhost"), 5671);
		
		client.lastDestination = null;
		client.lastMessage = null;
		table.addNode(newNode);		
		searcher.searchFailed(fileName);
		
		Thread.sleep(150);
		SearchMessage sm = client.lastMessage;
		assertEquals("find_value", sm.getCommand());
		assertEquals(newNode, client.lastDestination);
	}

}