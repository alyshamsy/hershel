package com.search;

import java.io.IOException;

public interface Searcher
{
	void putSearchRequest(SearchId fileName) throws IOException;
	void searchSuccessful(SearchId fileName);
	void searchFailed(SearchId fileName) throws IOException;
	void close();
}
