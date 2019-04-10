package be.kuleuven.econ.cbf.metrics.simplicity;

import java.util.Map;
import java.util.TreeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;

@Metric(genericName = "Weighed P/T Average Arc Degree", author = "CoBeFra", classification = "Simplicity")
public class WeighedPlaceTransitionArcDegree extends AbstractSimpleMetric {

	private Petrinet petrinet = null;
	private double alpha;
	
	public WeighedPlaceTransitionArcDegree() {
		alpha = .5D;
	}
	
	@Override
	public synchronized void calculate() {
		if (petrinet == null)
			throw new IllegalStateException();
		int placeSum = 0;
		int transitionSum = 0;
		for (PetrinetNode n : petrinet.getPlaces())
			placeSum += petrinet.getInEdges(n).size() + petrinet.getOutEdges(n).size();
		for (PetrinetNode n : petrinet.getTransitions())
			transitionSum += petrinet.getInEdges(n).size() + petrinet.getOutEdges(n).size();
		
		double placeAvg = (double) placeSum / petrinet.getPlaces().size();
		double transitionAvg = (double) transitionSum / petrinet.getTransitions().size();
		
		double result = (alpha) * placeAvg + (1D-alpha) * transitionAvg;
		setResult(result);
	}
	
	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		petrinet = mapping.getPetrinet();
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		if (alpha >= 0 && alpha <= 1D)
			this.alpha = alpha;
		else
			throw new IllegalArgumentException(
					"Alpha must be between 0 and 1");
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("alpha", Double.toString(alpha));
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		String alpha = properties.get("alpha");
		setAlpha(Double.parseDouble(alpha));
	}
}
