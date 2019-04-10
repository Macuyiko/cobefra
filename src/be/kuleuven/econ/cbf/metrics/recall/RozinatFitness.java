package be.kuleuven.econ.cbf.metrics.recall;

import java.util.Set;

import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;
import org.processmining.plugins.rozinatconformance.result.ConformanceLogReplayResult;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;

@Metric(genericName = "Fitness", author = "Rozinat et al.", classification = "Recall")
public class RozinatFitness extends RozinatMetric {

	@Override
	protected void obtainResult(ConformanceAnalysisResults result) {
		Set<AnalysisResult> results = result.getAnalysisConfiguration().getResultObjects();
		results.addAll(result.getAnalysisConfiguration().getChildrenResultObjects());
		ConformanceLogReplayResult myResult = null;
		for (AnalysisResult loopResult : results)
			if (loopResult instanceof ConformanceLogReplayResult)
				myResult = (ConformanceLogReplayResult) loopResult;
		setResult(myResult.getFitnessMeasure());
	}

	@Override
	protected void setMetric() {
		settings.selectedMetrics.clear();
		settings.selectedMetrics.add(ConformanceSettings.Metric.TokenBasesFitness);
	}
}
