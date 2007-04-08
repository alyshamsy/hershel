package com.search.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jvnet.substance.SubstanceLookAndFeel;

import com.filetransfer.FileTransferListener;
import com.search.NetworkSearchClient;
import com.search.SHA1Utils;
import com.search.SearchId;
import com.search.SearchResult;
import com.shadanan.P2PMonitor.IRemote;

public class SearchWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField fileNameBox = null;

	private JTextField saveAsBox = null;

	private JButton searchButton = null;

	private JButton downloadButton = null;

	private JButton clearButton = null;

	private JScrollPane jScrollPane = null;

	private JTable results = null;

	private IRemote searchGUI;

	private NetworkSearchClient client;

	private FileTransferListener ftl;

	private JProgressBar downloadProgressBar = null;

	private JPanel downloadPanel = null;

	private int fileToDownload = 0;

	private ArrayList<String> filesDownloaded = null;

	/**
	 * This is the default constructor
	 */
	public SearchWindow(NetworkSearchClient client, IRemote r, FileTransferListener l) {
		super();
		filesDownloaded = new ArrayList<String>();
		this.client = client;
		searchGUI = r;
		ftl = l;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			// Use default.
		}

		setSize(300, 250);
		setContentPane(getJContentPane());
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new BorderLayout());
		jContentPane.add(getSearchPanel(), BorderLayout.NORTH);
		jContentPane.add(getInfoPanel(), BorderLayout.CENTER);

		jContentPane.add(getDownloadPanel(), BorderLayout.SOUTH);
		return jContentPane;
	}

	/**
	 * This method initializes fileNameBox
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getFileNameBox() {
		if (fileNameBox == null) {
			fileNameBox = new JTextField();
			fileNameBox.setColumns(18);
			fileNameBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					sendSearchMessage();
				}

			});
		}
		return fileNameBox;
	}

	/**
	 * This method initializes searchButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getSearchButton() {
		if (searchButton == null) {
			searchButton = new JButton();
			searchButton.setText("Search");
			searchButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					sendSearchMessage();
				}
				
			});
		}
		return searchButton;
	}

	private void sendSearchMessage() {
		String fileName = fileNameBox.getText();
		searchGUI.message("search " + fileName);

		try {
			Thread.sleep(200);
		} catch (InterruptedException ignored) {}

		SearchId fileNameHash =
			new SearchId(SHA1Utils.getSHA1Digest(fileName.getBytes()));
		SearchResult r = client.getHandler().database().get(fileNameHash);

		SearchTableModel stm = (SearchTableModel)(results.getModel());
		FileState fs = new FileState(r);
		stm.addSearchResult(fs);
		for (String s : filesDownloaded) {
			if (s.equals(fs.fileName)) {
				fs.downloaded = true;
			}
		}
		results.updateUI();
		ftl.registerProgressBar(downloadProgressBar);
	}

	/**
	 * This method initializes searchPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getSearchPanel() {
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new FlowLayout());
		searchPanel.add(getFileNameBox(), null);
		searchPanel.add(getSearchButton(), null);

		return searchPanel;
	}

	/**
	 * This method initializes buttonPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(getDownloadButton(), null);
		buttonPanel.add(getClearButton(), null);

		return buttonPanel;
	}

	/**
	 * This method initializes downloadButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDownloadButton() {
		if (downloadButton == null) {
			downloadButton = new JButton();
			downloadButton.setText("Start Download");
			downloadButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					sendDownloadMessage();
				}

			});
		}
		return downloadButton;
	}

	private void sendDownloadMessage() {
		SearchTableModel stm =
			(SearchTableModel)(results.getModel());
		FileState targetFile = stm.getRow(fileToDownload);

		if (!targetFile.downloaded) {
			searchGUI.message("download " + targetFile.fileName +
				" " + saveAsBox.getText());
		}
	}

	/**
	 * This method initializes clearButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getClearButton() {
		if (clearButton == null) {
			clearButton = new JButton();
			clearButton.setText("Clear");
			clearButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					((SearchTableModel)(results.getModel())).clear();
					results.updateUI();
				}

			});
		}
		return clearButton;
	}

	/**
	 * This method initializes infoPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getInfoPanel() {
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.add(getSavePanel(), BorderLayout.SOUTH);

		infoPanel.add(getJScrollPane(), BorderLayout.CENTER);
		return infoPanel;
	}

	/**
	 * This method initializes savePanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getSavePanel() {
		JPanel savePanel = new JPanel();
		savePanel.setLayout(new FlowLayout());
		savePanel.add(new JLabel("Save As:"), null);
		savePanel.add(getSaveAsBox(), null);

		return savePanel;
	}

	/**
	 * This method initializes saveAsBox
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getSaveAsBox() {
		if (saveAsBox == null) {
			saveAsBox = new JTextField();
			saveAsBox.setColumns(20);
			saveAsBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					sendDownloadMessage();
				}

			});
		}
		return saveAsBox;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getResults());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes results	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getResults() {
		if (results == null) {
			results = new JTable(new SearchTableModel());
			results.setShowGrid(false);
			results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			results.getColumnModel().getColumn(0).setPreferredWidth(150);

			ListSelectionModel rowSM = results.getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
			    public void valueChanged(ListSelectionEvent e) {
			        //Ignore extra messages.
			        if (e.getValueIsAdjusting()) return;

			        ListSelectionModel lsm =
			            (ListSelectionModel)e.getSource();
			        if (lsm.isSelectionEmpty()) {
			            return;
			        } else {
			            fileToDownload = lsm.getMinSelectionIndex();
			            SearchTableModel model =
							(SearchTableModel)(results.getModel());
						FileState targetFile = model.getRow(fileToDownload);
						if (targetFile.downloaded) {
							downloadProgressBar.setValue(100);
							results.updateUI();
						} else {
							downloadProgressBar.setValue(0);
							results.updateUI();
						}
			        }
			    }
			});
		}
		return results;
	}

	/**
	 * This method initializes downloadProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getDownloadProgressBar() {
		if (downloadProgressBar == null) {
			downloadProgressBar = new JProgressBar();
			downloadProgressBar.setStringPainted(true);
			downloadProgressBar.addPropertyChangeListener(new PropertyChangeListener() {

				public void propertyChange(PropertyChangeEvent e) {
					if (downloadProgressBar.getValue() == 100) {
						SearchTableModel model =
							(SearchTableModel)(results.getModel());
						FileState targetFile = model.getRow(fileToDownload);

						targetFile.downloaded = true;
						filesDownloaded.add(targetFile.fileName);
						results.updateUI();
					}
				}
				
			});
		}
		return downloadProgressBar;
	}

	/**
	 * This method initializes downloadPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDownloadPanel() {
		if (downloadPanel == null) {
			downloadPanel = new JPanel();
			downloadPanel.setLayout(new BorderLayout());
			downloadPanel.add(getButtonPanel(), BorderLayout.NORTH);
			downloadPanel.add(getDownloadProgressBar(), BorderLayout.SOUTH);
		}
		return downloadPanel;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
