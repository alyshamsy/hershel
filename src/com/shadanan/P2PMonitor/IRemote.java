package com.shadanan.P2PMonitor;

import java.net.InetSocketAddress;
import java.util.HashMap;

public interface IRemote {
	public void close();
	public void addContact(InetSocketAddress peer);
	public int getLayerCount();
	public InetSocketAddress[] getPeers(int layer);
	public InetSocketAddress getLocalAddress();
	public void message(String text);
	public HashMap<String, String> getInfo();
}
