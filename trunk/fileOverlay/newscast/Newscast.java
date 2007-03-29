package newscast;

import newscast.PartialView;
import java.sql.Timestamp;

public class Newscast 
{
	public static final int SIZE = 20;
	
	private PartialView []view = new PartialView[SIZE];
	
	public boolean addElement(PartialView elem, int index)
	{
		if (index >= 0 && index < SIZE)
		{
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
	
	//sorts array based on Timestamp
	public void sortArray (PartialView[] array)
	{
		Timestamp temp;
		for(int i = SIZE*2; i > 0 ; i--)
		{
			for(int j = 0; j < i; j++)
			{
				if(array[j - 1].time.after(array[j].time))
				{
					temp = array[j - 1].time;
					array[j-1].time = array[j].time;
					array[j].time = temp;
				}
			}
		}
	}
	
	public void newsUpdate(PartialView[] news) //throws IOException
	{
		PartialView[] merge = new PartialView[SIZE*2];
		
		for(int i = 0; i < SIZE; i++)
		{
			merge[i] = view[i];
			merge[SIZE + i] = news[i];
		}
		
		sortArray(merge);
		
		for(int j = 0; j < SIZE; j++)
			view[j] = merge[j];
	}
	
}
