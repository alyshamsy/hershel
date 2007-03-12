package com.search;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketException;

import javax.swing.*;

public class SearchGUI {

	private class InputHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String[] command = input.getText().split(" ");
			output.append(">");
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

	private JFrame frame;
	private JTextField input;
	private JTextArea output;
	private InputHandler handler;
	private NetworkSearchClient client;

	public SearchGUI(String id) throws SocketException {
		client = new NetworkSearchClient(id, 10000);
		handler = new InputHandler();
		setUpWindow();
		client.registerUI(this);
		client.start();
	}

	public SearchGUI(int port) throws SocketException {
		String randomId = SearchId.getRandomId().toString();
		System.out.println(randomId);
		client = new NetworkSearchClient(randomId, port);
		handler = new InputHandler();
		setUpWindow();
		client.registerUI(this);
		client.start();
	}

	private void setUpWindow() {
		frame = new JFrame("Search Demo");
		frame.setSize(350, 300);
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
		frame.setVisible(true);
		input.requestFocusInWindow();
	}

	public void getMessage(String s) {
		output.append("< " + s + "\n");
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
		new SearchGUI("1234567890123456789012345678901234567890");
		new SearchGUI(10050);
		new SearchGUI(10051);
	}

}
