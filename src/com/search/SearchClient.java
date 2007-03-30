package com.search;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface SearchClient
{

    public abstract void sendMessage(SearchMessage message, NodeState destination) throws IOException;

    public abstract void close();

    void sendToUI(String s, String direction);

}