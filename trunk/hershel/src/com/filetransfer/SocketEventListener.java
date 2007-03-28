package com.filetransfer;

import java.io.Writer;
import java.net.InetSocketAddress;

public interface SocketEventListener
{

    public abstract void readReady(InetSocketAddress peer, String message, Writer writer);

	public void disconnected(InetSocketAddress peer);

}