package com.shadanan.P2PMonitor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import javax.swing.*;
import javax.swing.Timer;

/**
 * @author Shadanan Sharma
 * @since 1.4.2 Created on: Aug 4, 2005 E-Mail: shadanan@gmail.com Web Site:
 *        http://www.convergence2000.com
 */
public class Monitor extends JPanel {
	private static final long serialVersionUID = 2142293610794851788L;

	private Settings settings;

	private volatile ArrayList<Node> nodes = new ArrayList<Node>(); // @jve:decl-index=0:

	private IPosition position = null; // @jve:decl-index=0:
	private ArrayList<IPosition> positions = null;
	private IAdjacency adjacency = null; // @jve:decl-index=0:
	private ArrayList<IAdjacency> adjacencies = null;
	private ArrayList<IUserNodeEventListener> userNodeEventListeners = null;

	private Point offset = null;
	private long lastClickTime;
	private Node current = null;
	private Node mousedOverNode = null;
	private int sleepTime = 25;
	private Timer timer = null;

	private JFrame monitorFrame = null; // @jve:decl-index=0:visual-constraint="46,38"
	private JPopupMenu nodePopupMenu = null;
	private JMenuItem connectMenuItem = null;
	private JMenuItem disconnectMenuItem = null;
	private JMenuItem killMenuItem = null;
	private ArrayList<JCheckBoxMenuItem> repositionMenuItems = null; // @jve:decl-index=0:
	private ArrayList<JCheckBoxMenuItem> adjacencyMenuItems = null; // @jve:decl-index=0:
	private ArrayList<JCheckBoxMenuItem> renderedLayersMenuItems = null;
	private JMenuItem removeMenuItem = null;
	private JMenuItem addContactMenuItem = null;
	private JMenu adjacencyModeMenu = null;
	private JMenuItem clearDisconnectedMenuItem = null;
	private JCheckBoxMenuItem activeRefreshCheckBoxMenuItem = null;
	private JCheckBoxMenuItem animatedCheckBoxMenuItem = null;
	private JCheckBoxMenuItem autoClearDeletedNodesCheckBoxMenuItem = null;
	private JCheckBoxMenuItem autoConnectNewNodesCheckBoxMenuItem = null;
	private JMenuItem setTimeoutMenuItem = null;
	private JMenu repositionMenu = null;
	private JMenu renderedLayerMenu = null;
	private JMenuItem requeryAllMenuItem = null;
	private JMenuItem requeryMenuItem = null;
	private JMenu renderMenu = null;
	private JCheckBoxMenuItem drawLabelCheckBoxMenuItem = null;
	private JCheckBoxMenuItem drawInfoCheckBoxMenuItem = null;
	private JCheckBoxMenuItem drawAdjacencyCheckBoxMenuItem = null;
	private JMenuItem infoFilterMenuItem = null;

	public Monitor() {
		super();
		initialize();
		getMonitorFrame().setVisible(true);
	}

	/**
	 * <p>
	 * This method allows the user to manually add nodes to the monitor. By
	 * default, the monitor will attempt to connect to nodes that it learns
	 * about automatically. In order to do this, the monitor assumes that the
	 * monitor service is running on the node's port+1. If this is not the case,
	 * the monitor program will not be able to add nodes automatically.
	 * </p>
	 * 
	 * <p>
	 * It is recommended that addContact be used in order to inform the monitor
	 * of nodes rather than letting the monitor discover nodes on its own.
	 * Adding nodes manually allows the monitor to capture the most amount of
	 * debugging messages. If nodes are added automatically, bootstrap messages
	 * may not be received by the monitor.
	 * </p>
	 * 
	 * @param host
	 *            the host name of the contact.
	 * @param port
	 *            the port of the contact.
	 */
	public void addContact(String host, int port) {
		connect(host, port);
	}

	private void initialize() {
		settings = Settings.load(); // Loads settings for the monitor.

		// Register for mouse events.
		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (e.getButton() == MouseEvent.NOBUTTON) {
					Node node = getNode(e.getPoint());
					if (node == null && mousedOverNode != null) {
						mousedOverNode.mouseOut();
						mousedOverNode = null;
						if (!settings.animated)
							repaint();
					}
					if (node != null && mousedOverNode == null) {
						mousedOverNode = node;
						mousedOverNode.mouseOver(e.getPoint().x, e.getPoint().y);
						if (!settings.animated)
							repaint();
					}
				}
			}

			public void mouseDragged(java.awt.event.MouseEvent e) {
				moveNode(e.getPoint());
			}
		});
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (!settings.animated) repaint();
				if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) {
					long currentClick = System.currentTimeMillis();
					Node n = getNode(e.getPoint());
					if (n == null) return;
					if (lastClickTime == 0 || currentClick - lastClickTime > 300) n.setConsoleVisible(true, monitorFrame);
					lastClickTime = currentClick;
				}
			}

			public void mouseReleased(java.awt.event.MouseEvent e) {
				getNodePopupMenu().setVisible(false);
				if (e.isPopupTrigger()) {
					showContextMenu(e.getPoint());
				}
				moveNode(e.getPoint());
			}

			public void mousePressed(java.awt.event.MouseEvent e) {
				mousedOverNode = null;
				liftNode(e.getPoint());
			}
		});
		// Register for resizes of the main window
		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				validate();
				reposition();
			}
		});

		userNodeEventListeners = new ArrayList<IUserNodeEventListener>();

		// Add the default adjacencies.
		adjacencies = new ArrayList<IAdjacency>();

		adjacencies.add(new IAdjacency() {
			private Color[] layerColors;

			private void drawLayer(Graphics2D g, ArrayList<Node> nodes, int layer) {
				g.setColor(layerColors[layer]);
				for (int i = 0; i < nodes.size(); i++) {
					Node n = (Node) nodes.get(i);

					for (int j = 0; j < n.getNumNodes(layer); j++) {
						Node m = n.getNodeAt(layer, j);
						if (m == null) continue;
						g.drawLine(n.getX(), n.getY(), m.getX(), m.getY());
					}
				}
			}

			public void paint(Graphics2D g, ArrayList<Node> nodes) {
				int layers = getNumLayers();
				layerColors = new Color[layers];
				for (int i = 0; i < layers; i++) {
					int grade = ((layers - i) * 200) / layers;
					layerColors[i] = new Color(grade, grade, grade);
				}

				for (int i = 0; i < layers; i++) {
					if (getRenderedLayersMenuItems(i).getState()) drawLayer(g, nodes, i);
				}
			}

			public String getName() {
				return "Simple";
			}
		});

		adjacencies.add(new IAdjacency() {
			private Color[] layerColors;

			private void drawLayer(Graphics2D g, ArrayList<Node> nodes, int layer) {
				g.setColor(layerColors[layer]);
				for (int i = 0; i < nodes.size(); i++) {
					Node n = (Node) nodes.get(i);

					for (int j = 0; j < n.getNumNodes(layer); j++) {
						Node m = n.getNodeAt(layer, j);
						if (m == null) continue;
						g.drawLine(n.getX() + 1, n.getY() + 1, m.getX(), m.getY());
						g.drawLine(n.getX() - 1, n.getY() + 1, m.getX(), m.getY());
						g.drawLine(n.getX() + 1, n.getY() - 1, m.getX(), m.getY());
						g.drawLine(n.getX() - 1, n.getY() - 1, m.getX(), m.getY());
						g.drawLine(n.getX(), n.getY(), m.getX(), m.getY());
					}
				}
			}

			public void paint(Graphics2D g, ArrayList<Node> nodes) {
				int layers = getNumLayers();
				layerColors = new Color[layers];
				for (int i = 0; i < layers; i++) {
					int grade = ((layers - i) * 200) / layers;
					layerColors[i] = new Color(grade, grade, grade);
				}

				for (int i = 0; i < layers; i++) {
					if (getRenderedLayersMenuItems(i).getState()) drawLayer(g, nodes, i);
				}
			}

			public String getName() {
				return "Arrow";
			}
		});

		adjacencies.add(new IAdjacency() {
			private void drawLayer(Graphics2D g, ArrayList<Node> nodes, int layer) {
				Color begin = Color.black;
				Color end = new Color(255, 255, 255, 0);
				for (int i = 0; i < nodes.size(); i++) {
					Node n = (Node) nodes.get(i);

					for (int j = 0; j < n.getNumNodes(layer); j++) {
						Node m = n.getNodeAt(layer, j);
						if (m == null) continue;
						g.setPaint(new GradientPaint(n.getX(), n.getY(), begin, m.getX(), m.getY(), end));
						g.drawLine(n.getX(), n.getY(), m.getX(), m.getY());
					}
				}
			}

			public void paint(Graphics2D g, ArrayList<Node> nodes) {
				int layers = getNumLayers();
				for (int i = 0; i < layers; i++) {
					if (getRenderedLayersMenuItems(i).getState()) drawLayer(g, nodes, i);
				}
			}

			public String getName() {
				return "Fade (Flat)";
			}
		});

		// Add the default positionings.
		positions = new ArrayList<IPosition>();

		positions.add(new IPosition() {
			private void positionNodesInBox(ArrayList nodes, int x, int y, int width, int height) {
				for (int i = 0; i < nodes.size(); i++) {
					Node n = (Node) nodes.get(i);
					int xn = (int) Math.round(width / 2 * Math.cos(2 * Math.PI * i / nodes.size())) + width / 2 + x;
					int yn = (int) Math.round(height / 2 * Math.sin(2 * Math.PI * i / nodes.size())) + height / 2 + y;
					n.moveTo(xn, yn);
				}
			}

			public String getName() {
				return "Circular";
			}

			public void reposition(Dimension screen, ArrayList<Node> nodes) {
				for (int i = 0; i < nodes.size(); i++) {
					positionNodesInBox(nodes, 50, 50, screen.width - 100, screen.height - 100);
				}
			}
		});

		positions.add(new IPosition() {
			private void positionNodesInBox(ArrayList nodes, int x, int y, int width, int height) {
				for (int i = 0; i < nodes.size(); i++) {
					Node n = (Node) nodes.get(i);
					int xn = (int) Math.round(width / 2 * Math.cos(2 * Math.PI * i / nodes.size())) + width / 2 + x;
					int yn = (int) Math.round(height / 2 * Math.sin(2 * Math.PI * i / nodes.size())) + height / 2 + y;
					n.moveTo(xn, yn);
				}
			}

			private void partition(ArrayList<Node> part1, ArrayList<Node> part2, Node n) {
				part1.remove(n);
				part2.add(n);
				for (int i = 0; i < n.getNumLayers(); i++) {
					for (int j = 0; j < n.getNumNodes(i); j++) {
						Node curr = findNode(n.getPeer(i, j), part1);
						if (curr != null) {
							partition(part1, part2, curr);
						}
					}
				}
			}

			public String getName() {
				return "Partition Check";
			}

			public void reposition(Dimension screen, ArrayList<Node> nodes) {
				if (nodes.size() == 0) return;
				ArrayList<ArrayList> partitions = new ArrayList<ArrayList>();
				ArrayList<Node> oldnodes = new ArrayList<Node>(nodes);
				ArrayList<Node> currentPart = null;

				while (!oldnodes.isEmpty()) {
					currentPart = new ArrayList<Node>();
					partitions.add(currentPart);
					partition(oldnodes, currentPart, oldnodes.get(0));
				}

				int partSize = (screen.width - 100) / partitions.size();
				for (int j = 0; j < partitions.size(); j++) {
					ArrayList part = (ArrayList) partitions.get(j);
					positionNodesInBox(part, (partSize + 100 / partitions.size()) * j + 50 / partitions.size(), 50, partSize, screen.height - 100);
				}
			}
		});

		IAdjacency savedAdjacency = getAdjacencyByName(settings.selectedAdjacency);
		setAdjacency(savedAdjacency == null ? adjacencies.get(0) : savedAdjacency);
		IPosition savedPosition = getRepositionByName(settings.selectedReposition);
		setReposition(savedPosition == null ? positions.get(0) : savedPosition);

		timer = new Timer(sleepTime, new AnimationLoop());
		timer.start();
	}

	/**
	 * <p>
	 * Allows the user to add his own reposition method. This is useful if the
	 * user's overlay topology has a specific structure that would be better
	 * visualized if drawn a specific way. In order to support custom reposition
	 * methods, the user should create an object that implements the IPosition
	 * interface. See the comments in the IPosition interface class for more
	 * information on how to create a custom reposition method.
	 * </p>
	 * 
	 * @see
	 * 
	 * @param reposition
	 *            the custom reposition interface to use.
	 */
	public void addUserReposition(IPosition reposition) {
		positions.add(reposition);
		IPosition savedPosition = getRepositionByName(settings.selectedReposition);
		setReposition(savedPosition == null ? positions.get(0) : savedPosition);
	}

	/**
	 * <p>
	 * Allows the user to programmatically specify which reposition to use in
	 * the monitor. This is useful if the user has just added a custom
	 * reposition interface and would now like to set it as the default one
	 * without having to choose the reposition interface from the monitor's
	 * right click menu.
	 * </p>
	 * 
	 * <p>
	 * The user may use the method getRepositionByName to get a reference to the
	 * reposition interface if he does not already have a reference.
	 * </p>
	 * 
	 * @param reposition
	 *            the reposition interface to use.
	 */
	public void setReposition(IPosition reposition) {
		this.position = reposition;
		if (repositionMenuItems == null) return;
		if (this.position != null) settings.selectedReposition = this.position.getName();
		for (JCheckBoxMenuItem menuItem : repositionMenuItems) {
			if (menuItem.getActionCommand().equals(reposition.getName())) {
				menuItem.setSelected(true);
			} else {
				menuItem.setSelected(false);
			}
		}
	}

	/**
	 * <p>
	 * Gets a reposition interface given it's name. The default reposition
	 * interfaces are called:
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Circular</li>
	 * <li>Partition Check</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * This method compares name to the return value of the getName() method of
	 * the IPosition interface.
	 * </p>
	 * 
	 * @param name
	 *            the name of the reposition interface.
	 * @return a reference to the IPosition interface corresponding to name or
	 *         null if it does not exist.
	 */
	public IPosition getRepositionByName(String name) {
		for (IPosition reposition : positions) {
			if (reposition.getName().equals(name)) return reposition;
		}
		return null;
	}

	/**
	 * <p>
	 * This method returns the currently selected reposition interface. This is
	 * the reposition interface specified by the checkmark in the monitor's
	 * right click menu.
	 * </p>
	 * 
	 * @return the currently selected reposition interface
	 */
	public IPosition getSelectedReposition() {
		return position;
	}

	/**
	 * <p>
	 * Allows the user to specify his own adjacency draw method. The adjacency
	 * interface specifies how to draw adjacency lines between nodes. In order
	 * to specify an adjacency draw, the user must implement the IAdjacency
	 * interface.
	 * </p>
	 * 
	 * @param adjacency
	 *            the adjacency draw to add to the list of adjacency draw items.
	 */
	public void addUserAdjacency(IAdjacency adjacency) {
		adjacencies.add(adjacency);
		IAdjacency savedAdjacency = getAdjacencyByName(settings.selectedAdjacency);
		setAdjacency(savedAdjacency == null ? adjacencies.get(0) : savedAdjacency);
	}

	/**
	 * <p>
	 * Allows the user to programmatically specify which adjacency draw to use
	 * in the monitor. This is useful if the user has just added a custom
	 * adjacency draw interface and would now like to set it as the default one
	 * without having to choose the adjacency draw interface from the monitor's
	 * right click menu.
	 * </p>
	 * 
	 * <p>
	 * The user may use the method getAdjacencyByName to get a reference to the
	 * adjacency draw interface if he does not already have a reference.
	 * </p>
	 * 
	 * @param adjacency
	 *            the adjacency draw interface to use.
	 */
	public void setAdjacency(IAdjacency adjacency) {
		this.adjacency = adjacency;
		if (adjacencyMenuItems == null) return;
		if (this.adjacency != null) settings.selectedAdjacency = this.adjacency.getName();
		for (JCheckBoxMenuItem menuItem : adjacencyMenuItems) {
			if (menuItem.getActionCommand().equals(adjacency.getName())) {
				menuItem.setSelected(true);
			} else {
				menuItem.setSelected(false);
			}
		}
	}

	/**
	 * <p>
	 * Gets an adjacency draw interface given it's name. The default adjacency
	 * draw interfaces are called:
	 * </p>
	 * 
	 * <p>
	 * <ul>
	 * <li>Simple</li>
	 * <li>Arrow</li>
	 * <li>Fade (Flat)</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * This method compares name to the return value of the getName() method of
	 * the IAdjacency interface.
	 * </p>
	 * 
	 * @param name
	 *            the name of the adjacency draw interface.
	 * @return a reference to the IAdjacency interface corresponding to name or
	 *         null if it does not exist.
	 */
	public IAdjacency getAdjacencyByName(String name) {
		for (IAdjacency adjacency : adjacencies) {
			if (adjacency.getName().equals(name)) return adjacency;
		}
		return null;
	}

	/**
	 * <p>
	 * This method returns the currently selected adjacency draw interface. This
	 * is the adjacency draw interface specified by the checkmark in the
	 * monitor's right click menu.
	 * </p>
	 * 
	 * @return the currently selected adjacency draw interface
	 */
	public IAdjacency getSelectedAdjacency() {
		return adjacency;
	}

	/**
	 * <p>
	 * Adds a userNodeEventListener to the monitor. userNodeEventListener's are
	 * objects that allow the monitor to respond to custom user events. User
	 * events are generated every time a node receives new information from it's
	 * corresponding MonitorService.
	 * </p>
	 * 
	 * @param userNodeEventListener
	 *            an object that implements the IUserNodeEventListener
	 *            interface.
	 */
	public void addUserNodeEventListener(IUserNodeEventListener userNodeEventListener) {
		userNodeEventListeners.add(userNodeEventListener);
	}

	/**
	 * Calls the appropriate userNodeEventListener method of the
	 * IUserNodeEventListener interface for the specified node.
	 * 
	 * @param node
	 *            the node that has generated the user event.
	 */
	private void userEvent(Node node) {
		for (IUserNodeEventListener nodeUserEventListener : userNodeEventListeners) {
			nodeUserEventListener.nodeEvent(this, node);
		}
	}

	/**
	 * Gets the number of layers as indicated by the first node inserted into
	 * the overlay. This is a rather crude way to get this information. Ideally,
	 * we could get the information as a parameter from the constructor, but
	 * this means one more thing for the student to worry about.
	 * 
	 * @return the number of layers in the overlay as indicated by the first
	 *         node inserted into the overlay.
	 */
	private synchronized int getNumLayers() {
		int layers = 0;
		try {
			layers = nodes.get(0).getNumLayers();
		} catch (Exception e) {}
		return layers;
	}

	/**
	 * Shows the context menu at a given position. Appropriately changes the
	 * state of the individual menu items depending on whether or not a node or
	 * empty are was clicked.
	 * 
	 * @param pos
	 *            the position that was right clicked.
	 */
	private void showContextMenu(Point pos) {
		getNodePopupMenu().setInvoker(this);
		getNodePopupMenu().setLocation((int) (pos.getX() + monitorFrame.getX()), (int) (pos.getY() + monitorFrame.getY()));
		if (current == null) {
			getRequeryMenuItem().setEnabled(false);
			getDisconnectMenuItem().setEnabled(false);
			getAddContactMenuItem().setEnabled(false);
			getRemoveMenuItem().setEnabled(false);
			getKillMenuItem().setEnabled(false);
		} else {
			getRequeryMenuItem().setEnabled(true);
			getDisconnectMenuItem().setEnabled(true);
			getAddContactMenuItem().setEnabled(true);
			getRemoveMenuItem().setEnabled(true);
			getKillMenuItem().setEnabled(true);
		}
		getNodePopupMenu().setVisible(true);
	}

	/**
	 * Gets the node object who is identified by the specified InetSocketAddress
	 * node.
	 * 
	 * @param node
	 *            the InetSocketAddress of the node to find.
	 * @return the Node object corresponding to the specified InetSocketAddress
	 *         or null if it does not exist.
	 */
	private synchronized Node getNode(InetSocketAddress node) {
		for (int i = 0; i < nodes.size(); i++) {
			Node curr = (Node) nodes.get(i);
			if (curr.addressIsEqual(node)) return curr;
		}
		return null;
	}

	/**
	 * Creates a new Node and adds the node to the monitor.
	 * 
	 * @param me
	 *            the InetSocketAddress where the node is located.
	 * @return a new Node object at the address specified by me.
	 */
	private synchronized Node newNode(InetSocketAddress me) {
		Node n = new Node(me);
		nodes.add(n);
		reposition();
		return n;
	}

	/**
	 * Creates a new Node and adds the node to the monitor.
	 * 
	 * @param host
	 *            the hostname of the node.
	 * @param port
	 *            the port the node is listening on.
	 * @return a new Node object at the address specified by the combination of
	 *         host and port.
	 */
	private synchronized Node connect(String host, int port) {
		Node n = new Node(host, port);
		nodes.add(n);
		reposition();
		return n;
	}

	/**
	 * Configures the nodes with a new refresh mode.
	 */
	private synchronized void setRefreshMode() {
		settings.active = getActiveRefreshCheckBoxMenuItem().getState();
		for (Node n : nodes) {
			n.setRefreshMode();
		}
	}

	/**
	 * Shows an input dialog box to allow the user to add a contact to the node
	 * that was right clicked.
	 */
	private void addContact() {
		if (current == null) return;
		String nodeInfo = JOptionPane.showInputDialog(this,
				"Enter control session URL", "Connect",
				JOptionPane.QUESTION_MESSAGE);
		if (nodeInfo == null) return;
		String host = nodeInfo.substring(0, nodeInfo.indexOf(":"));
		int port = Integer.parseInt(nodeInfo.substring(nodeInfo.indexOf(":") + 1));
		InetSocketAddress contact = new InetSocketAddress(host, port);
		current.addContact(contact);
	}

	/**
	 * Shows the connect dialog box to allow the user to add a contact to the
	 * monitor.
	 */
	private void showConnect() {
		String nodeInfo = JOptionPane.showInputDialog(this,
				"Enter control session URL", "Connect",
				JOptionPane.QUESTION_MESSAGE);
		if (nodeInfo == null) return;
		String host = nodeInfo.substring(0, nodeInfo.indexOf(":"));
		int port = Integer.parseInt(nodeInfo.substring(nodeInfo.indexOf(":") + 1));
		connect(host, port);
	}

	/**
	 * Disconnects a node.
	 */
	private void disconnect() {
		if (current == null) return;
		current.close();
	}

	/**
	 * Remove a node from the monitor.
	 */
	private synchronized void remove() {
		if (current == null) return;
		current.close();
		nodes.remove(current);
		reposition();
	}

	/**
	 * Disposes the current Monitor session cleanly disconnecting all connected
	 * nodes.
	 */
	private synchronized void close() {
		for (int i = 0; i < nodes.size(); i++) nodes.get(i).close();
		nodes.clear();
		timer.stop();
		settings.bounds = getMonitorFrame().getBounds();
		getMonitorFrame().dispose();
		settings.save();
	}

	/**
	 * Clears the nodes that are currently disconnected.
	 */
	private synchronized void clearDisconnected() {
		Iterator<Node> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			Node n = iterator.next();
			if (n.isDisconnected()) {
				n.close();
				iterator.remove();
			}
		}
		reposition();
	}

	/**
	 * Find a node with the specified InetSocketAddress in the ArrayList
	 * specified by nodes.
	 * 
	 * @param addr
	 *            the InetSocketAddress corresponding to the Node we want to
	 *            find.
	 * @param nodes
	 *            the ArrayList we want to search for the Node.
	 * @return the node specified by InetSocketAddress or null if it does not
	 *         exist.
	 */
	private Node findNode(InetSocketAddress addr, ArrayList<Node> nodes) {
		for (Node n : nodes) {
			if (n.addressIsEqual(addr)) return n;
		}
		return null;
	}

	/**
	 * Calls the reposition method of the currently selected reposition
	 * interface.
	 */
	private synchronized void reposition() {
		position.reposition(this.getSize(), nodes);
	}

	/**
	 * Shows a dialog box allowing the user to specify a new timeout interval.
	 */
	private void setTimeout() {
		String result = JOptionPane.showInputDialog(this, "New timeout value:",
				"Set Timeout Value", JOptionPane.INFORMATION_MESSAGE);
		if (result == null) return;
		try {
			settings.timeout = Long.parseLong(result);
		} catch (NumberFormatException e) {}
	}

	/**
	 * Gets the node located at the point p.
	 * 
	 * @param p
	 *            the point where the node is located.
	 * @return the node located at p or null if there is no node at that
	 *         position.
	 */
	private synchronized Node getNode(Point p) {
		for (int i = 0; i < nodes.size(); i++) {
			Node n = (Node) nodes.get(i);
			if (n.isClicked(p.x, p.y)) {
				return n;
			}
		}
		return null;
	}

	/**
	 * Moves a node to position p.
	 * 
	 * @param p
	 *            the position to move the node to.
	 */
	private void moveNode(Point p) {
		if (current == null) return;
		current.setPos(p.x + offset.x, p.y + offset.y);
		if (!settings.animated) repaint();
	}

	/**
	 * Sets the current node variable and offset variables to the node at
	 * location p.
	 * 
	 * @param p
	 *            the location of the node to lift.
	 */
	private synchronized void liftNode(Point p) {
		current = null;
		for (int i = 0; i < nodes.size(); i++) {
			Node n = (Node) nodes.get(i);
			if (n.isClicked(p.x, p.y)) {
				current = n;
				offset = new Point((int) (n.getX() - p.x), (int) (n.getY() - p.y));
				return;
			}
		}
		if (!settings.animated) repaint();
	}

	/**
	 * Cleanly termiantes the node currently specified by the current variable.
	 */
	private void killNode() {
		if (current == null) return;
		current.kill();
		if (!settings.animated) repaint();
	}

	/**
	 * Paints all adjacencies as specified by the current IAdjacency interface.
	 * 
	 * @param g
	 *            the current graphics context.
	 */
	private synchronized void adjacencyDraw(Graphics2D g) {
		if (settings.drawAdjacencies) adjacency.paint(g, nodes);
	}

	/**
	 * Paints all nodes.
	 * 
	 * @param g
	 *            the current graphics context.
	 */
	private synchronized void nodeDraw(Graphics2D g) {
		for (int i = 0; i < nodes.size(); i++) {
			Node n = (Node) nodes.get(i);
			n.paint(g);
		}
		if (mousedOverNode != null) mousedOverNode.paintInfoBox(g);
	}

	/**
	 * Requeries all nodes for all information.
	 */
	private synchronized void requeryAll() {
		for (Node n : nodes) n.requery();
	}

	/**
	 * Requries the currently selected node.
	 */
	private void requery() {
		if (current == null) return;
		current.requery();
	}

	/**
	 * Repaints the main window.
	 */
	public void paint(Graphics g) {
		paintBuffer((Graphics2D) g);
	}

	/**
	 * Paints the scene to the graphics context.
	 * 
	 * @param g
	 *            the graphics context where the scene will be painted.
	 */
	private synchronized void paintBuffer(Graphics2D g) {
		g.setBackground(Color.white);
		g.clearRect(0, 0, getWidth(), getHeight());
		adjacencyDraw(g);
		nodeDraw(g);
		g.setColor(new Color(0, 0, 128));
		g.setFont(new Font("Tahoma", Font.BOLD, 11));
		String[] displayText = {
				"Total Nodes: " + nodes.size(),
				"Message Timeout: " + settings.timeout,
				(settings.active ? "Active Refresh Enabled" : "Active Refresh Disabled"),
				(settings.animated ? "Animation ON" : "Animation OFF"),
				(settings.autoconnect ? "AutoConnect New Nodes ON" : "AutoConnect New Nodes OFF"),
				(settings.autoclear ? "AutoClear Old Nodes ON" : "AutoClear Old Nodes OFF"),
				"Info Text Filter: \"" + settings.infoFilter + "\"" };

		for (int i = 0; i < displayText.length; i++) {
			g.drawString(displayText[i], 10, 20 + 12 * i);
		}
	}

	/**
	 * Returns a reference to the main JFrame.
	 * 
	 * @return a reference to the main JFrame.
	 */
	private JFrame getMonitorFrame() {
		if (monitorFrame == null) {
			monitorFrame = new JFrame();
			monitorFrame.setBounds(settings.bounds);
			monitorFrame.setTitle("P2P Monitor");
			monitorFrame.setContentPane(this);
			monitorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					close();
					System.exit(0);
				}
			});
		}
		return monitorFrame;
	}

	/**
	 * This method initializes nodePopupMenu
	 * 
	 * @return javax.swing.JPopupMenu
	 */
	private JPopupMenu getNodePopupMenu() {
		if (nodePopupMenu == null) {
			nodePopupMenu = new JPopupMenu();
			nodePopupMenu.add(getAddContactMenuItem());
			nodePopupMenu.add(getRequeryMenuItem());
			nodePopupMenu.add(getRemoveMenuItem());
			nodePopupMenu.add(getDisconnectMenuItem());
			nodePopupMenu.addSeparator();
			nodePopupMenu.add(getKillMenuItem());
			nodePopupMenu.addSeparator();
			nodePopupMenu.add(getConnectMenuItem());
			nodePopupMenu.add(getClearDisconnectedMenuItem());
			nodePopupMenu.addSeparator();
			nodePopupMenu.add(getActiveRefreshCheckBoxMenuItem());
			nodePopupMenu.add(getRequeryAllMenuItem());
			nodePopupMenu.addSeparator();
			nodePopupMenu.add(getAnimatedCheckBoxMenuItem());
			nodePopupMenu.add(getAutoClearDeletedNodesCheckBoxMenuItem());
			nodePopupMenu.add(getAutoConnectNewNodesCheckBoxMenuItem());
			nodePopupMenu.add(getSetTimeoutMenuItem());
			nodePopupMenu.addSeparator();
			nodePopupMenu.add(getRepositionMenu());
			nodePopupMenu.add(getAdjacencyModeMenu());
			nodePopupMenu.add(getRenderMenu());
		}
		return nodePopupMenu;
	}

	/**
	 * This method initializes connectMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getConnectMenuItem() {
		if (connectMenuItem == null) {
			connectMenuItem = new JMenuItem();
			connectMenuItem.setText("Connect");
			connectMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showConnect();
				}
			});
		}
		return connectMenuItem;
	}

	/**
	 * This method initializes disconnectMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getDisconnectMenuItem() {
		if (disconnectMenuItem == null) {
			disconnectMenuItem = new JMenuItem();
			disconnectMenuItem.setText("Disconnect");
			disconnectMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					disconnect();
				}
			});
		}
		return disconnectMenuItem;
	}

	/**
	 * This method initializes killMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getKillMenuItem() {
		if (killMenuItem == null) {
			killMenuItem = new JMenuItem();
			killMenuItem.setText("Kill");
			killMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					killNode();
				}
			});
		}
		return killMenuItem;
	}

	/**
	 * This method initializes removeMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getRemoveMenuItem() {
		if (removeMenuItem == null) {
			removeMenuItem = new JMenuItem();
			removeMenuItem.setText("Remove");
			removeMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					remove();
				}
			});
		}
		return removeMenuItem;
	}

	/**
	 * This method initializes addContactMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getAddContactMenuItem() {
		if (addContactMenuItem == null) {
			addContactMenuItem = new JMenuItem();
			addContactMenuItem.setText("Add Contact");
			addContactMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getNodePopupMenu().setVisible(false);
					addContact();
				}
			});
		}
		return addContactMenuItem;
	}

	/**
	 * Gets checkbox menu items for all reposition menu items.
	 * 
	 * @param index
	 *            the index of a reposition menu item.
	 * @return a JCheckBoxMenuItem corresponding to the IPosition interface
	 *         indexed by index.
	 */
	private JCheckBoxMenuItem getRepositionMenuItems(int index) {
		if (repositionMenuItems == null) {
			repositionMenuItems = new ArrayList<JCheckBoxMenuItem>();
		}
		if (index >= repositionMenuItems.size()) {
			JCheckBoxMenuItem repositionMenuItem = new JCheckBoxMenuItem();
			repositionMenuItem.setText(positions.get(index).getName());
			repositionMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setReposition(getRepositionByName(e.getActionCommand()));
					reposition();
				}
			});
			if (repositionMenuItem.getActionCommand().equals(position.getName())) repositionMenuItem.setSelected(true);
			repositionMenuItems.add(index, repositionMenuItem);
		}
		return repositionMenuItems.get(index);
	}

	/**
	 * This method initializes repositionMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getRepositionMenu() {
		if (repositionMenu == null) {
			repositionMenu = new JMenu();
			repositionMenu.setText("Reposition");
			for (int i = 0; i < positions.size(); i++) {
				repositionMenu.add(getRepositionMenuItems(i));
			}
		}
		return repositionMenu;
	}

	/**
	 * Gets checkbox menu items for all adjacency draw menu items.
	 * 
	 * @param index
	 *            the index of an adjacency draw menu item.
	 * @return a JCheckBoxMenuItem corresponding to the IAdjacency interface
	 *         indexed by index.
	 */
	private JCheckBoxMenuItem getAdjacencyMenuItems(int index) {
		if (adjacencyMenuItems == null) {
			adjacencyMenuItems = new ArrayList<JCheckBoxMenuItem>();
		}
		if (index >= adjacencyMenuItems.size()) {
			JCheckBoxMenuItem adjacencyMenuItem = new JCheckBoxMenuItem();
			adjacencyMenuItem.setText(adjacencies.get(index).getName());
			adjacencyMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setAdjacency(getAdjacencyByName(e
							.getActionCommand()));
				}
			});
			if (adjacencyMenuItem.getActionCommand().equals(adjacency.getName())) adjacencyMenuItem.setSelected(true);
			adjacencyMenuItems.add(index, adjacencyMenuItem);
		}
		return adjacencyMenuItems.get(index);
	}

	/**
	 * This method initializes adjacencyModeMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getAdjacencyModeMenu() {
		if (adjacencyModeMenu == null) {
			adjacencyModeMenu = new JMenu();
			adjacencyModeMenu.setText("Adjacency Mode");
			adjacencyModeMenu.add(getRenderedLayersMenu());
			for (int i = 0; i < adjacencies.size(); i++) {
				adjacencyModeMenu.add(getAdjacencyMenuItems(i));
			}
		}
		return adjacencyModeMenu;
	}

	/**
	 * This method initializes renderedLayerMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getRenderedLayersMenu() {
		if (renderedLayerMenu == null) {
			renderedLayerMenu = new JMenu();
			renderedLayerMenu.setText("Rendered Layers");

			for (int i = 0; i < getNumLayers(); i++) {
				renderedLayerMenu.add(getRenderedLayersMenuItems(i));
			}
		}
		return renderedLayerMenu;
	}

	/**
	 * Initializes the rendered layers menu items.
	 * 
	 * @param index
	 *            the index of a rendered layer menu item.
	 * @return the JCheckBoxMenuItem corresponding to the rendered layer menu
	 *         item indexed by index.
	 */
	private JCheckBoxMenuItem getRenderedLayersMenuItems(int index) {
		if (renderedLayersMenuItems == null) {
			renderedLayersMenuItems = new ArrayList<JCheckBoxMenuItem>();
		}
		if (index >= renderedLayersMenuItems.size()) {
			JCheckBoxMenuItem renderedLayersMenuItem = new JCheckBoxMenuItem();
			renderedLayersMenuItem.setText("Layer " + index);
			renderedLayersMenuItem.setSelected(true);
			renderedLayersMenuItems.add(renderedLayersMenuItem);
		}
		return renderedLayersMenuItems.get(index);
	}

	/**
	 * This method initializes clearDisconnectedMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getClearDisconnectedMenuItem() {
		if (clearDisconnectedMenuItem == null) {
			clearDisconnectedMenuItem = new JMenuItem();
			clearDisconnectedMenuItem.setText("Clear Disconnected");
			clearDisconnectedMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					clearDisconnected();
				}
			});
		}
		return clearDisconnectedMenuItem;
	}

	/**
	 * This method initializes activeRefreshCheckBoxMenuItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getActiveRefreshCheckBoxMenuItem() {
		if (activeRefreshCheckBoxMenuItem == null) {
			activeRefreshCheckBoxMenuItem = new JCheckBoxMenuItem();
			activeRefreshCheckBoxMenuItem.setText("Active Refresh");
			activeRefreshCheckBoxMenuItem.setSelected(settings.active);
			activeRefreshCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					setRefreshMode();
				}
			});
		}
		return activeRefreshCheckBoxMenuItem;
	}

	/**
	 * This method initializes animatedCheckBoxMenuItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getAnimatedCheckBoxMenuItem() {
		if (animatedCheckBoxMenuItem == null) {
			animatedCheckBoxMenuItem = new JCheckBoxMenuItem();
			animatedCheckBoxMenuItem.setSelected(settings.animated);
			animatedCheckBoxMenuItem.setText("Animated");
			animatedCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					settings.animated = animatedCheckBoxMenuItem.getState();
					if (settings.animated) {
						timer.start();
					} else {
						timer.stop();
						repaint();
					}
				}
			});
		}
		return animatedCheckBoxMenuItem;
	}
	
	private JCheckBoxMenuItem getAutoClearDeletedNodesCheckBoxMenuItem() {
		if (autoClearDeletedNodesCheckBoxMenuItem == null) {
			autoClearDeletedNodesCheckBoxMenuItem = new JCheckBoxMenuItem();
			autoClearDeletedNodesCheckBoxMenuItem.setSelected(settings.autoclear);
			autoClearDeletedNodesCheckBoxMenuItem.setText("AutoClear Disconnected");
			autoClearDeletedNodesCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					settings.autoclear = autoClearDeletedNodesCheckBoxMenuItem.getState();
				}
			});
		}
		return autoClearDeletedNodesCheckBoxMenuItem;
	}
	
	private JCheckBoxMenuItem getAutoConnectNewNodesCheckBoxMenuItem() {
		if (autoConnectNewNodesCheckBoxMenuItem == null) {
			autoConnectNewNodesCheckBoxMenuItem = new JCheckBoxMenuItem();
			autoConnectNewNodesCheckBoxMenuItem.setSelected(settings.autoconnect);
			autoConnectNewNodesCheckBoxMenuItem.setText("AutoConnect New Nodes");
			autoConnectNewNodesCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					settings.autoconnect = autoConnectNewNodesCheckBoxMenuItem.getState();
				}
			});
		}
		return autoConnectNewNodesCheckBoxMenuItem;
	}

	/**
	 * This method initializes setTimeoutMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSetTimeoutMenuItem() {
		if (setTimeoutMenuItem == null) {
			setTimeoutMenuItem = new JMenuItem();
			setTimeoutMenuItem.setText("Set Timeout...");
			setTimeoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setTimeout();
				}
			});
		}
		return setTimeoutMenuItem;
	}

	/**
	 * This method initializes requeryMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getRequeryMenuItem() {
		if (requeryMenuItem == null) {
			requeryMenuItem = new JMenuItem();
			requeryMenuItem.setText("Requery");
			requeryMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					requery();
				}
			});
		}
		return requeryMenuItem;
	}

	/**
	 * This method initializes requeryAllMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getRequeryAllMenuItem() {
		if (requeryAllMenuItem == null) {
			requeryAllMenuItem = new JMenuItem();
			requeryAllMenuItem.setText("Requery All Nodes");
			requeryAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					requeryAll();
				}
			});
		}
		return requeryAllMenuItem;
	}

	/**
	 * This method initializes renderMenu
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getRenderMenu() {
		if (renderMenu == null) {
			renderMenu = new JMenu();
			renderMenu.setText("Rendered Components");
			renderMenu.add(getDrawLabelCheckBoxMenuItem());
			renderMenu.add(getDrawAdjacencyCheckBoxMenuItem());
			renderMenu.addSeparator();
			renderMenu.add(getDrawInfoCheckBoxMenuItem());
			renderMenu.add(getInfoFilterMenuItem());
		}
		return renderMenu;
	}

	/**
	 * This method initializes infoFilterMenuItem
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenuItem getInfoFilterMenuItem() {
		if (infoFilterMenuItem == null) {
			infoFilterMenuItem = new JMenuItem();
			infoFilterMenuItem.setText("Info Filter...");
			infoFilterMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String result = JOptionPane.showInputDialog(
									Monitor.this,
									"Enter a regular expression to filter the rendered\n"
											+ "info text. A \".*\" will match all expressions and will\n"
											+ "therefore not filter any text.",
									"Info Text Regex Filter",
									JOptionPane.INFORMATION_MESSAGE);
					if (result == null) return;
					settings.infoFilter = result;
				}
			});
		}
		return infoFilterMenuItem;
	}

	/**
	 * This method initializes drawLabelCheckBoxMenuItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getDrawLabelCheckBoxMenuItem() {
		if (drawLabelCheckBoxMenuItem == null) {
			drawLabelCheckBoxMenuItem = new JCheckBoxMenuItem();
			drawLabelCheckBoxMenuItem.setSelected(settings.drawLabel);
			drawLabelCheckBoxMenuItem.setText("Labels");
			drawLabelCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					settings.drawLabel = drawLabelCheckBoxMenuItem.getState();
				}
			});
		}
		return drawLabelCheckBoxMenuItem;
	}

	/**
	 * This method initializes drawInfoCheckBoxMenuItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getDrawInfoCheckBoxMenuItem() {
		if (drawInfoCheckBoxMenuItem == null) {
			drawInfoCheckBoxMenuItem = new JCheckBoxMenuItem();
			drawInfoCheckBoxMenuItem.setSelected(settings.drawInfo);
			drawInfoCheckBoxMenuItem.setText("User Info");
			drawInfoCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					settings.drawInfo = drawInfoCheckBoxMenuItem.getState();
				}
			});
		}
		return drawInfoCheckBoxMenuItem;
	}

	/**
	 * This method initializes drawAdjacencyCheckBoxMenuItem
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getDrawAdjacencyCheckBoxMenuItem() {
		if (drawAdjacencyCheckBoxMenuItem == null) {
			drawAdjacencyCheckBoxMenuItem = new JCheckBoxMenuItem();
			drawAdjacencyCheckBoxMenuItem.setSelected(settings.drawAdjacencies);
			drawAdjacencyCheckBoxMenuItem.setText("Adjacencies");
			drawAdjacencyCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					settings.drawAdjacencies = drawAdjacencyCheckBoxMenuItem.getState();
				}
			});
		}
		return drawAdjacencyCheckBoxMenuItem;
	}

	public static void main(String args[]) throws IOException {
		Monitor m = new Monitor();
		com.search.SearchGUI ui = new com.search.SearchGUI();
		ui.start();
		m.addContact("localhost", 10001);
		int port = 10002;
		for (int i = 0; i < 39; i++, port += 2) {
			try {
				Thread.sleep(750);
			} catch (InterruptedException ignored) {}
			ui = new com.search.SearchGUI(port);
			ui.start();
		}
	}

	/**
	 * The animation loop.
	 */
	private class AnimationLoop implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (settings.animated) repaint();
			if (settings.autoclear) clearDisconnected();
		}
	}

	/**
	 * Representation of a Node.
	 */
	public class Node extends Thread {
		private InetSocketAddress me = null;
		private InetSocketAddress peers[][] = null;
		private Node[][] peerCache = null;

		private int r = 5;
		private float x = 0;
		private float y = 0;
		private int fx;
		private int fy;
		private int fa;
		private int mx;
		private int my;

		private Color connectedColor = new Color(0, 100, 200);
		private Color disconnectedColor = new Color(200, 100, 0);

		private long lastUpdate = -1;
		private boolean mouseOver = false;
		private ArrayList<String> infolines = null;
		private String label = null;
		private HashMap<String, String> info = null;
		private int fontSize = 11;

		private boolean running = true;

		private Socket s = null;
		private PrintWriter out = null;
		private BufferedReader in = null;
		private String host = null;
		private int port = -1;

		private Console console = null;

		/**
		 * Node constructor. Creates a new Node and connects to it.
		 * 
		 * @param host
		 *            the hostname of the node.
		 * @param port
		 *            the port where the node is listening.
		 */
		public Node(String host, int port) {
			initialize();
			connect(host, port);
		}

		/**
		 * Node constructor. Creates a new Node and connects to it.
		 * 
		 * @param me
		 *            the InetSocketAddress of the node.
		 */
		public Node(InetSocketAddress me) {
			this.me = me;

			String host = me.getAddress().getHostAddress();
			int port = me.getPort() + 1;
			initialize();
			connect(host, port);
		}

		/**
		 * Initializes the node.
		 */
		private void initialize() {
			info = new HashMap<String, String>();
			y = (float) Math.random() * 993;
		}

		/**
		 * A call to this method informs this node that the mouse is currently
		 * hovering over it. Its primary purpose is to allow the node to
		 * generate the info box which it will display in the paint methods.
		 * 
		 * @param mx
		 *            specifies the x coordinates of the mouse.
		 * @param my
		 *            specifies the y coordinates of the mouse.
		 */
		private void mouseOver(int mx, int my) {
			fa = 0;
			this.mx = mx;
			this.my = my;
			generateInfoBox();
			mouseOver = true;
		}

		/**
		 * Informs this node that the mouse is no longer hovering over it.
		 */
		private void mouseOut() {
			mouseOver = false;
		}

		/**
		 * Specifies the number of layers in the overlay.
		 * 
		 * @return the number of layers in the overlay.
		 */
		private int getNumLayers() {
			if (peers == null) return 0;
			return peers.length;
		}

		/**
		 * Gets the number of peers at a specific layer.
		 * 
		 * @param layer
		 *            the layer at which to count peers.
		 * @return the number of peers at the layer given by layer.
		 */
		private int getNumNodes(int layer) {
			try {
				return peers[layer].length;
			} catch (Exception e) {}
			return 0;
		}

		/**
		 * Gets the InetSocketAddress of the peer at (layer, index).
		 * 
		 * @param layer
		 *            the layer.
		 * @param index
		 *            the index.
		 * @return the InetSocketAddress of the peer at (layer, index).
		 */
		private InetSocketAddress getPeer(int layer, int index) {
			try {
				return peers[layer][index];
			} catch (Exception e) {}
			return null;
		}

		/**
		 * Gets the Node at (layer, index). This is essentially a method to
		 * automatically cache node information.
		 * 
		 * @param layer
		 *            the layer.
		 * @param index
		 *            the index.
		 * @return the Node at (layer, index).
		 */
		private Node getNodeAt(int layer, int index) {
			if (peers == null || peers[layer] == null || peers[layer][index] == null) return null;
			if (peerCache[layer][index] == null || !peerCache[layer][index].me.equals(peers[layer][index])) {
				Node n = getNode(peers[layer][index]);
				if (n == null && settings.autoconnect) n = newNode(peers[layer][index]);
				peerCache[layer][index] = n;
			}
			return peerCache[layer][index];
		}

		/**
		 * Returns whether or not this node is connected.
		 * 
		 * @return wheter or not this node is connected.
		 */
		private boolean isConnected() {
			return (s != null && s.isConnected());
		}
		
		private boolean isDisconnected() {
			return (s != null && s.isClosed());
		}

		/**
		 * Gets whether or not this node was clicked given the coordinates of
		 * the click.
		 * 
		 * @param cx
		 *            the x coordinate of the click.
		 * @param cy
		 *            the y coordinate of the click.
		 * @return whether or not the current node was clicked.
		 */
		private boolean isClicked(int cx, int cy) {
			return cx >= x - r && cx <= x + r && cy >= y - r && cy <= y + r;
		}

		/**
		 * Sets the console's visibility.
		 * 
		 * @param visible
		 *            true to set the console to visible, false to hide it.
		 * @param parent
		 *            the parent of the console. Used only to decide the
		 *            location of the console window. It must not be null.
		 */
		private void setConsoleVisible(boolean visible, Component parent) {
			if (console == null) return;
			console.setBounds((int) x + parent.getX(), (int) y + parent.getY(), console.getWidth(), console.getHeight());
			console.setVisible(visible);
			sendConsoleCmd("start_ui " + host + " " + port);
		}

		/**
		 * Connects the node to the specified host and port.
		 * 
		 * @param host
		 *            the hostname of the node.
		 * @param port
		 *            the port where the node is listening.
		 */
		private void connect(String host, int port) {
			if (isConnected()) return;
			this.host = host;
			this.port = port;
			console = new Console("Console: " + host + ":" + port);
			this.start();
		}
		
		/**
		 * Sets the console's title.
		 * 
		 * @param title
		 *            the title to use for the console window.
		 */
		private void setConsoleTitle(String title) {
			if (console == null) return;
			String prevTitle = console.getTitle();
			String newTitle = "Console: " + host + ":" + port + " " + title;
			if (newTitle.equals(prevTitle)) return;
			console.setTitle(newTitle);
		}

		/**
		 * Disconnects this node.
		 */
		private void close() {
			running = false;
			try {
				if (out != null) out.println("quit");
				if (out != null) out.close();
				if (in != null) in.close();
				if (s != null) s.close();
			} catch (Exception e) {}
			if (console != null) console.dispose();
		}

		/**
		 * Returns whether or not this node's InetSocketAddress matches the
		 * specified InetSocketAddress.
		 * 
		 * @param node
		 *            the InetSocketAddress to compare this node to.
		 * @return true if this Node's InetSocketAddress matches the
		 *         InetSocketAddress specified by Node, and false otherwise.
		 */
		private boolean addressIsEqual(InetSocketAddress node) {
			if (me == null) return false;
			if (me.equals(node)) return true;
			return false;
		}

		/**
		 * Causes this node to die.
		 */
		private void kill() {
			if (running && out != null) {
				out.println("kill");
				peers = null;
			}
		}

		/**
		 * Change's this node's refresh mode (either active or passive).
		 */
		private void setRefreshMode() {
			if (running && out != null) {
				if (settings.active) out.println("active");
				else out.println("passive");
			}
		}

		/**
		 * Adds a contact to this node.
		 * 
		 * @param contact
		 *            the contact to add to this node.
		 */
		private void addContact(InetSocketAddress contact) {
			if (running && out != null) {
				Message send = new Message("addcontact", contact.getAddress().getHostAddress() + ":" + contact.getPort());
				out.println(send);
			}
		}

		/**
		 * Requries this node for all information (all information about this
		 * node is re-requested from the corresponding MonitorService.
		 */
		private void requery() {
			if (running && out != null) {
				out.println("requery");
			}
		}

		/**
		 * Sends a console command to the node.
		 * 
		 * @param message
		 *            the command to send.
		 */
		private void sendConsoleCmd(String message) {
			if (running && out != null) {
				Message send = new Message("console", message);
				out.println(send);
			}
		}

		/**
		 * Gets this node's x position.
		 * 
		 * @return this node's x position.
		 */
		public int getX() {
			return (int) x;
		}

		/**
		 * Gets this node's y position.
		 * 
		 * @return this node's y position.
		 */
		public int getY() {
			return (int) y;
		}

		/**
		 * Move's this node a new location. This method automatically animates
		 * the movement of the node. To move the node immediately to a new
		 * location without animation, use the setPos method.
		 * 
		 * @param fx
		 *            the x position of the new location.
		 * @param fy
		 *            the y position of the new location.
		 */
		public void moveTo(int fx, int fy) {
			this.fx = fx;
			this.fy = fy;
		}

		/**
		 * Move's this node to a new location. This method moves the node
		 * immediately without animating it. To animate the node, use the moveTo
		 * method.
		 * 
		 * @param x
		 *            the x position of the new location.
		 * @param y
		 *            the y position of the new location.
		 */
		public void setPos(int x, int y) {
			this.x = x;
			this.y = y;
			this.fx = x;
			this.fy = y;
		}

		/**
		 * Gets user information given a specific key value.
		 * 
		 * @param key
		 *            the key of the information we want.
		 * @return the value indexed by key.
		 */
		public String getUserInfo(String key) {
			return info.get(key);
		}

		/**
		 * This thread's run method maintains communcation with the
		 * MonitorService on the remote end.
		 */
		public void run() {
			try {
				running = true;
				s = new Socket(host, port);
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(s.getOutputStream(), true);
			} catch (Exception e) {
				System.out.println("Could not establish monitor session: " + e.getMessage());
				close();
			}

			setRefreshMode();
			requery();

			while (running) {
				Message recv = null;
				try {
					String input = in.readLine();
					if (input == null) {
						close();
						continue;
					}
					recv = Message.parse(input);
				} catch (IOException e) {
					close();
					continue;
				}

				if (recv.cmdEquals("console")) {
					String[] data = recv.getData().split("\r\n");
					for (String line : data) console.writeToConsole(line + "\n");
					continue;
				}

				if (recv.cmdEquals("layers")) {
					int layerCount = Integer.parseInt(recv.getData());
					peers = new InetSocketAddress[layerCount][];
					peerCache = new Node[layerCount][];
					for (int i = 0; i < peers.length; i++) {
						Message send = new Message("getpeers", Integer.toString(i));
						out.println(send);
					}
					continue;
				}

				if (recv.cmdEquals("peers")) {
					lastUpdate = System.currentTimeMillis();
					int layer = Integer.parseInt(recv.getData(0));
					if (peers == null) continue;
					peers[layer] = new InetSocketAddress[recv.countDataTokens() - 1];
					peerCache[layer] = new Node[recv.countDataTokens() - 1];
					for (int i = 1; i < recv.countDataTokens(); i++) {
						String host = recv.getData(i).substring(0, recv.getData(i).indexOf(":"));
						int port = Integer.parseInt(recv.getData(i).substring(recv.getData(i).indexOf(":") + 1));
						peers[layer][i - 1] = new InetSocketAddress(host, port);
					}
					continue;
				}

				if (recv.cmdEquals("identity")) {
					String host = recv.getData().substring(0, recv.getData().indexOf(":"));
					int port = Integer.parseInt(recv.getData().substring(recv.getData().indexOf(":") + 1));
					me = new InetSocketAddress(host, port);
					continue;
				}

				if (recv.cmdEquals("info")) {
					for (int i = 0; i < recv.countDataTokens(); i++) {
						String data = recv.getData(i);
						String key = data.substring(0, data.indexOf('='));
						String value = data.substring(data.indexOf('=') + 1);
						info.put(key, value);
					}
					userEvent(this);
					continue;
				}
			}
		}

		/**
		 * Animation method: updates node position for the current frame.
		 */
		private void updatePosition() {
			if (fx == x && fy == y)
				return;

			if (!settings.animated) {
				x = fx;
				y = fy;
				return;
			}

			float dx = 0, dy = 0;
			if (Math.round(x) != fx || Math.round(y) != fy) {
				dx = (fx - x) / 8;
				dy = (fy - y) / 8;
			} else {
				x = fx;
				y = fy;
				dx = 0;
				dy = 0;
			}
			x += dx;
			y += dy;
		}

		/**
		 * Generates the information box that is dispalyed on mouse over of a
		 * node.
		 */
		private void generateInfoBox() {
			infolines = new ArrayList<String>();
			infolines.add(me.toString());
			for (int i = 0; peers != null && i < peers.length; i++) {
				infolines.add("Layer " + i + " Peers");
				for (int j = 0; peers[i] != null && j < peers[i].length; j++) {
					infolines.add("  " + peers[i][j]);
				}
			}
			infolines.add("");
			infolines.add("Node Info:");
			Set<String> keys = info.keySet();
			for (String key : keys) {
				infolines.add("  " + key + " = " + info.get(key));
			}
		}

		/**
		 * Paints the info box.
		 * 
		 * @param g
		 *            the graphics context.
		 */
		private void drawInfoBox(Graphics2D g) {
			if (infolines == null) return;
			if (settings.animated && fa != 255) fa = fa + (255 - fa) / 4;
			else fa = 255;
			g.setFont(new Font("Tahoma", Font.BOLD, 10));
			FontMetrics fm = g.getFontMetrics();

			int margin = 5;
			int width = 0;
			int xpos = mx;
			int ypos = my;
			for (int i = 0; i < infolines.size(); i++) {
				int tempWidth = fm.bytesWidth(infolines.get(i).getBytes(), 0, infolines.get(i).length());
				if (tempWidth > width) width = tempWidth;
			}
			int height = fm.getHeight() * (infolines.size() + 1);
			if (xpos + width > getWidth()) xpos = getWidth() - width - 2 * margin;
			if (ypos + height > getHeight()) ypos = getHeight() - height - 2 * margin;

			g.setColor(new Color(0, 0, 80, (fa * 150) / 255));
			g.fillRoundRect(xpos, ypos, width + 2 * margin, height + 2 * margin, 10, 10);
			g.setColor(new Color(0, 0, 40, fa));
			g.drawRoundRect(xpos, ypos, width + 2 * margin, height + 2 * margin, 10, 10);
			g.setColor(new Color(255, 255, 255, fa));
			g.drawString("Last Update: " + (System.currentTimeMillis() - lastUpdate), xpos + margin, ypos + margin + fm.getAscent());
			for (int i = 0; i < infolines.size(); i++) {
				g.drawString(infolines.get(i), xpos + margin, ypos + margin + fm.getAscent() + fm.getHeight() * (i + 1));
			}
		}

		/**
		 * Paints node information directly above the node.
		 * 
		 * @param g
		 *            the graphics context.
		 */
		private void drawInfo(Graphics2D g) {
			g.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
			long time = System.currentTimeMillis() - lastUpdate;
			if (time > settings.timeout && settings.active) g.setColor(Color.RED);
			else g.setColor(Color.BLACK);
			FontMetrics fm = g.getFontMetrics();
			String infoText = "";
			Set<String> keys = info.keySet();
			for (String key : keys) {
				try {
					if (key.matches(settings.infoFilter)) {
						String value = info.get(key);
						infoText += " " + key + "=" + value;
					}
				} catch (PatternSyntaxException e) {
					settings.infoFilter = ".*";
					JOptionPane.showMessageDialog(Monitor.this,
							"There was a syntax error in the regular expression entered.\n" 
							+ e.getMessage() + "\n"
							+ "The expression was reverted to the default expression.",
							"Syntax Error in Regular Expression",
							JOptionPane.ERROR_MESSAGE);
					if (key.matches(settings.infoFilter)) {
						String value = info.get(key);
						infoText += " " + key + "=" + value;
					}
				}
			}
			infoText = infoText.trim();
			setConsoleTitle(infoText);
			int infoWidth = fm.bytesWidth(infoText.getBytes(), 0, infoText.length());
			int infoHeight = fm.getAscent();
			g.drawString(infoText, x - infoWidth / 2, y - infoHeight / 2);
		}

		/**
		 * Paints the node's label directly above the node.
		 * 
		 * @param g
		 *            the graphics context.
		 */
		private void drawLabel(Graphics2D g) {
			g.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
			g.setColor(Color.BLACK);
			FontMetrics fm = g.getFontMetrics();
			if (me != null) label = me.toString();
			long time = System.currentTimeMillis() - lastUpdate;
			if (time > settings.timeout && settings.active) g.setColor(Color.RED);
			if (label != null) {
				String label = this.label;
				int labelWidth = fm.bytesWidth(label.getBytes(), 0, label.length());
				int labelHeight = fm.getAscent();
				g.drawString(label, x - labelWidth / 2, y - labelHeight / 2 - 12);
			} else {
				String label = "Unknown";
				int labelWidth = fm.bytesWidth(label.getBytes(), 0, label.length());
				int labelHeight = fm.getAscent();
				g.drawString(label, x - labelWidth / 2, y - labelHeight / 2 - 12);
			}
		}

		/**
		 * Paints the node.
		 * 
		 * @param g
		 *            the graphics context.
		 */
		private void drawNode(Graphics2D g) {
			if (isConnected()) g.setColor(connectedColor);
			else g.setColor(disconnectedColor);
			g.fillOval(Math.round(x) - r, Math.round(y) - r, r + r, r + r);
		}

		/**
		 * Paints the info box.
		 * 
		 * @param g
		 *            the graphics context.
		 */
		private void paintInfoBox(Graphics2D g) {
			if (mouseOver)
				drawInfoBox(g);
		}

		/**
		 * Paints the node and all related info.
		 * 
		 * @param g
		 *            the graphics context.
		 */
		private void paint(Graphics2D g) {
			updatePosition();
			drawNode(g);
			if (settings.drawLabel) drawLabel(g);
			if (settings.drawInfo) drawInfo(g);
		}

		private class Console extends JFrame {
			private static final long serialVersionUID = -825862307404524407L;
			private JPanel jContentPane = null;
			private JTextPane consoleTextPane = null;
			private JScrollPane jScrollPane = null;
			private JTextField commandTextField = null;

			public Console(String title) {
				super(title);
				initialize();
			}

			private void initialize() {
				this.setSize(500, 300);
				this.setContentPane(getJContentPane());
				this.setDefaultCloseOperation(HIDE_ON_CLOSE);
				this.addPropertyChangeListener("visible", new java.beans.PropertyChangeListener() {
					public void propertyChange(java.beans.PropertyChangeEvent e) {
						if (isVisible()) commandTextField.requestFocusInWindow();
					}
				});
			}

			private JPanel getJContentPane() {
				if (jContentPane == null) {
					jContentPane = new JPanel();
					jContentPane.setLayout(new BorderLayout());
					jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
					jContentPane.add(getCommandTextField(), BorderLayout.SOUTH);
				}
				return jContentPane;
			}

			private JScrollPane getJScrollPane() {
				if (jScrollPane == null) {
					jScrollPane = new JScrollPane();
					jScrollPane.setViewportView(getConsoleTextPane());
				}
				return jScrollPane;
			}

			private JTextPane getConsoleTextPane() {
				if (consoleTextPane == null) {
					consoleTextPane = new JTextPane();
					consoleTextPane.setEditable(false);
					consoleTextPane.setFont(new Font("Lucida Console", Font.PLAIN, 10));
					consoleTextPane.addFocusListener(new java.awt.event.FocusAdapter() {
						public void focusGained(java.awt.event.FocusEvent e) {
							commandTextField.requestFocusInWindow();
						}
					});
				}
				return consoleTextPane;
			}

			private JTextField getCommandTextField() {
				if (commandTextField == null) {
					commandTextField = new JTextField();
					commandTextField.setFont(new Font("Lucida Console", Font.PLAIN, 10));
					commandTextField.addActionListener(new java.awt.event.ActionListener() {
						/**
						 * Reads commands from the console window and sends them
						 * to the MonitorService of the current node.
						 */
						public void actionPerformed(java.awt.event.ActionEvent e) {
							String command = commandTextField.getText().trim();
							writeToConsole("> " + command + "\n");
							if (command.equals("")) return;
							sendConsoleCmd(command);
							commandTextField.setText("");
						}
					});
				}
				return commandTextField;
			}

			/**
			 * Writes text to the console window.
			 * 
			 * @param message
			 *            the message to write to the console window.
			 */
			private void writeToConsole(String message) {
				String current = consoleTextPane.getText();
				consoleTextPane.setText(current + message);
				try {
					consoleTextPane.setCaretPosition(consoleTextPane.getText().length());
				} catch (Exception e) {};
			}
		}
	}
}