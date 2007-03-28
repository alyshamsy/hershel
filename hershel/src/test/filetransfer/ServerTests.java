package test.filetransfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
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

    @Before public void setUp() throws IOException, InterruptedException
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
        Thread.sleep(100);
        
        Assert.assertNotNull(mock.lastSignaled);
        out.close();        
    }
    
    @Test public void sendMessagesByIpAddress() throws InterruptedException, IOException
    {
        Thread.sleep(100);
        Assert.assertEquals(1, s.connectedPeers().size());
        s.send((InetSocketAddress)s.connectedPeers().keySet().toArray()[0], "hello\r\n");        
        
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        Assert.assertEquals("hello", in.readLine());
    }
    
    @Test public void removeDisconnectingPeers() throws InterruptedException, IOException
    {
        Thread.sleep(100);
        Assert.assertEquals(1, s.connectedPeers().size());
        client.shutdownOutput();
        client.shutdownInput();
        Thread.sleep(100);
        Assert.assertEquals(0, s.connectedPeers().size());
    }
    
    public class MockEventListener implements SocketEventListener
    {
        public InetSocketAddress lastSignaled;
        public void readReady(InetSocketAddress peer, String message, Writer writer)
        {
            lastSignaled = peer;
        }
    }
}
