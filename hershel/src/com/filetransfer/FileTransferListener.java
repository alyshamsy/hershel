package com.filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import com.search.SearchResult;

public class FileTransferListener implements SocketEventListener
{
    private FileList list;

    private Connector connector;

    private int concurentPieces = 1;

    private int inProgress = 0;

    private class PartialMessage
    {
        public byte[] alreadyRead;
        public int remain;
        private String file;
        private int piece;

        public PartialMessage(byte[] alreadyRead, int remain, String file, int piece)
        {
            this.alreadyRead = alreadyRead;
            this.remain = remain;
            this.file = file;
            this.piece = piece;
        }
    }

    private HashMap<InetSocketAddress, PartialMessage> partialMessages = new HashMap<InetSocketAddress, PartialMessage>();

    public FileTransferListener(FileList fileList)
    {
        list = fileList;
    }

    public synchronized void  readReady(InetSocketAddress peer, InputStream message, Writer out)
    {
        try
        {
            if (partialMessages.containsKey(peer))
            {
                finishPartialMessage(peer, message);
            }

            int c = 0;
            while (c != -1)
            {
                StringBuilder header = new StringBuilder();
                while ((c = message.read()) != -1)
                {
                    if (c == '\r')
                    {
                        message.read();
                        break;
                    }
                    header.append((char) c);
                }
                // System.out.println(header);
                String[] words = header.toString().split("\\s");
                String command = words[0];
                if (command.equals("get_pieces"))
                {
                    sendHave(out, words, peer);
                }
                else if (command.equals("get"))
                {
                    sendPiece(out, words);
                }
                else if (command.equals("have"))
                {
                    updatePieceState(out, words, peer);
                }
                else if (command.equals("piece"))
                {
                    receivePiece(peer, words, message);
                }

                out.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void finishPartialMessage(InetSocketAddress peer, InputStream message) throws IOException
    {
        PartialMessage m = partialMessages.get(peer);
        int read = message.read(m.alreadyRead, m.alreadyRead.length - m.remain, m.remain);
        if (read < m.remain)
        {
            m.remain -= read;
        }
        else
        {
            savePiece(m.piece, m.file, m.alreadyRead);
            partialMessages.remove(peer);
            requestNewPiece(m.file);
        }
    }

    private void receivePiece(InetSocketAddress peer, String[] words, InputStream message) throws IOException
    {
        int piece = Integer.parseInt(words[1]);
        //System.out.println(piece);
        String file = words[2];
        int expectedLength = Integer.parseInt(words[3]);

        byte[] data = new byte[expectedLength];

        int read = message.read(data);
        if (read < expectedLength)
        {
            partialMessages.put(peer, new PartialMessage(data, expectedLength - read, file, piece));
        }
        else
        {
            savePiece(piece, file, data);
            requestNewPiece(file);
        }
    }

    private void savePiece(int piece, String filenameHash, byte[] data) throws IOException
    {
        list.writePiece(filenameHash, piece, data);
        File file = list.getFile(filenameHash);
        file.missingPieces.remove(piece);
        file.availablePieces().add(piece);
        inProgress-=1;
        System.out.println("Got " + piece);
        System.out.println(file.missingPieces.size() + " remain");
    }

    private void updatePieceState(Writer out, String[] words, InetSocketAddress peer) throws IOException
    {
        if (words.length >= 3)
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
    }

    private void requestNewPiece(String filenameHash)
    {
        File file = list.getFile(filenameHash);
        for (int piece : file.missingPieces.keySet())
        {
            if (inProgress >= concurentPieces)
            {
                return;
            }
            if (!file.missingPieces.get(piece).isEmpty())
            {
                InetSocketAddress peer = file.missingPieces.get(piece).get(0);
                connector.send(peer, String.format("get %d %s\r\n", piece, filenameHash));
                System.out.println("Requested "+piece);
                inProgress += 1;
            }            
        }
    }

    private void sendPiece(Writer out, String[] words) throws IOException
    {
        Piece piece = list.getPiece(words[2], Integer.parseInt(words[1]));
        out.write(String.format("piece %s %s %d\r\n", words[1], words[2], piece.data.length));
        out.write(new String(piece.data));
    }

    private void sendHave(Writer out, String[] words, InetSocketAddress peer) throws IOException
    {
        String filename = words[1];
        String pieces = "";
        if (list.files().contains(filename))
        {
            for (Integer i : list.getFile(filename).availablePieces())
            {
                pieces = pieces.equals("") ? i.toString() : pieces + "," + i.toString();
            }
            out.write("have " + filename + " " + pieces + "\r\n");
        }

    }

    public void download(SearchResult newFile, String destinationName, Connector connector)
    {
        this.connector = connector;
        list.registerDownload(newFile, destinationName);
        for (InetSocketAddress peer : newFile.peers)
        {
            connector.connect(peer);
        }
    }

    public void disconnected(InetSocketAddress peer)
    {
        for (String file : list.files())
        {
            File f = list.getFile(file);
            for (ArrayList<InetSocketAddress> peers : f.missingPieces.values())
            {
                peers.remove(peer);
            }
        }
    }

    public void connected(InetSocketAddress peer)
    {
        for (String file : list.files())
        {
            connector.send(peer, "get_pieces " + file + "\r\n");
        }
    }
}
