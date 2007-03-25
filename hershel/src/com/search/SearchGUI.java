package com.search;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import com.shadanan.P2PMonitor.IRemote;
import com.shadanan.P2PMonitor.MonitorService;

public class SearchGUI implements GUI, IRemote {
	private static String initialId =
		"1234567890123456789012345678901234567890";
	private MonitorService ms;
	private NetworkSearchClient client;

	public SearchGUI() throws IOException {
		client = new NetworkSearchClient(initialId, 10000);
		ms = new MonitorService(10001, this,
				new InetSocketAddress(InetAddress.getLocalHost(), 10000));
		client.registerUI(this);
		client.start();
		ms.start();
	}

	public SearchGUI(int port) throws IOException {
		String randomId = SearchId.getRandomId().toString();
		client = new NetworkSearchClient(randomId, port);
		ms = new MonitorService(port + 1, this,
				new InetSocketAddress(InetAddress.getLocalHost(), port));
		client.registerUI(this);
		client.start();
		ms.start();
	}

	public void getMessage(String s) {
		
	}

	public void addContact(InetSocketAddress peer) {
		// TODO Auto-generated method stub

	}

	public void close() {
		System.out.println("I get called!");
	}

	public HashMap<String, String> getInfo() {
		// TODO Auto-generated method stub
		return new HashMap<String, String>();
	}

	public int getLayerCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	public InetSocketAddress getLocalAddress() {
		try {
			return new InetSocketAddress(InetAddress.getLocalHost(), 10000);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public InetSocketAddress[] getPeers(int layer) {
		ArrayList<ArrayList<NodeState>> rt =
			client.getHandler().routingTable().getRoutingTable();
		ArrayList<InetSocketAddress> peers =
			new ArrayList<InetSocketAddress>();
		for (int i = 0; i < 160; i++) {
			ArrayList<NodeState> kbucket = rt.get(i);
			for (NodeState n : kbucket) {
				peers.add(new InetSocketAddress(n.address, n.port));
			}
		}
		return peers.toArray(new InetSocketAddress[0]);
	}

	public void message(String text) {
		String[] command = text.split(" ");
		MessageHandler h = client.getHandler();
		if (command[0].equals("find_value")) {
			try {
				h.findValue(new SearchId(SHA1Utils.getSHA1Digest(command[1].getBytes())));
			} catch (IOException ex) {
				ms.println("! Searching error.\n");
			}
		}
	}

}
