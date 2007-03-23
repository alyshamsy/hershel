package com.search;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

import com.shadanan.P2PMonitor.IRemote;
import com.shadanan.P2PMonitor.MonitorService;

public class SearchGUI implements GUI, IRemote {

	private MonitorService ms;
	private NetworkSearchClient client;

	public SearchGUI() throws IOException {
		client = new NetworkSearchClient("1234567890123456789012345678901234567890", 10010);
		ms = new MonitorService(10000, this,
				new InetSocketAddress(InetAddress.getLocalHost(), 10002));
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
		// TODO Auto-generated method stub

	}

	public HashMap<String, String> getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLayerCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public InetSocketAddress getLocalAddress() {
		// return new InetSocketAddress("localhost", 10000);
		// TODO Auto-generated method stub
		return null;
	}

	public InetSocketAddress[] getPeers(int layer) {
		// TODO Auto-generated method stub
		return null;
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
