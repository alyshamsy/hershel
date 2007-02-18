package com.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SearchMessage
{
    
    private String command;
    private Map<String, String> arguments;
    
    public SearchMessage(String command)
    {
       this.command = command;
       arguments = new HashMap<String, String>();
    }
    
    public String toString()
    {
        Message encoder = new Message(command);
        for(Entry<String, String> argument : arguments.entrySet())
        {
            encoder.getArguments().add(argument.getKey());
            encoder.getArguments().add(argument.getValue());
        }
        return encoder.toString();
    }

    public static SearchMessage parse(String encodedMessage)
    {
       Message decoder = Message.parse(encodedMessage);
       SearchMessage result = new SearchMessage(decoder.getCmd());
       if(decoder.getArguments().size() % 2 ==0)
       {
           for(int i = 0; i<decoder.getArguments().size(); i+=2)
           {
               result.arguments().put(decoder.getArguments().get(i), decoder.getArguments().get(i+1));
           }
       }
       return result;
    }

    public String getCommand()
    {
        return command;
    }

    public Map<String, String> arguments()
    {
        return arguments;
    }

    public byte[] getBytes()
    {
        return toString().getBytes();
    }

}
