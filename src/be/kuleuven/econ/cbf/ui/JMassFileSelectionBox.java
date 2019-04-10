package be.kuleuven.econ.cbf.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

public class JMassFileSelectionBox extends JPanel {
	private List<SingleFileSelector> files;
	private JButton addButton;
	private FileFilter filter;
	private List<ChangeListener> changeListeners;

	public JMassFileSelectionBox(FileFilter filter) {
		changeListeners = new ArrayList<ChangeListener>();
		this.filter = filter;
		this.setOpaque(false);
		files = new ArrayList<SingleFileSelector>();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addButton = new JButton("+ new");
		addFileSelector();
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				addFileSelector();
			}
		});
		this.add(addButton);
	}

	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	void addFileSelector() {
		addFileSelector("");
	}

	void addFileSelector(String file) {
		SingleFileSelector s = new SingleFileSelector(this, filter);
		files.add(s);
		this.remove(addButton);
		this.add(s);
		this.add(addButton);
		this.revalidate();
		s.setFile(file);
		fireChangeListeners(new ChangeEvent(this));
	}

	public String[] getFiles() {
		String[] output = new String[this.files.size()];
		for (int i = 0; i < output.length; i++)
			output[i] = files.get(i).getFile();
		return output;
	}

	public void setFiles(String[] files) {
		for (SingleFileSelector f : this.files)
			this.removeFileSelector(f);
		for (String f : files)
			this.addFileSelector(f);
		this.removeFileSelector(this.files.get(0));
	}

	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	void removeFileSelector(SingleFileSelector file) {
		this.remove(file);
		files.remove(file);
		if (files.isEmpty())
			addFileSelector();
		else
			this.revalidate();
		fireChangeListeners(new ChangeEvent(this));
	}

	boolean inhibit = false;

	void fireChangeListeners(ChangeEvent e) {
		if (inhibit)
			return;
		for (ChangeListener l : changeListeners)
			l.stateChanged(e);
	}
}

class SingleFileSelector extends JPanel {

	private JTextField textField;
	private JButton browseButton;
	private JButton removeButton;
	private FileFilter filter;
	private JMassFileSelectionBox parent;

	public SingleFileSelector(JMassFileSelectionBox parent, FileFilter filter) {
		this.filter = filter;
		this.parent = parent;
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		textField = new JTextField(1);
		browseButton = new JButton("...");
		removeButton = new JButton("X");
		removeButton.setToolTipText("Remove this file");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(textField);
		this.add(Box.createRigidArea(new Dimension(5, 1)));
		this.add(browseButton);
		this.add(Box.createRigidArea(new Dimension(5, 1)));
		this.add(removeButton);
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				UISettings.LINE_COMPONENT_HEIGHT_MAX));
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectFile();
			}

		});
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SingleFileSelector.this.parent
						.removeFileSelector(SingleFileSelector.this);
			}
		});
		textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				SingleFileSelector.this.parent
						.fireChangeListeners(new ChangeEvent(
								SingleFileSelector.this));
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				SingleFileSelector.this.parent
						.fireChangeListeners(new ChangeEvent(
								SingleFileSelector.this));
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				SingleFileSelector.this.parent
						.fireChangeListeners(new ChangeEvent(
								SingleFileSelector.this));
			}
		});
	}

	private void selectFile() {
		JFileChooser chooser = new JFileChooser(textField.getText());
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(
				(UIGlobals.lastLocation == null || !new File(UIGlobals.lastLocation).isDirectory()) 
				? null : new File(UIGlobals.lastLocation));
		int rValue = chooser.showDialog(null, "Select");
		parent.inhibit = true;
		if (rValue == JFileChooser.APPROVE_OPTION) {
			UIGlobals.lastLocation = chooser.getCurrentDirectory().getAbsolutePath();
			File[] files = chooser.getSelectedFiles();
			if (files.length == 0)
				return;
			textField.setText(files[0].getAbsolutePath());
			for (int i = 1; i < files.length; i++)
				parent.addFileSelector(files[i].getAbsolutePath());
		}
		parent.inhibit = false;
		parent.fireChangeListeners(new ChangeEvent(this));
	}

	public void setFile(String file) {
		textField.setText(file);
		parent.fireChangeListeners(new ChangeEvent(this));
	}

	public String getFile() {
		return textField.getText();
	}
}