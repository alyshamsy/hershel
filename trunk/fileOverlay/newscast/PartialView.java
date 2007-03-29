package newscast;

import java.net.*;

import java.sql.Timestamp;

public class PartialView {
	InetAddress addr;
	Timestamp time;
	
	PartialView(){
		this.addr = null;
		this.time = null;
	}
	
	PartialView (InetAddress addr, long time){
		this.addr = addr;
		this.time.setTime(time);
	}
	
	void setAddr(InetAddress addr){
		this.addr = addr;
	}
	
	InetAddress getAddr(){
		return this.addr;
	}
	
	Timestamp getTime(){
		return this.time;
	}
	
	void setTime(long time){
		this.time.setTime(time);
	}
	
	//Updates the age of the node in the list
	void updateTime (Timestamp temp){
		long newTime = temp.getTime();
		long oldTime = time.getTime();
		long updateTime = newTime - oldTime;
		
		time.setTime(updateTime);
	}
	
}
