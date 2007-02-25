package test.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.search.SearchId;
import com.search.SearchMessage;


public class TestSearchMessage
{
    @Test public void decodeCommandName()
    {
       SearchMessage message = new SearchMessage("greeting");      
       SearchMessage decoded = SearchMessage.parse(message.toString());
       assertEquals("greeting", decoded.getCommand());
    }
    
    @Test public void decodeArguments()
    {
        SearchMessage message = new SearchMessage("greeting");
        message.arguments().put("text", "hello world");
        message.arguments().put("name", "Jason");
        SearchMessage decoded = SearchMessage.parse(message.toString());
        assertEquals(2, decoded.arguments().size());
        assertEquals("hello world", decoded.arguments().get("text"));
        assertEquals("Jason", decoded.arguments().get("name"));
    }   
    
}
