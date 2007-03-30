package test.telnetudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UdpClient
{
    private DatagramSocket socket;
    private InetAddress destination;
    private int port;
    
    public UdpClient(String host, int port) throws SocketException, UnknownHostException
    {
        socket = new DatagramSocket();
        destination = InetAddress.getByName(host);
        this.port = port;
    }
    
    public void send(String message) throws IOException
    {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, destination, port);
        socket.send(packet);
    }
}
