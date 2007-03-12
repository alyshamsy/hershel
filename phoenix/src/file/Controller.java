package file;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

//main class that controls ControllerClient and Controller Server
public class Controller{
	public PartialView view;
	public Nodeid node;
	public Socket s;
	public ServerSocket ss;
	private InetAddress ip;
	private int port;
	private double filesize;
	private String filename;
	
	

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
		port = 650;
		filesize = 1024*1000;
		filename = "blah.txt";
	}
	
	public void go(){
		ControllerServer server = new ControllerServer();
		server.start();
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
	//classes for Newscast
	public class Newscast extends Thread{
		private Socket s;
		private ServerSocket ss;
		private BufferedReader inA;
		private BufferedReader inP;
		private PrintWriter outP;
		private OutputStream outData;
		private PrintWriter outA;
		
		public Newscast(Socket s , ServerSocket ss){
			this.s = s;
			this.ss = ss;
		}
		
		//handles the passive and active threads
		public void run(){
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
	public class PartialView{
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
	public class Nodeid{
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
	
	//server class which deals with requests for files and dispatches them 
	public class ControllerServer extends Thread {
		//sockets for recieving files and sending files
		private ServerSocket ss; 
		public Socket s;
		public static final int MAX_TASKS_ALIVE = 5;
		private boolean running;
		
		
		public ControllerServer() {
			running = true;
			try {
				ss = new ServerSocket(10027);
			} catch (IOException e) {
				System.out.println("Error: Can't bind socket to port");
			}
		}
		
		public void close() throws IOException{
			running = false;
			ss.close();
			s.close();
			
		}
		
		//Run
		//No parameters
		//Runs forever accepting connections and dispatching
		//instances of the server class to handle file requests
		public synchronized void run() {
			
			while (running) {
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
			private String path = "/u/0T8/brownjo/ece361/fileoverlay/Phoenix/Shared/";
			private Socket s;
			private BufferedReader in;
			private PrintWriter out;
			private OutputStream outData;
			private boolean running;
			private boolean more;
			private File tempfile;
			private byte[] data;
			
			//Constructor
			//parameters: a socket that is the redirection of an accepted connection
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
					if(myMessage.getCmd().equalsIgnoreCase("get"))
							{
								
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
		                        //break requested file into chunks
			
								try {
									file = new RandomAccessFile(tempfile, "rw");
									view.getNode(FileSystem.getIPaddr()).chunkSegment(file, FileSystem.getFileSize(filename), filename);
							    } catch (FileNotFoundException e2) {
									//this should not happen as we already checked if the file existed
								}
							    catch(IOException e){
							    	System.out.println("Error: IOException on chunk segment");
							    }
							    //assign data to correct chunk
								
							    try {
									data = view.getNode(FileSystem.getIPaddr()).download_chunk(filename, chunkNum);
								} catch (IOException e1) {
									System.out.println("Error: IOException on download_chunk");
								}
									
								
								if(data == null){
									System.out.println("Do not have requested chunk");
									running = false;
									continue;
								}
								System.out.println(data.toString());
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
}

