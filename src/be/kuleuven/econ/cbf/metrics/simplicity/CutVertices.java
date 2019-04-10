package be.kuleuven.econ.cbf.metrics.simplicity;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.plugins.cutvertices.CutVertexAlgorithm;
import org.processmining.plugins.cutvertices.DirectedCutVertexAlgorithm;
import org.processmining.plugins.cutvertices.UndirectedCutVertexAlgorithm;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;

@Metric(genericName = "Count Cut Vertices", author = "CoBeFra", classification = "Simplicity")
public class CutVertices extends AbstractSimpleMetric {

	private Petrinet petrinet = null;
	
	private boolean useDirectedAlgorithm;
	private boolean countTransitions;
	private boolean countPlaces;
		
	public CutVertices() {
		useDirectedAlgorithm = false;
		countTransitions = false;
		countPlaces = true;
	}
	
	@Override
	public synchronized void calculate() {
		CutVertexAlgorithm algorithm;
		if (useDirectedAlgorithm)
			algorithm = new DirectedCutVertexAlgorithm(petrinet);
		else
			algorithm = new UndirectedCutVertexAlgorithm(petrinet);
		
		Set<PetrinetNode> cuttingNodes = algorithm.cutVertices();
		int count = 0;
		for (PetrinetNode n : cuttingNodes) {
			if (countPlaces && n instanceof Place) {
				count++;
			}
			if (countTransitions && n instanceof Transition) {
				count++;
			}
		}
		
		setResult(count);
	}
	
	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		petrinet = mapping.getPetrinet();
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> properties = new TreeMap<String, String>();
		properties.put("bDirected", useDirectedAlgorithm ? "1" : "0");
		properties.put("bPlaces", countPlaces ? "1" : "0");
		properties.put("bTransitions", countTransitions ? "1" : "0");
		return properties;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		setUseDirectedAlgorithm(properties.get("bDirected").equals("1"));
		setCountPlaces(properties.get("bPlaces").equals("1"));
		setCountTransitions(properties.get("bTransitions").equals("1"));
	}

	public boolean isUseDirectedAlgorithm() {
		return useDirectedAlgorithm;
	}

	public void setUseDirectedAlgorithm(boolean useDirectedAlgorithm) {
		this.useDirectedAlgorithm = useDirectedAlgorithm;
	}

	public boolean isCountTransitions() {
		return countTransitions;
	}

	public void setCountTransitions(boolean countTransitions) {
		this.countTransitions = countTransitions;
	}

	public boolean isCountPlaces() {
		return countPlaces;
	}

	public void setCountPlaces(boolean countPlaces) {
		this.countPlaces = countPlaces;
	}

	
	
}
