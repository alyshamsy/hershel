package com.filetransfer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class FileTransferServer extends Thread {

	private ServerSocketChannel channel;

	public FileTransferServer(int port) throws IOException {
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(port));
	}

	public void close() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		try {
			Selector selector = Selector.open();
			channel.register(selector, SelectionKey.OP_ACCEPT);

			// Wait for something of interest to happen
			while (selector.select() > 0) {
				// Get set of ready objects
				Set readyKeys = selector.selectedKeys();
				Iterator readyItor = readyKeys.iterator();

				// Walk through set
				while (readyItor.hasNext()) {

					// Get key from set
					SelectionKey key = (SelectionKey) readyItor.next();

					// Remove current entry
					readyItor.remove();

					if (key.isAcceptable()) {
						// Get channel
						ServerSocketChannel keyChannel = (ServerSocketChannel) key
								.channel();

						// Get server socket
						ServerSocket serverSocket = keyChannel.socket();

						// Accept request
						Socket socket = serverSocket.accept();

						socket.close();
					} else {
						System.err.println("Ooops");
					}

				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
