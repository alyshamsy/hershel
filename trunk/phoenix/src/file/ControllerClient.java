package file;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ControllerClient extends Thread {
	private boolean running;
	
	// FILE PARAMETERS
	private FileSystem file;
	private String FILE_NAME;
	
	// CHUNK PARAMETERS
	private static final int MAX_CHUNKS_ALIVE = 1;	// max number of chunks to download concurrently
	private static final int CHUNK_SIZE = 524288;	// 512kB
	private int NUM_CHUNKS;	
	private ArrayList<Chunk> Chunks;
	private volatile char[] pieceStates;			// holds
	
	// CHUNK STATES
	private static final char NOT_STARTED = 0;
	private static final char IN_PROGRESS = 1;
	private static final char FAILED = 2;
	private static final char SUCCESS = 3;
	
	// SOCKET AND PORT PARAMETERS
	private InetSocketAddress portAddr[];
	private Socket[] cs;
	private static final int CONNECT_TIME_OUT = 10000;	// number of ms before socket throws exception on connect
	private static final int INPUT_TIME_OUT = 1000;		//number of ms before socket throws exception on input stream read
	
	// TESTING!!!!!!!!!!!!!!!!!!!!!!!!!!
	private static final int RYAN_PORT = 10126;
	private static final int JORDAN_PORT = 10035;
	private static final String JORDAN_IP = "128.100.8.189";
	
	public static void main(String args[]) throws IOException {
		int ports[] = {RYAN_PORT};	//port #s to use on this client machine for D/Ling
		ControllerClient a = new ControllerClient(ports, 29, "test.txt");
		a.start();
	}
	
	/*
	 * ControllerClient()
	 * 	vars:
	 * 		port[]: an array of ports deemed acceptable for use by the caller
	 * 		fileSize: the size of the file
	 * 		fileName: the name of the file
	 * 	fctn:
	 * 			
	 */
	public ControllerClient(int[] port, long fileSize, String fileName) {
		running = true;
		
		Chunks = new ArrayList<Chunk>();
		portAddr = new InetSocketAddress[port.length];
		
		// set number of chunks needed
		this.NUM_CHUNKS = (int)Math.ceil(fileSize/CHUNK_SIZE);
		if (this.NUM_CHUNKS == 0)
			this.NUM_CHUNKS = 1;	// number of chunks can be 1 at minimum
		pieceStates = new char[this.NUM_CHUNKS];
		
		for (int i = 0; i < pieceStates.length; i++) {
			pieceStates[i] = NOT_STARTED;
		}
		
		// initialize file
		this.FILE_NAME = fileName;
		
		file = new FileSystem();
		// SOCKETS AND PORTS
		cs = new Socket[MAX_CHUNKS_ALIVE];
		for (int i = 0; i < port.length; i++){
			this.portAddr[i] = new InetSocketAddress(port[i]);
		}
	}
	
	/*
	 * close()
	 * 	fctn:
	 * 		Closes all of the sockets if they are not null, freeing up the ports
	 * 		for later use.
	 */
	public void close() {
		running = false;
		for(int i = 0; i < MAX_CHUNKS_ALIVE; i++) {
			try {
				if (cs[i] != null) cs[i].close();
			} catch (Exception e) {
				System.out.println("Exception on socket close.");
			}
		}
	}
	
	/*
	 * getRandomPiece()
	 * 	fctn:
	 * 		Selects a random chunk id from the set of chunks that still require
	 * 		downloading.
	 */
	private synchronized int getRandomPiece() {
		// get available pieces. 
		ArrayList<Integer> availablePieces = new ArrayList<Integer>();
		for (int i = 0; i < pieceStates.length; i++) {
			if (pieceStates[i] == NOT_STARTED || pieceStates[i] == FAILED) availablePieces.add(new Integer(i));
		}
		
		System.out.println("Chunks remaining: " + availablePieces.size());
		
		if (availablePieces.size() == 0) return -1;
		int index = (int)Math.floor(Math.random()*availablePieces.size());
		return availablePieces.get(index).intValue();
	}
	
	/*
	 * isComplete()
	 * 	fctn:
	 * 		Returns a boolean value which is true if all chunks of the file are
	 * 		complete, and false otherwise.
	 */
	private synchronized boolean isComplete() {
		for (int i = 0; i < pieceStates.length; i++) {
			if (pieceStates[i] != SUCCESS) return false;
		}
		return true;
	}
	
	public synchronized void run() {
		System.out.println("Client Running");
		
		while (running) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				System.out.println("InterruptedException on wait(100).");
			}
			
			if (Chunks.size() < MAX_CHUNKS_ALIVE) {
				if (isComplete()) {
					running = false;
					System.out.println("Download complete");
					continue;
				}
				
				// SOCKET SETUP
				// find a free port to use
				int sockNum = 0;
				for (int i = 0; i < MAX_CHUNKS_ALIVE; i++) {
					try {
						if (cs[i] == null || !cs[i].isConnected())
							sockNum = i;
							continue;
					} catch (NullPointerException e) {
						System.out.println("NullPointerException on socket selection.");
					}
				}
												
				try {
					cs[sockNum] = new Socket();										// create a new socket
					cs[sockNum].setSoTimeout(INPUT_TIME_OUT);
					cs[sockNum].bind(portAddr[sockNum]);	
				} catch (IOException e1) {
					System.out.println("IOException on socket bind.");				// bind it to the free port
					try {
						cs[sockNum].close();
					} catch (IOException e) {}
					continue;
				}							
				
				try {
					InetAddress ipAddr = InetAddress.getByName(JORDAN_IP);	// construct the IP InetAddress
					InetSocketAddress addr = new InetSocketAddress(ipAddr, JORDAN_PORT);	// construct the full InetSocketAddress
					cs[sockNum].connect(addr, CONNECT_TIME_OUT);					// connect the socket
				}
				catch (UnknownHostException e) {
					System.out.println("UnknownHostException on socket connect.");
					continue;
				} catch (IOException e) {
					System.out.println("IOException on socket connect.");
					continue;
				} catch (NullPointerException e) {
					System.out.println("NullPointerException on socket connect.");
					continue;
				}
				
				int randomPieceIndex = getRandomPiece();	// select a random chunk to download
				if (randomPieceIndex == -1) 
					continue;								// if the download is complete, stop
				
				try {
					Chunk Chunk = new Chunk(randomPieceIndex, sockNum);		// create a new chunk object to download a chunk
					Chunks.add(Chunk);										// add it to the chunk list
					Chunk.start();											// start the chunk downloading!
					System.out.println("Piece " + Chunk.id + " Started.");
				} catch (NullPointerException e) {
					System.out.println("NullPointerException on Chunk creation.");
					continue;
				}
			}
		}
		
		System.out.println("Complete.");
		this.close();
	}
	

	/*
	 * Chunk class
	 * 		Each chunk object is responsible for downloading one chunk
	 * 		of the file.
	 */
	private class Chunk extends Thread {
		private int id;					// id of the chunk to be downloaded
		private int sockNum;			// socket number to be used
		private InputStream inData;		// input stream for receiving chunks
		private BufferedReader inCtrl;	// buffered reader for receiving control signals
		private PrintWriter out;		// print writer for requesting chunks
		
		/*
		 * Chunk() Constuctor
		 * 	vars:
		 * 		id: identifies the chunk number that will be downloaded by this object
		 * 		sockNum: identifies the socket to be used in the download
		 * 	fctn:
		 * 		Sets the if of the chunk number to be downloaded, and the socket number
		 * 		to be used during the download. The in and out streams are then directed
		 * 		over the socket specified. The socket specified is expected to be
		 * 		bound to a port and connected to a InetSocketAddress before this
		 * 		is called.
		 */
		public Chunk(int id, int sockNum) {
			pieceStates[id] = NOT_STARTED;
			this.id = id;
			this.sockNum = sockNum;
			
			// direct input and outputs
			try {
				this.inData = cs[sockNum].getInputStream();
				this.inCtrl = new BufferedReader(new InputStreamReader(this.inData));
				this.out = new PrintWriter(cs[sockNum].getOutputStream(), true);
			} catch (IOException e) {
				System.out.println("IOException on in/out stream direction.");
				pieceStates[id] = FAILED;
			}
		}
		
		/*
		 * close()
		 * 	fctn:
		 * 		Closes the in and out streams as well as the socket specified by the socket
		 * 		number.
		 */
		public void close() {
			if (out != null) out.close();
			try {
				if (inData != null) inData.close();
			} catch (Exception e) {}
			try {
				if (inCtrl != null) inData.close();
			} catch (Exception e) {}
			try {
				if (cs[sockNum] != null) cs[sockNum].close();
			} catch (Exception e) {}
		}
		
		/*
		 * run()
		 * 	fctn:
		 * 		Attempts to download the chunk. If there was a previous problem setting
		 * 		the in out streams, the piece state is set to FAILED, and the download is
		 * 		not attempted. Otherwise
		 */
		public void run() {
			if (pieceStates[id] == FAILED) {
				this.close();
				return;
			}
			
			pieceStates[id] = IN_PROGRESS;
			
			String[] params = {FILE_NAME, String.valueOf(this.id) + 1};	// send request for chunks
			Message req = new Message("get", params);
			out.println(req);
			
			String resp = "default";
			try {
				resp = inCtrl.readLine();
			} catch (IOException e) {
				pieceStates[id] = FAILED;
				System.out.println("IOException on control readline.");
				this.close();
				return;
			}
			
			if (resp.equalsIgnoreCase("Error: 404")){	// server does not have file
				pieceStates[id] = FAILED;
				System.out.println("Server does not have Filename: " + FILE_NAME + " Piece: " + id);
			}
			else if (resp.equalsIgnoreCase("OK: 200")) {	// server has file
				try {
					byte[] data = new byte[CHUNK_SIZE];
		
					inData.read(data, 0, CHUNK_SIZE - 1);
					file.updateFile(FILE_NAME, NUM_CHUNKS, data, id + 1);
					pieceStates[id] = SUCCESS;
				} catch (IOException e) {
					pieceStates[id] = FAILED;
					System.out.println("IOException on chunk read/write.");
				}
			}
			
			notifyChunkState(this);
			this.close();
		}
	}
	
	private synchronized void print() {
		System.out.println("Total Pieces: " + pieceStates.length);
		for (int i = 0; i < pieceStates.length; i++) {
			System.out.println(" Piece " + i + " State: " + getStateName(pieceStates[i]));
		}
		System.out.println();
	}
	
	
	private String getStateName(char state) {
		if (state == NOT_STARTED) return "Not Started";
		if (state == IN_PROGRESS) return "In Progress";
		if (state == FAILED) return "Failed";
		if (state == SUCCESS) return "Complete";
		return "Unknown";
	}
	
	private synchronized void notifyChunkState(Chunk chunk) {
		if (getStateName(pieceStates[chunk.id]) == "SUCCESS")
			Chunks.remove(chunk);
		System.out.println("Piece " + chunk.id + " State: " + getStateName(pieceStates[chunk.id]));
		print();
		notifyAll();
	}
}