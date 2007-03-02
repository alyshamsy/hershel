/* This is a parser for the File Overlay's Configuration File 
 * 
 * Configuration Variables will be in the following format:
 * 			<Variable> : <Value>
 * 
 *	Variable:	must contain no spaces
 *  Value:		must contain no spaces
 */
package file;

import java.io.*;

public class OverlayConf {
	
	public String Extract( String str ) {
		int beg = 0, end = 0 ;
		String extract;
		
		return str;
	}
	
	public int ReadAndParse( FileInputStream  fstream, String []var, int []value, final int MAX ){
	
		String strLine;
		boolean eof = false;
		
		// Redirecting the FileInputStream to BufferStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader buf = new BufferedReader( new InputStreamReader(in) );

		// Read the first line of the config file
		try{
			strLine = buf.readLine();	
			
			if (strLine == null)
				eof=true;
		
		}catch (IOException e1){
			System.out.println ("error: buffer read failed prior to extraction");
			return -1;
		}
		
		//Extraction stage - get <variable>:<value> out of text document
		for( int i = 0; (i < MAX) && ( !eof ); i++ ){
			String varExt, valueExt;
			
			varExt = Extract(strLine);
			valueExt = Extract (varExt);
			
			/*
			 * Add code the extract full var
			 */
			
						
			/*
			 * Convert value string to INT
			 */
			
			//var[i] = varExt;
			//value[i] = valueExt;
			
			try{
				strLine = buf.readLine();	
				
				if (strLine == null)
					eof=true;
			
			}catch (IOException e2){
				System.out.println ("error: buffer read failed during extraction");
				return -1;
			}
		}
			
		return 0;
	}
	
}
