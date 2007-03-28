package test.filetransfer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.filetransfer.Connector;
import com.filetransfer.DefaultFileList;
import com.filetransfer.FileTransferListener;
import com.search.SearchId;
import com.search.SearchResult;

public class DownloadTest
{
    private StringWriter writer;
    private FileTransferListener downloader;
    private MockDownloadFileList mockList;
    private SearchResult r;
    private MockServer mock;
    
    @Before public void setUp()
    {
        writer = new StringWriter();
        mockList = new MockDownloadFileList();
        downloader = new FileTransferListener(mockList);
        r = createSearchResult();
        mock = new MockServer();
    }
    
    @Test public void connectToPeers()
    {  
        downloader.download(r, "hello.txt", mock);
        Assert.assertEquals(2, mock.count);
    }
    
    @Test public void initiateHandshake()
    {
        downloader.download(r, "hello.txt", mock);        
        Assert.assertEquals("get_pieces 0987654321098765432109876543210987654321\r\n", mock.lastMessage);
    }
    
    @Test public void regesterPartialFile()
    {
        downloader.download(r, "hello.txt", mock);        
        Assert.assertNotNull(mockList.getFile("0987654321098765432109876543210987654321"));
    }
    
    @Test public void requestPiecesOnceAvailabilityIsKnown()
    {
        downloader.download(r, "hello.txt", mock);
        downloader.readReady(r.peers.get(0), toStream("have 0987654321098765432109876543210987654321 1\r\n"), writer);
        
        Assert.assertEquals("get 1 0987654321098765432109876543210987654321\r\n", mock.lastMessage);
        Assert.assertEquals(r.peers.get(0), mock.lastPeer);
    }
    
    public static InputStream toStream(String s)
    {
    	return new ByteArrayInputStream(s.getBytes());
    }
    
    public class MockDownloadFileList extends DefaultFileList
    {
        
    }
    
    public SearchResult createSearchResult()
    {
        SearchId fileNameHash = SearchId.fromHex("0987654321098765432109876543210987654321");
        SearchId fileHash = SearchId.getRandomId();
        ArrayList<SearchId> chunkHashes = new ArrayList<SearchId>();
        for(int i = 0; i<4; i++)
        {
            chunkHashes.add(SearchId.getRandomId());
        }
        
        ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();
        for(int i = 0; i<2; i++)
        {
            peers.add(new InetSocketAddress("localhost", i+10));           
        }
        
        return new SearchResult(fileNameHash, fileHash, chunkHashes, 4*512*1024-100, 512*1024, peers);
    }
    
    public class MockServer implements Connector
    {
        public int count = 0;
        public String lastMessage;
        public InetSocketAddress lastPeer;
        
        public void connect(InetSocketAddress peer)
        {
            count+=1;
        }
        
        public void send(InetSocketAddress peer, String message)
        {
            lastMessage = message;
            lastPeer = peer;
        }
    }
}
