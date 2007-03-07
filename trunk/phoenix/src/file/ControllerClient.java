//req: filename, chunk#s all on new out.println

package file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;

public class ControllerClient extends Thread {
	private Socket cs;
	
	public static final char NOT_STARTED = 0;
	public static final char IN_PROGRESS = 1;
	public static final char FAILED = 2;
	public static final char SUCCESS = 3;
	
	public static final int MAX_CHUNKS_ALIVE = 5;
	
	private boolean running;
	private volatile char[] pieceStates;
	
	private ArrayList<Chunk> Chunks;
	
	public static void main(String args[]) throws IOException {
		String input;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		ControllerClient c = new ControllerClient();
		c.start();
		
		while ((input = in.readLine()) != null) {
			if (c.running == false) break;
			
			if (input.equals("quit")) {
				c.running = false;
				break;
			}
			
			c.print();
		}
	}
	
	public ControllerClient(SocketAddress IP, SocketAddress port, double fileSize, String fileName) {
		running = true;
		Chunks = new ArrayList<Chunk>();
		
		int nChunks = (int)Math.ceil(fileSize/512000);	//number of chunks rounded up to include part chunks
		pieceStates = new char[nChunks];
		
		for (int i = 0; i < pieceStates.length; i++) {
			pieceStates[i] = NOT_STARTED;
		}
		
		//initialize the client socket
		this.cs = new Socket();
		try {
			this.cs.bind(port);
			this.cs.connect(IP);
		} catch (IOException e) {}
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
				out = new PrintWriter(s.getOutputStream(), true);
				wait(100);
			} catch (InterruptedException e) {}
			
			if (Chunks.size() < MAX_CHUNKS_ALIVE) {
				if (isComplete()) {
					running = false;
					continue;
				}
				
				int randomPieceIndex = getRandomPiece();
				if (randomPieceIndex == -1) continue;
				Chunk Chunk = new Chunk(randomPieceIndex);
				Chunks.add(Chunk);
				Chunk.start();
				System.out.println("Piece " + Chunk.id + " started: " + getStateName(pieceStates[Chunk.id]));
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
		Chunks.remove(chunk);
		System.out.println("Piece " + chunk.id + " finished: " + getStateName(pieceStates[chunk.id]));
		print();
		notifyAll();
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
}
