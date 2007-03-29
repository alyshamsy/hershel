package com.filetransfer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.search.SearchId;
import com.search.SearchResult;

public class FileUploader
{
    public static void main(String[] args) throws IOException, InterruptedException
    {       
        java.io.File f = new java.io.File("output.txt");
        if(f.exists())
            f.delete();
        
        DefaultFileList list = new DefaultFileList();
        list.register("wrnpc11.txt", 1024);
        FileTransferListener listener = new FileTransferListener(list);
        FileTransferServer server = new FileTransferServer(10000, listener);
        server.start();
        
        DefaultFileList list2 = new DefaultFileList();        
        FileTransferListener listener2 = new FileTransferListener(list2);
        FileTransferServer server2 = new FileTransferServer(10001, listener2);
        server2.start();
        listener2.download(createSearchResult(), "output.txt", server2);
    }
    
    public static SearchResult createSearchResult()
    {
        SearchId fileNameHash = SearchId.fromHex("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f");
        SearchId fileHash = SearchId.getRandomId();
        ArrayList<SearchId> chunkHashes = new ArrayList<SearchId>();
        for(int i = 0; i<4; i++)
        {
            chunkHashes.add(SearchId.getRandomId());
        }
        
        ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();
       
        peers.add(new InetSocketAddress("localhost", 10000));           
       
        
        return new SearchResult("sample.txt", fileNameHash, fileHash, chunkHashes, 3284807, 1024, peers);
    }
}
