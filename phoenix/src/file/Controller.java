package file;
import java.lang.Byte;
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
	
	public void main(){
		//start Newscast threads
		Nodeid node = new Nodeid("128.100.8.221", 0);
		view = new PartialView(node);
		ss = new ServerSocket(10000);
		Newscast active = new Newscast(s, null);
		Newscast passive = new Newscast(null, ss);
		active.start();
		passive.start();
		
		//start server 
		ControllerServer server = new ControllerServer();
		server.start();
		//start a client to download
		ControllerClient client = new ControllerClient(ip, port, filesize, filename);
		client.start();
		
		
		
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
		
		public String[] getNodes(String filename){
			String[] Ips = null; 
			return Ips;
		}
		
	}
//	Nodeid holds the age and id of a Node, as well as the files it has 
	public class Nodeid{
		private String id;
		private int age;
		private ArrayList<FileSystem> files;
		
		public Nodeid(String id, int age){
			this.id = id;
			this.age = age;
			files = new ArrayList<File>();
		}
		
		public boolean equals(Nodeid node){
			if(this.id.equalsIgnoreCase(node.id))return true;
			return false;
		}
	}
}

