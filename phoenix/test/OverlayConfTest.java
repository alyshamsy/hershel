package file;

import java.io.*;

public class OverlayConfTest {
	
	public static final int MAX 5;
	
	public static void main (String args[]){
		
		String var[MAX];
		int value[MAX];
		
		try{
			// Open an input stream
			FileInputStream fstream = new FileInputStream ("config.txt");

		    ReadAndParse( fstream, var, value, MAX );
		    
		    // Close our input stream
		    fstream.close();		
		    
		}catch (IOException e)
		{
			System.err.println ("Unable to read from file");
			System.exit(-1);
		}

	}
}
