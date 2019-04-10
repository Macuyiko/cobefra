package be.kuleuven.econ.cbf.ui.metrics.simplicity;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.simplicity.WeighedPlaceTransitionArcDegree;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {WeighedPlaceTransitionArcDegree.class})
public class WeighedPlaceTransitionArcDegreeVisualizer extends AbstractMetricVisualizer {

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	protected Component getVisualizer(AbstractMetric m) {
		final WeighedPlaceTransitionArcDegree metric = (WeighedPlaceTransitionArcDegree) m;
		
		JPanel panel = new JPanel();
		UISettings.prettify(panel);
				
		JLabel lblNewLabel = new JLabel("\u03B1 (alpha = weight for Place average):");
		JSpinner spinner = new JSpinner(
				new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01));
		spinner.setValue(metric.getAlpha());
		Dimension d = spinner.getPreferredSize();  
        d.width = UISettings.SPINNER_FIXED_WIDTH;  
        spinner.setPreferredSize(d);  
   
		panel.add(lblNewLabel);
		panel.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				JSpinner s = ((JSpinner)arg0.getSource());
				double value = (double) s.getValue();
				metric.setAlpha(value);
			}
		});

		return panel;
	}
}

