package test.filetransfer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.filetransfer.FileTransferServer;
import com.filetransfer.SocketEventListener;


public class ServerTests
{
    
    private FileTransferServer s;
    private Socket client;
    private MockEventListener mock;

    @Before public void setUp() throws IOException
    {
        mock = new MockEventListener();
        s = new FileTransferServer(10000, mock);
        s.start();
        
        client = new Socket();
        client.connect(new InetSocketAddress("localhost", 10000));
    }
    
    @After public void tearDown() throws IOException
    {
        client.close();
        s.close();
    }
    
	@Test public void ServerAcceptsConnection() throws UnknownHostException, IOException, InterruptedException
	{	
		Assert.assertTrue(client.isConnected());		
	}
    
    @Test public void notifyIfSocketHasInformation() throws IOException, InterruptedException
    {                    
        PrintWriter out = new PrintWriter(client.getOutputStream());
        out.println("hello");
        out.flush();
        Thread.sleep(200);
        
        Assert.assertNotNull(mock.lastSignaled);
        out.close();        
    }
    
    public class MockEventListener implements SocketEventListener
    {
        public Socket lastSignaled;
        public void readReady(Socket s)
        {
            lastSignaled = s;
        }
    }
}
