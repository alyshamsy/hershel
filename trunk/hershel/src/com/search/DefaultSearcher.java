package com.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class DefaultSearcher extends Thread implements Searcher
{

	private class SearchStatus
	{
		boolean searchFailed;
		int attemptsLeft;

		SearchStatus()
		{
			searchFailed = true;
			attemptsLeft = nodesToSearch;
		}
	}

	private int alpha = 3;
	private boolean running = true;
	private RoutingTable table;
	private SearchClient client;
	private SearchId id;
	private int nodesToSearch;
	private HashMap<SearchId, SearchStatus> searchesInProgress;

	public DefaultSearcher(RoutingTable table, SearchClient client, SearchId id)
	{
		this.table = table;
		this.client = client;
		this.id = id;
		searchesInProgress = new HashMap<SearchId, SearchStatus>();
		nodesToSearch = 10;
	}

	public DefaultSearcher(RoutingTable table, SearchClient client, SearchId id, int alpha)
	{
		this(table, client, id);
		this.alpha = alpha;
	}

	public synchronized void run()
	{
		while (running) {
			try {
				wait();
				for (Entry<SearchId, SearchStatus> e : searchesInProgress.entrySet()) {
					SearchStatus ss = e.getValue();					
					if ((ss.attemptsLeft > 0) && (ss.searchFailed == true)) {
						SearchId fileName = e.getKey();
						try {
							sendSearchRequest(fileName);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					} else if (ss.attemptsLeft <= 0) {
						searchesInProgress.remove(e.getKey());
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void close()
	{
		running = false;
	}

	public synchronized void putSearchRequest(SearchId fileName) throws IOException
	{
		searchesInProgress.put(fileName, new SearchStatus());
		sendSearchRequest(fileName);
	}

	public synchronized void searchSuccessful(SearchId fileName)
	{
		searchesInProgress.remove(fileName);
	}

	public synchronized void searchFailed(SearchId fileName) throws IOException
	{
		SearchStatus ss = searchesInProgress.get(fileName);
		if (ss == null) return;
		ss.searchFailed = true;
		notify();
	}

	private void sendSearchRequest(SearchId fileName) throws IOException
	{
		List nodes = table.findNode(fileName);
		SearchMessage request = new SearchMessage("find_value");
		request.arguments().put("id", id.toString());
		request.arguments().put("file_name", fileName.toString());

		int messagesSent = 0;
		for (int i = 0; i < alpha; i++)
		{
			if (i >= nodes.size()) break;
			client.sendMessage(request, (NodeState)(nodes.get(i)));
			messagesSent+=1;
		}

		SearchStatus ss = searchesInProgress.get(fileName);
		ss.attemptsLeft = ss.attemptsLeft - messagesSent;
		ss.searchFailed = false;
		searchesInProgress.put(fileName, ss);
	}

}
