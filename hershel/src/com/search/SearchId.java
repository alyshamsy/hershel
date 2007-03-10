package com.search;

import java.util.Arrays;
import java.util.Random;

//Test again
public class SearchId
{
    public byte[] id = new byte[20];
    private SearchId()
    {
        Random r = new Random(2);       
        r.nextBytes(id);
    }
    
    private SearchId(byte[] id)
    {
        this.id = id;
    }    
    
    public String toString()
    {  
        String result = "";
        for(byte b : id)
        {
            result += String.format("%02x", b);
        }
        return result;
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof SearchId)
        {
            SearchId other = (SearchId)o;
            return Arrays.equals(this.id, other.id);
        }
        return false;
    }
    
    public int hashCode()
    {
        // TODO add a better hash code to SearchId
        return 1;
    }
    
    public static SearchId getRandomId()
    {
        return new SearchId();
    }

    public static byte[] getDistance(SearchId n1, SearchId n2)
    {
    	byte[] a = n1.id;
    	byte[] b = n2.id;
    	byte[] result = new byte[20];

    	for (int i = 0; i < 20; i++)
    	{
    		result[i] = (byte)(a[i] ^ b[i]);
    	}

    	return result;
    }

    public static SearchId fromHex(String hexString)
    {       
        if(hexString.length() != 40) throw new IllegalArgumentException("Hex string must be 40 characters long");
        byte[] bts = new byte[20];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hexString.substring(2*i, 2*i+2), 16);
        }
        
        return new SearchId(bts);
    }

}
