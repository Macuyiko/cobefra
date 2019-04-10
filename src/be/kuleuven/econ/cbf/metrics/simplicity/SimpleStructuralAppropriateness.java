package be.kuleuven.econ.cbf.metrics.simplicity;

import java.util.Set;

import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;
import org.processmining.plugins.rozinatconformance.result.StructuralAnalysisResult;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;

@Metric(genericName = "Simple Structural Appropriateness",
		author = "Rozinat et al.",
		classification = "Simplicity")
public class SimpleStructuralAppropriateness extends RozinatMetric {

	@Override
	protected void obtainResult(ConformanceAnalysisResults result) {
		Set<AnalysisResult> results = result.getAnalysisConfiguration().getResultObjects();
		results.addAll(result.getAnalysisConfiguration().getChildrenResultObjects());
		StructuralAnalysisResult structuralResult = null;
		for (AnalysisResult loopResult : results)
			if (loopResult instanceof StructuralAnalysisResult)
				structuralResult = (StructuralAnalysisResult) loopResult;
			
		setResult(structuralResult.getStructuralAppropriatenessMeasure());
	}

	@Override
	protected void setMetric() {
		settings.selectedMetrics.clear();
		settings.selectedMetrics.add(ConformanceSettings.Metric.SimpleStructuralAppropriateness);
	}
	
}
