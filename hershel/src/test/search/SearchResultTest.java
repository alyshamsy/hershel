package test.search;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.search.SearchId;
import com.search.SearchResult;

public class SearchResultTest
{
    private SearchId fileNameHash;
    private SearchId fileHash;
    private ArrayList<SearchId> chunkHashes;
    private ArrayList<InetSocketAddress> peers;
    private SearchResult r;
    
    @Before public void setUp()
    {
        fileNameHash = SearchId.getRandomId();
        fileHash = SearchId.getRandomId();
        chunkHashes = new ArrayList<SearchId>();
        peers = new ArrayList<InetSocketAddress>();        
        
        for(int i = 0; i<4; i++)
        {
            chunkHashes.add(SearchId.getRandomId());
        }
        
        for(int i = 0; i<4; i++)
        {
            peers.add(new InetSocketAddress("localhost", i+10));           
        }
        
        r = new SearchResult(fileNameHash, fileHash, chunkHashes, 4*512*1024-100, peers);
    }

    @Test public void generateMessageWithStoreCommand()
    {            
        assertEquals("store", r.createMessage("store").getCommand());
    }
    
    @Test public void messageHasFileName()
    {
        assertEquals(fileNameHash.toString(), r.createMessage("store").arguments().get("file_name"));
    }
    
    @Test public void messageHasHashOfTheFile()
    {
        assertEquals(fileHash.toString(), r.createMessage("store").arguments().get("file"));
    }
    
    @Test public void messageHasFileSize()
    {
        assertEquals(4l*512l*1024l-100l, Long.parseLong(r.createMessage("store").arguments().get("file_size")));
    }
    
    @Test public void messageHasHashOfEachChunk()
    {
        String expected = "";
        for(SearchId id : chunkHashes)
        {
            expected += id.toString();
        }
        
        assertEquals(expected, r.createMessage("store").arguments().get("chunks"));
    }
    
    @Test public void messageHasPeers()
    {
        assertEquals("127.0.0.1:10;127.0.0.1:11;127.0.0.1:12;127.0.0.1:13", r.createMessage("store").arguments().get("peers"));        
    }
    
    @Test public void resultCreatedFromMessage()
    {
        SearchResult copy = SearchResult.fromMessage(r.createMessage("store"));
        assertEquals(r, copy);
    }
}
