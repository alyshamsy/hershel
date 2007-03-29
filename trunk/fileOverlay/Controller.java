
import java.io.*;
import java.net.*;
import java.util.ArrayList;

//main class that controls ControllerClient and Controller Server
public class Controller{
	public volatile PartialView view;
	public Nodeid node;
	public Socket s;
	public ServerSocket ss;
	private static final int CHUNK_SIZE = 10;
	//for testing!!!!
	public static final int serverPort = 10010;
	private static final int JORDAN_PORT = 10010;//this should be the same as server port(leave for now)
	private static int[] RYAN_PORTS = {10107, 10108, 10109};
	private static final String[] JORDAN_IP = {"128.100.8.159", "128.100.8.159", "128.100.8.159"};
	
	

	public Controller(){
		String tempip;
		try {
			tempip = FileSystem.getIPaddr();
		} catch (IOException e1) {
		
		}
		try {
			Nodeid node = new Nodeid(FileSystem.getIPaddr(), 0);
			view = new PartialView(node);
			//must listen on same port as connecting to
			//ss = new ServerSocket(10002);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		s = new Socket();
		//try {
			//ip = InetAddress.getByName(tempip);
		//} catch (UnknownHostException e) {}
	}
	
	public void go(){
		ControllerServer server = new ControllerServer();
		server.start();
		
		//Only need the next portion for downloads
		//ControllerClient client = new ControllerClient(RYAN_PORTS, 62, "test.txt");
		//client.start();
		
		
		
		//start Newscast threads
		
		
		/*
		Newscast active = new Newscast(s, null);
		Newscast passive = new Newscast(null, ss);
		active.start();
		passive.start();*/
//		start a client to download
		//ControllerClient client = new ControllerClient(ip, port, filesize, filename);
		//client.start();
	}
	
	public static void main(String args[])throws IOException{
		Controller master = new Controller();
		master.go();
		
	}
	
	/*
	//classes for Newscast
	public class Newscast extends Thread
	{
		private Socket s;
		private ServerSocket ss;
		private BufferedReader inA;
		private BufferedReader inP;
		private PrintWriter outP;
		private OutputStream outData;
		private PrintWriter outA;
		
		public Newscast(Socket s , ServerSocket ss)
		{
			this.s = s;
			this.ss = ss;
		}
		
		//handles the passive and active threads
		public void run()
		{
			//active thread
			if(ss==null){
				while(true){
					String input = null;
					String ipstr = view.getRandomNode();
					InetSocketAddress ipAddr;
					InetSocketAddress portAddr; 
					InetAddress ip;
					
					//address using
					portAddr = new InetSocketAddress(10001);
					try {
						s.bind(portAddr);
					} catch (IOException e1) {
						System.out.println("Error: Could not bind socket to port(NEWSCAST A)");
					}
					try {
						ip = InetAddress.getByName(ipstr);
						//address connecting to
						ipAddr = new InetSocketAddress(ip, 10002);
						s.connect(ipAddr);
					} catch (UnknownHostException e1) {
						System.out.println("Error: Could not find host(NEWSCAST A)");
					} catch (IOException e1) {
						System.out.println("Error: Could not connect to host(NEWSCAST A)");
					}	
					
					try {
						inA = new BufferedReader(new InputStreamReader(s.getInputStream()));
						outA = new PrintWriter(s.getOutputStream(), true);
					} catch (IOException e){System.out.println("Error: Could not get input stream (NEWSCAST A)");}
					
					try {
						wait(10000);
					} catch (InterruptedException e) {
						System.out.println("Error: Could not wait(NEWSCAST A)");
					}
					Message outView = new Message("gossip", view.toStringArray()); 
					outA.println(outView);
					try {
						input = inA.readLine();
					} catch (IOException e) {
						System.out.println("Error: Could not read a line (NEWSCAST A)");}
						Message inView = Message.parse(input);
					if(inView.cmdEquals("gossip")){
						
					}
				}
			}
			//passive thread
			if(s==null){
				String input = null;
				while(true){
					try {
						s = ss.accept();
					} catch (IOException e){}
					try {
						inP = new BufferedReader(new InputStreamReader(s.getInputStream()));
						outP = new PrintWriter(s.getOutputStream(), true);
					} catch (IOException e){System.out.println("Error: Could not get input stream (NEWSCAST P)");}
					try {
						input = inP.readLine();
					} catch (IOException e) {
						System.out.println("Error: Could not read a line (NEWSCAST P)");}
					
					Message inView = Message.parse(input);
					Message outView = new Message("gossip", view.toStringArray());
					outP.println(outView);
					if(inView.cmdEquals("gossip")){
						
					}
				}	
				
			}
		}
		
	}
//	partial view holds and array of 10 Nodeid objects
	public class PartialView
	{
		private ArrayList<Nodeid> nodes;
		
		public PartialView(Nodeid id){
			//try{
				nodes = new ArrayList<Nodeid>();
				nodes.add(id);
			//}
			//catch(Exception e){System.out.println("SHIT");}
		}
		
		//Merge takes two partial views and adds all objects in the parameters
		//Node list to the objects node list.
		//Then, the 10 freshest Nodeid's are saved and the rest discarded
		public void merge(String[] view){
			boolean add = false;
			Nodeid newN = null;
			for(int j = 0;j<view.length;j++){
				for(int i = 0;i<this.nodes.size();i++){
					if(!this.nodes.get(i).equals(view[j])){
						add = true;
						//change this : newN = new Nodeid(view[j], 0);
					}
					else{
						add = false;
					}
					if(add)this.nodes.add(newN);
				}
			}
			int tempage = 0;
			Nodeid tempnode =  null;
			
			while(this.nodes.size()>10){
				for(int i=0; i< this.nodes.size(); i++){
					if(this.nodes.get(i).age> tempage ){
						tempage = this.nodes.get(i).age;
						tempnode = this.nodes.get(i);
					}
				}
				if(tempnode!=null) this.nodes.remove(tempnode);
			}
		}
		
		public String getRandomNode(){
			int index = (int)Math.floor(Math.random()*this.nodes.size());
			return this.nodes.get(index).id;
		}
		
		//return a node based on it's IP address
		public FileSystem getNode(String Address){
			for(int i=0;i<nodes.size();i++){
				if(nodes.get(i).equals(Address)) return nodes.get(i).files;
			}
			return null;
		}
		
		public String[] getNodes(String filename){
			String[] Ips = null; 
			return Ips;
		}
		
		public String[] toStringArray(){
			String nodeFiles[] = new String[nodes.size()];
			for(int i =0;i<nodes.size();i++){
				nodeFiles[i] = nodes.get(i).id + nodes.get(i).age + " " + nodes.get(i).toString();
			}
			return nodeFiles;
		}
		
	}
	
//	Nodeid holds the age and id of a Node, as well as the files it has 
	public class Nodeid
	{
		private String id;
		private int age;
		private FileSystem files;
		
		public Nodeid(String id, int age){
			this.id = id;
			this.age = age;
			files = new FileSystem();
			files.fileInfo();
			
		}
		
		//equality between 2 nodes
		public boolean equals(Nodeid node){
			if(this.id.equalsIgnoreCase(node.id))return true;
			return false;
		}
		
		//equality between a node and an IP address
		public boolean equals(String id){
			if(this.id.equalsIgnoreCase(id))return true;
			return false;
		}
		
		public String toString(){
			String fileList = null;
			fileList = files.toString();
			return fileList;
		}
	}
	*/
	
	//server class which deals with requests for files and dispatches them 
	public class ControllerServer extends Thread {
		//sockets for recieving files and sending files
		private ServerSocket ss; 
		public Socket s;
		private boolean go;
		
		
		public ControllerServer() {
			go = true;
			try {
				ss = new ServerSocket(serverPort);
			} catch (IOException e) {
				System.out.println("Error: Can't bind socket to port");
			}
		}
		
		public void close() throws IOException{
			go = false;
			ss.close();
			s.close();
			
		}
		
		//Run
		//No parameters
		//Runs forever accepting connections and dispatching
		//instances of the server class to handle file requests
		public synchronized void run() {
			
			while (go) {
				try {
				//start the server class to handle incoming connection
				System.out.println("Waiting for connections");
				s = ss.accept();
				Server server = new Server(s);
		    	server.start();
				
				} catch (IOException e) {System.out.println("Error: Could not accept connection");}
			}
				
		}
		

		//Server class which will parse incoming request and dispatch
		//available chunks
		private class Server extends Thread{
			private String filename;
			private int chunkNum;
			private String path = FileSystem.absPath() + "/";//"/u/0T8/brownjo/ece361/fileoverlay/Phoenix/Shared/";
			private Socket s;
			private BufferedReader in;
			private PrintWriter out;
			private OutputStream outData;
			private boolean running;
			private File tempfile;
			private byte[] data;
			
			//Constructor
			//parameters: a socket that is the redirection of an accepted connection
			public Server(Socket s) {
				this.s = s;
				this.running = false;
			}
			
			public void close() {
				if (out != null) out.close();
				try {
					if (in != null) in.close();
				} catch (Exception e) {}
				try {
					if (s != null) s.close();
				} catch (Exception e) {}
				try{
					if(outData != null) outData.close();
				}catch (IOException e) {}
				
				
			}
			//Run
			//No parameters
			//Communicates with client and dispatches a requested chunk
			public void run(){
				System.out.println("Connection Established");
				RandomAccessFile file;
				try {
					in = new BufferedReader(new InputStreamReader(s.getInputStream()));
					out = new PrintWriter(s.getOutputStream(), true);
					outData = s.getOutputStream();
					running = true;
				} catch (IOException e) {
					System.out.println("IOException: allocating Buffers");
					close();
				}
				
				while (running) {
					String input = null;
					try {
						//receive the command
						input = in.readLine();
					} catch (IOException e) {
						running = false;
						System.out.println("Error: IOException when reading first command");
						continue;
					}
					
					if (input == null) {
						running = false;
						continue;
					}
					
					Message myMessage = Message.parse(input);
					if(myMessage.getCmd().equalsIgnoreCase("get")){
								
								//check if the file exists and if not dispatch error code
								//the first token is the filename, and the second
								//is the chunk number requested
								filename = myMessage.getData(0);
								chunkNum = Integer.valueOf(myMessage.getData(1));
								System.out.println("File requested: "+ filename);
								tempfile = new File(path + filename);
								if(!tempfile.exists()){
									out.println("Error: 404");
									System.out.println("Error: 404");
									running = false;
									continue;
								}
								//file exists, respond 
								out.println("OK: 200");
								System.out.println("OK: 200");
								
								//SEND OUT REQUESTED CHUNK
								//***********************************
								System.out.println("Chunk requested: " + chunkNum);
		   
							    //assign data to correct chunk
								
							    try {
									data = view.getNode(FileSystem.getIPaddr()).download_chunk(filename, chunkNum);
								} catch (IOException e1) {
									System.out.println("Error: IOException on download_chunk");
									running = false;
							    	continue;
								}
									
								
								if(data == null){
									System.out.println("Do not have requested chunk");
									running = false;
									continue;
								}
							    //send the data
								if (running) {
									try {
										System.out.println("Sending data of length " + data.length);
										//System.out.write(data);
										outData.write(data);
									} catch (IOException e) {
									//should we retry?
										System.out.println("Error: could not send data");
										running = false;
								    	continue;
									}	
								}
								//}
									
							}
					running = false;
					continue;
				}
				
			this.close();
		}
	}
	}

	public class ControllerClient extends Thread {
		private boolean running;
		
		// FILE PARAMETERS
		private String FILE_NAME;
		private int FILE_SIZE;
		
		// CHUNK PARAMETERS
		private static final int MAX_CHUNKS_ALIVE = 3;	// max number of chunks to download concurrently
		private int NUM_CHUNKS;	
		private volatile ArrayList<Chunk> Chunks;
		private volatile char[] pieceStates;			// holds the state of each chunk
		
		// CHUNK STATES
		private static final char NOT_STARTED = 0;
		private static final char IN_PROGRESS = 1;
		private static final char FAILED = 2;
		private static final char SUCCESS = 3;
		
		// SOCKET AND PORT PARAMETERS
		private int port[];									// array of port numbers
		private InetSocketAddress portAddr[];				// array of port numbers as addresses
		private volatile Socket[] cs;						// array of sockets
		private static final int CONNECT_TIME_OUT = 10000;	// number of ms before socket throws exception on connect
		private static final int INPUT_TIME_OUT = 1000;		// number of ms before socket throws exception on input stream read
		
		/*public static void main(String args[]) throws IOException {
			ControllerClient a = new ControllerClient(RYAN_PORTS, 2294, "test.txt");
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

			// set number of chunks needed
			this.NUM_CHUNKS = (int)Math.ceil((double)fileSize/(double)CHUNK_SIZE);

			if (this.NUM_CHUNKS == 0)
				this.NUM_CHUNKS = 1;	// number of chunks can be 1 at minimum
			pieceStates = new char[this.NUM_CHUNKS];
			
			for (int i = 0; i < pieceStates.length; i++) {
				pieceStates[i] = NOT_STARTED;
			}
			
			// initialize file
			this.FILE_NAME = fileName;
			this.FILE_SIZE = (int)fileSize;
			
			// SOCKETS AND PORTS
			this.port = new int[MAX_CHUNKS_ALIVE];					
			this.portAddr = new InetSocketAddress[MAX_CHUNKS_ALIVE];
			this.cs = new Socket[MAX_CHUNKS_ALIVE];
			for (int i = 0; i < MAX_CHUNKS_ALIVE; i++){
				
				this.port[i] = port[i];
				this.portAddr[i] = new InetSocketAddress(port[i]);
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
			
			if (availablePieces.size() == 0)
				return -1;
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
		
		public synchronized void run() {
			System.out.println("Client Running");
			
			while (running) {
				try {
					wait(100); //ASK SHAD
				} catch (InterruptedException e) {
					System.out.println("InterruptedException on wait(100).");
				}
				
				if (Chunks.size() < MAX_CHUNKS_ALIVE && Chunks.size() < NUM_CHUNKS) {
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
							if (cs[i] == null || !cs[i].isConnected() || !cs[i].isBound()){
								sockNum = i;
								break;
							}
						} catch (NullPointerException e) {
							System.out.println("NullPointerException on socket selection.");
						}
					}
					
					// creating and binding the socket
					System.out.println("Socket " + sockNum + " binding to port " + sockNum + ": " + port[sockNum]);
					try {
						cs[sockNum] = new Socket();								// create a new socket
						cs[sockNum].setSoTimeout(INPUT_TIME_OUT);
						cs[sockNum].bind(portAddr[sockNum]);					// bind it to the free port
					} catch (IOException e1) {
						System.out.println("IOException on socket bind.");
						System.out.println("\tSocket could not bind to port " + sockNum + ": " + port[sockNum]);
						try {
							cs[sockNum].close();
						} catch (IOException e) {}
						continue;
					}
					System.out.println("Socket " + sockNum + " bound to port " + sockNum + ": " + port[sockNum]);
					
					// connecting the socket
					System.out.println("Socket " + sockNum + " connecting to IP: " + JORDAN_IP[sockNum]);
					try {
						InetAddress ipAddr = InetAddress.getByName(JORDAN_IP[sockNum]);			// construct the IP InetAddress
						InetSocketAddress addr = new InetSocketAddress(ipAddr, JORDAN_PORT);	// construct the full InetSocketAddress
						cs[sockNum].connect(addr, CONNECT_TIME_OUT);							// connect the socket
					}
					catch (UnknownHostException e) {
						System.out.println("UnknownHostException on socket connect.");
						System.out.println("\tSocket could not connect to IP: " + JORDAN_IP[sockNum]);
						try {
							cs[sockNum].close();
						} catch (IOException e1) {}
						continue;
					} catch (IOException e) {
						System.out.println("IOException on socket connect.");
						System.out.println("\tSocket could not connect to IP: " + JORDAN_IP[sockNum]);
						try {
							cs[sockNum].close();
						} catch (IOException e1) {}
						continue;
					} catch (NullPointerException e) {
						System.out.println("NullPointerException on socket connect.");
						System.out.println("\tSocket could not connect to IP: " + JORDAN_IP[sockNum]);
						try {
							cs[sockNum].close();
						} catch (IOException e1) {}
						continue;
					}
					System.out.println("Socket " + sockNum + " connected to IP: " + JORDAN_IP[sockNum]);
					
					// CHUNK DOWNLOAD SETUP
					// select the chunk
					int randomPieceIndex = getRandomPiece();	// select a random chunk to download
					if (randomPieceIndex == -1) 
						continue;								// if the download is complete, stop
					
					// start the chunk downloading
					try {
						Chunk Chunk = new Chunk(randomPieceIndex, sockNum);		// create a new chunk object to download a chunk
						Chunks.add(Chunk);										// add it to the chunk list
						Chunk.start();											// start the chunk downloading!
						System.out.println("Piece " + Chunk.id + " Started.");
					} catch (NullPointerException e) {
						System.out.println("NullPointerException on Chunk creation.");
						try {
							cs[sockNum].close();
						} catch (IOException e1) {}
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
			 * 
			 * 		If there is an error during the in out stream direction process, the chunk
			 * 		state is set to FAILED.
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
				} 
				// error in direction of in out streams
				catch (IOException e) {
					System.out.println("IOException on in/out stream direction.");
					pieceStates[id] = FAILED;
				}
			}
			
			/*
			 * close()
			 * 	fctn:
			 * 		Closes the in and out streams as well as the socket specified by the socket
			 * 		number. Also notifies the state of the chunk.
			 */
			public void close() {
				if (this.out != null) this.out.close();
				try {
					if (this.inCtrl != null) this.inCtrl.close();
				} catch (Exception e) {}
				try {
					if (this.inData != null) this.inData.close();
				} catch (Exception e) {}
				try {
					if (cs[sockNum] != null) cs[sockNum].close();
				} catch (Exception e) {}
				
				System.out.println("Notifyin");
				notifyChunkState(this);
				System.out.println("Notified");
			}
			
			/*
			 * run()
			 * 	fctn:
			 * 		Attempts to download the chunk. If there was a previous problem setting
			 * 		the in out streams, or the client was set to shut down, the piece state
			 * 		is set to failed and the chunk download is not attempted.
			 */
			public void run() {
				
				// the chunk failed during in out stream direction
				// or the client was set to shut down
				if (pieceStates[id] == FAILED || running == false) {
					this.close();
					return;
				}
				
				// chunk in out stream setup was OK
				// the request for download begins
				pieceStates[id] = IN_PROGRESS;
				
				// send request
				String[] params = {FILE_NAME, String.valueOf(this.id + 1)};	// send request for the chunk
				Message req = new Message("get", params);
				out.println(req);
				
				// receive response
				String resp = "default";
				try {
					resp = inCtrl.readLine();
				} 
				// error on reading response from the server, timeout
				catch (IOException e) {	
					pieceStates[id] = FAILED;
					System.out.println("IOException on control readline.");
					this.close();
					return;
				}
				
				// server does not have the file
				if (resp.equalsIgnoreCase("Error: 404")){
					pieceStates[id] = FAILED;
					System.out.println("Server does not have Filename: " + FILE_NAME + " Piece: " + id);
				}
				// server has the file
				else if (resp.equalsIgnoreCase("OK: 200")) {
					try {
						byte[] data;
						
						int remainder = FILE_SIZE - id * CHUNK_SIZE;
		            	if (remainder < CHUNK_SIZE){
							data = new byte[remainder];						// set up byte array to receive hold chunk
							inData.read(data, 0, remainder);
		            	}
						else{
							data = new byte[CHUNK_SIZE];
							inData.read(data, 0, CHUNK_SIZE);
						}
		            
		            	view.getNode(FileSystem.getIPaddr()).updateFile(FILE_NAME, NUM_CHUNKS + 1, data, id + 1, FILE_SIZE);	// update file with the new chunk
						pieceStates[id] = SUCCESS;
					} 
					// error in receiving chunk, or writing the chunk to file
					catch (IOException e) {
						pieceStates[id] = FAILED;
						System.out.println("IOException on chunk read/write.");
					}
				}
				this.close();
			}
		}
		
		/*
		 * print()
		 * 	fctn:
		 * 		Outputs to the console the total number of chunks in the file,
		 * 		the number of chunks left to be downloaded, and the state of all
		 * 		chunks in the respective file.
		 */
		private synchronized void print() {
			System.out.println("\tTotal Pieces: " + pieceStates.length + " / Remaining Pieces: " + numRemainingChunks());
			for (int i = 0; i < pieceStates.length; i++) {
				System.out.println("\tPiece " + i + " State: " + getStateName(pieceStates[i]));
			}
			System.out.println();
		}
		
		/*
		 * getStateName()
		 * 	fctn:
		 * 		Returns an appropriate string of the state sent in.
		 */
		private synchronized String getStateName(char state) {
			if (state == NOT_STARTED) return "Not Started";
			if (state == IN_PROGRESS) return "In Progress";
			if (state == FAILED) return "Failed";
			if (state == SUCCESS) return "Complete";
			return "Unknown";
		}
		
		/*
		 * numRemainingPieces()
		 * 	fctn:
		 * 		Returns the number of remaining chunks to be downloaded. That is,
		 * 		the number of chunks whose state is not SUCCESS.
		 */
		private synchronized int numRemainingChunks() {
			int remaining = 0;
			for (int i = 0; i < pieceStates.length; i++) {
				if (pieceStates[i] != SUCCESS)
					remaining ++;
			}
			
			return remaining;
		}
		
		/*
		 * notifyChunkState()
		 * 	vars:
		 * 		chunk: The chunk whose state will be notified.
		 * 	fctn:
		 * 		Notifies the state of the chunk passed in, as well as printing its
		 * 		state to the console, and then the state of all chunks. If the chunk's
		 * 		state is SUCCESS, it is removed from the chunk list.
		 */
		private synchronized void notifyChunkState(Chunk chunk) {
			Chunks.remove(chunk);
			System.out.println("Piece " + chunk.id + " State: " + getStateName(pieceStates[chunk.id]));
			print();
			notifyAll();
		}
	}

}
