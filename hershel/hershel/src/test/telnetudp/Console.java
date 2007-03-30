package test.telnetudp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console
{
    public static void main(String[] argv) throws IOException
    {
        String host = argv[0];
        int port = Integer.parseInt(argv[1]);
        int listenPort = Integer.parseInt(argv[2]);
        
        UdpServer server = new UdpServer(listenPort);
        server.start();
        
        UdpClient client = new UdpClient(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input;
        while((input = in.readLine()) != null)
        {
             if(input.equals("/quit"))
             {
                 System.exit(0);
             }
             
             client.send(input);
        }
    }
}
