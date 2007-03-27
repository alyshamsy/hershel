package com.filetransfer;

import java.util.ArrayList;

public class File
{
    private java.io.File file;
    private int pieceSize;
    private int numberOfPieces;
    private int lastPiece;
    private ArrayList<Integer> availablePieces;

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
}
