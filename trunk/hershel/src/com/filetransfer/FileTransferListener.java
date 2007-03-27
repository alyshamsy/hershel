package com.filetransfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetSocketAddress;

public class FileTransferListener implements SocketEventListener
{   
    private FileList list;
      
    public FileTransferListener(FileList fileList)
    {   
        list = fileList;
    }   

    public void readReady(InetSocketAddress peer, String message, Writer out)
    {
        try
        {
            BufferedReader in = new BufferedReader(new StringReader(message));
            String header = in.readLine();
            String[] words = header.split("\\s");
            String command = words[0];
            if(command.equals("get_pieces"))
            { 
                sendHave(out, words);
            }
            else if(command.equals("get"))
            {
                sendPiece(out, words);
            }
            
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendPiece(Writer out, String[] words) throws IOException
    {      
        Piece piece = list.getPiece(words[2], Integer.parseInt(words[1]));
        out.write(String.format("piece %s %s %d\r\n", words[1], words[2], piece.data.length));
        out.write(new String(piece.data));
    }

    private void sendHave(Writer out, String[] words) throws IOException
    {
        String filename = words[1];
        String pieces = null;
        for (Integer i : list.getFile(filename).availablePieces())
        {
            pieces = pieces == null ? i.toString() : pieces + "," + i.toString();
        }

        out.write("have " + pieces+"\r\n");
        out.write("get_pieces " + filename+"\r\n");
    }
}
