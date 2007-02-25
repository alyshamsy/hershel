package com.search;

import java.net.InetAddress;

public class NodeState
{
    public NodeState(String id, InetAddress address, int port)
    {
       this.id = new SearchId(id);
       this.address = address;
       this.port = port;
    }

    public NodeState(SearchId id, InetAddress address, int port)
    {
    	this.id = id;
        this.address = address;
        this.port = port;
    }

    public SearchId id;
    public InetAddress address;
    public int port;
}
