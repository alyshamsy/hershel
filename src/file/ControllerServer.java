package file;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;


public class ControllerServer extends Thread {
	//sockets for recieving files and sending files
	private ServerSocket ss; 
	public Socket s;
	public static final int MAX_TASKS_ALIVE = 5;
	private boolean running;
	
	public static void main(String args[]) throws IOException {		
		ControllerServer server = new ControllerServer();
		server.start();
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
			ss = new ServerSocket(10011);
		} catch (IOException e) {
			System.out.println("Error: Can't bind socket to port");
		}
	}
	
	public void close() throws IOException{
		running = false;
		ss.close();
		s.close();
		
	}
	
	public synchronized void run() {
		
		while (running) {
			try {
			//start the server class to handle incoming connection
			System.out.println("Waiting for connections");
			s = ss.accept();
			Server server = new Server(s);
	    	server.start();
			
			try {
				wait(100);
			} catch (Exception e) {}
			} catch (IOException e) {}
		}
			
	}
	

	//Server class which will parse incoming request and dispatch
	//available chunks
	private class Server extends Thread{
		private String path = "/u/0T8/brownjo/ece361/fileoverlay/Phoenix/src/file/";
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
			System.out.println("Connection Established");
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(s.getOutputStream(), true);
				running = true;
			} catch (IOException e) {
				System.out.println("IOException: allocating Buffers");
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
					System.out.println("File requested "+ myMessage.getData(0));
					tempfile = new File(path + myMessage.getData(0));
					if(!tempfile.exists()){
						out.println("Error: 404");
						System.out.println("Error: 404");
						running = false;
						continue;
					}
					//file exists, respond 
					out.println("OK: 200");
					System.out.println("OK: 200");
					chunkNum = myMessage.countDataTokens()-1;
					//send out chunks that were requested
					System.out.println("Number of Chunks: " + chunkNum);
					//for(int i = 0; i<chunkNum; i++){
						//assign data to the chunk(ALY CODING) 
						FileInputStream fstream;
						byte [] data = new byte[15];
						
							try {
								outData = s.getOutputStream();
								fstream = new FileInputStream(tempfile.toString());
								DataInputStream inData = new DataInputStream(fstream);
								System.out.println("Read in " + inData.read(data, Integer.getInteger(myMessage.getData(1))*12, 12) + " bytes");
							} catch (IOException e1) {
								System.out.println("IOException: getting Socket output stream");
							}
						 
						
							
						//send the data
						if (running) {
							try {
								System.out.println("Sending data");
								outData.write(data);
							} catch (IOException e) {
							//should we retry?
								System.out.println("Error: could not send data");
							}	
						}
					//}
						
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
		//this.close();
	}
}
}
