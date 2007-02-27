package com.search;

import java.util.ArrayList;


public class DefaultPinger extends Thread implements Pinger
{
    private class PendingPing
    {
        public NodeState target;
        public long timePlaced;
        public NodeState replacement;

        public PendingPing(NodeState target, NodeState replacement)
        {
            this.target = target;
            this.timePlaced = System.currentTimeMillis();
            this.replacement = replacement;
        }
    }

    private PingCommunicator comm;
    private RoutingTable table;
    private int timeout = 1000;
    private boolean running = true;
    
    private ArrayList<PendingPing> pendingPings;
    
    public DefaultPinger(PingCommunicator comm)
    {
        this.comm = comm;
        pendingPings = new ArrayList<PendingPing>();
    }

    public synchronized void putPingRequest(NodeState targetNode, NodeState replacementNode)
    {
        for(PendingPing p : pendingPings)
        {
            if(p.target.id.equals(targetNode.id))
                return;
        }
        comm.ping(targetNode);
        pendingPings.add(new PendingPing(targetNode, replacementNode));
    }

    public void setRoutingTable(RoutingTable table)
    {
        this.table = table;
    }

    public synchronized void pingReceived(SearchId id)
    {
        for(PendingPing n : pendingPings)
        {
            if(n.target.id.equals(id))
            {
                table.pingResponded(n.target);
                pendingPings.remove(n);
                break;
            }
        }
    }
    
    public synchronized void run()
    {
        try
        {           
            while(running)
            {
                wait(timeout);
                ArrayList<PendingPing> expired = new ArrayList<PendingPing>();
                for(PendingPing p : pendingPings)
                {
                    if(System.currentTimeMillis() - p.timePlaced > timeout)
                    {
                        table.pingTimedOut(p.target, p.replacement);
                        expired.add(p);
                    }
                }
                
                pendingPings.removeAll(expired);
            }
        }
        catch (InterruptedException e)
        {            
            e.printStackTrace();
        }
    }

    public synchronized void setTimeout(int millis)
    {
        this.timeout = millis;
        notify();        
    }

    public void close()
    {
        running = false;        
    }   

}
