package test.filetransfer;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.filetransfer.FileTransferServer;


public class ServerTests
{
	@Test public void ServerAcceptsConnection() throws UnknownHostException, IOException
	{
		FileTransferServer s = new FileTransferServer(10000);
		s.start();
        
		Socket client = new Socket("localhost", 10000);
		Assert.assertTrue(client.isConnected());
		client.close();
		s.close();
	}
}
