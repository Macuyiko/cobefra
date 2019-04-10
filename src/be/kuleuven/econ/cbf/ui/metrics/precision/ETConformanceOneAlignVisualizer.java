package be.kuleuven.econ.cbf.ui.metrics.precision;

import be.kuleuven.econ.cbf.metrics.precision.ETConformanceOneAlign;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.other.AryaMetricVisualizer;

@MetricVisualizer(metrics = {ETConformanceOneAlign.class})
public class ETConformanceOneAlignVisualizer extends AryaMetricVisualizer {

	@Override
	protected void buildPanels() {
		super.buildPanels();
		// Add the final configuration panel
		panels.add(new ETCAfterAlignmentConfiguration(((ETConformanceOneAlign) metric).getGamma()));
	}

	@Override
	protected void applyParameters() {
		super.applyParameters();
		((ETConformanceOneAlign) metric).setGamma(
			((ETCAfterAlignmentConfiguration) panels.get(panels.size()-1)).getGamma());
	}
	
}
