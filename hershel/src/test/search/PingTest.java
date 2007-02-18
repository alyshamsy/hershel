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
import com.search.SearchClient;
import com.search.SearchId;
import com.search.SearchMessage;

public class PingTest
{    
    private SearchClient client;
    private DatagramSocket socket;
    
    @Before public void setUp() throws SocketException
    {
        client = new SearchClient("09876543210987654321", 5678);
        client.start();
        socket = new DatagramSocket(4567);
    }
    
    @After public void tearDown()
    {
        client.close();
        socket.close();
    }

    @Test public void pingResponse()
    {
        SearchMessage pingMessage = createPingMessage();
        
        MessageHandler handler = new MessageHandler(new SearchId("09876543210987654321"));
        SearchMessage response = handler.respondTo(pingMessage);
        assertEquals("ping", response.getCommand());
        assertEquals("09876543210987654321", response.arguments().get("id"));
    }

    private SearchMessage createPingMessage()
    {
        SearchMessage pingMessage = new SearchMessage("ping");
        pingMessage.arguments().put("id", "12345678901234567890");
        return pingMessage;
    }
    
    @Test public void pingOverUdp() throws IOException
    {        
        send(createPingMessage());        
        SearchMessage echo = receive();
        
        assertEquals("ping", echo.getCommand());
        assertEquals("09876543210987654321", echo.arguments().get("id"));       
    } 
    
    @Test public void pingUpdatesRoutingTable() throws IOException
    {        
        send(createPingMessage());        
        receive();    
        
        assertEquals("12345678901234567890", client.routingTable().get(0).id.toString());
        assertEquals("127.0.0.1", client.routingTable().get(0).address.getHostAddress());
        assertEquals(4567, client.routingTable().get(0).port);       
    }    

    private SearchMessage receive() throws IOException
    {
        byte response[] = new byte[256];
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        socket.receive(responsePacket);
        SearchMessage echo = SearchMessage.parse(new String(response));
        return echo;
    }

    private void send(SearchMessage pingMessage) throws SocketException, UnknownHostException, IOException
    {        
        DatagramPacket pingPacket = new DatagramPacket(pingMessage.getBytes(), pingMessage.getBytes().length, 
                InetAddress.getByName("localhost"), 5678);
        socket.send(pingPacket);
    }    
}
