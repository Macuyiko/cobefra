package be.kuleuven.econ.cbf.metrics.recall;

import java.util.Map;
import java.util.TreeMap;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.behavioralconformance.BehavioralConformanceChecker;
import org.processmining.plugins.behavioralconformance.BehavioralConformanceChecker.ComplianceMetrics;
import org.processmining.plugins.behavioralconformance.MatthiasBehavioralConformanceChecker;
import org.processmining.plugins.behavioralconformance.SeppeBehavioralConformanceChecker;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.utils.MappingUtils;

@Metric(genericName = "Behavioral Profile Conformance", author = "Weidlich et al.", classification = "Recall")
public class BehavioralConformance extends AbstractSimpleMetric {

	private Petrinet petrinet = null;
	private XLog log = null;
	private PetrinetLogMapper logMapper = null;
	private org.processmining.models.semantics.petrinet.Marking initialMarking = null;

	private boolean useOriginalImplementation;
	private int valueToReturn;
	private ComplianceMetrics complianceMetric;

	public BehavioralConformance() {
		useOriginalImplementation = false;
		valueToReturn = 0;
		complianceMetric = ComplianceMetrics.MCC_MODEL_RELATIVE;
	}

	@Override
	public synchronized void calculate() {
		BehavioralConformanceChecker checker;
		if (useOriginalImplementation)
			checker = new MatthiasBehavioralConformanceChecker(log, petrinet, initialMarking, logMapper);
		else
			checker = new SeppeBehavioralConformanceChecker(log, petrinet, initialMarking, logMapper);

		double[] metricSummary = checker.getComplianceMetricSummary(complianceMetric);

		setResult(metricSummary[valueToReturn]);
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		Object[] netandmarking = mapping.getPetrinetWithMarking();
		petrinet = (Petrinet) netandmarking[0];
		log = mapping.getLog();
		MappingUtils.setInvisiblesInPetrinet(mapping, petrinet);
		logMapper = MappingUtils.getPetrinetLogMapper(mapping, petrinet, log);
		initialMarking = (org.processmining.models.semantics.petrinet.Marking) netandmarking[1];
		if (initialMarking == null || initialMarking.isEmpty()) {
			System.err.println("The initial marking in the net was empty -- trying to create one.");
			initialMarking = PetrinetUtils.getInitialMarking(petrinet);
		}
		if (initialMarking != null && initialMarking.isEmpty())
			System.err.println("An initial marking was constructed for the given Petri net, but its marking was empty. "
					+ "Unreliable results might follow. If you want to avoid this, add tokens using a Petri net "
					+ "editing tool.");
		if (initialMarking == null)
			System.err.println("No initial marking could be obtained.");
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> properties = new TreeMap<String, String>();
		properties.put("bUseOriginal", useOriginalImplementation ? "1" : "0");
		properties.put("iMetricValue", "" + valueToReturn);
		properties.put("complianceMetric", "" + complianceMetric.name());
		return properties;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		setUseOriginalImplementation(properties.get("bUseOriginal").equals("1"));
		setValueToReturn(Integer.parseInt(properties.get("iMetricValue")));
		setComplianceMetric(ComplianceMetrics.valueOf(properties.get("complianceMetric")));
	}

	public boolean isUseOriginalImplementation() {
		return useOriginalImplementation;
	}

	public void setUseOriginalImplementation(boolean useOriginalImplementation) {
		this.useOriginalImplementation = useOriginalImplementation;
	}

	public int getValueToReturn() {
		return valueToReturn;
	}

	public void setValueToReturn(int valueToReturn) {
		if (valueToReturn < 0 || valueToReturn > 4)
			throw new IllegalArgumentException("Cannot determine chosen return value: " + valueToReturn);
		else
			this.valueToReturn = valueToReturn;
	}

	public ComplianceMetrics getComplianceMetric() {
		return complianceMetric;
	}

	public void setComplianceMetric(ComplianceMetrics complianceMetric) {
		this.complianceMetric = complianceMetric;
	}

}
