package com.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NetworkSearchClient extends Thread implements SearchClient
{
    private boolean running = true;
    private DatagramSocket socket;    
    private MessageHandler handler;
    
    public NetworkSearchClient(String idToUse, int port) throws SocketException
    {
        socket = new DatagramSocket(port);
        handler = new MessageHandler(new SearchId(idToUse), this);        
    }
    
    
    public void run()
    {
        while(running)
        {
            try
            {
                DatagramPacket incommingPacket = receiveMessage();                
                String message = new String(incommingPacket.getData(), 0, findMessageLength(incommingPacket.getData()));                
                SearchMessage request = SearchMessage.parse(message);       
                
                handler.respondTo(request, incommingPacket.getAddress(), incommingPacket.getPort());
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
    
    private DatagramPacket receiveMessage() throws IOException
    {
        byte[] buffer = new byte[1024];
        DatagramPacket incommingPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(incommingPacket);
        return incommingPacket;
    }

    private int findMessageLength(byte[] buffer)
    {
        int length = 0;
        for(; length< buffer.length; length++)
        {
            if(buffer[length] == 0)
                break;
        }
        return length;
    }  


    /* (non-Javadoc)
     * @see com.search.SearchClient#sendMessage(com.search.SearchMessage, com.search.NodeState)
     */
    public void sendMessage(SearchMessage message, NodeState destination) throws IOException
    {
        DatagramPacket outgoingPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, 
                destination.address, destination.port);                
        socket.send(outgoingPacket);
    }
    
    /* (non-Javadoc)
     * @see com.search.SearchClient#close()
     */
    public void close()
    {
        socket.close();
    }
}
