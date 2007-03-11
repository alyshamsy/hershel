package test.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;

import com.search.*;

import org.junit.Before;
import org.junit.Test;

public class TestSearcher {

	private SearchClient client;
	private DefaultSearcher searcher;
	private RoutingTable table;

	private class MockPinger implements Pinger {

		public void close() {
			// TODO Auto-generated method stub
			
		}

		public boolean expected(SearchId id) {
			// TODO Auto-generated method stub
			return false;
		}

		public void pingReceived(SearchId id) {
			// TODO Auto-generated method stub
			
		}

		public void putPingRequest(NodeState targetNode, NodeState replacementNode) throws IOException {
			// TODO Auto-generated method stub
			
		}

		public void setRoutingTable(RoutingTable table) {
			// TODO Auto-generated method stub
			
		}

		public void setTimeout(int millis) {
			// TODO Auto-generated method stub
			
		}
		
	}

	@Before
	public void setUp() throws Exception {
		client = new MessageHandlingTests().new MockSearchClient();
		table = new RoutingTable(SearchId.getRandomId(), 10, new MockPinger());
		searcher = new DefaultSearcher(table, client);

		String nodeId =
            "1234567890123456789012345678901234567800";
		SearchId id =  SearchId.fromHex(nodeId);
        
        NodeState node = new NodeState(id,
        		InetAddress.getByName("localhost"), 5670);
        table.addNode(node);
	}

	@Test
	public void findValueMessageSent() throws IOException {
		SearchId fileName =
			SearchId.fromHex("1234567890123456789012345678901234567890");
		searcher.putSearchRequest(fileName);

		SearchMessage sm =
			((MessageHandlingTests.MockSearchClient)client).lastMessage;
		assertEquals("find_value", sm.getCommand());
		assertEquals(fileName.toString(), sm.arguments().get("file_name"));
	}

}