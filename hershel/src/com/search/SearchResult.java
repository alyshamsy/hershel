package com.search;

import java.net.InetSocketAddress;
import java.util.ArrayList;


public class SearchResult
{
    public SearchId fileNameHash, fileHash;
    public long fileSize;
    public ArrayList<SearchId> chunkHashes;
    public ArrayList<InetSocketAddress> peers;
    
    public SearchResult(SearchId fileNameHash, SearchId fileHash, ArrayList<SearchId> chunkHashes,
            long fileSize, ArrayList<InetSocketAddress> peers)
    {
        this.fileNameHash = fileNameHash;
        this.fileHash = fileHash;
        this.fileSize = fileSize;
        this.chunkHashes = chunkHashes;
        this.peers = peers;
    }

    public SearchMessage storeMessage()
    {
        SearchMessage storeMessage = new SearchMessage("store");
        storeMessage.arguments().put("file_name", fileNameHash.toString());
        storeMessage.arguments().put("file", fileHash.toString());
        storeMessage.arguments().put("file_size", String.valueOf(fileSize));
        String chunks = "";
        for(SearchId id : chunkHashes)
        {
            chunks += id.toString();
        }
        storeMessage.arguments().put("chunks", chunks);
        
        String peerList = "";
        for(InetSocketAddress peer : peers)
        {
            peerList += String.format("%s:%d;", peer.getAddress().getHostAddress(), peer.getPort());
        }
        peerList = peerList.substring(0, peerList.lastIndexOf(';'));
        
        storeMessage.arguments().put("peers", peerList);
        return storeMessage;
    }

}
