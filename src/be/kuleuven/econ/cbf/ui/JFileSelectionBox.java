package be.kuleuven.econ.cbf.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

class JFileSelectionBox extends JPanel {
	private JTextField textField;
	private JButton browseButton;
	private FileFilter filter;
	private List<ChangeListener> changeListeners;

	public JFileSelectionBox(FileFilter filter) {
		changeListeners = new ArrayList<ChangeListener>();
		this.filter = filter;
		this.setOpaque(false);
		textField = new JTextField(1);
		browseButton = new JButton("...");
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(textField);
		this.add(Box.createRigidArea(new Dimension(5, 1)));
		this.add(browseButton);
		textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
				UISettings.LINE_COMPONENT_HEIGHT_MAX));
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				fireChangeListeners(new ChangeEvent(JFileSelectionBox.this));
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				fireChangeListeners(new ChangeEvent(JFileSelectionBox.this));
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				fireChangeListeners(new ChangeEvent(JFileSelectionBox.this));
			}
		});
	}

	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	private void selectFile() {
		JFileChooser chooser = new JFileChooser(textField.getText());
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(
				(UIGlobals.lastLocation == null || !new File(UIGlobals.lastLocation).isDirectory()) 
				? null : new File(UIGlobals.lastLocation));
		int rValue = chooser.showDialog(null, "Select");
		inhibit = true;
		if (rValue == JFileChooser.APPROVE_OPTION) {
			textField.setText(chooser.getSelectedFile().getAbsolutePath());
			UIGlobals.lastLocation = chooser.getCurrentDirectory().getAbsolutePath();
		}
		inhibit = false;
		fireChangeListeners(new ChangeEvent(this));
	}

	public void setFile(String file) {
		textField.setText(file);
	}

	public String getFile() {
		return textField.getText();
	}

	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	boolean inhibit = false;

	private void fireChangeListeners(ChangeEvent e) {
		if (inhibit)
			return;
		for (ChangeListener l : changeListeners)
			l.stateChanged(e);
	}
}