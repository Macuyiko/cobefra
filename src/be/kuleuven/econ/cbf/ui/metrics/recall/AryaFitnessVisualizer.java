package be.kuleuven.econ.cbf.ui.metrics.recall;

import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.other.AryaMetricVisualizer;

@MetricVisualizer(metrics = {AryaFitness.class})
public class AryaFitnessVisualizer extends AryaMetricVisualizer {
	@Override
	protected void buildPanels() {
		super.buildPanels();
		// Add the final configuration panel
		panels.add(new AryaResultChooser(((AryaFitness) metric).getResultType()));
	}

	@Override
	protected void applyParameters() {
		super.applyParameters();
		((AryaFitness) metric).setResultType(((AryaResultChooser)panels.get(panels.size()-1)).getResultType());
	}
}
