package com.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.shadanan.P2PMonitor.MonitorService;

public class RoutingTable
{

    private SearchId self;
    private ArrayList<ArrayList<NodeState>> table;
    private int K = 20;
    private Pinger pinger;
    private MonitorService ms;

    public RoutingTable(SearchId self, int k, Pinger pinger, MonitorService ms)
    {
        this.pinger = pinger;
        pinger.setRoutingTable(this);
        K = k;
        this.self = self;
        table = new ArrayList<ArrayList<NodeState>>(160);        
        for (int i = 0; i < 160; i++)
            table.add(new ArrayList<NodeState>());
        this.ms = ms;
    }    

    public RoutingTable(SearchId self, int k, Pinger pinger)
    {
    	this(self, k, pinger, null);
    }

    public ArrayList<ArrayList<NodeState>> getRoutingTable()
    {
        return table;
    }

    public synchronized void addNode(NodeState node) throws IOException
    {
        int index = findIndex(node.id);
        if (index < 0)
            return;

        ArrayList<NodeState> kBucket = table.get(index);
        if(kBucket.contains(node)) return;

        if (kBucket.size() < K)
        {
            kBucket.add(node);
        }
        else
        {
            pinger.putPingRequest(kBucket.get(0), node);
        }

        if (ms != null) ms.notifyNewPeers(0);
    }

    public synchronized ArrayList<NodeState> findNode(SearchId nodeId)
    {
    	ArrayList<NodeState> nodes = new ArrayList<NodeState>();
        int index = findIndex(nodeId);
        if (index < 0)
            return nodes;
        
        int i = index, j = 0;
        boolean searchRight = true, searchLeft = true;;
        while ((searchRight || searchLeft) && nodes.size() < K)
        {
        	i += (j % 2 == 0) ? j : -j;        	
            j++;
            if (i >= 160)
            {
            	searchRight = false;
                continue;
            }
            if (i < 0)
            {
            	searchLeft = false;
            	continue;
            }

            for (NodeState n : table.get(i))
            {
                if (nodes.size() < K)
                    nodes.add(n);
                else break;
            }
        }

        return nodes;
    }

    /*
     * A node belongs to a k-bucket if its distance falls between 2^i and
     * 2^(i+1), where i is the index of the k-bucket.
     */
    private int findIndex(SearchId newNode)
    {
        int mask = 0x80;
        int index = 19;
        byte[] distance = SearchId.getDistance(self, newNode);

        for (int i = 159; i >= 0; i--)
        {
        	if ((mask & distance[index]) == mask) return i;

        	mask >>>= 1;
            if (mask == 0)
            {
            	mask = 0x80;
            	index--;
            }
        }

        return -1;
    }

    public synchronized void pingResponded(NodeState nodePinged)
    {
        int kbucket = findIndex(nodePinged.id);
        table.get(kbucket).remove(nodePinged);
        table.get(kbucket).add(nodePinged);        
    }

    public synchronized void pingTimedOut(NodeState nodePinged, NodeState replacement)
    {
        int kbucket = findIndex(nodePinged.id);
        table.get(kbucket).remove(nodePinged);
        table.get(kbucket).add(replacement);      
        
    }

    public void setPinger(Pinger newPinger)
    {
         pinger.close();
         this.pinger = newPinger;        
    }

    public void setMonitorService(MonitorService ms)
    {
    	this.ms = ms;
    }

}
