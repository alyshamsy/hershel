package com.filetransfer;

import java.io.InputStream;
import java.io.Writer;
import java.net.InetSocketAddress;

public interface SocketEventListener
{

    public abstract void readReady(InetSocketAddress peer, InputStream message, Writer writer);

	public void disconnected(InetSocketAddress peer);

}