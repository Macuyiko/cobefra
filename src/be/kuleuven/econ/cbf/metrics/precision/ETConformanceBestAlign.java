package be.kuleuven.econ.cbf.metrics.precision;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nl.tue.astar.AStarException;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.alignetc.core.ReplayAutomaton;
import org.processmining.plugins.alignetc.result.AlignETCResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.IPNMatchInstancesLogReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.BestWithFitnessBoundAlignmentsTreeAlg;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.NBestAlignmentsAlg;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.ParamSettingBestWithFitnessBoundAlg;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.ParamSettingExpressAlg;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.ParamSettingNBestAlg;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.utils.MappingUtils;

@Metric(genericName = "Best Align Precision", author = "Adriansyah et al.", classification = "Precision")
public class ETConformanceBestAlign extends AbstractSimpleMetric {
	private Petrinet petrinet = null;
	private Marking marking;
	private XLog log = null;
	private TransEvClassMapping logMapper = null;
	private PluginContext context = null;
	private Object[] parameters = null;
	private Marking initialMarking, finalMarking = null;
	private PNMatchInstancesRepResult allRepResult = null;
	
	private IPNMatchInstancesLogReplayAlgorithm chosenAlgorithm;
	private double gamma;
	private boolean createInitialMarking, createFinalMarking;
	public ETConformanceBestAlign() {
		chosenAlgorithm = null;
		createInitialMarking = true;
		createFinalMarking = true;
	}

	@Override
	public synchronized void calculate() {
		try {
			allRepResult = chosenAlgorithm.replayLog(
						context, 
						petrinet, 
						initialMarking, finalMarking,
						log, logMapper, parameters);
		} catch (AStarException e1) {
			throw new IllegalStateException("Could not calculate arya metric: "+e1.getMessage());
		}
		
		AlignETCResult alignETCResult = null;
		try {
			alignETCResult = performAlignETC(allRepResult);
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
		ReplayAutomaton ra = new ReplayAutomaton(context, alignments, petrinet);
		ra.cut(res.escTh);
		ra.extend(petrinet, initialMarking);
		ra.conformance(res);
		res.alignments = alignments;

		return res;
	}

	@Override
	public boolean isComplete() {
		return (chosenAlgorithm != null);
	}

	@Override
	public synchronized void load(Mapping mapping) {
		context = new FakePluginContext();
		
		Object[] petrimarking = mapping.getPetrinetWithMarking();
		
		petrinet = (Petrinet) petrimarking[0];
		marking = (Marking) petrimarking[1];
		log = mapping.getLog();
		MappingUtils.setInvisiblesInPetrinet(mapping, petrinet);
		logMapper = MappingUtils.getTransEvClassMapping(mapping, petrinet, log);

		Marking initMarking = marking;
		Marking finaMarking = null;

		if (createInitialMarking && (initMarking == null || initMarking.isEmpty()))
			initMarking = PetrinetUtils.getInitialMarking(petrinet);
		if (createFinalMarking && (finaMarking == null || finaMarking.isEmpty()))
			finaMarking = PetrinetUtils.getFinalMarking(petrinet);

		if (initMarking != null && initMarking.isEmpty())
			System.err.println("An initial marking was constructed for the given Petri net, but its marking was empty. "
					+ "Unreliable results might follow. If you want to avoid this, add tokens using a Petri net "
					+ "editing tool.");
		if (initMarking == null)
			System.err.println("No initial marking could be obtained.");
		
		if (finaMarking == null) // Important for all-align
			finaMarking = new Marking();
		
		initialMarking = initMarking;	
		finalMarking = finaMarking;
		
		
		// Perform additional config
		List<Object> allParameters = new LinkedList<Object>();
	    if (chosenAlgorithm instanceof NBestAlignmentsAlg) {
				if (chosenAlgorithm instanceof BestWithFitnessBoundAlignmentsTreeAlg) {
					ParamSettingBestWithFitnessBoundAlg paramSetting = new ParamSettingBestWithFitnessBoundAlg();
					paramSetting.populateCostPanel(petrinet, log, logMapper);
					Object[] params = paramSetting.getAllParameters();
					for (Object o : params) {
			            allParameters.add(o);
			        }
				} else {
					ParamSettingNBestAlg paramSetting = new ParamSettingNBestAlg();
					paramSetting.populateCostPanel(petrinet, log, logMapper);
					Object[] params = paramSetting.getAllParameters();
					for (Object o : params) {
			            allParameters.add(o);
			        }
				}
		} else {
			ParamSettingExpressAlg paramSetting = new ParamSettingExpressAlg();
			paramSetting.populateCostPanel(petrinet, log, logMapper);
			Object[] params = paramSetting.getAllParameters();
			for (Object o : params) {
	            allParameters.add(o);
	        }
		}
		
	    parameters = allParameters.toArray();
	      
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("chosenAlgorithm", chosenAlgorithm.getClass().getName());
		map.put("createInitialMarking", createInitialMarking ? "1" : "0");
		map.put("createFinalMarking", createFinalMarking ? "1" : "0");
		map.put("gamma", Double.toString(gamma));
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		try {
			setChosenAlgorithm(Class.forName(properties.get("chosenAlgorithm")));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unable to parse properties", e);
		}

		createInitialMarking = properties.get("createInitialMarking").equals("1");
		createFinalMarking = properties.get("createFinalMarking").equals("1");
		setGamma(Double.parseDouble(properties.get("gamma")));
		
		fireCompletenessChanged();
	}

	public IPNMatchInstancesLogReplayAlgorithm getChosenAlgorithm() {
		return chosenAlgorithm;
	}

	public void setChosenAlgorithm(IPNMatchInstancesLogReplayAlgorithm chosenAlgorithm) {
		this.chosenAlgorithm = chosenAlgorithm;
		fireCompletenessChanged();
	}

	public void setChosenAlgorithm(Class<?> chosenAlgorithmClass) {
		IPNMatchInstancesLogReplayAlgorithm alg;
		try {
			alg = (IPNMatchInstancesLogReplayAlgorithm) chosenAlgorithmClass.newInstance();
			this.chosenAlgorithm = alg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireCompletenessChanged();
	}

	public boolean isCreateInitialMarking() {
		return createInitialMarking;
	}

	public void setCreateInitialMarking(boolean createInitialMarking) {
		this.createInitialMarking = createInitialMarking;
		fireCompletenessChanged();
	}

	public boolean isCreateFinalMarking() {
		return createFinalMarking;
	}

	public void setCreateFinalMarking(boolean createFinalMarking) {
		this.createFinalMarking = createFinalMarking;
		fireCompletenessChanged();
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
	}
}
