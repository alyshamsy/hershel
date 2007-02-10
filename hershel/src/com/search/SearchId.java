package com.search;

import java.util.Random;

//Test
public class SearchId
{
    private byte[] id = new byte[20];
    private SearchId()
    {
        Random r = new Random();
        r.nextBytes(id);
    }
    
    public String toString()
    {
        return SHA1Utils.digestToHexString(id);
    }
    
    public static SearchId getRandomId()
    {
        return new SearchId();
    }

}
