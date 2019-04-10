package be.kuleuven.econ.cbf.metrics.generalization;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.NegativeEventConformanceMetric;

@Metric(genericName = "Negative Event Generalization", author = "vanden Broucke et al.", classification = "Generalization")
public class NegativeEventGeneralizationMetric extends NegativeEventConformanceMetric {
	
	public synchronized void calculate() {
		setResult(getMetricValue("generalization"));
	}
	
}
