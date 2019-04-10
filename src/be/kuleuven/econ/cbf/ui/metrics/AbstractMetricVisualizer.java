package be.kuleuven.econ.cbf.ui.metrics;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.utils.ClassFinder;

public abstract class AbstractMetricVisualizer {

	private static Map<AbstractMetric, JFrame> activeFrames;
	private static AbstractMetricVisualizer chain = null;

	static {
		loadClass();
	}

	public static AbstractMetricVisualizer getChain() {
		return chain;
	}

	public static void loadClass() {
		if (chain != null)
			return;
		chain = new AbstractMetricVisualizer() {

			@Override
			protected boolean canShow(AbstractMetric m) {
				return false;
			}

			@Override
			protected Component getVisualizer(AbstractMetric m) {
				return null;
			}
		};
		
		ClassFinder finder = new ClassFinder() {
			@Override
			public boolean isIncluded(Class<?> type) {
				boolean b1 = AbstractMetricVisualizer.class.isAssignableFrom(type);
				boolean b2 = type.getAnnotation(MetricVisualizer.class) != null;
				return b1 && b2;
			}
		};
		
		Set<Class<?>> list = finder.getAllClasses("be.kuleuven.econ.cbf");
		for (Class<?> type : list)
			try {
				AbstractMetricVisualizer v = (AbstractMetricVisualizer) type.newInstance();
				chain.add(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		activeFrames = new HashMap<AbstractMetric, JFrame>();
	}

	protected static void showFail(AbstractMetric m) {
		JOptionPane
				.showMessageDialog(
						null,
						"<html><p style=\"width: 400px;\">This metric does not any configuration information linked to it. You should not receive this message unless you are implementing your own metric. Please contact the developer of the metric to get this fixed.</p><p>The metric which caused this error is of the type '"
								+ m.getClass() + "'</p></html>",
						"Metric Configuration", JOptionPane.ERROR_MESSAGE);
	}

	private AbstractMetricVisualizer next = null;

	public void add(AbstractMetricVisualizer v) {
		if (next == null)
			next = v;
		else
			next.add(v);
	}

	protected boolean canShow(AbstractMetric m) {
		MetricVisualizer mv = this.getClass().getAnnotation(MetricVisualizer.class);
		if (mv != null)
			for (Class<? extends AbstractMetric> mc : mv.metrics())
				if (mc.isAssignableFrom(m.getClass()))
					return true;
		return false;
	}
	
	protected boolean overrides(AbstractMetric m, AbstractMetricVisualizer bestSoFar) {
		MetricVisualizer mv = this.getClass().getAnnotation(MetricVisualizer.class);
		MetricVisualizer yv = bestSoFar.getClass().getAnnotation(MetricVisualizer.class);
		if (mv != null && yv != null)
			for (Class<? extends AbstractMetric> mc : mv.metrics())
				for (Class<? extends AbstractMetric> yc : yv.metrics())
					if (mc.isAssignableFrom(m.getClass()) && yc.isAssignableFrom(m.getClass()) && yc.isAssignableFrom(mc))
						return true;
		return false;
	}

	/**
	 * Allowed to return null.
	 */
	protected abstract Component getVisualizer(AbstractMetric m);

	public void show(AbstractMetric m) {
		show(m, null);
	}
	public void show(AbstractMetric m, AbstractMetricVisualizer bestMatch) {
		if (canShow(m)) {
			if (bestMatch == null || overrides(m, bestMatch))
				bestMatch = this;
		}
		if (next != null)
			next.show(m, bestMatch);
		else if (bestMatch != null)
			bestMatch.showMetric(m);
		else
			showFail(m);
	}

	protected void showMetric(final AbstractMetric m) {
		if (activeFrames.containsKey(m)) {
			activeFrames.get(m).toFront();
			return;
		}
		JFrame frame = new JFrame() {
			@Override
			public void dispose() {
				super.dispose();
				activeFrames.remove(m);
			}
		};
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setTitle("Metric configuration");
		frame.setLocationByPlatform(true);
		JPanel content = new JPanel();
		frame.add(content);
		content.setBorder(BorderFactory.createEmptyBorder(
				MARGIN, MARGIN,
				MARGIN, MARGIN));
		content.setBackground(COLOUR_BACKGROUND);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		Component c = getVisualizer(m);
		content.add(new NameModifier(m));
		if (c != null) {
			content.add(Box.createVerticalStrut(MARGIN));
			content.add(c);
		}

		frame.pack();
		frame.setVisible(true);

		activeFrames.put(m, frame);
	}
	
	protected void hideMetric(final AbstractMetric m) {
		if (activeFrames.containsKey(m)) {
			activeFrames.get(m).dispose();
		}
	}
}
