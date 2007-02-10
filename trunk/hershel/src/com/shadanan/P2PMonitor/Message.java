package com.shadanan.P2PMonitor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Message {
	private String cmd;
	private String[] data;
	
	public Message() {
		cmd = null;
		data = null;
	}
	
	public static Message parse(String input) {
		Message result = new Message();
		
		int indexOfFirstSpace = input.indexOf(" ");
		if (indexOfFirstSpace == -1) {
			result.cmd = input;
		} else {
			result.cmd = input.substring(0, indexOfFirstSpace);
			String[] temp = input.substring(indexOfFirstSpace+1).split("\\s");
			result.data = new String[temp.length];
			for (int i = 0; i < temp.length; i++) {
				try {
					result.data[i] = URLDecoder.decode(temp[i], "UTF-8");
				} catch (UnsupportedEncodingException e) {}
			}
		}
		
		return result;
	}
	
	public Message(String cmd, String data) {
		this.cmd = cmd;
		this.data = new String[1];
		this.data[0] = data;
	}
	
	public Message(String cmd, String[] data) {
		this.cmd = cmd;
		this.data = data;
	}
	
	public boolean cmdEquals(String cmd) {
		return (this.cmd.equals(cmd));
	}
	
	public String getCmd() {
		return cmd;
	}
	
	public String getData() {
		if (data == null || data.length == 0) return null;
		return data[0];
	}
	
	public String getData(int index) {
		if (data == null || index < 0 || index >= data.length) return null;
		return data[index];
	}
	
	public int countDataTokens() {
		if (data == null) return 0;
		return data.length;
	}
	
	public String toString() {
		String result = "";
		if (cmd != null) result += cmd;
		if (data != null) {
			for (int i = 0; i < data.length && data[i] != null; i++) {
				try {
					result += " " + URLEncoder.encode(data[i], "UTF-8");
				} catch (UnsupportedEncodingException e) {}
			}
		}
		return result;
	}
}
