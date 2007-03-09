package file;

import java.io.*;
import java.net.InetAddress;
import java.util.logging.FileHandler;

public class Log extends FileHandler {
	final int LIMIT = 1000000;
	final boolean APPEND = true;
	
	/*
	 * Pattern - stores file name
	 * Limit - stores max. number of bytes to write to one file
	 * Append - specifies append mode
	 */
	private String pattern;
	private int limit;
	private boolean append;
	
	
	public Log() throws IOException
	{
		this.pattern = "";
		this.limit = LIMIT;
		this.append	= APPEND;
	}
	
	public Log( String pattern) throws IOException
	{
		this.pattern = pattern;
		this.limit = LIMIT;
		this.append	= APPEND;
	}
	
	public Log( String pattern, int limit, boolean append ) throws IOException 
	{
		this.pattern = pattern;
		this.limit = limit;
		this.append = append;
	}

	public void writeLogInitialize(){
		File newFile;
		FileWriter fout;
		String initialize;
		
		try{
			newFile = new File(pattern);
			fout = new FileWriter(newFile,true);
			initialize = "FileName        NodeID          IP        Port        Chunk#" +
			      "       ChunkSize       FileSize\n";
			fout.write(initialize, 0, initialize.length());
			initialize = "\n";
			fout.write(initialize, 0, initialize.length());
			fout.close();
		}catch (IOException e){
			System.out.println("File initialization error");
		}			
	}
	/*
	 * This function writes to the log file that keeps track of the following:
	 * 	1) Name of the file you're downloading.
	 *  2) Node ID of the node you're downloading from.
	 *  3) Port of the node you're downloading from.
	 *  4) Chunk size of the chunk being downloaded.
	 *  5) Total File size.
	 *  
	 *  Formart it will appear in:
	 *  File:<FileName> NodeID: <NodeID> IP:<IP Address> Port: <Port #>  Chunk#: <Chunk #> Chunk Size: <Chunk Size> File Size: <File Size>
	 */
	public void writeTransferLog( String downloadFileName, int recvNodeID, 
			InetAddress IPaddr, int recvPort, int chunkNum, int chunkSize, int fileSize ) 
	{
		String output;
		String IP;
		try{
			int b,e;
			IP = IPaddr.toString();
			
			b = IP.indexOf('/');
			e = IP.length();
			
			IP = IP.substring(b+1,e);
				
			//Open the file
			FileWriter fileOut = new FileWriter(pattern, true);
			
			//Write the  data to the file
			output = ( downloadFileName + "         " + recvNodeID + "      " + IP + "    " + recvPort +
					"        " + chunkNum + "        " + chunkSize + "           " + fileSize +"\n" );
			
			if (output.length() < this.limit && append)
				fileOut.write( output, 0, output.length());
			
			fileOut.close();
			
		}catch (NullPointerException e ){
			System.err.println("NULL ADDRESS PASSED");
			System.exit(-1);
		}
		catch (IOException fileopen){
			System.err.println("error: " + pattern +" cannot open");
			System.exit(-1);
		}
	}
	
}
