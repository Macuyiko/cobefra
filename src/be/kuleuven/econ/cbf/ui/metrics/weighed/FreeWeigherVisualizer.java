package be.kuleuven.econ.cbf.ui.metrics.weighed;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.weighed.FreeWeigher;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {FreeWeigher.class})
public class FreeWeigherVisualizer extends AbstractMetricVisualizer {

	@Override
	protected Component getVisualizer(AbstractMetric m) {
		FreeWeigher metric = (FreeWeigher) m;
		JPanel panel = new JPanel();
		UISettings.prettify(panel);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 0, 2, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JLabel("Submetric index:"), c);
		c.gridx = 1;
		c.weightx = 1.0;
		panel.add(new JLabel("Relative weight:"), c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.gridwidth = 2;
		panel.add(
				new JLabel(
						"<html><p><i>Enter the relative weights of the submetrics of this free weigher. Note that the sum of the weights of the submetrics must be one.</i></p></html>"),
				c);
		c.gridwidth = 1;

		for (int i = 0; i < metric.getNbSubmetrics(); i++) {
			c.gridy = i + 2;
			c.gridx = 0;
			c.weightx = 0.0;
			String text;
			int hi = i + 1;
			if (hi == 1)
				text = "<html>1<sup>st</sup></html>";
			else if (hi == 2)
				text = "<html>2<sup>nd</sup></html>";
			else if (hi == 3)
				text = "<html>3<sup>rd</sup></html>";
			else
				text = "<html>" + hi + "<sup>th</sup></html>";
			panel.add(new JLabel(text), c);

			c.gridx = 1;
			c.weightx = 1.0;
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0,
					1.0, 0.01));
			spinner.setValue(metric.getWeight(i));
			panel.add(spinner, c);
			spinner.addChangeListener(new WeightSetter(metric, i, spinner));
		}

		return panel;
	}
}

class WeightSetter implements ChangeListener {

	private FreeWeigher metric;
	private int index;
	private JSpinner spinner;

	public WeightSetter(FreeWeigher metric, int index, JSpinner spinner) {
		this.metric = metric;
		this.index = index;
		this.spinner = spinner;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		metric.setWeight(index, (Double) spinner.getValue());
	}
}
