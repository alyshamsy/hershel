package com.search;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a great little encapsulator. It will take plain text commands
 * with parameters and encode them into a single line of text. You can then use
 * the parse method to retrieve the command and the parameters that were
 * originally passed to it.
 * 
 * @author Shad Sharma
 */
public class Message
{
    private String cmd;

    private List<String> data;

    /**
     * This is the default constructor. You probably don't want to use this
     * constructor. It is used internally by the parse method which is the one
     * you'd likely want to use.
     */
    private Message()
    {
        cmd = null;
        data = new ArrayList<String>();
    }

    /**
     * This method parses a line of text that was encoded by this class's
     * toString() method.
     * 
     * @param input
     *            The input line of text to parse.
     * @return A Message object with command and data parameters.
     */
    public static Message parse(String input)
    {
        Message result = new Message();

        int indexOfFirstSpace = input.indexOf(" ");
        if (indexOfFirstSpace == -1)
        {
            result.cmd = input;
        }
        else
        {
            result.cmd = input.substring(0, indexOfFirstSpace);
            String[] temp = input.substring(indexOfFirstSpace + 1).split("\\s|\\x00");
            result.data = new ArrayList<String>();
            for (int i = 0; i < temp.length; i++)
            {
                try
                {
                    result.data.add(URLDecoder.decode(temp[i], "UTF-8"));
                }
                catch (UnsupportedEncodingException e)
                {
                }
            }
        }

        return result;
    }

    /**
     * Creates a new Message object with a command and no parameters.
     * 
     * @param cmd
     *            The command to encode.
     */
    public Message(String cmd)
    {
        this.cmd = cmd;
        this.data = new ArrayList<String>();
    }

    /**
     * Creates a new Message object with a command and a single parameter.
     * 
     * @param cmd
     *            The command to encode.
     * @param data
     *            A single data parameter to encode.
     */
    public Message(String cmd, String data)
    {
        this.cmd = cmd;
        this.data = new ArrayList<String>();
        this.data.add(data);
    }

    /**
     * Creates a new Message object with a command and several parameters.
     * 
     * @param cmd
     *            The command to encode.
     * @param data
     *            An array of String parameters to encode.
     */
    public Message(String cmd, String[] data)
    {
        this.cmd = cmd;
        this.data = new ArrayList<String>();
        for(String argument : data)
        {
            this.data.add(argument);
        }
    }

    /**
     * Compares this Message objects' command field with the supplied parameter.
     * The comparison is case sensitive.
     * 
     * @param cmd
     *            The command to compare with.
     * @return true if the command field matches the given String and false
     *         otherwise.
     */
    public boolean cmdEquals(String cmd)
    {
        return (this.cmd.equals(cmd));
    }

    /**
     * Returns the command field.
     * 
     * @return The command field.
     */
    public String getCmd()
    {
        return cmd;
    }

    /**
     * This method will return the first parameter, or null if no parameter was
     * originally encoded.
     * 
     * @return The first parameter of the Message, or null if no parameter
     *         exists.
     */
    public String getData()
    {
        return getData(0);
    }

    /**
     * This method will return the index'th parameter, or null if the parameter
     * doesn't exist. You can use the countDataTokens() method to determine the
     * number of parameters.
     * 
     * @param index
     *            The index'th parameter in this Message object, or null if out
     *            of bounds.
     * @return The index'th parameter in this Message object.
     */
    public String getData(int index)
    {
        if (data == null || data.isEmpty())
            return null;
        return data.get(index);
    }
    
    public List<String> getArguments()
    {
        return data;
    }

    /**
     * Returns the number of parameters in this Message object.
     * 
     * @return The number of parameters in the Message obejct.
     */
    public int countDataTokens()
    {
        if (data == null)
            return 0;
        return data.size();
    }

    /**
     * This method prepares the Message object to be sent, encoding incompatible
     * characters using UTF-8. Because the toString() method is overridden to
     * provide this support, if you are sending a Message object over a socket,
     * you don't have to directly call the toString() method. You can simply
     * write: out.println(myMessageObject);
     * 
     * On the other side of the socket, you have to parse the message as
     * follows: String input = in.readLine(); Message.parse(input);
     * 
     * Note: input should not be null when calling parse.
     */
    public String toString()
    {
        String result = "";
        if (cmd != null)
            result += cmd;
        if (data != null)
        {
            for (int i = 0; i < data.size() && data.get(i) != null; i++)
            {
                try
                {
                    result += " " + URLEncoder.encode(data.get(i), "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                }
            }
        }
        return result;
    }
}
