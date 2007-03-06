package file;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Controller extends Thread {
	private ServerSocket ss;
	private Socket cs;  
	public static final char NOT_STARTED = 0;
	public static final char IN_PROGRESS = 1;
	public static final char FAILED = 2;
	public static final char SUCCESS = 3;
	
	public static final int MAX_TASKS_ALIVE = 5;
	
	private boolean running;
	private volatile char[] pieceStates;
	private RandomAccessFile file;
	
	private ArrayList<Chunk> chunks;
	
	public static void main(String args[]) throws IOException {
		Controller controller = new Controller();
		controller.allocateFile("testfile1024.dat", 1024);
		/*
		String input;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Controller c = new Controller();
		c.start();
		
		while ((input = in.readLine()) != null) {
			if (c.running == false) break;
			
			if (input.equals("quit")) {
				c.running = false;
				break;
			}
			
			c.print();
		}
		*/
	}
	
	public Controller() {
		running = true;
		try {
			ss = new ServerSocket(10000);
			cs = new Socket();
		} catch (IOException e) {
			return;
		}
		chunks = new ArrayList<Chunk>();
		pieceStates = new char[20];
		for (int i = 0; i < pieceStates.length; i++) {
			pieceStates[i] = NOT_STARTED;
		}
	}
	
	public synchronized int getRandomPiece() {
		//Get available pieces. 
		ArrayList<Integer> availablePieces = new ArrayList<Integer>();
		for (int i = 0; i < pieceStates.length; i++) {
			if (pieceStates[i] == NOT_STARTED || pieceStates[i] == FAILED) availablePieces.add(new Integer(i));
		}
		if (availablePieces.size() == 0) return -1;
		int index = (int)Math.floor(Math.random()*availablePieces.size());
		return availablePieces.get(index).intValue();
	}
	
	public synchronized boolean isComplete() {
		for (int i = 0; i < pieceStates.length; i++) {
			if (pieceStates[i] != SUCCESS) return false;
		}
		return true;
	}
	
	public synchronized void run() {
		
		while (running) {
			try {
			Socket s = ss.accept();
			Server server = new Server(s);
	    	server.start();
	    	
			try {
				wait(100);
			} catch (InterruptedException e) {}
			} catch (IOException e) {}
			
			
			if (chunks.size() < MAX_TASKS_ALIVE) {
				if (isComplete()) {
					running = false;
					continue;
				}
				
				int randomPieceIndex = getRandomPiece();
				if (randomPieceIndex == -1) continue;
				Chunk chunk = new Chunk(randomPieceIndex);
				chunks.add(chunk);
				chunk.start();
				System.out.println("Piece " + chunk.id + " started: " + getStateName(pieceStates[chunk.id]));
				continue;
			}
		}
		System.out.println("Complete.");
	}
	
	public synchronized void print() {
		System.out.println("Total Pieces: " + pieceStates.length);
		for (int i = 0; i < pieceStates.length; i++) {
			System.out.println(" Piece " + i + " State: " + getStateName(pieceStates[i]));
		}
		System.out.println();
	}
	
	public String getStateName(char state) {
		if (state == NOT_STARTED) return "Not Started";
		if (state == IN_PROGRESS) return "In Progress";
		if (state == FAILED) return "Failed";
		if (state == SUCCESS) return "Complete";
		return "Unknown";
	}
	
	public synchronized void notifyChunkComplete(Chunk chunk) {
		chunks.remove(chunk);
		System.out.println("Piece " + chunk.id + " finished: " + getStateName(pieceStates[chunk.id]));
		print();
		notifyAll();
	}
	
	public void allocateFile(String file, long size) throws IOException {
		this.file = new RandomAccessFile(file, "rw");
		this.file.seek(size-1);
		this.file.write(0);
	}
	
	public void writeToFile(byte[] data, long pos) throws IOException {
		file.seek(pos);
		file.write(data);
	}
	
	public byte[] readFromFile(long pos, int size) throws IOException {
		byte[] result = new byte[size];
		file.seek(pos);
		file.read(result);
		//Remember to check size read and handle accordingly.
		return result;
	}
	
	private class Chunk extends Thread {
		private int id;
		private int sleepTime;
		
		public Chunk(int piece) {
			this.id = piece;
			pieceStates[piece] = NOT_STARTED;
			sleepTime = (int)(Math.random()*10+2);
		}
		
		public void run() {
			pieceStates[id] = IN_PROGRESS;
			while (running && sleepTime > 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				sleepTime--;
			}
			if (Math.random() < 0.2) pieceStates[id] = FAILED;
			else pieceStates[id] = SUCCESS;
			notifyChunkComplete(this);
		}
	}
	
	private class Server extends Thread{
		private Socket s;
		private BufferedReader in;
		private PrintWriter out;
		private boolean running;
		
		public Server(Socket s) {
			this.s = s;
			running = false;
		}
		
		public void close() {
			running = false;
			if (out != null) out.close();
			try {
				if (in != null) in.close();
			} catch (Exception e) {}
			try {
				if (s != null) s.close();
			} catch (Exception e) {}
		}
		
		public void run(){
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(s.getOutputStream(), true);
				running = true;
			} catch (IOException e) {
				close();
			}
			
			while (running) {
				String input = null;
				try {
					input = in.readLine();
				} catch (IOException e) {
					close();
					continue;
				}
				
				if (input == null) {
					running = false;
					continue;
				}

				if (input.equalsIgnoreCase("quit")) {
					out.println("Goodbye!");
					out.close();
					continue;
				}
				
					
		}
		close();
	}
}
}
