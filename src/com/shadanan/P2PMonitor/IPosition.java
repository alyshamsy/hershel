package com.shadanan.P2PMonitor;

import java.awt.Dimension;
import java.util.ArrayList;

public interface IPosition {
	public void reposition(Dimension screen, ArrayList<Monitor.Node> nodes);
	public String getName();
}
