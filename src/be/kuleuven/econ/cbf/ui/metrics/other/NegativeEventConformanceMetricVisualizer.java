package be.kuleuven.econ.cbf.ui.metrics.other;

import java.util.HashMap;
import java.util.Map;

import org.processmining.plugins.neconformance.ui.wizard.WizardSettingsPanel;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;
import be.kuleuven.econ.cbf.metrics.other.NegativeEventConformanceMetric;

@MetricVisualizer(metrics = {NegativeEventConformanceMetric.class})
public class NegativeEventConformanceMetricVisualizer extends WizardVisualizer {
	private NegativeEventConformanceMetric metric;
	
	private WizardSettingsPanel settingsPanel;
	
	@Override
	protected void buildPanels() {
		settingsPanel = new WizardSettingsPanel();
		UISettings.applyUI(settingsPanel, true);
		
		panels.add(settingsPanel);
	}

	@Override
	protected void applyParameters() {
		Map<String, Object> settings = settingsPanel.getSettings();
		metric.setReplayer(Integer.parseInt(settings.get("replayer").toString()));
		metric.setUseWeighted(settings.get("useWeighted").toString().equals("1"));
		metric.setUseCutOff(settings.get("useCutOff").toString().equals("1"));
		metric.setUseBothRatios(settings.get("useBothRatios").toString().equals("1"));
		metric.setInducer(settings.get("useBagInducer").toString().equals("1") ? 1 : 0);
		metric.setUnmappedRecall(settings.get("unmappedRecall").toString().equals("1"));
		metric.setUnmappedPrecision(settings.get("unmappedPrecision").toString().equals("1"));
		metric.setUnmappedGeneralization(settings.get("unmappedGeneralization").toString().equals("1"));
		metric.setNegWindow(Integer.parseInt(settings.get("negWindow").toString()));
		metric.setGenWindow(Integer.parseInt(settings.get("genWindow").toString()));
		metric.setMultiThreaded(settings.get("useMultithreaded").toString().equals("1"));
	}

	@Override
	protected void performSetup(AbstractMetric m) {
		settingsPanel = new WizardSettingsPanel();
		metric = (NegativeEventConformanceMetric) m;
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("replayer", metric.getReplayer());
		settings.put("useBagInducer", metric.getInducer() == 1 ? "1" : "0");
		settings.put("useWeighted", metric.isUseWeighted() ? "1" : "0");
		settings.put("useBothRatios", metric.isUseBothRatios() ? "1" : "0");
		settings.put("useCutOff", metric.isUseCutOff() ? "1" : "0");
		settings.put("negWindow", ""+metric.getNegWindow());
		settings.put("genWindow", ""+metric.getGenWindow());
		settings.put("unmappedRecall", metric.isUnmappedRecall() ? "1" : "0");
		settings.put("unmappedPrecision", metric.isUnmappedPrecision() ? "1" : "0");
		settings.put("unmappedGeneralization", metric.isUnmappedGeneralization() ? "1" : "0");
		settings.put("useMultithreaded", metric.isMultiThreaded() ? "1" : "0");
		
		settingsPanel.setSettings(settings);

	}

}
