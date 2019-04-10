package be.kuleuven.econ.cbf.metrics.recall;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.NegativeEventConformanceMetric;

@Metric(genericName = "Negative Event Recall", author = "vanden Broucke et al.", classification = "Recall")
public class NegativeEventRecallMetric extends NegativeEventConformanceMetric {
	
	public synchronized void calculate() {
		setResult(getMetricValue("recall"));
	}
	
}
