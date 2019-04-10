package be.kuleuven.econ.cbf.ui;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ERROR_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_OK_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_WARNING_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.utils.LogTool;
import be.kuleuven.econ.cbf.utils.PetrinetTool;

public class InputConfigurationPanel extends JPanel {

	private static final long serialVersionUID = -3953022280744267314L;
	private JPanel newLogPanel;
	private List<LogPanel> logs;

	public InputConfigurationPanel(InputSet input) {
		if (input == null)
			input = new InputSet();
		logs = new ArrayList<LogPanel>();
		this.setBackground(COLOUR_BACKGROUND);
		this.setBorder(BorderFactory.createEmptyBorder(UISettings.MARGIN,
				UISettings.MARGIN, UISettings.MARGIN, UISettings.MARGIN));
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		initNewLogPanel();
		this.add(newLogPanel);
		if (input.size() == 0)
			addLog();
		else {
			ArrayList<Mapping> todo = new ArrayList<Mapping>();
			todo.addAll(input);
			while (todo.size() > 0) {
				ArrayList<Mapping> now = new ArrayList<Mapping>();
				String path = todo.get(0).getLogPath();
				for (int i = 0; i < todo.size(); i++)
					if (todo.get(i).getLogPath().equals(path)) {
						now.add(todo.get(i));
						todo.remove(i);
						i--;
					}
				addLog(now);
			}
		}
	}

	private void initNewLogPanel() {
		newLogPanel = new JPanel();
		newLogPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		newLogPanel.setBackground(COLOUR_ITEM_BACKGROUND);
		newLogPanel.setLayout(new BoxLayout(newLogPanel, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("add log");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		newLogPanel.add(Box.createVerticalStrut(UISettings.MARGIN));
		newLogPanel.add(label);
		newLogPanel.add(Box.createVerticalStrut(UISettings.MARGIN));
		newLogPanel.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				addLog();
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}

	private void addLog(ArrayList<Mapping> m) {
		Component box = Box.createVerticalStrut(UISettings.MARGIN);
		LogPanel panel = new LogPanel(this, box, m);
		logs.add(panel);
		this.remove(newLogPanel);
		this.add(panel);
		panel.revalidate();
		this.add(box);
		this.add(newLogPanel);
		this.revalidate();
	}

	private void addLog() {
		addLog(new ArrayList<Mapping>());
	}

	void removeLog(LogPanel panel) {
		this.remove(panel);
		logs.remove(panel);
		this.remove(panel.getBox());
		if (logs.size() == 0)
			addLog();
		else
			this.revalidate();
	}

	public InputSet getInputSet() {
		InputSet set = new InputSet();
		for (LogPanel l : logs)
			set.addAll(l.getMappings());
		return set;
	}

	public boolean isComplete() {
		for (LogPanel p : logs)
			if (!p.isComplete())
				return false;
		return true;
	}
}

class LogPanel extends JPanel {

	private JPanel left;
	private JPanel right;
	private InputConfigurationPanel icp;
	private Component box;
	private JLabel mappingStatus;
	private Map<String, Mapping> mappings;
	private String logfile;
	private JFileSelectionBox logFileSelector;
	private JMassFileSelectionBox modelFilesSelector;
	private JButton showMapping;
	private boolean complete;

	public LogPanel(InputConfigurationPanel icp, Component box) {
		this(icp, box, new ArrayList<Mapping>());
	}

	public boolean isComplete() {
		return complete;
	}

	public LogPanel(InputConfigurationPanel icp, Component box,
			List<Mapping> maps) {
		complete = false;
		mappings = new TreeMap<String, Mapping>();
		List<String> models = new ArrayList<String>();
		if (maps.size() > 0) {
			logfile = maps.get(0).getLogPath();
			for (int i = 0; i < maps.size(); i++) {
				models.add(maps.get(i).getPetrinetPath());
				mappings.put(maps.get(i).getPetrinetPath(), maps.get(i));
			}
		} else
			logfile = "";
		this.icp = icp;
		this.box = box;
		this.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.setBackground(COLOUR_ITEM_BACKGROUND);
		this.setLayout(new ColumnLayout(2, MARGIN));
		this.setBorder(BorderFactory.createEmptyBorder(MARGIN + 2, MARGIN,
				MARGIN, MARGIN));
		initLeft(MARGIN);
		initRight(models);
		this.add(left);
		this.add(right);
		initRemove();
		changeModels();
	}

	public Component getBox() {
		return box;
	}

	private void initLeft(int margin) {
		left = new JPanel();
		left.setAlignmentY(Component.TOP_ALIGNMENT);
		left.setOpaque(false);
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

		JLabel label = new JLabel("Select the logfile:");
		JPanel labelWrap = new JPanel();
		labelWrap.setLayout(new BoxLayout(labelWrap, BoxLayout.X_AXIS));
		labelWrap.setOpaque(false);
		labelWrap.add(label);
		labelWrap.add(Box.createHorizontalGlue());
		left.add(labelWrap);

		logFileSelector = new JFileSelectionBox(new FileNameExtensionFilter(
				"Log file (*.mxml, *.xes)", "mxml", "xes"));
		left.add(logFileSelector);
		logFileSelector.setFile(logfile);

		left.add(Box.createVerticalStrut(margin));

		showMapping = new JButton("model mapping");
		JPanel showMappingWrap = new JPanel();
		showMappingWrap.setOpaque(false);
		showMappingWrap.setLayout(new BoxLayout(showMappingWrap,
				BoxLayout.X_AXIS));
		showMappingWrap.add(Box.createRigidArea(new Dimension(50, 0)));
		showMappingWrap.add(showMapping);

		mappingStatus = new JLabel();
		showMappingWrap.add(Box.createRigidArea(new Dimension(10, 0)));
		showMappingWrap.add(mappingStatus);
		showMappingWrap.add(Box.createHorizontalGlue());
		
		left.add(showMappingWrap);

		logFileSelector.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				changeLogFile();
			}
		});
		setMatchingUnmapped();
		showMapping.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						doMapping();
					}
				});
				t.start();
			}
		});
	}

	private void initRight(List<String> models) {
		right = new JPanel();
		right.setAlignmentY(Component.TOP_ALIGNMENT);
		right.setOpaque(false);
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("Select the corresponding model(s):");
		JPanel labelWrap = new JPanel();
		labelWrap.setOpaque(false);
		labelWrap.setLayout(new BoxLayout(labelWrap, BoxLayout.X_AXIS));
		labelWrap.add(label);
		labelWrap.add(Box.createHorizontalGlue());
		right.add(labelWrap);
		modelFilesSelector = new JMassFileSelectionBox(
				new FileNameExtensionFilter("Petrinet Model (*.pnml)", "pnml"));
		modelFilesSelector.setFiles(models.toArray(new String[models.size()]));
		right.add(modelFilesSelector);
		modelFilesSelector.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				changeModels();
			}
		});
	}

	private void initRemove() {
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				boolean x = getWidth() - arg0.getX() <= 16;
				boolean y = arg0.getY() <= 16;
				if (x && y) {
					arg0.consume();
					icp.removeLog(LogPanel.this);
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(new Color(193, 21, 2));
		g2.fillOval(this.getWidth() - 16, 0, 16, 16);
		// g2.fillRect(this.getWidth() - 16, 0, 16, 16);
		g2.fillRect(this.getWidth() - 16, 0, 16, 8);
		g2.fillRect(this.getWidth() - 8, 0, 8, 16);
		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		g2.drawLine(this.getWidth() - 5, 5, this.getWidth() - 11, 11);
		g2.drawLine(this.getWidth() - 5, 11, this.getWidth() - 11, 5);
	}

	// called if the logfile has been changed
	private void changeLogFile() {
		if (logfile.equals(logFileSelector.getFile()))
			return;
		setMatchingUnmapped();
		mappings.clear();
		logfile = logFileSelector.getFile();
		if (!LogTool.isLogFile(logfile))
			JOptionPane.showMessageDialog(this,
					"The file you have selected cannot be used as logfile.",
					"Error", JOptionPane.ERROR_MESSAGE);
	}

	// called if any model is changed
	private void changeModels() {
		// 0 => nothing, 1 => partial, 2 => complete
		short mapping = 0;
		boolean allMapped = true;
		for (String file : modelFilesSelector.getFiles()) {
			if (!PetrinetTool.isPetrinetFile(file))
				continue;
			// System.out.println("bad model specified");
			Mapping m = mappings.get(file);
			if (m != null) {
				if (mapping < 1)
					mapping = 1;
				if (m.isComplete() && allMapped)
					mapping = 2;
				else {
					allMapped = false;
					mapping = 1;
				}
			} else {
				allMapped = false;
				if (mapping > 0)
					mapping = 1;
			}
		}
		if (mapping == 0)
			setMatchingUnmapped();
		else if (mapping == 1)
			setMatchingPartial();
		else
			setMatchingComplete();
	}

	private void setMatchingComplete() {
		complete = true;
		mappingStatus.setForeground(COLOUR_OK_DARK);
		mappingStatus.setText("status: mapped");
		mappingStatus.revalidate();
	}

	private void setMatchingUnmapped() {
		complete = false;
		mappingStatus.setForeground(COLOUR_ERROR_DARK);
		mappingStatus.setText("status: unmapped");
		mappingStatus.revalidate();
	}

	private void setMatchingPartial() {
		complete = false;
		mappingStatus.setForeground(COLOUR_WARNING_DARK);
		mappingStatus.setText("status: partially mapped");
		mappingStatus.revalidate();
	}

	// Gets called by the 'show mapping' button
	private void doMapping() {
		if (LogTool.isLogFile(logfile)) {
			showMapping.setEnabled(false);
			List<Mapping> list = new ArrayList<Mapping>();
			List<String> toload = new ArrayList<String>();
			for (String file : modelFilesSelector.getFiles())
				if (PetrinetTool.isPetrinetFile(file)) {
					if (!mappings.containsKey(file))
						toload.add(file);
					else
						list.add(mappings.get(file));
				} else
					JOptionPane.showMessageDialog(this,
							"One of your selected models is not a valid petrinet file.\n"
									+ file, "Error", JOptionPane.ERROR_MESSAGE);
			Mapping[] loaded = GraphicalMappingLoader.create(logfile,
					toload.toArray(new String[toload.size()]));
			for (int i = 0; i < loaded.length; i++) {
				list.add(loaded[i]);
				mappings.put(toload.get(i), loaded[i]);
			}
			@SuppressWarnings("unused")
			MappingFrame frame = new MappingFrame(list) {

				@Override
				public void windowClosed() {
					showMapping.setEnabled(true);
					changeModels();
				}
			};
		} else
			JOptionPane.showMessageDialog(this,
					"The file you have selected cannot be used as logfile.",
					"Error", JOptionPane.ERROR_MESSAGE);
	}

	public List<Mapping> getMappings() {
		List<Mapping> list = new ArrayList<Mapping>();
		for (String file : modelFilesSelector.getFiles())
			list.add(mappings.get(file));
		return list;
	}
}