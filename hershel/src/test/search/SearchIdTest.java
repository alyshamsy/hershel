package test.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.search.SearchId;


public class SearchIdTest
{
    @Test public void lengthMustBe20()
    {
        SearchId id = SearchId.fromHex("1234567890123456789012345678901234567890");
        assertEquals(20, id.id.length);
    }
    
    @Test public void fromHexString()
    {
        SearchId id = SearchId.fromHex("1234567890123456789012345678901234567890");
        assertEquals((byte)0x34, id.id[18]);
    }
    
    @Test public void reverseHexString()
    {
    	SearchId reverse = SearchId.fromReverseHex("4fc57a6c84e3a94cec839a613627cadde574d94d");
    	assertEquals(SearchId.fromHex("4dd974e5ddca2736619a83ec4ca9e3846c7ac54f"), reverse);
    }
    
    @Test public void strangeString()
    {
        assertEquals("?", new String(new byte[] {63}));
    }
    
    @Test public void equalsFromString()
    {
        SearchId id = SearchId.getRandomId();
        SearchId copy = SearchId.fromHex(id.toString());
        assertEquals(copy, id);
    }   
}
