package be.kuleuven.econ.cbf.ui;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ERROR_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_OK_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_SUBITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.MetricChangeListener;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.metrics.SubmetricList;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;

public class MetricsConfigurationPanel extends JPanel implements MetricPanelDad {

	private NewMetricButton newMetricPanel;
	private List<MetricPanel> metricList;

	public MetricsConfigurationPanel(MetricSet metrics) {
		metricList = new ArrayList<MetricPanel>();
		this.setBackground(COLOUR_BACKGROUND);
		this.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN,
				MARGIN));
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		initNewMetricPanel();
		for (AbstractMetric m : metrics)
			addMetric(m);
		if (metrics.size() == 0)
			addMetric(null);
		this.add(newMetricPanel);
	}

	private void initNewMetricPanel() {
		newMetricPanel = new NewMetricButton(COLOUR_ITEM_BACKGROUND) {
			@Override
			void action() {
				addMetric(null);
			}
		};
	}

	private void addMetric(AbstractMetric metric) {
		Component spacing = Box.createVerticalStrut(MARGIN);
		MetricPanel panel;
		if (metric == null)
			panel = new MetricPanel(this, spacing) {
				@Override
				public boolean canRemoveEmpty() {
					return true;
				}
			};
		else
			panel = new MetricPanel(this, spacing, metric) {
				@Override
				public boolean canRemoveEmpty() {
					return true;
				}
			};
		this.remove(newMetricPanel);
		this.add(panel);
		this.add(spacing);
		this.add(newMetricPanel);
		metricList.add(panel);	
		this.revalidate();
		for (MetricPanel p : metricList)
			p.revalidate();	
	}

	@Override
	public void removeMetric(MetricPanel metricPanel) {
		this.remove(metricPanel);
		this.remove(metricPanel.getSpacing());
		metricList.remove(metricPanel);
		if (metricList.isEmpty())
			addMetric(null);
		else
			this.revalidate();
	}

	public MetricSet getMetricSet() {
		MetricSet set = new MetricSet();
		for (MetricPanel p : metricList)
			if (p.getMetric() != null)
				set.add(p.getMetric());
		return set;
	}

	public boolean isComplete() {
		for (MetricPanel p : metricList)
			if (!p.isComplete())
				return false;
		return true;
	}
}

abstract class MetricPanel extends JPanel {

	protected ChooseMetricPanel choose;
	protected FullMetricPanel full;
	private Component spacing;
	protected MetricPanelDad dad;

	private void setup(MetricPanelDad dad, Component spacing) {
		this.dad = dad;
		this.spacing = spacing;
		this.setBackground(COLOUR_ITEM_BACKGROUND);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public boolean isComplete() {
		if (full == null)
			return false;
		return full.getMetric().isComplete();
	}

	public MetricPanel(MetricPanelDad dad, Component spacing) {
		setup(dad, spacing);
		choose = new ChooseMetricPanel(this) {
			@Override
			public boolean canRemove() {
				return canRemoveEmpty();
			}
		};
		full = null;
		this.add(choose);
	}

	public MetricPanel(MetricPanelDad dad, Component spacing,
			AbstractMetric metric) {
		setup(dad, spacing);
		choose = null;
		full = new FullMetricPanel(this, metric);
		this.add(full);
	}

	public MetricPanel(MetricPanelDad dad, Component spacing, String type,
			boolean includeWeighted) {
		setup(dad, spacing);
		choose = new ChooseMetricPanel(this, type, includeWeighted) {
			@Override
			public boolean canRemove() {
				return canRemoveEmpty();
			}
		};
		full = null;
		this.add(choose);
	}

	public void setMetric(Class<? extends AbstractMetric> type) {
		this.remove(choose);
		choose = null;
		full = new FullMetricPanel(this, type);
		this.add(full);
		this.revalidate();
	}

	public void removeMetric() {
		dad.removeMetric(this);
	}

	public Component getSpacing() {
		return spacing;
	}

	public AbstractMetric getMetric() {
		if (full != null)
			return full.getMetric();
		else
			return null;
	}

	public void unbind() {
		if (full != null)
			full.unbind();
	}

	public abstract boolean canRemoveEmpty();

	public int getMetricIndex() {
		if (choose == null)
			return -1;
		else
			return choose.getSelectedIndex();
	}

	public void setMetricIndex(int index) {
		if (choose != null)
			choose.setSelectedIndex(index);
	}
}

abstract class SubmetricPanel extends MetricPanel {

	public SubmetricPanel(MetricPanelDad dad, Component spacing,
			AbstractMetric metric) {
		super(dad, spacing, metric);
		this.setBackground(COLOUR_SUBITEM_BACKGROUND);
	}

	public SubmetricPanel(MetricPanelDad dad, Component spacing, String type,
			boolean includeWeighted) {
		super(dad, spacing, type, includeWeighted);
		this.setBackground(COLOUR_SUBITEM_BACKGROUND);
	}

	@Override
	public void setMetric(Class<? extends AbstractMetric> type) {
		this.remove(choose);
		choose = null;
		((FullMetricPanel) dad).setMetric(type);
	}
}

abstract class ChooseMetricPanel extends JPanel {

	private MetricPanel dad;
	private MetricSelectionBox selector;

	public ChooseMetricPanel(MetricPanel dad) {
		this(dad, null, true);
	}

	public ChooseMetricPanel(MetricPanel dad, String type,
			boolean includeWeighted) {
		this.dad = dad;
		this.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN,
				MARGIN));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setOpaque(false);
		JLabel label = new JLabel("Select metric type:");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(label);

		selector = new MetricSelectionBox(type, includeWeighted) {
			@Override
			public void action() {
				if (this.getSelectedMetric() != null)
					ChooseMetricPanel.this.dad.setMetric(this.getSelectedMetric());
			}
		};
		this.add(selector);
		initRemove();
	}

	public int getSelectedIndex() {
		return selector.getSelectedIndex();
	}

	public void setSelectedIndex(int index) {
		selector.setSelectedIndex(index);
	}
	
	private void initRemove() {
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (!canRemove())
					return;
				boolean x = getWidth() - arg0.getX() <= 16;
				boolean y = arg0.getY() <= 16;
				if (x && y) {
					arg0.consume();
					dad.removeMetric();
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
		if (!canRemove())
			return;
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

	public abstract boolean canRemove();
}

class FullMetricPanel extends JPanel implements MetricPanelDad, MetricChangeListener {
	private AbstractMetric metric;
	private JLabel name;
	private JLabel status;
	private MetricPanel dad;
	private MetricPanel[] submetrics;

	public FullMetricPanel(MetricPanel dad, AbstractMetric metric) {
		this.dad = dad;
		this.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN,
				MARGIN));
		this.setOpaque(false);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.metric = metric;
		this.submetrics = new MetricPanel[0];
		this.name = new JLabel(metric.getName());
		JLabel typeLabel = new JLabel(AbstractMetric.getGenericName(metric
				.getClass()));
		Font font = typeLabel.getFont();
		font = font.deriveFont(font.getSize() * 0.75f);
		typeLabel.setFont(font);
		status = new JLabel();
		this.add(name);
		this.add(typeLabel);
		this.add(status);
		this.add(Box.createHorizontalGlue());
		initRemove();
		initConfigure();
		initNewMetric();
		doSubmetrics();
		metric.addChangeListener(this);
		completenessChanged();
	}

	private void doStatusIncomplete() {
		status.setForeground(COLOUR_ERROR_DARK);
		status.setText("incomplete");
	}

	private void doStatusComplete() {
		status.setForeground(COLOUR_OK_DARK);
		status.setText("complete");
	}

	public void setMetric(Class<? extends AbstractMetric> type) {
		metric.add(type);
	}

	public FullMetricPanel(MetricPanel dad, Class<? extends AbstractMetric> type) {
		this(dad, AbstractMetric.createInstance(type));
	}

	public AbstractMetric getMetric() {
		return metric;
	}

	private void initRemove() {
		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				boolean x = getWidth() - arg0.getX() <= 16;
				boolean y = arg0.getY() <= 16;
				if (x && y) {
					arg0.consume();
					dad.removeMetric();
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

	private void initConfigure() {
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				int xP = getWidth() - arg0.getX();
				boolean x = xP > 16 && xP <= 32;
				boolean y = arg0.getY() <= 16;
				if (x && y) {
					arg0.consume();
					AbstractMetricVisualizer.getChain().show(getMetric());
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
		// Close button
		g2.setColor(new Color(193, 21, 2));
		g2.fillRect(this.getWidth() - 16, 0, 16, 16);
		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		g2.drawLine(this.getWidth() - 5, 5, this.getWidth() - 11, 11);
		g2.drawLine(this.getWidth() - 5, 11, this.getWidth() - 11, 5);
		// Edit button
		g2.setColor(new Color(31, 125, 165));
		g2.fillOval(this.getWidth() - 32, 0, 16, 16);
		g2.fillRect(this.getWidth() - 32, 0, 16, 8);
		g2.fillRect(this.getWidth() - 24, 0, 8, 16);
		g2.setColor(Color.WHITE);
		g2.fillOval(this.getWidth() - 28, 4, 8, 8);
		g2.setColor(new Color(31, 125, 165));
		g2.fillOval(this.getWidth() - 26, 6, 4, 4);
	}

	@Override
	public void removeMetric(MetricPanel panel) {
		AbstractMetric submetric = panel.getMetric();
		panel.setMetricIndex(-1);
		
		if (submetric != null)
			metric.remove(submetric);
		else
			metric.removeEmpty();
		doSubmetrics();
	}

	private void doSubmetrics() {
		if (metric.canAddMetric()) {
			this.remove(newMetricTopSpacing);
			this.remove(newMetricButton);
		}
		// Some hocus pocus to avoid selection loss in combo boxes
		List<Integer> selectedIndices = new ArrayList<Integer>();
		SubmetricList list = metric.getSubmetrics();
		for (MetricPanel c : submetrics) {
			int metricIndex = c.getMetricIndex();
			if (metricIndex >= 0)
				selectedIndices.add(metricIndex);
			c.unbind();
			this.remove(c);
			this.remove(c.getSpacing());
		}
		submetrics = new MetricPanel[list.getNbSubmetrics()];
		int uninstantiatedCounter = 0;
		for (int i = 0; i < list.getNbSubmetrics(); i++) {
			Component box = Box.createVerticalStrut(MARGIN);
			MetricPanel p;
			if (list.isInstantiated(i))
				p = new SubmetricPanel(this, box, list.getSubmetric(i)) {
					@Override
					public boolean canRemoveEmpty() {
						return metric.canRemoveEmpty();
					}
				};
			else {
				p = new SubmetricPanel(this, box, list.getUninstantiatedSubmetricType(i), false) {
					@Override
					public boolean canRemoveEmpty() {
						return metric.canRemoveEmpty();
					}
				};
				if (uninstantiatedCounter < selectedIndices.size())
					p.setMetricIndex(selectedIndices.get(uninstantiatedCounter++));
			}
			submetrics[i] = p;
			this.add(box);
			this.add(p);
		}
		if (metric.canAddMetric()) {
			this.add(newMetricTopSpacing);
			this.add(newMetricButton);
		}
		this.revalidate();
	}

	@Override
	public void nameChanged() {
		name.setText(metric.getName());
	}

	@Override
	public void submetricsChanged() {
		doSubmetrics();
	}

	public void unbind() {
		metric.removeChangeListener(this);
	}

	private NewMetricButton newMetricButton = null;
	private Component newMetricTopSpacing = null;

	private void initNewMetric() {
		if (metric.canAddMetric()) {
			newMetricButton = new NewMetricButton(COLOUR_SUBITEM_BACKGROUND) {
				@Override
				void action() {
					addMetric();
				}
			};
			newMetricTopSpacing = Box.createVerticalStrut(MARGIN);
		}
	}

	private void addMetric() {
		metric.addEmpty();
	}

	@Override
	public void completenessChanged() {
		if (metric.isComplete())
			doStatusComplete();
		else
			doStatusIncomplete();
	}
}

interface MetricPanelDad {

	public void removeMetric(MetricPanel panel);
}

abstract class NewMetricButton extends JPanel {

	public NewMetricButton(Color background) {
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setBackground(background);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel label = new JLabel("add metric");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(Box.createVerticalStrut(UISettings.MARGIN));
		this.add(label);
		this.add(Box.createVerticalStrut(UISettings.MARGIN));
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				action();
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

	abstract void action();
}