package be.kuleuven.econ.cbf.metrics.recall;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.MarkovianMetric;

@Metric(genericName = "Markovian Fitness", author = "Augusto et al.", classification = "Recall")
public class MarkovianRecall extends MarkovianMetric {

	public MarkovianRecall() {
		super();
	}

	@Override
	public synchronized void calculate() {
		double res = super.calculate(true);
		setResult(res);
	}

}
