package be.kuleuven.econ.cbf.metrics.recall;

import java.util.Set;

import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;
import org.processmining.plugins.rozinatconformance.result.ConformanceLogReplayResult;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;

@Metric(genericName = "Succesful Execution", author = "Rozinat et al.", classification = "Recall")
public class SuccesfulExecution extends RozinatMetric {

	@Override
	protected void obtainResult(ConformanceAnalysisResults result) {
		Set<AnalysisResult> results = result.getAnalysisConfiguration().getResultObjects();
		results.addAll(result.getAnalysisConfiguration().getChildrenResultObjects());
		ConformanceLogReplayResult myResult = null;
		for (AnalysisResult loopResult : results)
			if (loopResult instanceof ConformanceLogReplayResult)
				myResult = (ConformanceLogReplayResult) loopResult;
		setResult(myResult.getFractionOfSuccessfullyExecuted());
	}

	@Override
	protected void setMetric() {
		settings.selectedMetrics.clear();
		settings.selectedMetrics.add(ConformanceSettings.Metric.TokenBasesFitness);
		settings.selectedMetrics.add(ConformanceSettings.Metric.SuccessfulExecution);
	}

}
