package file;

import java.io.*;
import java.net.*;

/*
 * Created by: Shadanan Sharma
 * Created on: 26-Dec-2005
 * E-Mail: shadanan@gmail.com
 * Web Site: http://www.convergence2000.com
 */

public class Send extends Thread {
	public static final int NOT_STARTED = 0;
	public static final int SENDING = 1;
	public static final int FAILED = 2;
	public static final int SUCCEEDED = 3;
	
	private Socket s;
	private OutputStream out;
	
	private String dest;
	private int port;
	private byte[] data;
	private int state;
	private boolean running;
	
	public Send(String dest, int port, byte[] data) {
		this.dest = dest;
		this.port = port;
		this.data = data;
		this.state = NOT_STARTED;
		this.running = true;
	}
	
	public int getSendState() {
		return state;
	}
	
	public void close() {
		running = false;
		if (s != null) try {
			s.close();
		} catch (IOException e) {}
		if (out != null) try {
			out.close();
		} catch (IOException e) {}
	}
	
	public void run() {
		state = SENDING;
		try {
			s = new Socket(dest, port);
			out = s.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			state = FAILED;
			return;
		} catch (IOException e) {
			e.printStackTrace();
			state = FAILED;
			return;
		}
		
		if (running) {
			try {
				out.write(data);
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
