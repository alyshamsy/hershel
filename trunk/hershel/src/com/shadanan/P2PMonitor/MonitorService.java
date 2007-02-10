package com.shadanan.P2PMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * The monitor server allows a user to monitor a given node in a P2P network.
 * This monitor server will only allow one monitor connection at a time.
 * @author Shadanan Sharma
 */
public class MonitorService extends Thread {
	private ServerSocket ss = null;
	private Socket s = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private boolean running = false;

	private boolean active = true; 
	private InetSocketAddress peers[][];
	private InetSocketAddress me;
	private HashMap<String, String> info = null;
	private IRemote remote;
	
	public MonitorService(int port, IRemote remote, InetSocketAddress me) throws IOException {
		ss = new ServerSocket(port);
		this.me = me;
		this.remote = remote;
		running = true;
	}
	
	public void disconnect() {
		if (out != null) out.close(); out = null;
		try {if (in != null) in.close(); in = null;} catch (Exception e) {}
		try {if (s != null) s.close(); s = null;} catch (Exception e) {}
	}
	
	public void close() {
		running = false;
		if (out != null) out.close(); out = null;
		try {if (in != null) in.close(); in = null;} catch (Exception e) {}
		try {if (s != null) s.close(); s = null;} catch (Exception e) {}
		try {if (ss != null) ss.close(); ss = null;} catch (Exception e) {}
	}
	
	public synchronized void notifyNewLayers() {
		peers = new InetSocketAddress[remote.getLayerCount()][];
		if (active) sendLayers();
	}
	
	public synchronized void notifyNewPeers(int layer) {
		peers[layer] = remote.getPeers(layer);
		if (active) sendPeers(layer);
	}
	
	public synchronized void notifyNewIdentity() {
		this.me = remote.getLocalAddress();
		if (active) sendIdentity();
	}
	
	public synchronized void notifyNewInfo() {
		this.info = remote.getInfo();
		if (active) sendInfo();
	}
	
	private void sendLayers() {
		Message send = new Message("layers", Integer.toString(peers.length));
		if (out != null) out.println(send);
	}
	
	private void sendPeers(int layer) {
		if (peers == null || peers[layer] == null) return;
		String[] data = new String[peers[layer].length+1];
		data[0] = Integer.toString(layer);
		for (int i = 0; i < peers[layer].length && peers[layer][i] != null; i++) {
			data[i+1] = peers[layer][i].getAddress().getHostAddress() + ":" + peers[layer][i].getPort();
		}
		Message send = new Message("peers", data);
		if (out != null) out.println(send);
	}
	
	private void sendIdentity() {
		Message send = new Message("identity", me.getAddress().getHostAddress() + ":" + me.getPort());
		if (out != null) out.println(send);
	}
	
	private void sendInfo() {
		Set<String> set = info.keySet();
		String data[] = new String[set.size()];
		int i = 0;
		for (String key : set) {
			String value = info.get(key);
			data[i++] = key + "=" + value;
		}
		Message send = new Message("info", data);
		if (out != null) out.println(send);
	}
	
	private void requery() {
		notifyNewLayers();
		sendLayers();
		notifyNewIdentity();
		sendIdentity();
		for (int i = 0; i < peers.length; i++) {
			notifyNewPeers(i);
			sendPeers(i);
		}
		notifyNewInfo();
		sendInfo();
	}
	
	//To be used for sending console responses only!
	public void println(String response) {
		String[] tokens = response.split("\n");
		for (int i = 0; i < tokens.length; i++) {
			Message send = new Message("console", tokens[i]);
			if (out != null) out.println(send);
		}
	}
	
	public void run() {
		Message recv = null;
		requery();
		
		while (running) {
			try {Thread.sleep(10);} catch (InterruptedException e) {}
			
			if (ss == null) {
				close();
				continue;
			}
			
			if (s == null) {
				try {
					s = ss.accept();
					in = new BufferedReader(new InputStreamReader(s.getInputStream()));
					out = new PrintWriter(s.getOutputStream(), true);
				} catch (IOException e) {
					close();
					continue;
				}
			}
			
			try {
				String input = in.readLine();
				if (input == null) {
					disconnect();
					continue;
				}
				recv = Message.parse(input);
			} catch (IOException e) {
				//e.printStackTrace();
				disconnect();
				continue;
			}

			if (recv.cmdEquals("quit")) {
				disconnect();
				continue;
			}
			
			if (recv.cmdEquals("console")) {
				remote.message(recv.getData());
			}
			
			if (recv.cmdEquals("passive")) {
				active = false;
				continue;
			}
			
			if (recv.cmdEquals("active")) {
				requery();
				active = true;
				continue;
			}
			
			if (recv.cmdEquals("getlayers")) {
				sendLayers();
			}
			
			if (recv.cmdEquals("getpeers")) {
				int layer = Integer.parseInt(recv.getData());
				sendPeers(layer);
				continue;
			}
			
			if (recv.cmdEquals("getidentity")) {
				sendIdentity();
				continue;
			}
			
			if (recv.cmdEquals("getinfo")) {
				sendInfo();
				continue;
			}
			
			if (recv.cmdEquals("requery")) {
				requery();
				continue;
			}
			
			if (recv.cmdEquals("kill")) {
				remote.close();
				continue;
			}
			
			if (recv.cmdEquals("addcontact")) {
				for (int i = 0; i < recv.countDataTokens(); i++) {
					String host = recv.getData().substring(0, recv.getData().indexOf(":"));
					int port = Integer.parseInt(recv.getData().substring(recv.getData().indexOf(":")+1));
					InetSocketAddress contact = new InetSocketAddress(host, port);
					remote.addContact(contact);
				}
			}
		}
	}
}
