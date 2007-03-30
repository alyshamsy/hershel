package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;

public class FileSystem 
{
	 /* An array structure to hold file info.*/
     public static fileHolder[] fileTable;
     private static int size;
     private int chunkSize = 10;
     public static String dirName = "Shared";
     boolean made = (new File(dirName)).mkdir();
     public int count = 0;
	 public byte[] indiv_chunks;
    
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
         
         /*
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
         */
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
    	double fileSize = (double)file_size;
    	double number_of_chunks = fileSize/chunkSize;
    	int numchunks = (int)Math.ceil(number_of_chunks);
    	
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
             
             int numChunks = numberOfChunks(filesize);
             
             fileTable[i].chunkarray = new chunks[numChunks];
             for(int j = 0; j < numChunks; j++)
             {
            	 fileTable[i].chunkarray[j] = new chunks();
            	 
            	 fileTable[i].chunkarray[j].chunk_tag = j+1;
            	 fileTable[i].chunkarray[j].chunks_array = new byte[chunkSize];
             }
             
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
     
//   NEW FUNCTION START
     public String[] fileContents()
     {
    	 File dir = new File(dirName);
    	 String[] contents = dir.list();

    	 return contents;
     }
     //NEW FUNCTION END
     
     //modify this
     public synchronized boolean chunkSegment(RandomAccessFile file, int filesize, String fileName) throws IOException
     {
    	 int numOfChunks = numberOfChunks(filesize);
    	 
    	 for(int j = 0; j < size; j++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[j].fileName))
    		 {
    			 for(int i = 0; i < numOfChunks; i++)
    			 {
    				 fileTable[j].chunkarray[i].chunk_tag = i+1;
    				 if(filesize < chunkSize)
    					 file.readFully(fileTable[j].chunkarray[i].chunks_array, i*chunkSize, filesize);
    				 else
    					 file.readFully(fileTable[j].chunkarray[i].chunks_array, i*chunkSize, chunkSize);
    				 System.out.println("Breaks into chunks");
    				 return true;
    			 }
    		 }
    	 }
    	 return false;
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
    			 //int filesize = getFileSize(fileName);
    			 //int number_of_chunks = numberOfChunks(filesize);
    			 //createFile(fileName, chunk, chunkNumber, number_of_chunks);
    			 return true;
    		 }
    	 }
    	 return false;
     }
     
     public void createFile(String fileName, byte[] chunk, int chunkNumber, int numChunks, int fileSize)
     {
    	 size = size + 1;
    	 System.out.println("Size is: " + size);
    	 int i = size - 1;
    	 
    	 fileTable[i] = new fileHolder();
         fileTable[i].fileName = fileName;
         fileTable[i].fileSize = fileSize;
         fileTable[i].tag = i+1;
         
         fileTable[i].chunkarray = new chunks[numChunks];
         for(int j = 0; j < numChunks; j++)
         {
        	 fileTable[i].chunkarray[j] = new chunks();
        	 
        	 //fileTable[i].chunkarray[j].chunk_tag = j+1;
        	 fileTable[i].chunkarray[j].chunks_array = new byte[chunkSize];
         }
         
         fileTable[i].tag = i+1;
             
         File file = new File(dirName + "/" + fileTable[i].fileName);
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
     }
     
     public boolean updateFile(String fileName, int num_of_chunks, byte[] chunk, int chunkNumber, int fileSize)
     {
    	 int curr_file_pos = -1;
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[i].fileName))
    			 curr_file_pos = i;
       	 }
    	 
    	 if(curr_file_pos == -1)
		 {
			 createFile(fileName, chunk, chunkNumber, num_of_chunks, fileSize);
			 curr_file_pos = size-1;
			 return true;
		 }
    	 
    	 if(count == num_of_chunks)
    	 {
    		 System.out.println("The entire file exists");
    		 return false;
    	 }
    	 
    	 else
    	 {
    		if(fileTable[curr_file_pos].chunkarray[chunkNumber-1].chunk_exists == true)
    		{
    			System.out.println("The Chunk already exists");
    			return false;
    		}

    		else
    		{
    			//fileTable[curr_file_pos].chunkarray[chunkNumber].chunk_tag = chunkNumber;
    	        System.arraycopy(chunk, 0, fileTable[curr_file_pos].chunkarray[chunkNumber-1].chunks_array, 0, chunk.length);
    	        fileTable[curr_file_pos].chunkarray[chunkNumber-1].chunk_exists = true;
    	        count++;
    	        if(count == num_of_chunks)
    	        	fileTable[curr_file_pos].contains = true;
    	        return true;
    		}
    	 }
     }
     
     public synchronized byte[] download_chunk(String fileName, int chunkNumber)
     {
    	 for(int i = 0; i < size; i++)
    	 {
    		 if(fileName.equalsIgnoreCase(fileTable[i].fileName))
    		 {
    			 indiv_chunks = fileTable[i].chunkarray[chunkNumber-1].chunks_array;
    			 System.out.println("Download Chunks: " + indiv_chunks.toString());
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