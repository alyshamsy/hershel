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

	private final int CONCURRENT_SEARCHES = 3;
	private boolean running;
	private RoutingTable table;
	private SearchClient client;
	private int nodesToSearch;
	private HashMap<SearchId, SearchStatus> searchesInProgress;

	public DefaultSearcher(RoutingTable table, SearchClient client)
	{
		this.table = table;
		this.client = client;
		searchesInProgress = new HashMap<SearchId, SearchStatus>();
		nodesToSearch = 10;
	}

	public DefaultSearcher(RoutingTable table, SearchClient client, int nodesToSearch)
	{
		this.table = table;
		this.client = client;
		searchesInProgress = new HashMap<SearchId, SearchStatus>();
		this.nodesToSearch = nodesToSearch;
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
		ss.searchFailed = true;
		notify();
	}

	private void sendSearchRequest(SearchId fileName) throws IOException
	{
		List nodes = table.findNode(fileName);
		SearchMessage request = new SearchMessage("find_value");
		request.arguments().put("file_name", fileName.toString());

		for (int i = 0; i < CONCURRENT_SEARCHES; i++)
		{
			if (i >= nodes.size()) break;
			client.sendMessage(request, (NodeState)(nodes.get(i)));
		}

		SearchStatus ss = searchesInProgress.get(fileName);
		ss.attemptsLeft = ss.attemptsLeft - CONCURRENT_SEARCHES;
		ss.searchFailed = false;
		searchesInProgress.put(fileName, ss);
	}

}
