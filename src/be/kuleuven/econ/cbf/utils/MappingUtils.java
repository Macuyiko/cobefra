package be.kuleuven.econ.cbf.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.connections.logmodel.LogPetrinetConnection;
import org.processmining.connections.logmodel.LogPetrinetConnectionImpl;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.EvClassLogPetrinetConnectionFactoryUI;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.input.Mapping.Activity;

/**
 * A bunch of mapping utilities. Could be cleaned up...
 * @author n11093
 *
 */
public class MappingUtils {
	
	private static XEventClassifier defaultClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
	
	public static void setInvisiblesInPetrinet(Mapping mapping, Petrinet targetNet) {
		for (Transition t : targetNet.getTransitions()) {
			t.setInvisible(mapping.getActivityInvisible(t.getLabel()));
		}
	}
	
	public static XEventClass activityToEventClass(Activity act) {
		return new XEventClass(act.getName() + "+" + act.getType(), 0);
	}
	
	public static List<Activity> addUnmappedInPetrinet(Mapping mapping, Petrinet targetNet) {
		List<Activity> mappedActivities = new ArrayList<Activity>();
		for (Activity a : mapping.getActivities()) {
			if (mapping.getTransitions(a).length == 0) {
				Transition t = targetNet.addTransition(a.getName());
				Place p = targetNet.addPlace("Fake place");
				targetNet.addArc(p, t);
				mapping.setActivity(a.getName(), a, false);
				mappedActivities.add(a);
			}
		}
		return mappedActivities;
	}
	
	public static void removeUnmappedInMapping(Mapping mapping, List<Activity> toRemove) {
		for (Activity a : toRemove) {
			mapping.unsetActivity(a.getName());
		}
	}
	
	public static PetrinetLogMapper getPetrinetLogMapper(Mapping mapping, Petrinet net, XLog log) {
		PetrinetLogMapper m = new PetrinetLogMapper(
				defaultClassifier, 
				log, 
				net.getTransitions());
		
		for (Transition transition : m.getTransitions()) {
			if (mapping.getActivity(transition.getLabel()) == null) {
				// Unmapped activities should get an entry (non-invisible, blocking)
				// or remain unmapped (will be seen as invisible)
				if (!mapping.getActivityInvisible(transition.getLabel()))
					m.put(transition, PetrinetLogMapper.BLOCKING_CLASS);
				continue;
			}
			
			// Mapped activities are set to their event class
			String mappedId = activityToEventClass(mapping.getActivity(transition.getLabel())).getId();
			for (XEventClass clazz : m.getEventClasses()) {
				if (mappedId.equals(clazz.getId())) {
					m.put(transition, clazz);
					break;
				}
			}
		}
		
		return m;
	}
	
	public static TransEvClassMapping getTransEvClassMapping(Mapping mapping, Petrinet net, XLog log) {
		TransEvClassMapping transEvClassMapping = new TransEvClassMapping(defaultClassifier, EvClassLogPetrinetConnectionFactoryUI.DUMMY);
		Collection<XEventClass> eventClasses = XEventClasses.deriveEventClasses(defaultClassifier, log).getClasses();
		for (Transition transition : net.getTransitions()) {
			// Skip if unmapped and visible
			// If invisible, we should assign a dummy
			XEventClass found = EvClassLogPetrinetConnectionFactoryUI.DUMMY;
			Activity act = mapping.getActivity(transition.getLabel());
			if (act != null) {
				String mappedId = activityToEventClass(act).getId();
				for (XEventClass clazz : eventClasses) {
					if (mappedId.equals(clazz.getId())) {
						found = clazz;
						break;
					}
				}
			}
			transEvClassMapping.put(transition, found);
		}
		return transEvClassMapping;
	}

	public static EvClassLogPetrinetConnection getEvClassLogPetrinetConnection(Mapping mapping, Petrinet net, XLog log) {
		EvClassLogPetrinetConnection connection = 
				new EvClassLogPetrinetConnection("Mapping", 
						(PetrinetGraph) net, log,
						defaultClassifier,
						getTransEvClassMapping(mapping, net, log));
		return connection;
	}

	public static LogPetrinetConnection getLogPetrinetConnection(Mapping mapping, Petrinet net, XLog log) {
		XEventClasses classes = XEventClasses.deriveEventClasses(defaultClassifier, log);
		LogPetrinetConnection newConnection = 
				new LogPetrinetConnectionImpl(log, classes, 
						net, getLogPetrinetMapping(mapping, net, log));
		return newConnection;
	}

	public static Set<Pair<Transition, XEventClass>> getLogPetrinetMapping(Mapping mapping, Petrinet net, XLog log) {
		TransEvClassMapping evMapping = getTransEvClassMapping(mapping, net, log);
		Set<Pair<Transition, XEventClass>> m = new HashSet<Pair<Transition, XEventClass>>();
		for (Transition transition : net.getTransitions()) {
			if (evMapping.containsKey(transition)
					&& !evMapping.get(transition).equals(
							EvClassLogPetrinetConnectionFactoryUI.DUMMY)) {
				Pair<Transition, XEventClass> newPair = new Pair<Transition, XEventClass>(
						transition, evMapping.get(transition));
				m.add(newPair);
			}
		}
		return m;
	}
}
