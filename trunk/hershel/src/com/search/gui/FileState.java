package com.search.gui;

import com.search.SearchResult;

class FileState {

	String fileName;
	int numPeers;
	boolean downloaded;

	FileState(SearchResult sr) {
		fileName = sr.filename;
		numPeers = sr.peers.size();
		downloaded = false;
	}

	void setEntryAtColumn(Object o, int col) {
		switch (col) {
		case 0:
			fileName = (String)o;
			break;
		case 1:
			numPeers = ((Integer)o).intValue();
			break;
		case 2:
			downloaded = ((Boolean)o).booleanValue();
			break;
		default:
			break;
		}
	}

	Object getEntryAtColumn(int col) {
		switch (col) {
		case 0:
			return fileName;
		case 1:
			return numPeers;
		case 2:
			return downloaded;
		default:
			return null;
		}
	}

}
