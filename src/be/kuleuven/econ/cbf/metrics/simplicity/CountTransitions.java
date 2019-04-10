package be.kuleuven.econ.cbf.metrics.simplicity;

import java.util.Map;
import java.util.TreeMap;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;

@Metric(genericName = "Count Transitions", author = "CoBeFra", classification = "Simplicity")
public class CountTransitions extends AbstractSimpleMetric {

	private Mapping mapping = null;

	@Override
	public synchronized void calculate() {
		if (mapping == null)
			throw new IllegalStateException();
		setResult(mapping.getTransitions().length);
	}

	@Override
	protected Map<String, String> getProperties() {
		return new TreeMap<String, String>();
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		clearResult();
		this.mapping = mapping;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
	}
}
