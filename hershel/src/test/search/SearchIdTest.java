package test.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.search.SearchId;


public class SearchIdTest
{
    @Test public void lengthMustBe20()
    {
        SearchId id = SearchId.fromString("12345678901234567890");
        assertEquals(20, id.id.length);
    }
    
    @Test public void fromHexString()
    {
        SearchId id = SearchId.fromHex("1234567890123456789012345678901234567890");
        assertEquals((byte)0x34, id.id[1]);
    }
}
