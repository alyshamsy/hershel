package com.search;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Random;

//Test again
public class SearchId
{
    public byte[] id = new byte[20];
    private SearchId()
    {
        Random r = new Random();
        r.nextBytes(id);
    }
    
    private SearchId(byte[] id)
    {
        this.id = id;
    }
    
    public SearchId(String id)
    {
        if(id.getBytes().length != 20)
        	throw new IllegalArgumentException("Search id must be 20 bytes long");
        
        this.id = id.getBytes();
    }
    
    public static SearchId fromString(String id)
    {
        if(id.getBytes().length != 20)
            throw new IllegalArgumentException("Search id must be 20 bytes long");
        byte[] buffer = new byte[20];
        Charset utf = Charset.forName("UTF-8");
        System.arraycopy(utf.encode(id).array(), 0, buffer, 0, 20) ;
        return new SearchId(buffer);
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

    public static SearchId fromHex(String hexString)
    {       
        byte[] bts = new byte[20];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hexString.substring(2*i, 2*i+2), 16);
        }
        
        return new SearchId(bts);
    }

}
