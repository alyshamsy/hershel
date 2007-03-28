

import java.io.*;
//import java.lang.*;
import java.net.*;
//import java.security.*;

public class FileSystem 
{
	 /* An array structure to hold file info.*/
     public volatile static fileHolder[] fileTable;
     private static int size;
     private int chunkSize = 10;
     public static String dirName = "Shared";
     boolean made = (new File(dirName)).mkdir();
     public int count = 0;
	 public byte[] indiv_chunks;
    
     public class fileHolder
     {
    	 RandomAccessFile file;
    	 public int tag;
         public String fileName;
         public int fileSize;
         public String hashValue;
    	 boolean contains;
    	 public chunks[] chunkarray;
         
         public fileHolder()
         {
        	 tag = 0;
             fileName = null;
             fileSize = 0;
             hashValue = "";
    		 contains = false;
         }
         
     }
     
     
     public class chunks
     {
    	 public int chunk_tag;
    	 public byte[] chunks_array;
    	 public boolean chunk_exists;
    	 
    	 public chunks()
    	 {
    		 chunk_tag = 0;
    		 chunk_exists = false;
    	 }
     }
     
     
     public static int getFileSize(String fileName)
     {
         String temp = dirName + "/" + fileName;
         File file = new File(temp);
         int file_len = (int)file.length();
         
         return file_len;
     }
     
     public String toString(){
    	 String fileList = null;
    	 for(int i =0;i< fileTable.length;i++){
    		 fileList = fileList.concat(fileTable[i].fileName + " ");
    	 }
    	 return fileList;
     }
     
     public int numberOfChunks(int file_size)
     {
    	int numchunks = (int)Math.ceil((double)file_size/(double)chunkSize);
    	
    	if(numchunks == 0)
    		numchunks = 1;
    	
    	return numchunks;
     }
     
     public void fileInfo()
     {
    	 File dir = new File(dirName);
         fileTable = new fileHolder[100];
         String[] contents = dir.list();
         size = contents.length;
        
         if (contents == null) 
         {
            System.out.println("No shared directory exists");
            return;
         }
         
         //fileTable = null;
         
         //fileTable = new fileHolder[100];
         
         for (int i = 0; i < size; i++) 
         {
        	 fileTable[i] = new fileHolder();    		
    		
             fileTable[i].fileName = contents[i];
             
             System.out.println("The File Holder array contains: " + fileTable[i].fileName);
             		
             File file = new File(dirName + "/" + fileTable[i].fileName);
             int filesize = getFileSize(fileTable[i].fileName);
             fileTable[i].fileSize = filesize;
             
             
             File tempfile = new File(absPath() + "/" + fileTable[i].fileName);
             try {
            	 fileTable[i].file = new RandomAccessFile(tempfile, "rw");
    		} catch (IOException e1) {
    			System.out.println("File could not be created.");
    		}
             
    		
             int numChunks = numberOfChunks(filesize);
             
             System.out.println("FileInfo: " + fileTable[i].fileName + " has " + numChunks + " chunks");
             
             fileTable[i].chunkarray = new chunks[numChunks];
             for(int j = 0; j < numChunks; j++)
             {
            	 fileTable[i].chunkarray[j] = new chunks();
            	 
            	 fileTable[i].chunkarray[j].chunk_tag = j+1;
            	 int remainder = filesize - j * chunkSize;
            	 //System.out.println("FileInfo: " + fileTable[i].fileName + " has " + remainder + " remainder");
            	 if (remainder < chunkSize)
            		 fileTable[i].chunkarray[j].chunks_array = new byte[remainder];
            	 else
            		 fileTable[i].chunkarray[j].chunks_array = new byte[chunkSize];
             }
             
             fileTable[i].tag = i+1;
             try {
				chunkSegment(fileTable[i].file,fileTable[i].fileSize,fileTable[i].fileName, numChunks);
			} catch (IOException e1) {
				System.out.println("FileInfo: Could not break into chunks");
			}
                          
             String test = "";
             try 
             {
                 BufferedReader in = new BufferedReader(new FileReader(file));
                 test = test + in.readLine();
                 in.close();
             }
                 
             catch (IOException e) 
             {
            	 //System.out.println("TRY failed for reading the file");
             }
                  
             //byte[] data = test.getBytes();
             //byte[] result = SHA1utils.getSHA1Digest(data);
             //fileTable[i].hashValue = SHA1utils.digestToHexString(result);   
         }
     }
     
//   NEW FUNCTION START
     public String[] fileContents()
     {
    	 File dir = new File(dirName);
    	 String[] contents = dir.list();

    	 return contents;
     }
     //NEW FUNCTION END
     
     //modify this
     public synchronized boolean chunkSegment(RandomAccessFile file, int filesize, String fileName, int numOfChunks) throws IOException
     {
    	 
    	 for(int j = 0; j < size; j++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[j].fileName))
    		 {
    			 for(int i = 0; i < numOfChunks; i++)
    			 {
    				 fileTable[j].chunkarray[i].chunk_tag = i+1;
    				 
    				 int remainder = filesize - i * chunkSize;
                	 if (remainder < chunkSize){
    					 file.readFully(fileTable[j].chunkarray[i].chunks_array, 0, remainder);
                	 }
    				 else{
    					 file.readFully(fileTable[j].chunkarray[i].chunks_array, 0, chunkSize);
    				 }
    			 }
    			 break;
    		 }
    	 }
    	 System.out.println("Breaks into chunks");
    	 return true;
     }
          
     public boolean chunkStorage(String fileName, int chunkNumber, byte[] chunk)
     {
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[i].fileName))
    		 {
    			 if(fileTable[i].contains == true)
    			 {
    				 System.out.println("File contains all the chunks");
    				 return false;
    			 }
    			 else
    			 {
    				 System.arraycopy(chunk, 0, fileTable[i].chunkarray[i].chunks_array[chunkNumber], 0, chunk.length);
    				 fileTable[i].fileSize += chunk.length;
    				 return true;
    			 }
    		 }
    		 
    		 else
    		 {
    			 int filesize = getFileSize(fileName);
    			 int number_of_chunks = numberOfChunks(filesize);
    			 createFile(fileName, chunk, chunkNumber, number_of_chunks, filesize);
    			 return true;
    		 }
    	 }
    	 return false;
     }
     
     //createFile(test.txt,data,1,1);
     public synchronized void createFile(String fileName, byte[] chunk, int chunkNumber, int numChunks, long fileSize)
     {
    	 size = size + 1;
    	 System.out.println("Size is: " + size);
    	 int i = size - 1;
    	 
    	 fileTable[i] = new fileHolder();
         fileTable[i].fileName = fileName;
         fileTable[i].fileSize = (int)fileSize;
         fileTable[i].tag = i+1;
         
         fileTable[i].chunkarray = new chunks[numChunks];
         for(int j = 0; j < numChunks; j++)
         {
        	 fileTable[i].chunkarray[j] = new chunks();
        	 
        	 //fileTable[i].chunkarray[j].chunk_tag = j+1;
        	 int remainder = fileTable[i].fileSize - j * chunkSize;
        	 if (remainder < chunkSize && remainder > 0)
        		 fileTable[i].chunkarray[j].chunks_array = new byte[remainder];
        	 else
        		 fileTable[i].chunkarray[j].chunks_array = new byte[chunkSize];
         }
         
         System.out.println("Absolute path: " + absPath() + "/" + fileTable[i].fileName); 
         File tempfile = new File(absPath() + "/" + fileTable[i].fileName);
         try {
        	 tempfile.createNewFile();
        	 fileTable[i].file = new RandomAccessFile(tempfile, "rw");
        	 //fileTable[i].file.seek(fileTable[i].fileSize -1);
			 //fileTable[i].file.write(0);
		} catch (IOException e1) {
			System.out.println("File could not be created.");
		}
         String test = "";
         try 
         {
             BufferedReader in = new BufferedReader(new FileReader(tempfile));
             test = test + in.readLine();
             in.close();
         }
             
         catch (IOException e) 
         {
        	 //System.out.println("TRY failed for reading the file");
         }
       /*       
         byte[] data = test.getBytes();
         byte[] result = SHA1utils.getSHA1Digest(data);
         fileTable[i].hashValue = SHA1utils.digestToHexString(result);
         
         if(chunk.length < chunkSize)
        	 System.arraycopy(chunk, 0, fileTable[i].chunkarray[chunkNumber-1].chunks_array, 0, chunk.length);
         else
        	 System.arraycopy(chunk, 0, fileTable[i].chunkarray[chunkNumber-1].chunks_array, 0, chunkSize);
         
         fileTable[i].chunkarray[chunkNumber-1].chunk_exists = true;
         count++;
         
         if(numChunks == 1)
        	 fileTable[i].contains = true;
         else
        	 fileTable[i].contains = false;
        */
     }
     
     //file.updateFile(FILE_NAME, NUM_CHUNKS, data, id + 1);
     //                (test.txt, 1, data, 1)
     public synchronized boolean updateFile(String fileName, int num_of_chunks, byte[] chunk, int chunkNumber, long fileSize)
     {
    	 int curr_file_pos = -1;
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[i].fileName)){
    			 System.out.println("upDateFile: file exists at postition" + i);
    			 curr_file_pos = i;
    		 }
       	 }
    	 
    	 if(curr_file_pos == -1)
		 {
    		 System.out.println("upDateFile: Creating file");
			 createFile(fileName, chunk, chunkNumber, num_of_chunks, fileSize);
			 curr_file_pos = size-1;
			 System.out.println("curr_file_pos: " + curr_file_pos);
			 //return true;
		 }
    	 System.out.println("upDateFile: chunkNumber: " + chunkNumber);
    		if(fileTable[curr_file_pos].chunkarray[chunkNumber-1].chunk_exists == true)
    		{
    			System.out.println("The Chunk already exists");
    			return false;
    		}

    		else
    		{
    			//fileTable[curr_file_pos].chunkarray[chunkNumber].chunk_tag = chunkNumber;
    			try {
    				System.out.println("FileSystem: curr pos is " + curr_file_pos);
    				System.out.println("FileSystem: The seek pos is: " + (chunkNumber -1)*chunkSize);
    				
    				fileTable[curr_file_pos].file.seek((long)((chunkNumber -1)*chunkSize));
					fileTable[curr_file_pos].file.write(chunk);
				} catch (FileNotFoundException e) {
					//this should never happen
					System.out.println("FileSystem: RAF File not found");
				}
				catch(IOException e){
					System.out.println("FileSystem: RAF write/seek failed");
				}
    			
    	        System.arraycopy(chunk, 0, fileTable[curr_file_pos].chunkarray[chunkNumber-1].chunks_array, 0, chunk.length);
    	        fileTable[curr_file_pos].chunkarray[chunkNumber-1].chunk_exists = true;
    	        count++;
    	        if(count == num_of_chunks)
    	        	fileTable[curr_file_pos].contains = true;
    	        return true;
    		}
     }
     
     public synchronized byte[] download_chunk(String fileName, int chunkNumber)
     {
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[i].fileName))
    		 {
    			 indiv_chunks = fileTable[i].chunkarray[chunkNumber-1].chunks_array;
    			 return indiv_chunks;
    		 }
    	 }
    	 System.out.println("File Does not exist");
    	 return null;
     }
     
     public static String absPath()
     {
    	 File dir = new File(dirName);
    	 String absolutePath = dir.getAbsolutePath();
    	 return absolutePath;
     }
     
     public synchronized static String getIPaddr() throws IOException 
     {
    	 InetAddress addr = InetAddress.getLocalHost();
    	 // Get IP Address
    	 String ipaddr = addr.getHostAddress();
    	    
    	 // Get hostname
    	 //String hostname = addr.getHostName();
    	 return ipaddr;
     }
     
     public void printFileTable()
     {
    	 for(int i = 0; i < size; i++)
    	 {
    		 System.out.println("The file name is: " + fileTable[i].fileName);
    		 System.out.println("The file size is: " + fileTable[i].fileSize);
    		 System.out.println("The file id is: " + fileTable[i].tag);
    		 System.out.println("The hash value os the file is: " + fileTable[i].hashValue);
    		 System.out.println("The file is in its entirety: " + fileTable[i].contains);
    	 }
     }
}

