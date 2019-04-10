package be.kuleuven.econ.cbf.ui.metrics.other;

import be.kuleuven.econ.cbf.metrics.precision.AntiAlignPrecision;
import be.kuleuven.econ.cbf.metrics.generalization.AntiAlignGeneralization;
import be.kuleuven.econ.cbf.metrics.other.AntiAlignMetric;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {AntiAlignPrecision.class, AntiAlignGeneralization.class})
public class AntiAlignPrecisionGeneralizationVisualizer extends AryaMetricVisualizer {
	
	@Override
	protected void buildPanels() {
		super.buildPanels();
		
		// Add the anti-align and final configuration panels
		AntiAlignMetric metrica = (AntiAlignMetric) metric;
		
		panels.add(new AntiAlignParamPanel(metrica.getCutOffLength(), metrica.getMaxFactor(), metrica.getBacktrackLimit(), metrica.getBacktrackThreshold()));
		
		if (metric instanceof AntiAlignPrecision)
			panels.add(new AntiAlignResultChooser(metrica.getResultType(), AntiAlignPrecision.resultTypes));
		if (metric instanceof AntiAlignGeneralization)
			panels.add(new AntiAlignResultChooser(metrica.getResultType(), AntiAlignGeneralization.resultTypes));
		
	}

	@Override
	protected void applyParameters() {
		super.applyParameters();
		
		AntiAlignParamPanel panel = (AntiAlignParamPanel) panels.get(panels.size() - 2);
		AntiAlignResultChooser panel2 = (AntiAlignResultChooser) panels.get(panels.size() - 1);
		
		((AntiAlignMetric) metric).setCutOffLength(panel.getCutoffLength());
		((AntiAlignMetric) metric).setMaxFactor(panel.getMaxFactor());
		((AntiAlignMetric) metric).setBacktrackLimit(panel.getBacktrackLimit());
		((AntiAlignMetric) metric).setBacktrackThreshold(panel.getBacktrackThreshold());
		((AntiAlignMetric) metric).setResultType(panel2.getResultType());
	}
	

}
