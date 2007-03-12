package test.search;

import java.io.IOException;

import com.search.NodeState;
import com.search.SearchClient;
import com.search.SearchMessage;

public class MockSearchClient implements SearchClient
{

    public SearchMessage lastMessage;

    public NodeState lastDestination;

    public void close()
    {

    }

    public void sendMessage(SearchMessage message, NodeState destination) throws IOException
    {
        lastMessage = message;
        lastDestination = destination;
    }

}