package com.search;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.filetransfer.DefaultFileList;
import com.filetransfer.File;
import com.filetransfer.FileTransferListener;
import com.filetransfer.FileTransferServer;
import com.shadanan.P2PMonitor.IRemote;
import com.shadanan.P2PMonitor.MonitorService;

public class SearchGUI implements GUI, IRemote
{

    private static final boolean debug = true;

    private static String initialId = "1234567890123456789012345678901234567890";

    private MonitorService ms;
    private NetworkSearchClient client;
    private int port;
    private FileTransferListener listener;
    private FileTransferServer fileTransferServer;

    public SearchGUI() throws IOException
    {
        port = 10000;
        client = new NetworkSearchClient(initialId, 10000);
        ms = new MonitorService(10001, this, new InetSocketAddress(InetAddress.getByName("localhost"), 10000));
        client.registerUI(this);
        ms.start();
        client.getHandler().routingTable().setMonitorService(ms);
        client.start();

        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        results.add(createSearchResult("wrnpc11.txt"));
        results.add(createSearchResult("chekhov.txt"));
        client.initializeDatabase(results);

        DefaultFileList list = new DefaultFileList();
        list.register("wrnpc11.txt", 16 * 1024);
        list.register("chekhov.txt", 16 * 1024);
        listener = new FileTransferListener(list);
        fileTransferServer = new FileTransferServer(port + 6000, listener);
        fileTransferServer.start();
    }

    public SearchGUI(int port) throws IOException
    {
        this.port = port;
        String randomId = SearchId.getRandomId().toString();
        client = new NetworkSearchClient(randomId, port);
        ms = new MonitorService(port + 1, this, new InetSocketAddress(InetAddress.getByName("localhost"),
                port));
        client.registerUI(this);
        ms.start();
        client.getHandler().routingTable().setMonitorService(ms);
        client.start();

        DefaultFileList list = new DefaultFileList();
        listener = new FileTransferListener(list);
        fileTransferServer = new FileTransferServer(port + 6000, listener);
        fileTransferServer.start();
    }

    public static SearchResult createSearchResult(String fileName)
    {
        //SearchId fileNameHash = SearchId.fromHex("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f");
    	SearchId fileNameHash = new SearchId(SHA1Utils.getSHA1Digest(
    			fileName.getBytes()));
        SearchId fileHash = SearchId.getRandomId();
        ArrayList<SearchId> chunkHashes = new ArrayList<SearchId>();
        for (int i = 0; i < 4; i++)
        {
            chunkHashes.add(SearchId.getRandomId());
        }

        ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();

        peers.add(new InetSocketAddress("localhost", 16000));

        com.filetransfer.File f = new File(fileName, 16 * 1024);
        //3284807 - wrnpc11.txt file size

        return new SearchResult(fileName, fileNameHash, fileHash,
        		chunkHashes, f.length(), 16 * 1024, peers);
    }

    public void getMessage(String s)
    {
        if (debug)
            ms.println(s);
    }

    public void addContact(InetSocketAddress peer)
    {
        // TODO Auto-generated method stub

    }

    public void close()
    {

    }

    public HashMap<String, String> getInfo()
    {
        // TODO Auto-generated method stub
        return new HashMap<String, String>();
    }

    public int getLayerCount()
    {
        return 1;
    }

    public InetSocketAddress getLocalAddress()
    {
        try
        {
            return new InetSocketAddress(InetAddress.getByName("localhost"), port);
        }
        catch (UnknownHostException e)
        {
            return null;
        }
    }

    public InetSocketAddress[] getPeers(int layer)
    {
        ArrayList<ArrayList<NodeState>> rt = client.getHandler().routingTable().getRoutingTable();
        ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();
        for (int i = 0; i < 160; i++)
        {
            ArrayList<NodeState> kbucket = rt.get(i);
            for (NodeState n : kbucket)
            {
                peers.add(new InetSocketAddress(n.address, n.port));
            }
        }

        return peers.toArray(new InetSocketAddress[0]);
    }

    public void message(String text)
    {
        String[] command = text.split(" ");
        MessageHandler h = client.getHandler();
        if (command[0].equalsIgnoreCase("search"))
        {
            try
            {
                SearchId file = new SearchId(SHA1Utils.getSHA1Digest(command[1].getBytes()));
                h.findValue(file);
            }
            catch (IOException ex)
            {
                ms.println("! Searching error.\n");
            }
        }
        else if (command[0].equalsIgnoreCase("download"))
        {
            try
            {
                SearchId file = new SearchId(SHA1Utils.getSHA1Digest(command[1].getBytes()));
                SearchResult result = h.database().get(file);
                client.updateDatabase(result.fileNameHash, new InetSocketAddress("localhost", port+6000));
                String outputFileName = (command[2] != null) ? command[2] : "output.txt";
                listener.download(result, outputFileName, fileTransferServer);
            }
            catch (Exception ex)
            {
                ms.println("! Downloading error.\n");
            }
        }
        else if (command[0].equalsIgnoreCase("help"))
        {
            ms.println("? P2P Commands:\n");
            ms.println("? search <filename>\n");
            ms.println("?    - search for <filename>\n");
            ms.println("? download <filename> <save as>\n");
            ms.println("?    - begin download for <filename>\n");
            ms.println("?    - save file as <save as>\n");
            ms.println("?    - you should run 'find' first\n");
            ms.println("? help\n");
            ms.println("?    - this listing\n");
        }
        else if (command[0].equalsIgnoreCase("start_ui"))
        {
        	//new SearchWindow("My Console");
        }
        else
        {
            ms.println("! Invalid command. " + "Type 'help' for a list of commands.\n");
        }
    }

}

class SearchWindow extends JFrame {

	SearchWindow(String title) {
		super(title);
		setContentPane(createContentPane());
		setSize(640, 480);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setVisible(true);
	}

	private JPanel createContentPane() {
		JPanel pane = new JPanel();

		return pane;
	}

}
