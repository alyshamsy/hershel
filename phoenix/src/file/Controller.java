package file;
import java.io.*;
import java.util.ArrayList;

public class Controller extends Thread {
	public static final char NOT_STARTED = 0;
	public static final char IN_PROGRESS = 1;
	public static final char FAILED = 2;
	public static final char SUCCESS = 3;
	
	public static final int MAX_TASKS_ALIVE = 5;
	
	private boolean running;
	private volatile char[] pieceStates;
	private RandomAccessFile file;
	
	private ArrayList<Task> tasks;
	
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
		tasks = new ArrayList<Task>();
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
				wait(100);
			} catch (InterruptedException e) {}
			
			if (tasks.size() < MAX_TASKS_ALIVE) {
				if (isComplete()) {
					running = false;
					continue;
				}
				
				int randomPieceIndex = getRandomPiece();
				if (randomPieceIndex == -1) continue;
				Task task = new Task(randomPieceIndex);
				tasks.add(task);
				task.start();
				System.out.println("Piece " + task.id + " started: " + getStateName(pieceStates[task.id]));
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
	
	public synchronized void notifyTaskComplete(Task task) {
		tasks.remove(task);
		System.out.println("Piece " + task.id + " finished: " + getStateName(pieceStates[task.id]));
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
	
	private class Task extends Thread {
		private int id;
		private int sleepTime;
		
		public Task(int piece) {
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
			notifyTaskComplete(this);
		}
	}
}
