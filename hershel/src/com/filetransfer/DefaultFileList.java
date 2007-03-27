package com.filetransfer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import com.search.SHA1Utils;
import com.search.SearchId;

public class DefaultFileList implements FileList
{
    private HashMap<String, File> trackedFiles;
    
    public DefaultFileList()
    {
        trackedFiles = new HashMap<String, File>();
    }
    
    public Piece getPiece(String filenameHash, int index) throws IOException
    {
        File file = getFile(filenameHash);
        RandomAccessFile in = new RandomAccessFile(file.getName(), "r");
        in.seek(index*file.length());
        byte[] data = new byte[file.sizeOfPiece(index)];
        in.read(data);
        in.close();
        
        return new Piece(data);
    }

    public void register(String filename, int pieceSize)
    {
       SearchId hash = new SearchId(SHA1Utils.getSHA1Digest(filename.getBytes()));      
       trackedFiles.put(hash.toString(), new File(filename, pieceSize));    
    }    

    public File getFile(String filenameHash)
    {
        return trackedFiles.get(filenameHash);
    }

}
