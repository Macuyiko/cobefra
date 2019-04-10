package be.kuleuven.econ.cbf.metrics.simplicity;

import java.util.Set;

import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;
import org.processmining.plugins.rozinatconformance.result.StateSpaceExplorationResult;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;

@Metric(genericName = "Advanced Structural Appropriateness",
		author = "Rozinat et al.",
		classification = "Simplicity")
public class AdvancedStructuralAppropriateness extends RozinatMetric {

	@Override
	protected void obtainResult(ConformanceAnalysisResults result) {
		Set<AnalysisResult> results = result.getAnalysisConfiguration().getResultObjects();
		results.addAll(result.getAnalysisConfiguration().getChildrenResultObjects());
		StateSpaceExplorationResult stateSpaceResult = null;
		for (AnalysisResult loopResult : results)
			if (loopResult instanceof StateSpaceExplorationResult)
				stateSpaceResult = (StateSpaceExplorationResult) loopResult;
			
		setResult(stateSpaceResult.getImprovedStructuralAppropriatenessMeasure());
	}

	@Override
	protected void setMetric() {
		settings.selectedMetrics.clear();
		settings.selectedMetrics.add(ConformanceSettings.Metric.AdvancedStructuralAppropriateness);
	}
	
}
