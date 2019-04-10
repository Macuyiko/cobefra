package be.kuleuven.econ.cbf.metrics.generalization;

import nl.tue.astar.AStarException;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.AntiAlignMetric;

@Metric(genericName = "Anti Alignment Based Generalization", author = "van Dongen et al.", classification = "Generalization")
public class AntiAlignGeneralization extends AntiAlignMetric {
	
	public static final String[] resultTypes = new String[] {
		"Generalization",
		"Generalization (trace-based)",
		"Generalization (log-based)"
	};
	
	@Override
	public synchronized void calculate() {
		try {
			replayLog();
			setResult(Double.parseDouble(repResult.getInfo().get(AntiAlignGeneralization.resultTypes[resultType]).toString()));
		} catch (AStarException e) {
			e.printStackTrace();
			throw new IllegalStateException("The metric could not be calculated (replay error)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
