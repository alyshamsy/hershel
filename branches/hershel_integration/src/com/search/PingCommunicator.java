package com.search;

import java.io.IOException;

public interface PingCommunicator
{

    void ping(NodeState targetNode) throws IOException;

}
