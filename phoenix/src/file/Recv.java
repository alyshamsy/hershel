package file;

import java.io.*;
import java.net.*;

/*
 * Created by: Shadanan Sharma
 * ECE361 - Simple data transfer tool.
 * Created on: 22-Dec-2005
 * E-Mail: shadanan@gmail.com
 * Web Site: http://www.convergence2000.com
 */

public class Recv extends Thread {
	public static final int NOT_STARTED = 0;
	public static final int RECEIVING = 1;
	public static final int FAILED = 2;
	public static final int SUCCEEDED = 3;
	
	private ServerSocket ss;
	private Socket s;
	private InputStream in; //Input from socket.
	
	private int port;
	private int size;
	private byte[] data;
	private int state;
	private boolean running;
	
	public Recv(int port, int size) {
		this.port = port;
		this.size = size;
		this.data = new byte[(int)size];
		this.state = NOT_STARTED;
		this.running = true;
	}
	
	public int getRecvState() {
		return state;
	}
	
	/**
	 * If state is failed, this will return null.
	 * @return
	 */
	public byte[] getData() {
		if (state == SUCCEEDED) return data;
		else return null;
	}
	
	public void close() {
		running = false;
		try {
			if (in != null) in.close();
		} catch (IOException e) {}
		try {
			if (s != null) s.close();
		} catch (IOException e) {}
		try {
			if (ss != null) ss.close();
		} catch (IOException e) {}
	}
	
	/* Might want to modify run() such that you pass it the serversocket and port
	 * so that we don't open multiple sockets each time.  
	 */
	public void run() {
		state = RECEIVING;
		try {
			ss = new ServerSocket(port);
			s = ss.accept();
			in = s.getInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			state = FAILED;
			close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			state = FAILED;
			close();
			return;
		}
		
		int n = 0;
		while (running) {
			try {
				int len = in.read(data, n, data.length-n);
				n += len;
				if (n == size) running = false;
			} catch (IOException e) {
				e.printStackTrace();
				state = FAILED;
				close();
				return;
			}
		}
		
		state = SUCCEEDED;
		close();
	}
}
