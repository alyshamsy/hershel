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

    public SearchMessage createMessage(String message)
    {
        SearchMessage storeMessage = new SearchMessage(message);
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
    
    public boolean equals(Object o)
    {
        if(o instanceof SearchResult)
        {
            SearchResult other = (SearchResult)o;
            
            return fileNameHash.equals(other.fileNameHash) && fileSize == fileSize && other.fileHash.equals(fileHash)
                   && chunkHashes.equals(other.chunkHashes) && peers.equals(other.peers);
        }
        return false;
    }

    public static SearchResult fromMessage(SearchMessage message)
    {
        if(!message.getCommand().equals("store")) 
            throw new IllegalArgumentException("Expected a store command not "  + message.getCommand());
        
        SearchId fileName = SearchId.fromHex(message.arguments().get("file_name"));
        SearchId fileHash = SearchId.fromHex(message.arguments().get("file"));
        long fileSize = Long.parseLong(message.arguments().get("file_size"));
        ArrayList<SearchId> chunks = new ArrayList<SearchId>();
        for(int i = 0; i<message.arguments().get("chunks").length(); i+=40)
        {
            chunks.add(SearchId.fromHex(message.arguments().get("chunks").substring(i, i+40)));
        }
        
        ArrayList<InetSocketAddress> peers = new ArrayList<InetSocketAddress>();
        String[] addresses = message.arguments().get("peers").split(";");
        for(String address : addresses)
        {
            peers.add(new InetSocketAddress(address.substring(0, address.indexOf(':')), 
                    Integer.parseInt(address.substring(address.indexOf(':')+1))));
        }
        
        return new SearchResult(fileName, fileHash, chunks, fileSize, peers);
    }

}
