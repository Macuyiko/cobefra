package be.kuleuven.econ.cbf.metrics.simplicity;

import java.util.Map;
import java.util.TreeMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;

@Metric(genericName = "Count Places", author = "CoBeFra", classification = "Simplicity")
public class CountPlaces extends AbstractSimpleMetric {

	private Petrinet petrinet = null;

	@Override
	public synchronized void calculate() {
		if (petrinet == null)
			throw new IllegalStateException();
		setResult(petrinet.getPlaces().size());
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
		petrinet = mapping.getPetrinet();
	}

	@Override
	public void setProperties(Map<String, String> properties) {
	}
}
