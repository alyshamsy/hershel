package test.filetransfer;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.filetransfer.DefaultFileList;

public class FileListTest
{
    @Test public void calculateHashOfFileName()
    {
        DefaultFileList list = new DefaultFileList();
        list.register("wrnpc11.txt", 11);
        Assert.assertEquals("wrnpc11.txt", list.getFile("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f").getName());
    }
    
    @Test public void readPiece() throws IOException
    {
        DefaultFileList list = new DefaultFileList();
        list.register("wrnpc11.txt", 11);
        Assert.assertEquals("The Project", new String(list.getPiece("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f", 0).data));
    }
}
