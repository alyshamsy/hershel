package com.filetransfer;

public class FileList
{
    public Piece getPiece(String file, int index)
    {
        byte[] data = new byte[10];
        for(int i = 0; i< 10; i++)
        {
            data[i] = (byte)(i+'0');
        }
        
        return new Piece(data);
    }
}   
