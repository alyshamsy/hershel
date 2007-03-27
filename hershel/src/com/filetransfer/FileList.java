package com.filetransfer;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileList
{

    public Piece getPiece(String file, int index) throws FileNotFoundException, IOException;

    public File getFile(String filenameHash);

}