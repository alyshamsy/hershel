package file;

import java.io.*;
import java.lang.*;
import java.net.*;
//import java.security.*;

public class FileSystem 
{
	 /* An array structure to hold file info.*/
     public static fileHolder[] fileTable;
     private static int size;
     private int chunkSize = 524288;
     public static String dirName = "Shared";
     boolean made = (new File(dirName)).mkdir();
     public int count = 0;
     public byte[] ipAddr;
     public String hostname;
	 //public static chunks[] chunks;
    
     public class fileHolder
     {
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
         
         public class chunks
         {
        	 public int chunk_tag;
        	 public byte[] chunks_array;
        	 public boolean chunk_exists;
        	 
        	 public chunks()
        	 {
        		 chunk_tag = 0;
        		 chunks_array[0] = 0;
        		 chunk_exists = false;
        	 }
         }
     }
     
     /*
     public class chunks
     {
    	 public int chunk_tag;
    	 public byte[] chunks_array;
    	 
    	 public chunks()
    	 {
    		 chunk_tag = 0;
    		 chunks_array[0] = 0;
    	 }
     }
     */
     
     public static int getFileSize(String fileName)
     {
         String temp = dirName + "/" + fileName;
         File file = new File(temp);
         int file_len = (int)file.length();
         
         return file_len;
     }
     
     public int numberOfChunks(int file_size)
     {
    	double fileSize = (double)file_size;
    	double number_of_chunks = fileSize/chunkSize;
    	int numchunks = (int)Math.ceil(number_of_chunks);
    	
    	return numchunks;
     }
     
     public void fileInfo()
     {
    	 File dir = new File(dirName);
    	    
         String[] contents = dir.list();
         size = contents.length;
         
         if (contents == null) 
         {
            System.out.println("No shared directory exists");
            return;
         }
         
         fileTable = null; // garbage collector should take care of that
         
         fileTable = new fileHolder[size];
         
         for (int i = 0; i < size; i++) 
         {
        	 fileTable[i] = new fileHolder();
             fileTable[i].fileName = contents[i];
                 
             File file = new File(dirName + "/" + fileTable[i].fileName);
             int filesize = getFileSize(fileTable[i].fileName);
             fileTable[i].fileSize = filesize;
             
             fileTable[i].tag = i+1;
                          
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
                  
             byte[] data = test.getBytes();
             byte[] result = SHA1utils.getSHA1Digest(data);
             fileTable[i].hashValue = SHA1utils.digestToHexString(result);   
         }
     }
     
     //modify this
     public boolean chunkSegment(RandomAccessFile file, int filesize) throws IOException
     {
    	 int numOfChunks = numberOfChunks(filesize);
    	 
    	 for(int j = 0; j < size; j++)
    	 {
    		 for(int i = 0; i < numOfChunks; i++)
    		 {
    			 fileTable[j].chunkarray[i].chunk_tag = i;
    			 file.readFully(fileTable[j].chunkarray[i].chunks_array, i*chunkSize, chunkSize);
    		 }
    	 }
    	 
    	 return true;
     }
          
     public boolean chunkStorage(String fileName, int chunkNumber, byte[] chunk)
     {
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName == fileTable[i].fileName)
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
    			 //int filesize = getFileSize(fileName);
    			 //int number_of_chunks = numberOfChunks(filesize);
    			 createFile(fileName, chunk, chunkNumber);
    			 return true;
    		 }
    	 }
    	 return false;
     }
     
     public void createFile(String fileName, byte[] chunk, int chunkNumber)
     {
    	 size = size+1;
    	 int i = size - 1;
    	 
    	 fileTable[i] = new fileHolder();
         fileTable[i].fileName = fileName;
         fileTable[i].fileSize = chunk.length;
         fileTable[i].contains = false;
         fileTable[i].tag = i+1;
         
         fileTable[i].chunkarray[chunkNumber].chunk_tag = chunkNumber;
         System.arraycopy(chunk, 0, fileTable[i].chunkarray[chunkNumber].chunks_array, 0, chunk.length);
         fileTable[i].chunkarray[chunkNumber].chunk_exists = true;
         count++;
     }
     
     public boolean updateFile(String fileName, int num_of_chunks, byte[] chunk, int chunkNumber)
     {
    	 int curr_file_pos = 0;
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName == fileTable[i].fileName)
    			 curr_file_pos = i;
    		 else
    		 {
    			 createFile(fileName, chunk, chunkNumber);
    			 return false;
    		 }
    	 }
    	 
    	 if(count == num_of_chunks)
    	 {
    		 System.out.println("The entire file exists");
    		 return false;
    	 }
    	 
    	 else
    	 {
    		if(fileTable[curr_file_pos].chunkarray[chunkNumber].chunk_exists == true)
    		{
    			System.out.println("The Chunk already exists");
    			return false;
    		}

    		else
    		{
    			fileTable[curr_file_pos].chunkarray[chunkNumber].chunk_tag = chunkNumber;
    	        System.arraycopy(chunk, 0, fileTable[curr_file_pos].chunkarray[chunkNumber].chunks_array, 0, chunk.length);
    	        fileTable[curr_file_pos].chunkarray[chunkNumber].chunk_exists = true;
    	        count++;
    	        if(count == num_of_chunks)
    	        	fileTable[curr_file_pos].contains = true;
    	        return true;
    		}
    	 }
     }
     
     public String absPath(File dirName)
     {
    	 String absolutePath = dirName.getAbsolutePath();
    	 return absolutePath;
     }
     
     public void getIPaddr() throws IOException 
     {
    	 InetAddress addr = InetAddress.getLocalHost();
    	    
    	 // Get IP Address
    	 ipAddr = addr.getAddress();
    	    
    	 // Get hostname
    	 hostname = addr.getHostName();
     }
}
