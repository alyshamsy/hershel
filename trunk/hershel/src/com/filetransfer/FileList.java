package com.filetransfer;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.search.SearchResult;

public interface FileList
{

    public Piece getPiece(String file, int index) throws FileNotFoundException, IOException;

    public File getFile(String filenameHash);

    public void registerDownload(SearchResult newFile, String destinationName);

}