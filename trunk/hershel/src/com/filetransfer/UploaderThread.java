package com.filetransfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

public class UploaderThread extends Thread
{
	private BufferedReader in;
	private Writer out;
	
	private HashMap<String, ArrayList<Integer>> availablePieces;
	public UploaderThread(Reader reader, Writer writer, HashMap<String, ArrayList<Integer>> availablePieces) {
		in = new BufferedReader(reader);
		out = writer;
		this.availablePieces = availablePieces;
	}
	
	public void run()
	{
		try {
			String handshake = in.readLine();
			String filename = handshake.split("\\s")[1];
			
			String pieces = null;
			for(Integer i: availablePieces.get(filename))
			{
				pieces = pieces == null ? i.toString() : pieces + "," + i.toString();
			}
			
			out.write("have "+pieces);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
