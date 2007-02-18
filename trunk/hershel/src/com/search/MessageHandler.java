package com.search;

public class MessageHandler
{
    private SearchId myId;
    
    public MessageHandler(SearchId myId)
    {
        this.myId = myId;
    }
    public SearchMessage respondTo(SearchMessage pingMessage)
    {
        SearchMessage response = new SearchMessage("ping");
        response.arguments().put("id", myId.toString());
        return response;
    }

}
