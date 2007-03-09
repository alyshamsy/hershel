package testcode;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import file.Log;

public class LogTest {

	public static void main(String[] args){
		String fileName = "Hello.mp3";
		int NodeID = 34;
		String host = "www.hotmail.com";
		InetAddress IP; 
		int port = 50000;
		int chunkNum = 4434542;
		int chunkSize = 1500;
		int totalFileSize = 14440;
		
		String logName = "log.txt";
		
		try{
			IP = InetAddress.getByName(host);
			Log nodeActivity = new Log(logName);
		
			nodeActivity.writeLogInitialize(); 
			nodeActivity.writeTransferLog( fileName, NodeID, IP, port, 
					chunkNum, chunkSize, totalFileSize);
		}catch (UnknownHostException e){
			System.out.println("IP not found");
		}
		catch (IOException e){
			System.out.println("Something didn't work");
		}
	}
}
