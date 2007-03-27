package com.filetransfer;

import java.io.IOException;

public class FileUploader
{
    public static void main(String[] args) throws IOException
    {       
        DefaultFileList list = new DefaultFileList();
        list.register("wrnpc11.txt", 16*1024);
        FileTransferListener listener = new FileTransferListener(list);
        FileTransferServer server = new FileTransferServer(10000, listener);
        server.start();
    }
}
