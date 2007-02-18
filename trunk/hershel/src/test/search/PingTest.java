package test.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.junit.Test;

import com.search.MessageHandler;
import com.search.SearchClient;
import com.search.SearchId;
import com.search.SearchMessage;

public class PingTest
{    
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
        SearchClient client = new SearchClient("09876543210987654321", 5678);
        client.start();
        
        SearchMessage pingMessage = createPingMessage();
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket pingPacket = new DatagramPacket(pingMessage.getBytes(), pingMessage.getBytes().length, InetAddress.getLocalHost(), 5678);
        socket.send(pingPacket);
        
        byte response[] = new byte[256];
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        socket.receive(responsePacket);
        SearchMessage echo = SearchMessage.parse(new String(response));
        
        assertEquals("ping", echo.getCommand());
        assertEquals("09876543210987654321", echo.arguments().get("id"));
        client.close();
        socket.close();
    }
}
