package test.filetransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import com.filetransfer.File;
import com.filetransfer.FileList;
import com.filetransfer.Piece;
import com.search.SearchResult;

public class MockFileList implements FileList
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

    public File getFile(String filenameHash)
    {
        return new MockFile();
    }
    
    public class MockFile extends File
    {
        @Override
        public ArrayList<Integer> availablePieces()
        {
            return pieces;
        }

        private ArrayList<Integer> pieces;

        public MockFile()
        {
            super("hello", 1);    
            pieces(3);
        }
        
        private ArrayList<Integer> pieces(int number) {
            pieces = new ArrayList<Integer>();
            for(int i = 0; i<number; i++)
                pieces.add(i);
            return pieces;
        }
        
    }

    public void registerDownload(SearchResult newFile, String destinationName)
    {
        // TODO Auto-generated method stub
        
    }

	public Set<String> files() {
		// TODO Auto-generated method stub
		return null;
	}

    public void writePiece(String filenameHash, int piece, byte[] data) throws IOException
    {
        // TODO Auto-generated method stub
        
    }
}   
