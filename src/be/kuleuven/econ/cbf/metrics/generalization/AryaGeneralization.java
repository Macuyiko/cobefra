package be.kuleuven.econ.cbf.metrics.generalization;

import java.util.Map;

import nl.tue.astar.AStarException;

import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.AryaMetric;

@Metric(genericName = "Alignment Based Probabilistic Generalization", author = "Adriansyah et al.", classification = "Generalization")
public class AryaGeneralization extends AryaMetric {
	private boolean traceGrouped;
	
	public AryaGeneralization() {
		super();
		traceGrouped = false;
	}

	@Override
	public synchronized void calculate() {
		try {
			replayLog();
			AlignmentPrecGen aPrecGen = new AlignmentPrecGen();
			AlignmentPrecGenRes result = 
					aPrecGen.measureConformanceAssumingCorrectAlignment(context, logMapper, repResult,
							petrinet, initialMarking, traceGrouped);
			double generalization = result.getGeneralization();
			setResult(generalization);
		} catch (AStarException e) {
			e.printStackTrace();
			throw new IllegalStateException("The metric could not be calculated (replay error)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = super.getProperties();
		map.put("traceGrouped", traceGrouped ? "1" : "0");
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		traceGrouped = properties.get("traceGrouped").equals("1");
		fireCompletenessChanged();
	}

	public boolean isTraceGrouped() {
		return traceGrouped;
	}

	public void setTraceGrouped(boolean traceGrouped) {
		this.traceGrouped = traceGrouped;
	}
}
