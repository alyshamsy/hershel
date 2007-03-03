package file;

import java.io.*;

public class LogTest {

	public static void main(String[] args){
		String fileName = "Hello.mp3";
		int NodeID = 34;
		String IP = "128.0.0.1";
		int port = 33200;
		int chunkNum = 4434542;
		int chunkSize = 1500;
		int totalFileSize = 14440;
		
		String logName = "config.txt";
		
		try{
			
			Log nodeActivity = new Log(logName);
			
			nodeActivity.writeLogInitialize(); 
			nodeActivity.writeTransferLog( fileName, NodeID, IP, port, 
					chunkNum, chunkSize, totalFileSize);
		}catch (IOException e){
			System.out.println("Something didn't work");
		}
	}
}