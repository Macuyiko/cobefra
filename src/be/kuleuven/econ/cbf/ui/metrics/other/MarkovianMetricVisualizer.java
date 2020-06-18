package be.kuleuven.econ.cbf.ui.metrics.other;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.other.MarkovianMetric;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {MarkovianMetric.class})
public class MarkovianMetricVisualizer extends WizardVisualizer {
	private MarkovianMetric metric;
	
	private MarkovianMetricConfigPanel settingsPanel;
	
	@Override
	protected void performSetup(AbstractMetric m) {
		settingsPanel = new MarkovianMetricConfigPanel();
		metric = (MarkovianMetric) m;
		
		settingsPanel.setAbs(metric.getType());
		settingsPanel.setOpd(metric.getOpd());
		settingsPanel.setOrder(metric.getOrder());

	}
	
	@Override
	protected void buildPanels() {
		panels.add(settingsPanel);
	}

	@Override
	protected void applyParameters() {
		metric.setType(settingsPanel.getAbs());
		metric.setOpd(settingsPanel.getOpd());
		metric.setOrder(settingsPanel.getOrder());
	}

	

}
