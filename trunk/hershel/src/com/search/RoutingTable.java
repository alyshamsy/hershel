package com.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoutingTable
{

    private SearchId self;
    private ArrayList<ArrayList<NodeState>> table;
    private int K = 20;
    private Pinger pinger;

    public RoutingTable(SearchId self, int k, Pinger pinger)
    {
        this.pinger = pinger;
        pinger.setRoutingTable(this);
        K = k;
        this.self = self;
        table = new ArrayList<ArrayList<NodeState>>(160);        
        for (int i = 0; i < 160; i++)
            table.add(new ArrayList<NodeState>());
    }    

    public ArrayList<ArrayList<NodeState>> getRoutingTable()
    {
        return table;
    }

    public synchronized void addNode(NodeState node) throws IOException
    {
        int index = findIndex(node.id);
        if (index < 0)
            throw new Error();

        ArrayList<NodeState> kBucket = table.get(index);
        for (NodeState n : kBucket)
        {
        	if (node.equals(n)) return;
        }

        if (kBucket.size() < K)
        {
            kBucket.add(node);
        }
        else
        {
            pinger.putPingRequest(kBucket.get(0), node);
        }
    }

    public synchronized List findNode(SearchId nodeId)
    {
        int index = findIndex(nodeId);
        if (index < 0)
            throw new Error();

        ArrayList<NodeState> nodes = new ArrayList<NodeState>();
        int i = index, j = 0;
        while (nodes.size() < K)
        {
        	i += (j % 2 == 0) ? j : -j;
            j++;
            if (i >= 160 || i < 0)
                break;

            for (NodeState n : table.get(index))
            {
                if (nodes.size() < K)
                    nodes.add(n);
                else
                    break;
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
        int index = 0;
        byte[] distance = SearchId.getDistance(self, newNode);

        for (int i = 159; i >= 0; i--)
        {
        	if ((mask & distance[index]) == mask) return i;

        	mask >>>= 1;
            if (mask == 0)
            {
            	mask = 0x80;
            	index++;
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

}
