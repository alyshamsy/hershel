package test.filetransfer;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.filetransfer.UploaderThread;

public class UploaderTests
{
	@Test public void SendAvailablePieces() throws InterruptedException
	{
		StringReader reader = new StringReader("get_pieces 1234567890123456789012345678901234567890");
		StringWriter writer = new StringWriter();
		HashMap<String, ArrayList<Integer>> availablePieces = new HashMap<String, ArrayList<Integer>>();
		availablePieces.put("1234567890123456789012345678901234567890", pieces(3));
		UploaderThread uploader = new UploaderThread(reader, writer, availablePieces);
		uploader.start();
		Thread.sleep(100);
		assertEquals("have 0,1,2", writer.toString());
	}

	private ArrayList<Integer> pieces(int number) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		for(int i = 0; i<number; i++)
			pieces.add(i);
		return pieces;
	}
}
