package be.kuleuven.econ.cbf.metrics.precision;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.NegativeEventConformanceMetric;

@Metric(genericName = "Negative Event Precision", author = "vanden Broucke et al.", classification = "Precision")
public class NegativeEventPrecisionMetric extends NegativeEventConformanceMetric {
	
	public synchronized void calculate() {
		setResult(getMetricValue("precision"));
	}
	
}
