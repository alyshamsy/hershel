package test.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.search.MessageHandler;
import com.search.NetworkSearchClient;
import com.search.NodeState;
import com.search.SearchId;
import com.search.SearchMessage;


public class NetworkTests
{
    public class MockHandler extends MessageHandler
    {
        
        public SearchMessage lastRequest;

        @Override
        public void ping(NodeState targetNode)
        {
           
        }

        @Override
        public void respondTo(SearchMessage request, InetAddress address, int port)
        {
            lastRequest = request;
        }

        public MockHandler()
        {
            super(SearchId.fromHex("0987654321098765432109876543210987654321"), null);            
        }

    }

    private NetworkSearchClient client;
    private DatagramSocket socket;
    private MockHandler handler;
    
    @Before public void setUp() throws SocketException
    {
        handler = new MockHandler();
        client = new NetworkSearchClient(handler, 5678);
        client.start();
        socket = new DatagramSocket(4567);
    }
    
    @After public void tearDown()
    {
        client.close();
        socket.close();
    }
    
    @Test public void receiveMessagesFromUdp() throws SocketException, UnknownHostException, IOException, InterruptedException
    {
        send(createPingMessage());
        Thread.sleep(100);
        assertEquals("ping", handler.lastRequest.getCommand());
        assertEquals("1234567890123456789012345678901234567890", handler.lastRequest.arguments().get("id"));    
    }
    
    private void send(SearchMessage pingMessage) throws SocketException, UnknownHostException, IOException
    {        
        DatagramPacket pingPacket = new DatagramPacket(pingMessage.getBytes(), pingMessage.getBytes().length, 
                InetAddress.getByName("localhost"), 5678);
        socket.send(pingPacket);
    }    
    
    private SearchMessage createPingMessage()
    {
        SearchMessage pingMessage = new SearchMessage("ping");
        pingMessage.arguments().put("id", "1234567890123456789012345678901234567890");
        return pingMessage;
    }   
    
    @Test public void sendMessageOverUdp() throws UnknownHostException, IOException
    {
        client.sendMessage(createPingMessage(), new NodeState(SearchId.fromHex("1234567890123456789012345678901234567890"), 
                InetAddress.getByName("localhost"), 4567));
        SearchMessage received = receive();
        
        assertEquals("ping", received.getCommand());
        assertEquals("1234567890123456789012345678901234567890", received.arguments().get("id"));
    }
    
    private SearchMessage receive() throws IOException
    {
        byte response[] = new byte[256];
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        socket.receive(responsePacket);
        SearchMessage echo = SearchMessage.parse(new String(response));
        return echo;
    }
    
    
}
