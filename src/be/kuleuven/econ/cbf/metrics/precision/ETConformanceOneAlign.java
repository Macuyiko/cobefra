package be.kuleuven.econ.cbf.metrics.precision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import nl.tue.astar.AStarException;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.plugins.alignetc.core.ReplayAutomaton;
import org.processmining.plugins.alignetc.result.AlignETCResult;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.other.AryaMetric;

@Metric(genericName = "One Align Precision", author = "Adriansyah et al.", classification = "Precision")
public class ETConformanceOneAlign extends AryaMetric {

	private double gamma;
	
	public ETConformanceOneAlign() {
		super();
		gamma = 0D;
	}

	@Override
	public synchronized void calculate() throws IllegalStateException {
		try {
			replayLog();
		} catch (AStarException e1) {
			throw new IllegalStateException("The arya metric could not be calculated (replay error): "+e1.getMessage());
		}
		
		if (repResult == null)
			throw new IllegalStateException("The arya metric could not be calculated");
		
		// Begin conversion to n-align
		Collection<AllSyncReplayResult> col = new ArrayList<AllSyncReplayResult>();
		for (SyncReplayResult rep : repResult) {
			List<List<Object>> nodes = new ArrayList<List<Object>>();
			nodes.add(rep.getNodeInstance());
			List<List<StepTypes>> types = new ArrayList<List<StepTypes>>();
			types.add(rep.getStepTypes());
			SortedSet<Integer> traces = rep.getTraceIndex();
			boolean rel = rep.isReliable();
			AllSyncReplayResult allRep = new AllSyncReplayResult(nodes, types, -1, rel);
			allRep.setTraceIndex(traces);
			col.add(allRep);
		}
		
		PNMatchInstancesRepResult alignments = new PNMatchInstancesRepResult(col);

		AlignETCResult alignETCResult = null;
		try {
			alignETCResult = performAlignETC(alignments);
		} catch (ConnectionCannotBeObtained e) {
			e.printStackTrace();
		} catch (IllegalTransitionException e) {
			e.printStackTrace();
		}
		
		if (alignETCResult == null)
			setResult(-1D);
		else
			setResult(alignETCResult.ap);
	}
	
	public AlignETCResult performAlignETC(PNMatchInstancesRepResult alignments)
			throws ConnectionCannotBeObtained, IllegalTransitionException {
		
		AlignETCResult res = new AlignETCResult();
		res.escTh = gamma;
		ReplayAutomaton ra = new ReplayAutomaton(getContext(), alignments, petrinet);
		ra.cut(res.escTh);
		ra.extend(petrinet, initialMarking);
		ra.conformance(res);
		res.alignments = alignments;

		return res;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		if (gamma >= 0D && gamma <= 1D)
			this.gamma = gamma;
		else
			throw new IllegalArgumentException(
					"Gamma has to be between one and zero");
		fireCompletenessChanged();
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = super.getProperties();
		map.put("gamma", Double.toString(gamma));
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		String gamma = properties.get("gamma");
		setGamma(Double.parseDouble(gamma));
		fireCompletenessChanged();
	}
}
