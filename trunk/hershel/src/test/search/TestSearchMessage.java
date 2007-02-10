package test.search;

import org.junit.Test;

import com.search.SearchMessage;

import static org.junit.Assert.*;


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
        message.arguments().put("name", "Maxim");
        SearchMessage decoded = SearchMessage.parse(message.toString());
        assertEquals(2, decoded.arguments().size());
        assertEquals("hello world", decoded.arguments().get("text"));
        assertEquals("Maxim", decoded.arguments().get("name"));
    }
}
