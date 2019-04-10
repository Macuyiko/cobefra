package be.kuleuven.econ.cbf.metrics.other;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;

import nl.tue.astar.AStarException;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.utils.MappingUtils;

public abstract class AryaMetric extends AbstractSimpleMetric {
	protected Petrinet petrinet = null;
	protected Marking marking;
	protected XLog log = null;
	protected TransEvClassMapping logMapper = null;
	protected PluginContext context = null;
	protected IPNReplayParameter parameters = null;
	protected PNRepResult repResult = null;
	protected Marking initialMarking = null;
	protected Marking finalMarking = null;
	
	protected IPNReplayAlgorithm chosenAlgorithm;
	protected boolean createInitialMarking;
	protected boolean createFinalMarking;
	
	public AryaMetric() {
		chosenAlgorithm = null;
		createInitialMarking = true;
		createFinalMarking = false;
	}

	@Override
	public boolean isComplete() {
		return (chosenAlgorithm != null);
	}
	
	public void replayLog() throws AStarException {
		repResult = chosenAlgorithm.replayLog(context, petrinet, log, logMapper, parameters);
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
		
		initialMarking = initMarking;	
		finalMarking = finaMarking;
		
		IPNReplayParamProvider provider = chosenAlgorithm
				.constructParamProvider(context, petrinet, log, logMapper);

		JComponent paramUI = provider.constructUI();
		parameters = provider.constructReplayParameter(paramUI);
		parameters.setCreateConn(false);
		parameters.setGUIMode(false);
		
		if (initMarking != null)
			parameters.setInitialMarking(initMarking);
		if (finaMarking != null)
			parameters.setFinalMarkings(finaMarking);
		
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("chosenAlgorithm", chosenAlgorithm.getClass().getName());
		map.put("createInitialMarking", createInitialMarking ? "1" : "0");
		map.put("createFinalMarking", createFinalMarking ? "1" : "0");
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
		fireCompletenessChanged();
	}

	public IPNReplayAlgorithm getChosenAlgorithm() {
		return chosenAlgorithm;
	}

	public void setChosenAlgorithm(IPNReplayAlgorithm chosenAlgorithm) {
		this.chosenAlgorithm = chosenAlgorithm;
		fireCompletenessChanged();
	}

	public void setChosenAlgorithm(Class<?> chosenAlgorithmClass) {
		IPNReplayAlgorithm alg;
		try {
			alg = (IPNReplayAlgorithm) chosenAlgorithmClass.newInstance();
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

	public PluginContext getContext() {
		return context;
	}

	public PNRepResult getPNRepResult() {
		return repResult;
	}

	public Petrinet getPetrinet() {
		return petrinet;
	}

	public Marking getInitialMarking() {
		return initialMarking;
	}

}
