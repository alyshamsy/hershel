package newscast;

import newscast.PartialView;

public class Newscast {
	public static final int SIZE = 20;
	
	private PartialView []view = new PartialView[SIZE];
	
	private boolean addElement(PartialView elem, int index){
		if (index >= 0 && index < SIZE){
			view[index] = elem;
			return true;
		}
		else
			return false;
	}
	
	private boolean removeElement( int index){
		if (index >= 0 && index < SIZE ){
			view[index] = null;
			return true;
		}
		else
			return false;
	}
	
	
}
