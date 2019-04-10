package be.kuleuven.econ.cbf.metrics.precision;

import java.util.Set;

import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;
import org.processmining.plugins.rozinatconformance.result.ConformanceLogReplayResult;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;

@Metric(genericName = "Simple Behavioural Appropriateness", author = "Rozinat et al.",
		classification = "Precision")
public class SimpleBehaviouralAppropriateness extends RozinatMetric {

	@Override
	protected void obtainResult(ConformanceAnalysisResults result) {
		Set<AnalysisResult> results = result.getAnalysisConfiguration().getResultObjects();
		results.addAll(result.getAnalysisConfiguration().getChildrenResultObjects());
		ConformanceLogReplayResult replayResult = null;
		for (AnalysisResult loopResult : results)
			if (loopResult instanceof ConformanceLogReplayResult)
				replayResult = (ConformanceLogReplayResult) loopResult;
		try {
			setResult(replayResult.getBehavioralAppropriatenessMeasure());
		} catch (Exception e) {
			setResult(0); // Normal
		}
	}

	@Override
	protected void setMetric() {
		settings.selectedMetrics.clear();
		settings.selectedMetrics.add(ConformanceSettings.Metric.SimpleBehavioralAppropriateness);
	}

}
