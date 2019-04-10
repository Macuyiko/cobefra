package be.kuleuven.econ.cbf.metrics.precision;

import nl.tue.astar.AStarException;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.AntiAlignMetric;

@Metric(genericName = "Anti Alignment Based Precision", author = "van Dongen et al.", classification = "Precision")
public class AntiAlignPrecision extends AntiAlignMetric {
	
	public static final String[] resultTypes = new String[] {
		"Precision",
		"Precision (trace-based)",
		"Precision (log-based)"
	};
	
	@Override
	public synchronized void calculate() {
		try {
			replayLog();
			setResult(Double.parseDouble(repResult.getInfo().get(AntiAlignPrecision.resultTypes[resultType]).toString()));
		} catch (AStarException e) {
			e.printStackTrace();
			throw new IllegalStateException("The metric could not be calculated (replay error)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
