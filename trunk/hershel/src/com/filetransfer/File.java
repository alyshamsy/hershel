package com.filetransfer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import com.search.SearchResult;

public class File
{
    private java.io.File file;
    private int pieceSize;
    private int numberOfPieces;
    private int lastPiece;
    private ArrayList<Integer> availablePieces;
    public HashMap<Integer, ArrayList<InetSocketAddress>> missingPieces;

    public File(String filename, int pieceSize)
    {
        file = new java.io.File(filename);
        this.pieceSize = pieceSize;
        numberOfPieces = (int)(file.length()/pieceSize);
        if(numberOfPieces*pieceSize < file.length())
        {
            numberOfPieces+=1;
        }
        lastPiece = numberOfPieces - 1;
        availablePieces = new ArrayList<Integer>();
        for(int i = 0; i<numberOfPieces; i++)
        {
            availablePieces.add(i);
        }
        
        missingPieces = new HashMap<Integer, ArrayList<InetSocketAddress>>();
    }
    
    public String getName()
    {
        return file.getName();
    }
    
    public long length()
    {
        return file.length();
    }
    
    public int sizeOfPiece(int index)
    {
        if(index == lastPiece)
        {
            return (int)(length() - pieceSize*(numberOfPieces-1));
        }
        else
        {
            return pieceSize;
        }
    }
    
    public ArrayList<Integer> availablePieces()
    {
       return availablePieces;
    }

    public static File downloadingFile(String destinationName, SearchResult newFile)
    {
        return new File(destinationName, 42);
    }
}
