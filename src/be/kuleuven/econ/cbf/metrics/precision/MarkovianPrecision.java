package be.kuleuven.econ.cbf.metrics.precision;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.MarkovianMetric;

@Metric(genericName = "Markovian Precision", author = "Augusto et al.", classification = "Precision")
public class MarkovianPrecision extends MarkovianMetric {

	public MarkovianPrecision() {
		super();
	}

	@Override
	public synchronized void calculate() {
		double res = super.calculate(false);
		setResult(res);
	}

}
