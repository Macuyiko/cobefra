package be.kuleuven.econ.cbf.ui.metrics.other;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.other.AryaMetric;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.other.AryaAlgorithmChooser;
import be.kuleuven.econ.cbf.ui.metrics.other.WizardVisualizer;

@MetricVisualizer(metrics = {AryaMetric.class})
public class AryaMetricVisualizer extends WizardVisualizer {
	protected AryaMetric metric;
	
	private Petrinet petrinet;
	private XLog log;
	private TransEvClassMapping mapping;
	private boolean booted; 
	
	private static int BOOT_STEP = 1;
	
	private void performBootSetup(int panelIndex) {
		if (booted) return;
		booted = true;
		
		btnPrevious.setEnabled(false);
		btnNext.setEnabled(false);
		JLabel lblBooting = new JLabel("...");
		lblBooting.setHorizontalAlignment(SwingConstants.CENTER);  
		
		scrollPane.setViewportView(lblBooting);
		content.paintAll(content.getGraphics());
		
		try {
			lblBooting.setText("Retrieving algorithms, please wait...");
			content.paintAll(content.getGraphics());
			Boot.boot(AryaMetricVisualizer.class, PluginContext.class);
		} catch (Exception e) {
			lblBooting.setText("Uh oh, something's gone wrong");
			content.paintAll(content.getGraphics());
			e.printStackTrace();
		}
		
		btnPrevious.setEnabled(true);
		btnNext.setEnabled(true);
		
		AryaAlgorithmChooser algorithmStep = new AryaAlgorithmChooser(petrinet, log, mapping, 
				metric.getChosenAlgorithm() == null ? null : metric.getChosenAlgorithm().getClass());
		panels.set(panelIndex, algorithmStep);
		
		currentStep = panelIndex;
		
		showPanel(currentStep);
	}
	
	@Override
	protected void showPanel(int number) {
		if (currentStep == BOOT_STEP && !booted) {
			performBootSetup(currentStep);
			return;
		}
		super.showPanel(number);
	}

	@Bootable
	public static void kicker(Object o) {
	}

	@Override
	protected void buildPanels() {
		// Marking options
		panels.add(new AryaMarkingConfiguration(metric.isCreateInitialMarking(), metric.isCreateFinalMarking()));
		// Set next one to null, this will be booted later
		BOOT_STEP = panels.size();
		panels.add(null);
	}

	@Override
	protected void applyParameters() {
		metric.setCreateInitialMarking(((AryaMarkingConfiguration)panels.get(0)).isCreateInitial());
		metric.setCreateFinalMarking(((AryaMarkingConfiguration)panels.get(0)).isCreateFinal());
		metric.setChosenAlgorithm(((AryaAlgorithmChooser)panels.get(BOOT_STEP)).getAlgorithm());
		
	}

	@Override
	protected void performSetup(AbstractMetric m) {
		metric = (AryaMetric) m;
		booted = false;
		
		// Create a dummy petrinet containing all possible transition
		// We can use this in the "generic" algorithm configuration screens
		// And then afterwards (for the actual benchmark runs) set the real
		// costs etc...
		petrinet = PetrinetFactory.newPetrinet("Dummy Petri net");
		Transition visibleMapped = petrinet.addTransition("Visible Mapped");
		Transition visibleUnmapped = petrinet.addTransition("Visible Unmapped");
		Transition invisibleUnmapped = petrinet.addTransition("Invisible Unmapped");
		invisibleUnmapped.setInvisible(true);
		
		// Create a dummy log, same concept.
		XFactory xFactory = new XFactoryBufferedImpl();
		log = xFactory.createLog();
		log.getAttributes().put("concept:name",
				xFactory.createAttributeLiteral("concept:name", "Dummy Event Log", XConceptExtension.instance()));
		log.getAttributes().put("lifecycle:model",
				xFactory.createAttributeLiteral("lifecycle:model", "standard", XLifecycleExtension.instance()));
		XTrace trace = xFactory.createTrace();
		trace.getAttributes().put("concept:name",
				xFactory.createAttributeLiteral("concept:name", "Dummy Event Trace", XConceptExtension.instance()));
		
		XEvent mappedEvent = xFactory.createEvent();
		XAttributeMap mappedEventA = xFactory.createAttributeMap();
		mappedEventA.put("concept:name",
					xFactory.createAttributeLiteral("concept:name", "Mapped_Event", XConceptExtension.instance()));
		mappedEventA.put("lifecycle:transition",
					xFactory.createAttributeLiteral("lifecycle:transition", "complete",
							XLifecycleExtension.instance()));
		mappedEvent.setAttributes(mappedEventA);
		
		XEvent unmappedEvent = xFactory.createEvent();
		XAttributeMap unmappedEventA = xFactory.createAttributeMap();
		unmappedEventA.put("concept:name",
					xFactory.createAttributeLiteral("concept:name", "Unmapped_Event", XConceptExtension.instance()));
		unmappedEventA.put("lifecycle:transition",
					xFactory.createAttributeLiteral("lifecycle:transition", "complete",
							XLifecycleExtension.instance()));
		unmappedEvent.setAttributes(unmappedEventA);
		
		trace.add(mappedEvent);
		trace.add(unmappedEvent);
		
		log.add(trace);
		
		// Create the mapping. We have all possible variants of trace-class combinations now.
		mapping = new TransEvClassMapping(XLogInfoImpl.STANDARD_CLASSIFIER,
				EvClassLogPetrinetConnectionFactoryUI.DUMMY);
		mapping.put(visibleMapped, new XEventClass("Mapped_Event+complete", 10));
		mapping.put(visibleUnmapped, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
		mapping.put(invisibleUnmapped, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
	}
	
	

}
