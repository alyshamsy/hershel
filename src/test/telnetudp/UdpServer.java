package test.telnetudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer extends Thread
{
    private DatagramSocket socket;
    private boolean running = true;
    
    public UdpServer(int  port) throws SocketException
    {
        socket = new DatagramSocket(port);
    }
    
    public void run()
    {
        while(running)
        {
            try
            {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                int length = 0;
                for(; length< data.length; length++)
                {
                    if(data[length] == 0)
                        break;
                }
                String message = new String(packet.getData(), 0, length);
                System.out.printf("From %s: %s\n", packet.getAddress().toString(), message);
            } 
            catch (IOException e)
            {
               System.out.println(e.getMessage());
            }            
        }
    }
    
}
