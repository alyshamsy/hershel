package com.shadanan.P2PMonitor;

import java.awt.Graphics2D;
import java.util.ArrayList;

public interface IAdjacency {
	public String getName();
	public void paint(Graphics2D g, ArrayList<Monitor.Node> nodes);
}
