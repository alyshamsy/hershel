package com.search;

import java.io.IOException;

public interface Pinger
{ 
    void putPingRequest(NodeState targetNode, NodeState replacementNode) throws IOException;

    void setRoutingTable(RoutingTable table);

    void setTimeout(int millis);

    void pingReceived(SearchId id);

    public void close();

    boolean expected(SearchId id);
}
