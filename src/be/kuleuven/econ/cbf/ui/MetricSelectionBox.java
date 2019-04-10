package be.kuleuven.econ.cbf.ui;

import static be.kuleuven.econ.cbf.ui.UISettings.LINE_COMPONENT_HEIGHT_MAX;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;

public abstract class MetricSelectionBox extends JPanel {

	private JComboBox<String> selector;
	boolean[] title;
	String[] text;
	Class<? extends AbstractMetric>[] metrics;

	public MetricSelectionBox() {
		this(null, true);
	}

	public MetricSelectionBox(String category) {
		this(category, true);
	}

	@SuppressWarnings("unchecked")
	public MetricSelectionBox(String category, boolean includeWeighted) {
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setOpaque(false);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		selector = new JComboBox<String>() {
			@Override
			public void setSelectedIndex(int index) {
				if (index > 0 && index < title.length && !title[index])
					super.setSelectedIndex(index);
				if (index == -1)
					super.setSelectedIndex(-1);
			}
		};
		selector.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				LINE_COMPONENT_HEIGHT_MAX));
		JButton ok = new JButton("ok");
		this.add(selector);
		this.add(Box.createRigidArea(new Dimension(MARGIN, 0)));
		this.add(ok);

		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				action();
			}
		});

		createItems(category, includeWeighted);
		for (String t : text)
			selector.addItem(t);

		selector.setRenderer(new ItemRenderer(this));
		selector.setSelectedIndex(-1);
	}

	@SuppressWarnings("unchecked")
	private void createItems(String filter, boolean includeWeighted) {
		List<Class<? extends AbstractMetric>> list = AbstractMetric
				.getAllMetrics();
		// If requested, remove the weighted metrics
		if (!includeWeighted)
			for (int i = 0; i < list.size(); i++)
				if (AbstractMetric.isWeighted(list.get(i))) {
					list.remove(i);
					i--;
				}
		Map<String, List<Class<? extends AbstractMetric>>> map = new HashMap<String, List<Class<? extends AbstractMetric>>>();
		// Split all metrics by their classification
		for (Class<? extends AbstractMetric> type : list) {
			String category = AbstractMetric.getClassification(type);
			if (map.containsKey(category))
				map.get(category).add(type);
			else {
				List<Class<? extends AbstractMetric>> l = new ArrayList<Class<? extends AbstractMetric>>();
				l.add(type);
				map.put(category, l);
			}
		}
		// If requested, remove all metrics that do not match the filter
		if (filter != null) {
			list = map.get(filter);
			if (list == null)
				list = new ArrayList<Class<? extends AbstractMetric>>();
			map.clear();
			map.put(filter, list);
		}
		// Sort all the categories by title
		String[] categories = map.keySet().toArray(new String[0]);
		Arrays.sort(categories, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				Map<String, Integer> customOrder = new HashMap<String, Integer>();
				customOrder.put("Recall", 0);
				customOrder.put("Precision", 1);
				customOrder.put("Generalization", 2);
				customOrder.put("Simplicity", 3);
				if (o1.equals("Other"))
					return 1;
				if (o2.equals("Other"))
					return -1;
				if (customOrder.containsKey(o1) && customOrder.containsKey(o2))
					return customOrder.get(o1) - customOrder.get(o2);
				return o1.compareTo(o2);
			}
		});
		// Sort the metrics by title
		for (List<Class<? extends AbstractMetric>> sublist : map.values())
			Collections.sort(sublist,
					new Comparator<Class<? extends AbstractMetric>>() {
						@Override
						public int compare(
								Class<? extends AbstractMetric> arg0,
								Class<? extends AbstractMetric> arg1) {
							String name0 = AbstractMetric.getGenericName(arg0);
							String name1 = AbstractMetric.getGenericName(arg1);
							return name0.compareTo(name1);
						}
					});
		// Put the metrics in the arrays
		int size = map.size() * 2 - 1 + list.size();
		if (size < 0)
			size = 0;
		title = new boolean[size];
		text = new String[size];
		metrics = new Class[size];
		int pointer = 0;
		for (String category : categories) {
			title[pointer] = true;
			text[pointer] = category;
			metrics[pointer] = null;
			pointer++;
			for (Class<? extends AbstractMetric> type : map.get(category)) {
				title[pointer] = false;
				text[pointer] = AbstractMetric.getGenericName(type) + " (" + AbstractMetric.getAuthor(type) + ")";
				metrics[pointer] = type;
				pointer++;
			}
			if (pointer < size) {
				title[pointer] = true;
				text[pointer] = "";
				metrics[pointer] = null;
				pointer++;
			}
		}
	}

	public Class<? extends AbstractMetric> getSelectedMetric() {
		if (getSelectedIndex() > 0)
			return metrics[selector.getSelectedIndex()];
		else
			return null;
	}
	
	public int getSelectedIndex() {
		return selector.getSelectedIndex();
	}

	public void setSelectedIndex(int index) {
		selector.setSelectedIndex(index);
	}

	public abstract void action();
}

class ItemRenderer extends BasicComboBoxRenderer {

	private MetricSelectionBox box;
	private Font normal;
	private Font title;

	public ItemRenderer(MetricSelectionBox box) {
		this.box = box;
		normal = this.getFont();
		title = normal.deriveFont(Font.BOLD | Font.ITALIC);
	}

	@Override
	public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, 
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		// Text
		if (value != null)
			this.setText(value.toString());
		else
			this.setText("");
		// Colour (default)
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		// Title stuff
		if (index >= 0 && index < box.title.length) {
			if (box.title[index]) {
				this.setText(" " + this.getText());
				if (isSelected) {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				this.setFont(title);
			} else
				this.setFont(normal);
		} else
			this.setFont(normal);
		return this;
	}

}