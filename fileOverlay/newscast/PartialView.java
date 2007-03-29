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
	
	PartialView (InetAddress addr, Timestamp time){
		this.addr = addr;
		this.time = time;
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
	
	void updateTime (Timestamp temp){
		long newTime = temp.getTime();
		long oldTime = time.getTime();
		long updateTime = newTime - oldTime;
		
		time.setTime(updateTime);
	}
	
}
