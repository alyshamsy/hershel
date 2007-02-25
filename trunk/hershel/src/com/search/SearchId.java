package com.search;

import java.math.BigInteger;
import java.util.Random;

//Test again
public class SearchId
{
    private byte[] id = new byte[20];
    private SearchId()
    {
        Random r = new Random();
        r.nextBytes(id);
    }
    
    public SearchId(String id)
    {
        if(id.getBytes().length != 20)
        	throw new IllegalArgumentException("Search id must be 20 bytes long");
        
        this.id = id.getBytes();
    }

    public String toString()
    {
        return new String(id);
    }
    
    public static SearchId getRandomId()
    {
        return new SearchId();
    }

    public static byte[] getDistance(SearchId n1, SearchId n2)
    {
    	BigInteger a = new BigInteger(n1.id);
    	BigInteger b = new BigInteger(n2.id);

    	return (a.xor(b)).toByteArray();
    }

}
