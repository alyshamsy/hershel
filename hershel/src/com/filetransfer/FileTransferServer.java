package com.filetransfer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;

public class FileTransferServer extends Thread implements Connector
{
   
    private SocketEventListener listener;
    private HashMap<InetSocketAddress, Socket> connectedPeers = new HashMap<InetSocketAddress, Socket>();
    private ServerSocket serverSocket;
    public FileTransferServer(int port, SocketEventListener listener) throws IOException
    {
        this.listener = listener;
        serverSocket = new ServerSocket(port);
    }

    public synchronized void close()
    {
        try
        {
            serverSocket.close();
            for (Entry<InetSocketAddress, Socket> e : connectedPeers.entrySet())
            {
                try
                {
                    e.getValue().close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
           e.printStackTrace();
        }        
    }
    
    private class SocketReadListener extends Thread
    {
        private Socket socket;
        public SocketReadListener(Socket s)
        {
            this.socket = s;
        }
        
        public void run()
        {
            try
            {
                InputStream in = socket.getInputStream();
                Writer out = new OutputStreamWriter(socket.getOutputStream());
                
                byte[] buffer = new byte[16*1024];
                
                while(true)
                {
                    int read = in.read(buffer);
                    if(read == -1)
                    {
                        synchronized (connectedPeers)
                        {
                            InetSocketAddress disconnectedPeer = new InetSocketAddress(socket
                                    .getInetAddress(), socket.getPort());
                            connectedPeers.remove(disconnectedPeer);
                            listener.disconnected(disconnectedPeer);
                            return;
                        }                        
                    }
                    
                    ByteArrayInputStream stream = new ByteArrayInputStream(buffer,0, read);
                    listener.readReady(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), stream, out);
                    out.flush();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }            
        }
    }

    public void run()
    {
        try
        {
            while (true)
            {
                Socket s = serverSocket.accept();
                addClient(s);
            }
        }
        catch (IOException ignore)
        {
            
        }
    }

    private synchronized void addClient(Socket socket) throws IOException
    {
        SocketReadListener l = new SocketReadListener(socket);
        connectedPeers.put(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), socket);
        l.start();
    }

    public void connect(InetSocketAddress peer)
    {
        try
        {
            Socket socket = new Socket(peer.getAddress(), peer.getPort());
            SocketReadListener l = new SocketReadListener(socket);
            connectedPeers.put(new InetSocketAddress(socket.getInetAddress(), socket.getPort()), socket);
            listener.connected(peer);
            l.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }        
    }

    public synchronized void send(InetSocketAddress peer, String message)
    {
        Socket s = connectedPeers.get(peer);
        try
        {
            s.getOutputStream().write(message.getBytes());
            s.getOutputStream().flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public HashMap<InetSocketAddress, Socket> connectedPeers()
    {
        return connectedPeers;
    }

}
