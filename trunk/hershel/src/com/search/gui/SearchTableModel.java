package com.search.gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

class SearchTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private static final String[] columnNames =
		{"File", "Peers", "Downloaded"};
	private ArrayList<FileState> entries;

	SearchTableModel() {
		entries = new ArrayList<FileState>();
	}

	void addSearchResult(FileState fs) {
		for (FileState f : entries) {
			if (f.fileName.equals(fs.fileName)) {
				f.numPeers = fs.numPeers;
				return;
			}
		}

		entries.add(fs);
	}

	void clear() {
		entries.clear();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return entries.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public FileState getRow(int row) {
		return entries.get(row);
	}

	public Object getValueAt(int row, int col) {
		return entries.get(row).getEntryAtColumn(col);
	}

	public void setValueAt(Object o, int row, int col) {
		entries.get(row).setEntryAtColumn(o, col);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

}
