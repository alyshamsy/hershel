package com.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SearchClient extends Thread
{
    private boolean running = true;
    private DatagramSocket socket;
    private SearchId myId;
    private ArrayList<NodeState> table;
    
    public SearchClient(String idToUse, int port) throws SocketException
    {
        socket = new DatagramSocket(port);
        myId = new SearchId(idToUse);
        table = new ArrayList<NodeState>();
    }
    
    
    public void run()
    {
        while(running)
        {
            try
            {
                DatagramPacket incommingPacket = receiveMessage();                
                int length = findMessageLength(incommingPacket.getData()); 
                
                SearchMessage response = respond(incommingPacket, length);               
                
                sendResponse(incommingPacket, response);
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
    
    private SearchMessage respond(DatagramPacket incommingPacket, int messageLength)
    {
        String message = new String(incommingPacket.getData(), 0, messageLength);                
        SearchMessage request = SearchMessage.parse(message);
        
        MessageHandler handler = new MessageHandler(myId);       
        table.add(new NodeState(request.arguments().get("id"), incommingPacket.getAddress(), incommingPacket.getPort()));
        
        return handler.respondTo(request);
    }
    
    private DatagramPacket sendResponse(DatagramPacket incommingPacket, SearchMessage response) throws IOException
    {
        DatagramPacket outgoingPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, 
                incommingPacket.getAddress(), incommingPacket.getPort());                
        socket.send(outgoingPacket);
        return outgoingPacket;
    }
    
    public void close()
    {
        socket.close();
    }


    public ArrayList<NodeState> routingTable() throws UnknownHostException
    {        
        return table;
    }

}
