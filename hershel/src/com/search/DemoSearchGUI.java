package com.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.*;

public class DemoSearchGUI {

	private class InputHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String[] command = input.getText().split(" ");
			output.append("#");
			for (int i = 0; i < command.length; i++)
				output.append(" " + command[i]);
			output.append("\n");
			input.setText(null);

			MessageHandler h = client.getHandler();
			if (command[0].equals("find_node")) {
				//h.findNode(targetNode, targetId);
			} else if (command[0].equals("find_value")) {
				try {
					h.findValue(new SearchId(SHA1Utils.getSHA1Digest(command[1].getBytes())));
				} catch (IOException ex) {
					output.append("! Searching error.\n");
				}
			}
		}
		
	}

	static private String initialId = "1234567890123456789012345678901234567890";
	private JFrame frame;
	private JTextField input;
	private JTextArea output;
	private InputHandler handler;
	private NetworkSearchClient client;

	public DemoSearchGUI(String id, boolean visible) throws SocketException {
		client = new NetworkSearchClient(id, 10000);
		handler = new InputHandler();
		addDemoKeywords();
		setUpWindow(id, visible);
		client.registerUI(this);
		client.start();
	}

	private void addDemoKeywords() {
		String[] keywords = {
				"bob", "maxim", "jason", "ryan", "jordan", "aly", "rock", "paper", "scissors",
				"poop", "push", "strain", "laxatives", "brown", "borat"
		};
		
		String fileName = keywords[(int)(Math.random() * 
				keywords.length)];
		System.out.println(fileName);
		SearchId hashedFilename = new SearchId(SHA1Utils.getSHA1Digest(fileName.getBytes()));
		
		SearchId fileHash = SearchId.getRandomId();
		ArrayList<SearchId> chunkHashes = new ArrayList<SearchId>();
		ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();        
        
        for(int i = 0; i<4; i++)
        {
            chunkHashes.add(SearchId.getRandomId());
        }
        
        for(int i = 0; i<4; i++)
        {
            peers.add(new InetSocketAddress("localhost", i+10));           
        }
        
        SearchResult r = new SearchResult(hashedFilename, fileHash, chunkHashes, 4*512*1024-100, peers);
        
        client.getHandler().database().put(hashedFilename, r);
	}

	public DemoSearchGUI(int port, boolean visible) throws SocketException {
		String randomId = SearchId.getRandomId().toString();
		client = new NetworkSearchClient(randomId, port);
		addDemoKeywords();
		handler = new InputHandler();
		setUpWindow(randomId, visible);
		client.registerUI(this);
		client.start();
	}

	private void setUpWindow(String title, boolean visible) {
		frame = new JFrame("Search Demo - " + title);
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		input = new JTextField(20);
		input.addActionListener(handler);
		JButton send = new JButton("Send");
		send.addActionListener(handler);

		JPanel inputArea = new JPanel();
		inputArea.add(input);
		inputArea.add(send);

		output = new JTextArea();
		output.setEditable(false);

		frame.add(inputArea, BorderLayout.SOUTH);
		frame.add(new JScrollPane(output), BorderLayout.CENTER);
		frame.setVisible(visible);
		input.requestFocusInWindow();
	}

	public void getMessage(String s) {
		output.append(s + "\n");
	}

	public static void main(String[] args) throws SocketException {
		/*if (args.length == 2) {
			if (args[0].equals("-id"))
				new SearchGUI(args[1]);
			else if (args[0].equals("-port"))
				new SearchGUI(Integer.parseInt(args[1]));
		} else {
			System.out.println("Usage: java SearchGUI <-port OR -id> <value>");
		}*/
		new DemoSearchGUI(initialId, true);
		int port = 10050;
		for (int i = 0; i < 50; i++, port++) {
			new DemoSearchGUI(port, i % 10 == 0);			
		}
	}

}
