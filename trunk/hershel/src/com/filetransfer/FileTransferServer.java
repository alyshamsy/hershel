package com.filetransfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class FileTransferServer extends Thread
{    
    private ServerSocket socket;
    public FileTransferServer(int port) throws IOException
    {     
       socket = new ServerSocket(port);
    }

    public void close()
    {
        try
        {
            socket.close();
        }
        catch (IOException e)
        {           
            e.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            while(true)
            {
                Socket client = socket.accept();
            }
        }
        catch (IOException ignored)
        {         
           
        }
    }

}
