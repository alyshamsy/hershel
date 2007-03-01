package file;

import java.io.*;

public class OverlayConfTest {
	
	public static void main (String args[]){
		FileInputStream fin;
		
		try{
			// Open an input stream
		    fin = new FileInputStream ("config.txt");

		    ReadAndParse(fin);
		    
		    // Close our input stream
		    fin.close();		
		    
		}catch (IOException e)
		{
			System.err.println ("Unable to read from file");
			System.exit(-1);
		}

	}
}
