package test.filetransfer;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.filetransfer.FileList;
import com.filetransfer.FileTransferListener;

public class UploaderTests
{
	@Test public void InitialHandshake() throws InterruptedException
	{
		StringReader reader = new StringReader("get_pieces 1234567890123456789012345678901234567890\r\n");
		StringWriter writer = new StringWriter();
        
		HashMap<String, ArrayList<Integer>> availablePieces = new HashMap<String, ArrayList<Integer>>();        
		availablePieces.put("1234567890123456789012345678901234567890", pieces(3));
		FileTransferListener uploader = new FileTransferListener(availablePieces, new FileList());
		
        uploader.readReady(new InetSocketAddress("localhost", 12000), reader, writer);
		assertEquals("have 0,1,2\r\nget_pieces 1234567890123456789012345678901234567890\r\n", writer.toString());
	}
    
    @Test public void SendPiece()
    {
        StringReader reader = new StringReader("get 5 1234567890123456789012345678901234567890");
        StringWriter writer = new StringWriter();
        
        HashMap<String, ArrayList<Integer>> availablePieces = new HashMap<String, ArrayList<Integer>>();        
        availablePieces.put("1234567890123456789012345678901234567890", pieces(3));
        FileTransferListener uploader = new FileTransferListener(availablePieces, new FileList());
        
        uploader.readReady(new InetSocketAddress("localhost", 12000), reader, writer);
        assertEquals("piece 5 1234567890123456789012345678901234567890 10\r\n0123456789", writer.toString());
    }

	private ArrayList<Integer> pieces(int number) {
		ArrayList<Integer> pieces = new ArrayList<Integer>();
		for(int i = 0; i<number; i++)
			pieces.add(i);
		return pieces;
	}
}
