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
	
	PartialView(PartialView elem){
		this.addr = elem.addr;
		this.time = elem.time;
	}
	
	PartialView (InetAddress addr, long time){
		this.addr = addr;
		this.time.setTime(time);
	}
	
	public void setAddr(InetAddress addr){
		this.addr = addr;
	}
	
	public InetAddress getAddr(){
		return this.addr;
	}
	
	public Timestamp getTime(){
		return this.time;
	}
	
	public void setTime(long time){
		this.time.setTime(time);
	}
	
	//Updates the age of the node in the list
	public void updateTime (Timestamp temp){
		long newTime = temp.getTime();
		long oldTime = time.getTime();
		long updateTime = newTime - oldTime;
		
		time.setTime(updateTime);
	}
	
}
