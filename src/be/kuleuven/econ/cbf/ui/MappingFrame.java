package be.kuleuven.econ.cbf.ui;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ERROR_LIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_OK_LIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.FRAME_HEIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.FRAME_WIDTH;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.Mapping.Activity;

public abstract class MappingFrame {

	private ArrayList<Mapping> mappings;
	private JFrame frame;
	private JScrollPane center;
	private MappingSelector selector;

	public MappingFrame(Collection<Mapping> mappings) {
		this.mappings = new ArrayList<Mapping>();
		this.mappings.addAll(mappings);

		frame = new JFrame();
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setTitle("Mapping tool");
		frame.setLocationByPlatform(true);

		selector = new MappingSelector(this, this.mappings);
		center = new JScrollPane();
		center.getVerticalScrollBar().setUnitIncrement(16);

		frame.setLayout(new BorderLayout());
		frame.add(selector, BorderLayout.NORTH);
		frame.add(center);

		center.getViewport().setBackground(COLOUR_BACKGROUND);
		frame.setVisible(true);

		JPanel wbusy = new JPanel();
		wbusy.setOpaque(false);
		wbusy.setLayout(new BoxLayout(wbusy, BoxLayout.Y_AXIS));
		JLabel busy = new JLabel("Building window, please wait...");
		busy.setAlignmentX(Component.CENTER_ALIGNMENT);
		busy.setAlignmentY(Component.TOP_ALIGNMENT);
		wbusy.add(busy);
		setMainContent(null, wbusy);

		selector.select(0);

		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				MappingFrame.this.windowClosed();
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		});
	}

	private MappingButton active = null;

	public void setMainContent(MappingButton button, Component c) {
		active = button;
		center.getViewport().removeAll();
		center.getViewport().add(c);
	}
	
	public MappingSelector getSelector() {
		return selector;
	}

	public MappingButton getActive() {
		return active;
	}

	public abstract void windowClosed();
}

class MappingSelector extends JPanel {

	private MappingButton[] buttons;
	private int leftest;
	private JScrollPane center;
	private JButton btnLeft;
	private JButton btnRight;

	public MappingSelector(MappingFrame frame, List<Mapping> mappings) {
		center = new JScrollPane();
		// center.getViewport().setLayout(new BoxLayout(center.getViewport(),
		// BoxLayout.X_AXIS));
		// center.getViewport().setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel ccenter = new JPanel();
		ccenter.setLayout(new BoxLayout(ccenter, BoxLayout.X_AXIS));

		ButtonGroup group = new ButtonGroup();
		buttons = new MappingButton[mappings.size()];
		for (int i = 0; i < mappings.size(); i++) {
			buttons[i] = new MappingButton(frame, mappings, mappings.get(i));
			// center.getViewport().add(buttons[i]);
			ccenter.add(buttons[i]);
			group.add(buttons[i]);
		}
		leftest = 0;
		// center.getViewport().add(Box.createHorizontalGlue());
		center.getViewport().add(ccenter);
		center.setBorder(BorderFactory.createEmptyBorder());
		center.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		btnLeft = new JButton(">");
		btnRight = new JButton("<");

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(btnRight);
		this.add(center);
		this.add(btnLeft);
		
		btnLeft.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				toLeft();
			}
		});
		btnRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				toRight();
			}
		});

		doBtnDisables();
	}
	
	public void checkAllMappings() {
		for (MappingButton button : buttons) {
			button.checkMyMapping();
		}
	}

	private void toLeft() {
		if (leftest <= buttons.length - 1) {
			buttons[leftest].setVisible(false);
			leftest++;
		}
		doBtnDisables();
	}

	private void toRight() {
		if (leftest > 0) {
			leftest--;
			buttons[leftest].setVisible(true);
		}
		doBtnDisables();
	}

	private void doBtnDisables() {
		btnRight.setEnabled(leftest != 0);
		btnLeft.setEnabled(leftest != buttons.length - 1);
	}

	public void select(int i) {
		if (i >= 0 && i < buttons.length) {
			buttons[i].setSelected(true);
			buttons[i].showMyScreen();
		}
	}
}

class MappingButton extends JToggleButton {

	private Mapping mapping;
	private MappingFrame frame;
	private List<Mapping> group;

	public MappingButton(final MappingFrame frame, List<Mapping> group,
			Mapping mapping) {
		this.mapping = mapping;
		this.frame = frame;
		this.group = group;

		String path = mapping.getPetrinetPath();
		int l = path.lastIndexOf(File.separatorChar);
		String text = path.substring(l + 1);
		this.setText(text);
		this.setToolTipText(path);
		this.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.getSelector().checkAllMappings();
				showMyScreen();
			}
		});
	}

	public void showMyScreen() {
		if (frame.getActive() != this)
			frame.setMainContent(this, new MappingScreen(group, mapping));
	}
	
	public void checkMyMapping() {
		if (mapping != null && mapping.isComplete()) {
			this.setBackground(COLOUR_OK_LIGHT);
		}else{
			this.setBackground(COLOUR_ERROR_LIGHT);
		}
	}
}

class MappingScreen extends JPanel {

	private Mapping mapping;
	private JLabel unusedActivities;

	public MappingScreen(List<Mapping> group, Mapping mapping) {
		this.mapping = mapping;
		this.setLayout(new GridBagLayout());
		this.setBackground(COLOUR_BACKGROUND);

		JPanel top = new JPanel();
		JPanel right = new JPanel();
		JPanel center = new JPanel();

		top.setBackground(COLOUR_ITEM_BACKGROUND);
		right.setBackground(COLOUR_ITEM_BACKGROUND);
		center.setBackground(COLOUR_ITEM_BACKGROUND);
		top.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN,
				MARGIN));
		right.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN,
				MARGIN));
		center.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN,
				MARGIN, MARGIN));

		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

		JLabel lblLogPath = new JLabel("Log file: " + mapping.getLogPath());
		JLabel lblNetPath = new JLabel("Petrinet file: "
				+ mapping.getPetrinetPath());
		lblLogPath.setAlignmentX(Component.LEFT_ALIGNMENT);
		lblNetPath.setAlignmentX(Component.LEFT_ALIGNMENT);
		top.add(lblLogPath);
		top.add(lblNetPath);

		unusedActivities = new JLabel();
		right.add(new JLabel("Unassigned transitions:"));
		right.add(Box.createVerticalStrut(MARGIN));
		JLabel explain = new JLabel(
				"<html><p><i>Assign all remaining (unassigned) transitions to be 'unmapped'. They can be either invisible or not.</i></p></html>");
		right.add(explain);
		right.add(Box.createVerticalStrut(MARGIN));
		JPanel pnlAssign = new JPanel();
		pnlAssign.setLayout(new BoxLayout(pnlAssign, BoxLayout.Y_AXIS));
		JButton btnInvisible = new JButton("assign invisible");
		JButton btnVisible = new JButton("assign visible");
		btnInvisible.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnVisible.setAlignmentX(Component.CENTER_ALIGNMENT);
		pnlAssign.add(Box.createVerticalStrut(0));
		pnlAssign.add(btnInvisible);
		pnlAssign.add(btnVisible);
		pnlAssign.setOpaque(false);
		pnlAssign.setAlignmentX(Component.LEFT_ALIGNMENT);
		right.add(pnlAssign);
		right.add(Box.createVerticalStrut(2 * MARGIN));

		btnVisible.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doAssignVisible();
			}
		});
		btnInvisible.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				doAssignInvisible();
			}
		});

		right.add(new JLabel("Unassigned activities:"));
		right.add(Box.createVerticalStrut(MARGIN));
		right.add(unusedActivities);
		right.add(Box.createVerticalGlue());

		right.setMinimumSize(new Dimension(200, 200));
		right.setPreferredSize(new Dimension(200, 200));
		right.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(MARGIN, MARGIN, MARGIN, MARGIN);
		c.anchor = GridBagConstraints.BASELINE_LEADING;
		c.weightx = 1.0f;
		c.weighty = 0.0f;
		this.add(top, c);

		c.gridy = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, MARGIN, MARGIN, MARGIN);
		c.weightx = 1.0f;
		c.weighty = 1.0f;
		this.add(center, c);

		c.gridx = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.insets = new Insets(0, 0, MARGIN, MARGIN);
		c.weightx = 0.0f;
		c.weighty = 1.0f;
		this.add(right, c);

		doUnusedActivities();
		initCenter(group, center);
	}

	void doUnusedActivities() {
		Activity[] activities = mapping.getActivities();
		boolean[] include = new boolean[activities.length];
		for (int i = 0; i < activities.length; i++) {
			include[i] = true;
			for (String transition : mapping.getTransitions()) {
				Activity activity = mapping.getActivity(transition);
				if (activity != null && activity.equals(activities[i])) {
					include[i] = false;
					break;
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < activities.length; i++) {
			if (!include[i])
				continue;
			sb.append(activities[i]);
			sb.append("<br>");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 4);
			sb.insert(0, "<html>");
			sb.append("</html>");
			unusedActivities.setText(sb.toString());
		} else
			unusedActivities
					.setText("<html><i>all activities assigned</i></html>");
	}

	private ArrayList<TransitionLine> lines = new ArrayList<TransitionLine>();

	private void initCenter(List<Mapping> group, JPanel center) {
		String[] transitions = mapping.getTransitions();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

		JPanel header = new JPanel();
		header.setLayout(new ColumnLayout(2, MARGIN));
		header.add(new JLabel("Transition:"));
		header.add(new JLabel("Activity:"));
		header.setOpaque(false);
		center.add(header);
		center.add(Box.createVerticalStrut(MARGIN));

		ActivityWrapper[] activities = null;
		if (group.size() > 0) {
			Activity[] t = group.get(0).getActivities();
			activities = new ActivityWrapper[t.length + 2];
			activities[0] = new ActivityWrapper(null, true,
					"unmapped - invisible");
			activities[1] = new ActivityWrapper(null, false,
					"unmapped - visible");
			for (int i = 0; i < t.length; i++)
				activities[i + 2] = new ActivityWrapper(t[i], false);
		}

		for (String transition : transitions) {
			TransitionLine line = new TransitionLine(group, mapping,
					transition, this, activities);
			lines.add(line);
			center.add(line);
		}
	}

	public void revalidateLines() {
		for (TransitionLine line : lines)
			line.revalidateLine();
	}

	public void doAssignVisible() {
		for (TransitionLine line : lines)
			if (line.getDropdown().getSelectedIndex() == -1)
				line.getDropdown().setSelectedIndex(1);
	}

	public void doAssignInvisible() {
		for (TransitionLine line : lines)
			if (line.getDropdown().getSelectedIndex() == -1)
				line.getDropdown().setSelectedIndex(0);
	}
}

class TransitionLine extends JPanel {

	private Mapping mapping;
	private String transition;
	private ActivityDropdown dropdown;
	private List<Mapping> group;

	public TransitionLine(List<Mapping> group, Mapping mapping,
			String transition, final MappingScreen screen,
			ActivityWrapper[] items) {
		this.mapping = mapping;
		this.transition = transition;
		this.group = group;

		this.setBorder(BorderFactory.createEmptyBorder(2, MARGIN, 2, MARGIN));
		this.setLayout(new ColumnLayout(2, MARGIN));
		this.add(new JLabel(transition));
		dropdown = new ActivityDropdown(mapping, transition, items) {

			@Override
			public void selectionChanged() {
				if (TransitionLine.this.mapping
						.getActivityCertaincy(getTransition()) < Mapping.CERTAINCY_THRESHOLD)
					TransitionLine.this.setBackground(COLOUR_ERROR_LIGHT);
				else
					TransitionLine.this.setBackground(COLOUR_OK_LIGHT);
				screen.doUnusedActivities();
				screen.revalidateLines();
				
				for (Mapping m : TransitionLine.this.group)
					if (m != TransitionLine.this.mapping)
						m.induceMapping(TransitionLine.this.mapping,
								TransitionLine.this.transition);
			}
		};
		this.add(dropdown);
		if (mapping.getActivityCertaincy(transition) < Mapping.CERTAINCY_THRESHOLD)
			this.setBackground(COLOUR_ERROR_LIGHT);
		else
			this.setBackground(COLOUR_OK_LIGHT);
	}

	public void revalidateLine() {
		dropdown.revalidateSelection();
	}

	public ActivityDropdown getDropdown() {
		return dropdown;
	}
}

abstract class ActivityDropdown extends JComboBox<ActivityWrapper> {

	private String transition;
	private Mapping mapping;
	private ActivityWrapper[] items;

	public ActivityDropdown(Mapping mapping, String transition,
			final ActivityWrapper[] items) {
		super(items);
		this.mapping = mapping;
		this.transition = transition;
		this.items = items;

		setRenderer();
		this.setSelectedIndex(-1);
		revalidateSelection();

		this.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				ActivityWrapper w = (ActivityWrapper) ActivityDropdown.this
						.getSelectedItem();
				if (w != null) {
					w.select(ActivityDropdown.this.mapping,
							ActivityDropdown.this.transition);
					selectionChanged();
				}
			}
		});
		this.setEditable(true);
		AutoCompletion.enable(this);
	}

	public void revalidateSelection() {
		ActionListener[] l = this.getActionListeners();
		for (ActionListener l1 : l)
			this.removeActionListener(l1);

		Activity target = mapping.getActivity(transition);
		boolean visibilityTarget = mapping.getActivityInvisible(transition);
		float certaincy = mapping.getActivityCertaincy(transition);
		ActivityWrapper w = (ActivityWrapper) this.getSelectedItem();

		boolean update = false;
		if (w == null)
			update = true;
		else {
			if (visibilityTarget != w.isInvisible())
				update = true;
			if (w.getActivity() == null) {
				if (w.getActivity() != null)
					update = true;
			} else if (!w.getActivity().equals(target))
				update = true;
		}

		if (update)
			if (certaincy == 0.0f)
				this.setSelectedIndex(-1);
			else
				for (int i = 0; i < items.length; i++) {
					boolean vis = items[i].isInvisible() == visibilityTarget;
					boolean act = false;
					if (target == null) {
						if (items[i].getActivity() == null)
							act = true;
					} else if (target.equals(items[i].getActivity()))
						act = true;
					if (act && vis) {
						this.setSelectedIndex(i);
						break;
					}
				}

		for (ActionListener l1 : l)
			this.addActionListener(l1);
	}

	// TODO remove me?
	private void setRenderer() {
		class Renderer extends JLabel implements
				ListCellRenderer<ActivityWrapper> {
			@Override
			public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list,
					ActivityWrapper value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String text;
				if (index == -1)
					text = "";
				else
					text = value.toString();
				this.setText(text);

				if (isSelected)
					this.setBackground(list.getSelectionBackground());
				else
					this.setBackground(list.getBackground());
				this.setForeground(list.getForeground());

				Font f;
				if (value.isSpecial())
					f = list.getFont().deriveFont(Font.ITALIC);
				else
					f = list.getFont().deriveFont(0);
				this.setFont(f);

				return this;
			}
		}
		;

		this.setRenderer(new Renderer());
	}

	public String getTransition() {
		return transition;
	}

	public abstract void selectionChanged();
}

class ActivityWrapper {

	private Activity activity;
	private boolean invisible;
	private String toStringOverride;

	public ActivityWrapper(Activity a, boolean b) {
		this(a, b, null);
	}

	public ActivityWrapper(Activity a, boolean b, String s) {
		this.activity = a;
		this.invisible = b;
		this.toStringOverride = s;
	}

	public boolean isInvisible() {
		return invisible;
	}

	public Activity getActivity() {
		return activity;
	}

	public void select(Mapping mapping, String transition) {
		mapping.setActivity(transition, activity, invisible);
	}

	public boolean isSpecial() {
		return toStringOverride != null;
	}

	@Override
	public String toString() {
		String out;
		if (toStringOverride == null)
			if (activity == null)
				out = "null";
			else
				out = activity.toString();
		else
			out = toStringOverride;
		return out;
	}
}