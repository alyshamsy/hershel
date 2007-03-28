package com.filetransfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.search.SearchResult;

public class FileTransferListener implements SocketEventListener
{
    private FileList list;

    private Connector connector;

    public FileTransferListener(FileList fileList)
    {
        list = fileList;
    }

    public void readReady(InetSocketAddress peer, InputStream message, Writer out)
    {
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(message));
            String header = in.readLine();
            String[] words = header.split("\\s");
            String command = words[0];
            if (command.equals("get_pieces"))
            {
                sendHave(out, words);
            }
            else if (command.equals("get"))
            {
                sendPiece(out, words);
            }
            else if (command.equals("have"))
            {
                updatePieceState(out, words, peer);
            }

            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void updatePieceState(Writer out, String[] words, InetSocketAddress peer) throws IOException
    {
        File file = list.getFile(words[1]);

        String[] indecies = words[2].split(",");
        for (String i : indecies)
        {
            int index = Integer.parseInt(i);
            if (!file.missingPieces.containsKey(index))
            {
                file.missingPieces.put(index, new ArrayList<InetSocketAddress>());
            }

            ArrayList<InetSocketAddress> peers = file.missingPieces.get(index);
            if (!peers.contains(peer))
            {
                peers.add(peer);
            }
        }

        requestNewPiece(words[1]);
    }

    private void requestNewPiece(String filenameHash)
    {
        File file = list.getFile(filenameHash);
        for(int piece : file.missingPieces.keySet())
        {
            if(!file.missingPieces.get(piece).isEmpty())
            {
                InetSocketAddress peer = file.missingPieces.get(piece).get(0);
                connector.send(peer, String.format("get %d %s\r\n", piece, filenameHash));
            }
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

        out.write("have " + pieces + "\r\n");
        out.write("get_pieces " + filename + "\r\n");
    }

    public void download(SearchResult newFile, String destinationName, Connector connector)
    {
        this.connector = connector;
        list.registerDownload(newFile, destinationName);
        for (InetSocketAddress peer : newFile.peers)
        {
            connector.connect(peer);
            connector.send(peer, "get_pieces " + newFile.fileNameHash.toString() + "\r\n");
        }
    }

	public void disconnected(InetSocketAddress peer)
	{
		for(String file : list.files())
		{
			File f = list.getFile(file);
			for(ArrayList<InetSocketAddress> peers: f.missingPieces.values())
			{
				peers.remove(peer);
			}
		}
	}
}
