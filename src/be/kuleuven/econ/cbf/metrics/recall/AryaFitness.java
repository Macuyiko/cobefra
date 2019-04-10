package be.kuleuven.econ.cbf.metrics.recall;

import java.util.Map;

import nl.tue.astar.AStarException;

import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.AryaMetric;

@Metric(genericName = "Alignment Based Fitness", author = "Adriansyah et al.", classification = "Recall")
public class AryaFitness extends AryaMetric {
	// PNetReplayer/Trunk/src/org/processmining/plugins/petrinet/replayresult/PNRepResult.java
	public static final String[] resultTypes = new String[] {
		PNRepResult.TRACEFITNESS,
		PNRepResult.BEHAVIORAPPROPRIATENESS,
		PNRepResult.MOVELOGFITNESS,
		PNRepResult.MOVEMODELFITNESS,
		PNRepResult.RAWFITNESSCOST,
		PNRepResult.MAXFITNESSCOST,
		PNRepResult.MAXMOVELOGCOST,
		PNRepResult.NUMSTATEGENERATED,
		PNRepResult.QUEUEDSTATE,
		PNRepResult.TIME,
		PNRepResult.ORIGTRACELENGTH,
		PNRepResult.TRAVERSEDARCS 
	};
	
	private int resultType;
	
	public AryaFitness() {
		super();
		resultType = 0;
	}

	@Override
	public synchronized void calculate() {
		try {
			replayLog();
		} catch (AStarException e) {
			throw new IllegalStateException("Could not calculate Arya metric: "+e.getMessage());
		}
		
		setResult(Double.parseDouble(repResult.getInfo().get(resultTypes[resultType]).toString()));
	}

	
	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = super.getProperties();
		map.put("resultType", "" + resultType);
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		resultType = Integer.parseInt(properties.get("resultType"));
		fireCompletenessChanged();
	}

	public int getResultType() {
		return resultType;
	}

	public void setResultType(int resultType) {
		this.resultType = resultType;
		fireCompletenessChanged();
	}

}
