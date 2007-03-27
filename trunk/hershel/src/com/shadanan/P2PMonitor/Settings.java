package com.shadanan.P2PMonitor;

import java.awt.Rectangle;
import java.io.*;

public class Settings implements Serializable {
	private static final long serialVersionUID = 2189567604671972409L;
	
	public boolean drawLabel;
	public boolean drawInfo;
	public boolean drawAdjacencies;
	public long timeout;
	public Rectangle bounds;
	public boolean active;
	public boolean animated;
	public boolean autoclear;
	public boolean autoconnect;
	public String infoFilter;
	public String selectedReposition;
	public String selectedAdjacency;
	
	public Settings() {
		drawLabel = true;
		drawInfo = true;
		drawAdjacencies = true;
		timeout = 10000;
		bounds = new Rectangle(0, 0, 800, 600);
		active = true;
		animated = true;
		autoclear = true;
		autoconnect = true;
		infoFilter = ".*";
		selectedReposition = null;
		selectedAdjacency = null;
	}
	
	public void save() {
		File file = new File(".P2PMonitorSettings");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(this);
			oos.close();
		} catch (Exception e) {}
	}
	
	public static Settings load() {
		File file = new File(".P2PMonitorSettings");
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			Settings settings = (Settings)ois.readObject();
			return settings;
		} catch (Exception e) {}
		file.delete();
		return new Settings();
	}
}
