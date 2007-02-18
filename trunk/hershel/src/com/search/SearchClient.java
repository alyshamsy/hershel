package com.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class SearchClient extends Thread
{
    private boolean running = true;
    private DatagramSocket socket;
    private SearchId myId;
    
    public SearchClient(String idToUse, int port) throws SocketException
    {
        socket = new DatagramSocket(port);
        myId = new SearchId(idToUse);
    }
    
    
    public void run()
    {
        while(running)
        {
            try
            {
                byte[] buffer = new byte[1024];
                DatagramPacket incommingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incommingPacket);
                int length = 0;
                for(; length< buffer.length; length++)
                {
                    if(buffer[length] == 0)
                        break;
                }
                String message = new String(incommingPacket.getData(), 0, length);               
                
                MessageHandler handler = new MessageHandler(myId);
                SearchMessage request = SearchMessage.parse(message);
                SearchMessage response = handler.respondTo(request);
                
                DatagramPacket outgoingPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, 
                        incommingPacket.getAddress(), incommingPacket.getPort());
                socket.send(outgoingPacket);
            } 
            catch (SocketException e)
            {
               System.out.println(e.getMessage());
               running = false;
            }
            catch (IOException e)
            {      
                e.printStackTrace();
            }            
        }
    }
    
    public void close()
    {
        socket.close();
    }

}
