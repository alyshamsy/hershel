package com.filetransfer;

import java.net.Socket;

public interface SocketEventListener
{

    public abstract void readReady(Socket s);

}