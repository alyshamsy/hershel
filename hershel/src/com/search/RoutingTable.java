package com.search;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RoutingTable {

	private SearchId self;
	private ArrayList<ArrayList<NodeState>> table;
	private final int K = 20;

	public RoutingTable(SearchId self) {
		this.self = self;
		table = new ArrayList<ArrayList<NodeState>>(160);
		// Not sure if this is needed:
		for (int i = 0; i < 160; i++)
			table.set(i, new ArrayList<NodeState>());
	}

	public List getRoutingTable() {
		return table;
	}

	public void addNode(NodeState node) throws Error {
		int index = findIndex(node.id);
		if (index < 0) throw new Error();

		ArrayList<NodeState> kBucket = table.get(index);
		if (kBucket.size() < K) {
			kBucket.add(node);
		} else {
			NodeState lastSeen = kBucket.remove(0);
			// boolean result = ping(lastSeen);
			// if (result) {
			//	kBucket.add(lastSeen);
			// } else {
			//	kBucket.add(node);
			// }
		}
	}

	public List findNode(NodeState node) throws Error {
		int index = findIndex(node.id);
		if (index < 0) throw new Error();

		ArrayList<NodeState> nodes = new ArrayList<NodeState>();
		int i = index, j = 0;
		while ((i < 160 || i >= 0) && nodes.size() < K) {
			i += (j % 2 == 0) ? j : -j;
			j++;
			if (i >= 160 || i < 0) continue;

			for (NodeState n : table.get(index)) {
				if (nodes.size() < K)
					nodes.add(n);
				else break;
			}
		}

		return nodes;
	}

	/* A node belongs to a k-bucket if its distance falls
	 * between 2^1 and 2^(i+1), where i is the index of
	 * the k-bucket.
	 */
	private int findIndex(SearchId newNode) {
		final BigInteger TWO = new BigInteger("2");
		BigInteger distance =
			new BigInteger(SearchId.getDistance(self, newNode));

		for (int i = 0; i < 160; i++) {
			if ((distance.compareTo(TWO.pow(i)) >= 0)
					&& (distance.compareTo(TWO.pow(i + 1)) < 0)) {
				// Is this guaranteed to happen?
				return i;
			}
		}
		return -1;
	}

}
