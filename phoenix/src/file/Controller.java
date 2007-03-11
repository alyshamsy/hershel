package file;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


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
		String tempip = "128.100.8.221 ";
		try {
			ip = InetAddress.getByName(tempip);
		} catch (UnknownHostException e) {}
		port = 650;
		filesize = 1024*1000;
		filename = "blah.txt";
	}
	
	public void main()throws IOException{
		//start Newscast threads
		Nodeid node = new Nodeid("128.100.8.145", 0);
		view = new PartialView(node);
		ss = new ServerSocket(10000);
		
		/*
		Newscast active = new Newscast(s, null);
		Newscast passive = new Newscast(null, ss);
		active.start();
		passive.start();*/
		
		//start server 
		ControllerServer server = new ControllerServer();
		server.start();
		//start a client to download
		//ControllerClient client = new ControllerClient(ip, port, filesize, filename);
		//client.start();
		
		
		
	}
	//classes for Newscast
	public class Newscast extends Thread{
		private Socket s;
		private ServerSocket ss;
		private BufferedReader in;
		private PrintWriter out;
		private OutputStream outData;
		
		public Newscast(Socket s , ServerSocket ss){
			this.s = s;
			this.ss = ss;
		}
		
		//handles the passive and active threads
		public void run(){
			//active thread
			if(ss==null){
				
			}
			//passive thread
			if(s==null){
				while(true){
					try {
						s = ss.accept();
					} catch (IOException e){}
					try {
						in = new BufferedReader(new InputStreamReader(s.getInputStream()));
					} catch (IOException e){}
					
				}
				
				
			}
		}
		
	}
//	partial view holds and array of 10 Nodeid objects
	public class PartialView{
		private ArrayList<Nodeid> nodes;
		
		public PartialView(Nodeid id){
			nodes.add(id);
		}
		
		//Merge takes two partial views and adds all objects in the parameters
		//Node list to the objects node list.
		//Then, the 10 freshest Nodeid's are saved and the rest discarded
		public void merge(PartialView view){
			boolean add = false;
			for(int j = 0;j<view.nodes.size();j++){
				for(int i = 0;i<this.nodes.size();i++){
					if(!view.nodes.get(j).equals(this.nodes.get(i))){
						add = true;
					}
				}
				if(add) this.nodes.add(view.nodes.get(j));
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
		
		public Nodeid getRandomNode(){
			int index = (int)Math.floor(Math.random()*this.nodes.size());
			return this.nodes.get(index);
		}
		
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
		
	}
//	Nodeid holds the age and id of a Node, as well as the files it has 
	public class Nodeid{
		private String id;
		private int age;
		private FileSystem files;
		
		public Nodeid(String id, int age){
			this.id = id;
			this.age = age;
			files.fileInfo();
		}
		
		public boolean equals(Nodeid node){
			if(this.id.equalsIgnoreCase(node.id))return true;
			return false;
		}
		
		public boolean equals(String id){
			if(this.id.equalsIgnoreCase(id))return true;
			return false;
		}
	}
	
	public class ControllerServer extends Thread {
		//sockets for recieving files and sending files
		private ServerSocket ss; 
		public Socket s;
		public static final int MAX_TASKS_ALIVE = 5;
		private boolean running;
		
		
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
			
			public void run(){
				System.out.println("Connection Established");
				RandomAccessFile file;
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
							file = new RandomAccessFile(tempfile, "r");
							
							view.getNode("128.100.8.145").chunkSegment(file, FileSystem.getFileSize(myMessage.getData(0)));
							
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
}

