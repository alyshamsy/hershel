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

	private static final boolean debug = true;
	private static String initialId =
		"1234567890123456789012345678901234567890";
	private MonitorService ms;
	private NetworkSearchClient client;
	private int port;

	public SearchGUI() throws IOException {
		port = 10000;
		client = new NetworkSearchClient(initialId, 10000);
		ms = new MonitorService(10001, this,
				new InetSocketAddress(InetAddress.getByName("localhost"), 10000));
		client.registerUI(this);
		client.start();
		ms.start();
	}

	public SearchGUI(int port) throws IOException {
		this.port = port;
		String randomId = SearchId.getRandomId().toString();
		client = new NetworkSearchClient(randomId, port);
		ms = new MonitorService(port + 1, this,
				new InetSocketAddress(InetAddress.getByName("localhost"), port));
		client.registerUI(this);
		client.start();
		ms.start();
	}

	public void getMessage(String s) {
		if (debug)
			ms.println(s);
	}

	public void addContact(InetSocketAddress peer) {
		// TODO Auto-generated method stub

	}

	public void close() {

	}

	public HashMap<String, String> getInfo() {
		// TODO Auto-generated method stub
		return new HashMap<String, String>();
	}

	public int getLayerCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	public InetSocketAddress getLocalAddress() {
		try {
			return new InetSocketAddress(InetAddress.getByName("localhost"), port);
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
		if (command[0].equalsIgnoreCase("search")) {
			try {
				SearchId file = new SearchId(SHA1Utils
						.getSHA1Digest(command[1].getBytes()));
				h.findValue(file);
			} catch (IOException ex) {
				ms.println("! Searching error.\n");
			}
		} else if (command[0].equalsIgnoreCase("download")) {
			try {
				SearchId file = new SearchId(SHA1Utils
						.getSHA1Digest(command[1].getBytes()));
				SearchResult result = h.database().get(file);
		    	//fileClient.beginDownload(result);
			} catch (Exception ex) {
				ms.println("! Downloading error.\n");
			}
		} else if (command[0].equalsIgnoreCase("help")) {
			ms.println("? P2P Commands:\n");
			ms.println("? search <filename>\n");
			ms.println("?    - search for <filename>\n");
			ms.println("? download <filename>\n");
			ms.println("?    - begin download for <filename>\n");
			ms.println("?    - you should run 'find' first\n");
			ms.println("? help\n");
			ms.println("?    - this listing\n");
		} else {
			ms.println("! Invalid command. " +
					"Type 'help' for a list of commands.\n");
		}
	}

}
