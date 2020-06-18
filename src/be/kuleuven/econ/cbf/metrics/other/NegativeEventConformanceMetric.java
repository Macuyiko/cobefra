package be.kuleuven.econ.cbf.metrics.other;

import java.util.Map;
import java.util.TreeMap;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.plugins.PetrinetEvaluatorPlugin;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.utils.MappingUtils;

public abstract class NegativeEventConformanceMetric extends AbstractSimpleMetric {
	protected Petrinet petrinet = null;
	protected XLog log = null;
	protected PetrinetLogMapper logMapper = null;
	protected Marking initialMarking = null;
	
	protected int replayer;
	protected int inducer;
	protected boolean useWeighted;
	protected boolean useBothRatios;
	protected boolean useCutOff;
	protected boolean multiThreaded;
	protected int negWindow;
	protected int genWindow;
	protected boolean unmappedRecall;
	protected boolean unmappedPrecision;
	protected boolean unmappedGeneralization;
	
	public NegativeEventConformanceMetric() {
		replayer = 0;
		inducer = 0;
		useWeighted = true;
		useBothRatios = false;
		useCutOff = false;
		multiThreaded = false;
		negWindow = -1;
		genWindow = -1;
		unmappedRecall = true;
		unmappedPrecision = true;
		unmappedGeneralization = true;
	}
	
	public abstract void calculate();
	
	protected double getMetricValue(String metric) {
		PetrinetEvaluatorPlugin.SUPPRESS_OUTPUT = true;
		double value = PetrinetEvaluatorPlugin.getMetricValue(log, petrinet, initialMarking, logMapper, 
				replayer, inducer, useWeighted, useBothRatios, useCutOff, negWindow, genWindow, 
				unmappedRecall, unmappedPrecision, unmappedGeneralization, multiThreaded, metric);
		return value;
	}
	
	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		petrinet = mapping.getPetrinet();
		log = mapping.getLog();
		MappingUtils.setInvisiblesInPetrinet(mapping, petrinet);
		logMapper = MappingUtils.getPetrinetLogMapper(mapping, petrinet, log);
		initialMarking = PetrinetUtils.getInitialMarking(petrinet);
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("replayer", ""+replayer);
		map.put("inducer", ""+inducer);
		map.put("useWeighted", useWeighted ? "1" : "0");
		map.put("useBothRatios", useBothRatios ? "1" : "0");
		map.put("useCutOff", useCutOff ? "1" : "0");
		map.put("multiThreaded", multiThreaded ? "1" : "0");
		map.put("negWindow", ""+negWindow);
		map.put("genWindow", ""+genWindow);
		map.put("unmappedRecall", unmappedRecall ? "1" : "0");
		map.put("unmappedPrecision", unmappedPrecision ? "1" : "0");
		map.put("unmappedGeneralization", unmappedGeneralization ? "1" : "0");
		
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		replayer = Integer.parseInt(properties.get("replayer").toString());
		inducer = Integer.parseInt(properties.get("inducer").toString());
		useWeighted = properties.get("useWeighted").toString().equals("1");
		useBothRatios = properties.get("useBothRatios").toString().equals("1");
		useCutOff = properties.get("useCutOff").toString().equals("1");
		multiThreaded = properties.get("multiThreaded").toString().equals("1");
		negWindow = Integer.parseInt(properties.get("negWindow").toString());
		genWindow = Integer.parseInt(properties.get("genWindow").toString());
		unmappedRecall = properties.get("unmappedRecall").toString().equals("1");
		unmappedPrecision = properties.get("unmappedPrecision").toString().equals("1");
		unmappedGeneralization = properties.get("unmappedGeneralization").toString().equals("1");
		
		fireCompletenessChanged();
	}
	
	public int getReplayer() {
		return replayer;
	}

	public void setReplayer(int replayer) {
		this.replayer = replayer;
	}

	public int getInducer() {
		return inducer;
	}

	public void setInducer(int inducer) {
		this.inducer = inducer;
	}
	
	public boolean isMultiThreaded() {
		return multiThreaded;
	}

	public void setMultiThreaded(boolean multiThreaded) {
		this.multiThreaded = multiThreaded;
	}

	public boolean isUseWeighted() {
		return useWeighted;
	}

	public void setUseWeighted(boolean useWeighted) {
		this.useWeighted = useWeighted;
	}

	public boolean isUseBothRatios() {
		return useBothRatios;
	}

	public void setUseBothRatios(boolean useBothRatios) {
		this.useBothRatios = useBothRatios;
	}

	public boolean isUseCutOff() {
		return useCutOff;
	}

	public void setUseCutOff(boolean useCutOff) {
		this.useCutOff = useCutOff;
	}

	public int getNegWindow() {
		return negWindow;
	}

	public void setNegWindow(int negWindow) {
		this.negWindow = negWindow;
	}

	public int getGenWindow() {
		return genWindow;
	}

	public void setGenWindow(int genWindow) {
		this.genWindow = genWindow;
	}

	public boolean isUnmappedRecall() {
		return unmappedRecall;
	}

	public void setUnmappedRecall(boolean unmappedRecall) {
		this.unmappedRecall = unmappedRecall;
	}

	public boolean isUnmappedPrecision() {
		return unmappedPrecision;
	}

	public void setUnmappedPrecision(boolean unmappedPrecision) {
		this.unmappedPrecision = unmappedPrecision;
	}

	public boolean isUnmappedGeneralization() {
		return unmappedGeneralization;
	}

	public void setUnmappedGeneralization(boolean unmappedGeneralization) {
		this.unmappedGeneralization = unmappedGeneralization;
	}
	
}
