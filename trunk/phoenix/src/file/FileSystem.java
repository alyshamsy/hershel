package file;

import java.io.*;
import java.lang.*;
import java.security.*;

public class FileSystem 
{
	 /* An array structure to hold file info.*/
     public static fileHolder[] fileTable;
     private static int size;
     private int chunkSize = 524288;
     public static String dirName = "local directory";
     public static byte[] data;
	 public static chunks[] chunks;
    
     public class fileHolder
     {
    	 public int tag;
         public String fileName;
         public int fileSize;
         public String hashValue;
         
         public fileHolder()
         {
        	 tag = 0;
             fileName = null;
             fileSize = 0;
             hashValue = "";
         }       
     }
     
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
         
         fileTable = new fileHolder[contents.length];
         
         for (int i=0; i<contents.length; i++) 
         {
        	 fileTable[i] = new fileHolder();
             fileTable[i].fileName = contents[i];
                 
             File file = new File(dirName + "/" + fileTable[i].fileName);                    
             fileTable[i].fileSize = getFileSize(fileTable[i].fileName);
             
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
                  
             data = test.getBytes();
             byte[] result = SHA1Utils.getSHA1Digest(data);
             fileTable[i].hashValue = SHA1Utils.digestToHexString(result);   
         }
     }
     
     public void chunkSegment(RandomAccessFile file, int filesize) throws IOException
     {
    	 int numOfChunks = numberOfChunks(filesize);
    	 
    	 for(int i = 0; i < numOfChunks; i++)
    	 {
    		 chunks[i].chunk_tag = i;
    		 file.readFully(chunks[i].chunks_array, i*chunkSize, chunkSize);
    	 }
     }
     
        
     
}
