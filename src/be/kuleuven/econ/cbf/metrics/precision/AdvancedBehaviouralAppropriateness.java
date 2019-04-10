package be.kuleuven.econ.cbf.metrics.precision;

import java.util.Set;

import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.logreplay.AnalysisResult;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;
import org.processmining.plugins.rozinatconformance.result.ConformanceLogReplayResult;
import org.processmining.plugins.rozinatconformance.result.StateSpaceExplorationResult;
import org.processmining.plugins.rozinatconformance.ui.BehAppropriatenessAnalysisGUI;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;

@Metric(genericName = "Advanced Behavioural Appropriateness", author = "Rozinat et al.",
		classification = "Precision")
public class AdvancedBehaviouralAppropriateness extends RozinatMetric {

	@Override
	protected void obtainResult(ConformanceAnalysisResults result) {
		Set<AnalysisResult> results = result.getAnalysisConfiguration().getResultObjects();
		results.addAll(result.getAnalysisConfiguration().getChildrenResultObjects());
		StateSpaceExplorationResult stateSpaceResult = null;
		ConformanceLogReplayResult replayResult = null;
		for (AnalysisResult loopResult : results)
			if (loopResult instanceof StateSpaceExplorationResult)
				stateSpaceResult = (StateSpaceExplorationResult) loopResult;
			else if (loopResult instanceof ConformanceLogReplayResult)
				replayResult = (ConformanceLogReplayResult) loopResult;
		
		BehAppropriatenessAnalysisGUI.cleanLogAndModelRelations(stateSpaceResult, replayResult);
		BehAppropriatenessAnalysisGUI.matchLogAndModelRelations(stateSpaceResult, replayResult);
		
		setResult(stateSpaceResult.getImprovedBehavioralAppropriatenessMeasure());
	}

	@Override
	protected void setMetric() {
		settings.selectedMetrics.clear();
		settings.selectedMetrics.add(ConformanceSettings.Metric.AdvancedBehavioralAppropriateness);
	}

}
