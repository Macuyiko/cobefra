package be.kuleuven.econ.cbf.metrics.precision;

import java.util.Map;
import java.util.TreeMap;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.etconformance.ETCAlgorithm;
import org.processmining.plugins.etconformance.ETCResults;
import org.processmining.plugins.etconformance.ETCSettings;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.utils.MappingUtils;

@Metric(genericName = "ETC Conformance", author = "Munoz-Gama et al.", classification = "Precision")
public class ETConformance extends AbstractSimpleMetric {

	private Petrinet petrinet = null;
	private UIPluginContext context = null;
	private XLog log = null;
	private Marking initialMarking = null;
	private ETCResults res = new ETCResults();
    private ETCSettings settings = new ETCSettings(res);
	private TransEvClassMapping transMapping = null;
	
	@Override
	public synchronized void calculate() {
		String logName = XConceptExtension.instance().extractName(log);
	    String modelName = petrinet.getLabel();

	    res.setModelName(modelName);
	    res.setLogName(logName);
	    
	    try {
			ETCAlgorithm.exec(context, log, petrinet, initialMarking, transMapping, res);
		} catch (Exception e) {
			System.err.println(e.getStackTrace());
		}
	   
	    setResult(res.getEtcp());
	}
	
	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		context = new FakePluginContext();
		petrinet = mapping.getPetrinet();
		log = mapping.getLog();
		MappingUtils.setInvisiblesInPetrinet(mapping, petrinet);
		initialMarking = PetrinetUtils.getInitialMarking(petrinet);
		transMapping = MappingUtils.getTransEvClassMapping(mapping, petrinet, log);
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> properties = new TreeMap<String, String>();
		properties.put("dEscTh", ""+res.getEscTh());
		properties.put("bConfidence", res.isConfidence() ? "1" : "0");
		properties.put("ikConfidence", ""+res.getkConfidence());
		properties.put("bMdt", res.isMdt() ? "1" : "0");
		properties.put("bSeverity", res.isSeverity() ? "1" : "0");
		properties.put("dSeverityTh", ""+res.getSeverityTh());
		properties.put("bAutomaton", res.isAutomaton() ? "1" : "0");
		properties.put("bLazyInv", res.isLazyInv() ? "1" : "0");
		properties.put("bRandomIndet", res.isRandomIndet() ? "1" : "0");
		return properties;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		res.setEscTh(Double.parseDouble(properties.get("dEscTh")));
	    res.setConfidence(properties.get("bConfidence").equals("1"));
	    res.setkConfidence(Integer.parseInt(properties.get("ikConfidence")));
	    res.setMdt(properties.get("bMdt").equals("1"));
	    res.setSeverity(properties.get("bSeverity").equals("1"));
	    res.setSeverityTh(Double.parseDouble(properties.get("dSeverityTh")));
	    res.setAutomaton(properties.get("bAutomaton").equals("1"));
	    res.setLazyInv(properties.get("bLazyInv").equals("1"));
	    res.setRandomIndet(properties.get("bRandomIndet").equals("1"));
	}

	public ETCSettings getSettings() {
		return settings;
	}

	public void setSettings(ETCSettings settings) {
		this.settings = settings;
		this.settings.setSettings();
	}
}
