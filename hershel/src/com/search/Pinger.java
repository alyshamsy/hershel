package com.search;

public interface Pinger
{ 
    void putPingRequest(NodeState targetNode, NodeState replacementNode);

    void setRoutingTable(RoutingTable table);

    void setTimeout(int millis);

    void pingReceived(SearchId id);
}
