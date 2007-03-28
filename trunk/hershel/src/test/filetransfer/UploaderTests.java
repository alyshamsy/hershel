package test.filetransfer;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.net.InetSocketAddress;

import org.junit.Before;
import org.junit.Test;

import com.filetransfer.FileTransferListener;

public class UploaderTests
{
	private StringWriter writer;
    private FileTransferListener uploader;
    
    @Before public void setUp()
    {
        writer = new StringWriter();
        uploader = new FileTransferListener(new MockFileList());
    }

    @Test public void InitialHandshake() throws InterruptedException
	{		
        uploader.readReady(new InetSocketAddress("localhost", 12000), DownloadTest.toStream("get_pieces 1234567890123456789012345678901234567890\r\n"), writer);
		assertEquals("have 0,1,2\r\nget_pieces 1234567890123456789012345678901234567890\r\n", writer.toString());
	}
    
    @Test public void SendPiece()
    {     
        uploader.readReady(new InetSocketAddress("localhost", 12000), DownloadTest.toStream("get 5 1234567890123456789012345678901234567890"), writer);
        assertEquals("piece 5 1234567890123456789012345678901234567890 10\r\n0123456789", writer.toString());
    }
}
