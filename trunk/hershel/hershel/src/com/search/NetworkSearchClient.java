package com.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class NetworkSearchClient extends Thread implements SearchClient
{
    private static NodeState firstNode;
	private boolean running = true;
    private DatagramSocket socket;    
    private MessageHandler handler;

    static {
    	try {
    		firstNode = new NodeState(
    				SearchId.fromHex("1234567890123456789012345678901234567890"),
    				InetAddress.getByName("localhost"), 10000);
    	} catch (UnknownHostException e) {
    		throw new Error("Can't connect to first node.");
    	}
    }

    public NetworkSearchClient(String idToUse, int port) throws SocketException
    {
        socket = new DatagramSocket(port);
        handler = new MessageHandler(SearchId.fromHex(idToUse), this);
    }
    
    
    public NetworkSearchClient(MessageHandler handler, int port) throws SocketException
    {
        socket = new DatagramSocket(port);
        this.handler = handler;
    }

    // Interface to the file overlay
    public void updateDatabase(SearchId file, InetSocketAddress node) throws IOException
    {
    	handler.updateDatabase(file, node);
    }

    public void initializeDatabase(ArrayList<SearchResult> files)
    {
    	handler.initializeDatabase(files);
    }

    public void run()
    {
    	// Connect to "first node".
    	try {
    		handler.routingTable().addNode(firstNode);
    		handler.findNode(firstNode, handler.getId());
    	} catch (IOException e) {
    		e.printStackTrace();
    		return;
    	}

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
        byte[] buffer = new byte[1024*2];
        DatagramPacket incommingPacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(incommingPacket);
        /*//Shad's Addition.
        socket.setSoTimeout(1000);
        try
        {
            socket.receive(incommingPacket);
        }
        catch (SocketTimeoutException e)
        {
        	return null;
        }
        return incommingPacket;
        */
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
    	sendToUI(message.toString(), "< ");
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

    public MessageHandler getHandler()
    {
    	return handler;
    }

    // For testing purposes.
    private GUI ui;
    
    public void registerUI(GUI ui)
    {
    	this.ui = ui;
    }

    public void sendToUI(String s, String direction)
    {
    	if (ui != null)
    		ui.getMessage(direction + s);
    }
}
