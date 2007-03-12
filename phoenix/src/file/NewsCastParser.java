package file;


import java.util.ArrayList;
import java.io.*;

import file.Message;import file.FileSystem;



public class NewsCastParser{
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
		//Nodeid holds the age and id of a Node, as well as the files it has 
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
		
		
		public Message[] Parser(PartialView temp) {
			int numNodes = temp.nodes.size();
			Message []decode = null;
			String tmp = "";
			for (int i = 0; i < numNodes; i++){
				
				String nodeID = temp.nodes.get(i).id;
				int nodeAge = temp.nodes.get(i).age;
				String[] nodeFiles = temp.nodes.get(i).files.fileContents();
				int nodeFilesSize = nodeFiles.length;
				
				tmp= nodeID + " " + nodeAge;
				
				for (int j =0; j < nodeFilesSize; j++){
					tmp = tmp + " " + nodeFiles[j];  
					
				}
				decode[i] = new Message(null,tmp);
			}
				
			return decode ;
		}
}