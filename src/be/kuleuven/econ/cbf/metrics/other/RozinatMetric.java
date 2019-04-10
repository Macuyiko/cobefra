package be.kuleuven.econ.cbf.metrics.other;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.rozinat.ui.RozinatJComponent;
import org.processmining.plugins.rozinatconformance.ConformanceSettings;
import org.processmining.plugins.rozinatconformance.VisualPetrinetEvaluatorPlugin;
import org.processmining.plugins.rozinatconformance.result.ConformanceAnalysisResults;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.Mapping.Activity;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.utils.MappingUtils;

public abstract class RozinatMetric extends AbstractSimpleMetric {

	protected XLog log;
	protected Petrinet net;
	protected PetrinetLogMapper mapper;
	
	protected boolean findBestShortestSequence;
	protected boolean punishUnmapped;
	protected int maxdepth;
	protected int timeoutLogReplay;
	protected int timeoutStateSpaceExploration;
	
	protected ConformanceSettings settings;
	
	public RozinatMetric() {
		findBestShortestSequence = false;
		punishUnmapped = false;
		maxdepth = 0;
		timeoutLogReplay = 0;
		timeoutStateSpaceExploration = 0;
		settings = new ConformanceSettings();

		log = null;
		net = null;
	}

	@Override
	public synchronized void calculate() throws IllegalStateException {
		settings.findBestShortestSequence = this.findBestShortestSequence;
		settings.maxDepth = this.maxdepth;
		settings.timeoutLogReplay = this.timeoutLogReplay;
		settings.timeoutStateSpaceExploration = this.timeoutStateSpaceExploration;
		this.setMetric();
		
		RozinatJComponent result = (RozinatJComponent) VisualPetrinetEvaluatorPlugin.main(new FakePluginContext(), 
				log, net, PetrinetUtils.getInitialMarking(net), mapper, settings);
		ConformanceAnalysisResults insideResult = (ConformanceAnalysisResults) result.getInside();
		this.obtainResult(insideResult);
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("findBestShortestSequence", Boolean.toString(findBestShortestSequence));
		map.put("punishUnmapped", Boolean.toString(punishUnmapped));
		map.put("maxdepth", Integer.toString(maxdepth));
		map.put("timeoutLogReplay", Integer.toString(timeoutLogReplay));
		map.put("timeoutStateSpaceExploration", Integer.toString(timeoutStateSpaceExploration));
		return map;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		net = mapping.getPetrinet();
		log = mapping.getLog();
		MappingUtils.setInvisiblesInPetrinet(mapping, net);
		List<Activity> acts = new ArrayList<Activity>();
		if (punishUnmapped)
			acts = MappingUtils.addUnmappedInPetrinet(mapping, net);
		mapper = MappingUtils.getPetrinetLogMapper(mapping, net, log);
		if (punishUnmapped)
			MappingUtils.removeUnmappedInMapping(mapping, acts);
		
	}
	
	protected abstract void obtainResult(ConformanceAnalysisResults result);
	protected abstract void setMetric();

	public boolean getFindBestShortestSequence() {
		return findBestShortestSequence;
	}

	public int getMaxDepth() {
		return maxdepth;
	}

	public int getTimeoutLogReplay() {
		return timeoutLogReplay;
	}

	public int getTimeoutStateSpaceExploration() {
		return timeoutStateSpaceExploration;
	}

	public void setFindBestShortestSequence(boolean b) {
		this.findBestShortestSequence = b;
	}

	public void setMaxDepth(int maxDepth) {
		if (maxDepth >= -1)
			this.maxdepth = maxDepth;
		else
			throw new IllegalArgumentException();
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		setPunishUnmapped(Boolean.parseBoolean(properties
				.get("punishUnmapped")));
		setFindBestShortestSequence(Boolean.parseBoolean(properties
				.get("findBestShortestSequence")));
		setMaxDepth(Integer.parseInt(properties.get("maxdepth")));
		setTimeoutLogReplay(Integer
				.parseInt(properties.get("timeoutLogReplay")));
		setTimeoutStateSpaceExploration(Integer.parseInt(properties
				.get("timeoutStateSpaceExploration")));
	}

	public void setTimeoutLogReplay(int timeoutLogReplay) {
		if (timeoutLogReplay >= 0)
			this.timeoutLogReplay = timeoutLogReplay;
		else
			throw new IllegalArgumentException();
	}

	public void setTimeoutStateSpaceExploration(int timeoutStateSpaceExploration) {
		if (timeoutStateSpaceExploration >= 0)
			this.timeoutStateSpaceExploration = timeoutStateSpaceExploration;
		else
			throw new IllegalArgumentException();
	}

	public boolean isPunishUnmapped() {
		return punishUnmapped;
	}

	public void setPunishUnmapped(boolean punishUnmapped) {
		this.punishUnmapped = punishUnmapped;
	}
}