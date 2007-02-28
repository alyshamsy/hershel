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
    
    public boolean equals(Object o)
    {
        if(o instanceof NodeState)
        {
            NodeState other = (NodeState)o;
            return other.id.equals(this.id) && other.address.equals(this.address) && other.port == this.port;
        }
        return false;
    }

    public SearchId id;
    public InetAddress address;
    public int port;
}
