package test.filetransfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.filetransfer.DefaultFileList;
import com.search.SearchId;
import com.search.SearchResult;

public class FileListTest
{
    private DefaultFileList list;
    
    @Before public void setUp()
    {
    	 list = new DefaultFileList();
    	 list.register("wrnpc11.txt", 11);
    }

	@Test public void calculateHashOfFileName()
    {     
        Assert.assertEquals("wrnpc11.txt", list.getFile("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f").getName());
    }
    
    @Test public void readPiece() throws IOException
    {
         Assert.assertEquals("The Project", new String(list.getPiece("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f", 0).data));
    }
    
    @Test public void writePiece() throws IOException
    {
    	java.io.File f = new java.io.File("output.txt");
    	if(f.exists())
    		f.delete();
    	
    	SearchResult r = createSearchResult();
    	list.registerDownload(r, "output.txt");
    	list.writePiece(r.fileNameHash.toString(), 0, "1234567890".getBytes("ISO-8859-1"));
    	list.writePiece(r.fileNameHash.toString(), 1, "1234567890".getBytes("ISO-8859-1"));
    	f = new java.io.File("output.txt");
    	Assert.assertEquals(52l, f.length());
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
        
        return new SearchResult(fileNameHash, fileHash, chunkHashes, 4*512*1024-100, 10, peers);
    }
}
