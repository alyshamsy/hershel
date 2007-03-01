/* This is a parser for the File Overlay's Configuration File */

package file;

import java.io.*;

public class OverlayConf {

	public void ReadAndParse( FileInputStream  fin){
		try{
			boolean eof = false;
			int byteValue = fin.read();
			
			while( !eof ){
				
				if (byteValue == -1)
					eof = true;
			}
		} catch (IOException e){
			System.out.println ("error: problem reading the file");
			System.exit (-1);
		}
		
	}
	
}
