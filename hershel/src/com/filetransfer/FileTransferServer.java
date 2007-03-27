package com.filetransfer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class FileTransferServer extends Thread
{
    private ServerSocketChannel channel;

    private Selector selector;
    
    private SocketEventListener listener;

    public FileTransferServer(int port, SocketEventListener listener) throws IOException
    {
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        this.listener = listener;
    }

    public void close()
    {
        try
        {
            channel.close();           
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        // Wait for something of interest to happen
        try
        {
            // Register interest in when connection
            channel.register(selector, SelectionKey.OP_ACCEPT);
            
            while (selector.select() > 0)
            {
                
                // Get set of ready objects
                Set readyKeys = selector.selectedKeys();
                Iterator readyItor = readyKeys.iterator();

                // Walk through set
                while (readyItor.hasNext())
                {
                    // Get key from set
                    SelectionKey key = (SelectionKey) readyItor.next();

                    // Remove current entry
                    readyItor.remove();                   
                    if (key.isValid() && key.isAcceptable())
                    {
                        
                        // Get channel
                        ServerSocketChannel keyChannel = (ServerSocketChannel) key.channel();

                        // Get server socket
                        ServerSocket serverSocket = keyChannel.socket();

                        // Accept request
                        Socket socket = serverSocket.accept();
                        addClient(socket);                        
                    }
                    else if(key.isValid() && key.isReadable())
                    {                       
                        SocketChannel channel = (SocketChannel)key.channel();
                        listener.readReady(new InetSocketAddress(channel.socket().getInetAddress(), channel.socket().getPort()), 
                                new InputStreamReader(channel.socket().getInputStream()), 
                                new OutputStreamWriter(channel.socket().getOutputStream()));
                    }
                    else
                    {
                        //System.err.println("Ooops");
                    }

                }
            }
        }
        catch(ClosedChannelException ignore)
        {
            ignore.printStackTrace();
        }
        catch (IOException e)
        {            
            e.printStackTrace();
        }
    }

    private void addClient(Socket socket) throws IOException
    {       
        SocketChannel channel = socket.getChannel();
        channel.configureBlocking(false);
        
        channel.register(selector, SelectionKey.OP_READ);
        
    }

}
