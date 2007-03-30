package com.filetransfer;

import java.net.InetSocketAddress;

public interface Connector
{

    public void connect(InetSocketAddress peer);

    public void send(InetSocketAddress peer, String message);

}