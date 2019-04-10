package be.kuleuven.econ.cbf.ui.metrics.simplicity;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.simplicity.CutVertices;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {CutVertices.class})
public class CutVerticesVisualizer extends AbstractMetricVisualizer {
	
	@Override
	protected Component getVisualizer(AbstractMetric m) {
		final CutVertices metric = (CutVertices) m;
		
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		Component c = new ConfigPanel(metric);
		JScrollPane scrollPane = new JScrollPane(c);
		scrollPane.setPreferredSize(new Dimension(300, 100));
		content.add(scrollPane);
		
		return content;
	}
	
	class ConfigPanel extends TwoColumnParameterPanel {
		private JCheckBox checkDirected, checkPlaces, checkTransitions;
		
		private CutVertices metric;

		private ConfigPanel(CutVertices metric) {
			super(6);
			this.metric = metric;
			init();
		}
		
		protected void init() {
			int r = 1;
			checkDirected = this.addCheckbox("Use directed graph",
					metric.isUseDirectedAlgorithm(), r++, true);
			r++;
			
			checkPlaces = this.addCheckbox("Count cutting places",
					metric.isCountPlaces(), r++, true);
			checkTransitions = this.addCheckbox("Count cutting transitions",
					metric.isCountTransitions(), r++, true);
						
			this.updateFields();
		}
		
		@Override
		protected void updateFields() {
			checkPlaces.setEnabled(true);
			checkTransitions.setEnabled(true);
			
			if (checkPlaces.isSelected() && !checkTransitions.isSelected())
				checkPlaces.setEnabled(false);
			if (!checkPlaces.isSelected() && checkTransitions.isSelected())
				checkTransitions.setEnabled(false);
			
			metric.setUseDirectedAlgorithm(checkDirected.isSelected());
			metric.setCountPlaces(checkPlaces.isSelected());
			metric.setCountTransitions(checkTransitions.isSelected());
		}		
	}

}
