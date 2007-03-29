package newscast;

import newscast.PartialView;

public class Newscast {
	public static final int SIZE = 20;
	
	private PartialView []view = new PartialView[SIZE];
	
	public boolean addElement(PartialView elem, int index){
		if (index >= 0 && index < SIZE){
			view[index] = new PartialView(elem);
			return true;
		}
		else
			return false;
	}
	
	public boolean removeElement( int index){
		if (index >= 0 && index < SIZE ){
			view[index] = null;
			return true;
		}
		else
			return false;
	}
	
	public PartialView[] getNews(){
	
		return this.view;
	}
	
}
