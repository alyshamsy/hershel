package com.filetransfer;

import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;

public interface SocketEventListener
{

    public abstract void readReady(InetSocketAddress peer, Reader reader, Writer writer);

}