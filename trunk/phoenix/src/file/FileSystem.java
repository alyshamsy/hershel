package file;

import java.io.*;
import java.lang.*;
import java.security.*;

public class FileSystem 
{
	 /* An array structure to hold file info.*/
     public static fileHolder[] fileTable;
     private static int size;
     private double chunkSize = 524288.0;
     public static String dirName = "local directory";
     public byte[] data;
    
     public class fileHolder
     {
         public String fileName;
         public int fileSize;
         public String hashValue;
         
         public fileHolder()
         {
             fileName = null;
             fileSize = 0;
             hashValue = "";
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
    	double number_of_chunks = file_size/chunkSize;
    	int chunks = (int)Math.ceil(number_of_chunks);
    	
    	return chunks;
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
             fileTable[i].fileSize = (int)file.length();

             /*
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
             byte[] result = SHA1Utils.getSHA1Digest(data);
             fileTable[i].hashValue = SHA1Utils.digestToHexString(result);
             */   
         }
     }
     
     public void Store(RandomAccessFile file, int filesize)
     {
    	 int chunks = numberOfChunks(filesize);
    	 
    	 //break the file into chunks each of size 512KB and store in the array
    	 //use either the seek function or the write function 
    	 //declare the array globally so easy to access throughout the file
     }
}
