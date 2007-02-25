package test.search;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.search.NodeState;
import com.search.RoutingTable;
import com.search.SearchId;

public class TestRoutingTable {

	private RoutingTable t;
	private String myId =
		"1234567890123456789012345678901234567890";
	private String[] otherIds = {
		"1234567890123456789012345678901234567800",
		"1234567890123456789012345678901234567810",
		"1234567890123456789012345678901234567895",
	};

	@Before
	public void setUp() throws Exception {
		SearchId id = SearchId.fromHex(myId);
		t = new RoutingTable(id);
	}

	@Test
	public void addToTable() {
		SearchId[] ids = {
				SearchId.fromHex(otherIds[0]),
				SearchId.fromHex(otherIds[1]),
				SearchId.fromHex(otherIds[2]),
		};
		NodeState node1 = new NodeState(ids[0], null, 5670);
		t.addNode(node1);
		NodeState node2 = new NodeState(ids[1], null, 5670);
		t.addNode(node2);
		NodeState node3 = new NodeState(ids[2], null, 5670);
		t.addNode(node3);

		byte[] d = SearchId.getDistance(ids[0], SearchId.fromHex(myId));
		assertEquals(new BigInteger("144"), new BigInteger(d));
		d = SearchId.getDistance(ids[1], SearchId.fromHex(myId));
		assertEquals(new BigInteger("128"), new BigInteger(d));
		d = SearchId.getDistance(ids[2], SearchId.fromHex(myId));
		assertEquals(new BigInteger("5"), new BigInteger(d));

		ArrayList<NodeState> al =
			(ArrayList<NodeState>)(t.getRoutingTable().get(7));
		assertEquals(al.get(0), node1);
		assertEquals(al.get(1), node2);
		al = (ArrayList<NodeState>)(t.getRoutingTable().get(2));
		assertEquals(al.get(0), node3);
	}

}
