package file;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class ControllerServer extends Thread {
	//sockets for recieving files and sending files
	private ServerSocket ss; 
	public static final int MAX_TASKS_ALIVE = 5;
	private boolean running;
	
	public static void main(String args[]) throws IOException {
		String temp = null;
		int num = Integer.parseInt(temp);
		System.out.println("the number is: "+ num);
		
		/*
		Controller controller = new Controller();
		controller.allocateFile("testfile1024.dat", 1024);
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
	
	public ControllerServer() {
		running = true;
		try {
			ss = new ServerSocket(10000);
		} catch (IOException e) {
			return;
		}
	}
	
	public void close() throws IOException{
		running = false;
		ss.close();
		
	}
	
	public synchronized void run() {
		
		while (running) {
			try {
			//start the server class to handle incoming connection
			Socket s = ss.accept();
			Server server = new Server(s);
	    	server.start();
			
			try {
				wait(100);
			} catch (InterruptedException e) {}
			} catch (IOException e) {}
		}
			
	}
	

	//Server class which will parse incoming request and dispatch
	//available chunks
	private class Server extends Thread{
		private Socket s;
		private BufferedReader in;
		private PrintWriter out;
		private OutputStream outData;
		private boolean running;
		private boolean more;
		private File tempfile;
		private RandomAccessFile file;
		
		public Server(Socket s) {
			this.s = s;
			this.running = false;
			more = true;
		}
		
		public void close() {
			this.running = false;
			if (out != null) out.close();
			try {
				if (in != null) in.close();
			} catch (Exception e) {}
			try {
				if (s != null) s.close();
			} catch (Exception e) {}
			try{
				if(outData != null) outData.close();
				file.close();
			}catch (IOException e) {}
			
			
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
		
		public void run(){
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(s.getOutputStream(), true);
				running = true;
			} catch (IOException e) {
				close();
			}
			
			while (running) {
				int chunkNum;
				String input = null;
				byte [] fileHash = null;
				try {
					//receive the command
					input = in.readLine();
				} catch (IOException e) {
					running = false;
					continue;
				}
				
				if (input == null) {
					running = false;
					continue;
				}
				
				Message myMessage = Message.parse(input);
				if(myMessage.getCmd().equalsIgnoreCase("get")){
					//check if the file exists and if not dispatch error code
					tempfile = new File(myMessage.getData(1));
					if(!tempfile.exists()){
						out.println("Error: 404");
						running = false;
						continue;
					}
					//file exists, respond 
					out.println("OK: 200");
					chunkNum = myMessage.countDataTokens()-1;
					//send out chunks that were requested
					for(int i = 0; i<chunkNum; i++){
						//assign data to the chunk(ALY CODING)   
						byte [] data = null;
						try {
							outData = s.getOutputStream();
						} catch (IOException e1) {
							
						}
						//send the data
						if (running) {
							try {
								outData.write(data);
							} catch (IOException e) {
							//should we retry?
							}	
						}
					}
						
				}
			}
			
				
				/*
				fileHash = SHA1Utils.getSHA1Digest(tempfile);
				
				 // Open the file 
                FileInputStream fstream = new FileInputStream(tempfile.toString());

                // Convert our input stream to a
                // DataInputStream
                DataInputStream in = new DataInputStream(fstream);

                // Continue to read lines while 
                // there are still some left to read
                while (in.available() !=0)
                {
                    in.r
                }
                */
		this.close();
	}
}
}
