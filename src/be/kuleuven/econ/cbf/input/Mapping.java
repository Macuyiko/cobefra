package be.kuleuven.econ.cbf.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.Pnml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import be.kuleuven.econ.cbf.utils.LogTool;
import be.kuleuven.econ.cbf.utils.PetrinetTool;

/**
 * This class is a representation of the way a log file and a petrinet are
 * mapped on each other. This mapping consists of mapping the transitions of a
 * petrinet onto the activities found in the logfile. For this purpose, all
 * transitions with the same activity are regarded as different transitions, but
 * mapped to the same activity. All activities with the same combination of
 * activity and status on the other hand are defined as being identical.
 * 
 * There are three ways activities can be mapped on transitions. The most
 * straightforward way is having a transition mapped onto an activity. Other
 * than that, a transition can be unmapped and invisible or unmapped and
 * visible. Transitions that have been mapped cannot be invisible.
 * 
 * Instances of this class are not actually associated with the log and petrinet
 * data. Instead they read this information from the file system and maintain
 * links to this information. This ensures memory consumption of a class is
 * maintainable (although the mapping may still take a considerable amount of
 * space). This class does provide an interface to obtain these resources, but
 * its objects will not maintain references to these objects.
 * 
 * @author Niels Lambrigts (niels.lambrigts@gmail.com)
 * @author Seppe vanden Broucke (Seppe.vandenBroucke@kuleuven.be)
 * @author Jochen De Weerdt (Jochen.DeWeerdt@econ.kuleuven.be)
 */
public class Mapping {

	/**
	 * Represents a single activity. An activity is uniquely denoted by its name
	 * and type.
	 */
	public static class Activity implements Comparable<Activity> {

		/**
		 * The name of the Activity. In XES logs this corresponds to the
		 * "concept:name".
		 */
		protected final String name;

		/**
		 * The type of the Activity. In XES logs this corresponds to the
		 * "transition:lifecycle".
		 */
		protected final String type;

		public Activity(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public Activity(XEvent xevent) {
			XAttributeMap attr = xevent.getAttributes();
			XAttribute aName = attr.get("concept:name");
			XAttribute aType = attr.get("lifecycle:transition");
			this.name = aName == null ? "" : aName.toString();
			this.type = aType == null ? "" : aType.toString();
		}

		@Override
		public int compareTo(Activity a) {
			return this.toString().compareTo(a.toString());
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Activity)
				return compareTo((Activity) o) == 0;
			else
				return false;
		}

		/**
		 * The name of the Activity. In XES logs this corresponds to the
		 * "concept:name".
		 */
		public String getName() {
			return name;
		}

		/**
		 * The type of the Activity. In XES logs this corresponds to the
		 * "transition:lifecycle".
		 */
		public String getType() {
			return type;
		}

		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public String toString() {
			return name + " (" + type + ")";
		}
	}

	/**
	 * Internal class used to remember both a activity and certaincy for an
	 * activity matching.
	 */
	private class MapElement {
		Activity activity;
		float certaincy;
		boolean invisible;
	}

	/**
	 * Minimum certaincy value to assume a mapping is correct.
	 */
	public static final float CERTAINCY_THRESHOLD = 0.9f;

	/**
	 * Read a Mapping object from the given InputStream and return it as the
	 * result of this method.
	 */
	public static Mapping readMapping(InputStream stream) throws IOException {
		String exceptionMessage = "Specified source does not contain a Mapping object";
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.parse(stream);
			// Version checks can happen here
			Element root = document.getDocumentElement();
			if (!root.getNodeName().equals("mapping"))
				throw new InputMismatchException(exceptionMessage);
			boolean trusted = root.getAttribute("trusted").equals("true");
			// Read the logpath
			String logpath;
			{
				NodeList list = root.getElementsByTagName("logpath");
				if (list.getLength() != 1)
					throw new InputMismatchException(exceptionMessage);
				Node node = list.item(0);
				Node value = node.getAttributes().getNamedItem("value");
				if (value == null)
					throw new InputMismatchException(exceptionMessage);
				logpath = value.getNodeValue();
			}
			// Read the netpath
			String netpath;
			{
				NodeList list = root.getElementsByTagName("netpath");
				if (list.getLength() != 1)
					throw new InputMismatchException(exceptionMessage);
				Node node = list.item(0);
				Node value = node.getAttributes().getNamedItem("value");
				if (value == null)
					throw new InputMismatchException(exceptionMessage);
				netpath = value.getNodeValue();
			}
			// Create the mapping
			Mapping mapping = null;
			try {
				mapping = new Mapping(logpath, netpath, !trusted, false);
			} catch (IllegalArgumentException e) {
				throw new IOException(e);
			}
			if (trusted) {
				// Read all transitions
				List<String> transitions = new ArrayList<String>();
				NodeList list = root.getElementsByTagName("transition");
				for (int i = 0; i < list.getLength(); i++)
					transitions.add(list.item(i).getTextContent());
				mapping.transitions = transitions;

				// Read all activities
				List<Activity> activities = new ArrayList<Activity>();
				list = root.getElementsByTagName("activity");
				for (int i = 0; i < list.getLength(); i++) {
					NamedNodeMap aMap = list.item(i).getAttributes();
					Node nName = aMap.getNamedItem("name");
					Node nType = aMap.getNamedItem("type");
					if (nName == null || nType == null)
						throw new InputMismatchException(exceptionMessage);
					String name = nName.getNodeValue();
					String type = nType.getNodeValue();
					activities.add(new Activity(name, type));
				}
				mapping.activities = activities;
			}
			// Read each transition
			{
				NodeList list = root.getElementsByTagName("map");
				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					NamedNodeMap map = node.getAttributes();
					Node nodeActivityName = map.getNamedItem("activity-name");
					Node nodeActivityType = map.getNamedItem("activity-type");
					Node nodeCertaincy = map.getNamedItem("certaincy");
					Node nodeInvisible = map.getNamedItem("invisible");
					Node nodeName = map.getNamedItem("transition");
					if (nodeCertaincy == null || nodeInvisible == null
							|| nodeName == null)
						throw new InputMismatchException(exceptionMessage);
					String transition = nodeName.getNodeValue();
					Activity activity = null;
					if (nodeActivityName != null && nodeActivityType != null) {
						String activityName = nodeActivityName.getNodeValue();
						String activityType = nodeActivityType.getNodeValue();
						activity = new Activity(activityName, activityType);
					}
					boolean invisible = Boolean.parseBoolean(nodeInvisible
							.getNodeValue());
					float certaincy = Float.parseFloat(nodeCertaincy
							.getNodeValue());
					mapping.setActivity(transition, activity, invisible,
							certaincy);
				}
			}
			return mapping;
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		}
		return null;
	}

	/**
	 * List containing all activities in the log. Duplicates are merged as being
	 * one activity.
	 */
	private List<Activity> activities;

	/**
	 * Location on the file system where the log file can be found.
	 */
	private String logPath;

	/**
	 * Map containing the actual mapping of a transition on an activity. In this
	 * map the transition activity is used as key.
	 */
	private Map<String, MapElement> mapping;

	/**
	 * Location on the file system where a petrinet can be found.
	 */
	private String petrinetPath;

	/**
	 * List containing all transitions in the petrinet. Duplicates exist for
	 * transitions with the same activity, but on a different node.
	 */
	private List<String> transitions;

	/**
	 * Create a new mapping for the given logfile and netfile. This constructor
	 * will ensure all data structures of this object are correctly initialised
	 * with as much determinable data as possible. This includes determining as
	 * much of the mapping as possible using default heuristics.
	 * 
	 * @param logfile
	 *            Location on the file system where a logfile can be found.
	 * @param netfile
	 *            Location on the file system where a petrinet can be found.
	 */
	public Mapping(String logfile, String netfile) {
		this(logfile, netfile, true, true);
	}
	
	/**
	 * Create a new mapping for the given log and petrinet.
	 * Will serialize both to a temporary disk location.
	 * @throws IOException 
	 */
	public Mapping(XLog log, Petrinet net) throws IOException {
		File pnmlTemp = File.createTempFile("cbf", ".pnml");
		File xesTemp = File.createTempFile("cbf", ".xes");
		
		// Serialize net
		Pnml.PnmlType type = Pnml.PnmlType.PNML;
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		HashMap<PetrinetGraph, Marking> markedNets = new HashMap<PetrinetGraph, Marking>();
		markedNets.put(net, new Marking());
		Pnml pnml = new Pnml().convertFromNet(markedNets, layout);
		pnml.setType(type);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n" + pnml.exportElement(pnml);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pnmlTemp)));
		bw.write(text);
		bw.close();
		
		// Serialize log
		XSerializer ser = new XesXmlSerializer();
		FileOutputStream fos = new FileOutputStream(xesTemp);
		ser.serialize(log, fos);
		
		this.logPath = xesTemp.getAbsolutePath();
		this.petrinetPath = pnmlTemp.getAbsolutePath();
		
		mapping = new HashMap<String, MapElement>();
		
		HashSet<Activity> activities = new HashSet<Activity>();
		for (XTrace trace : log)
			for (XEvent event : trace) {
				Activity activity = new Activity(event);
				if (!activities.contains(activity))
					activities.add(activity);
			}
		this.activities = new ArrayList<Activity>();
		this.activities.addAll(activities);
		Collections.sort(this.activities);

		transitions = new ArrayList<String>();
		for (Transition t : net.getTransitions())
			transitions.add(t.getLabel());
		Collections.sort(transitions);
		
		doMapping();
	}

	/**
	 * Create a new mapping for the given logfile and netfile. This constructor
	 * will ensure all data structures of this object are correctly initialised
	 * and this is requested, it will initialise as much of the mapping as
	 * possible using information that can be deducted from the input.
	 * 
	 * @param logfile
	 *            Location on the file system where a logfile can be found.
	 * @param netfile
	 *            Location on the file system where a petrinet can be found.
	 * @param doReadFiles
	 *            Whether or not to initialise the mapping with the transitions
	 *            and activity types that can be found in the specified files.
	 *            If this boolean is false you will need to set these in another
	 *            way. You could call {@link #readActivities()} and
	 *            {@link #readTransitions()} to do this.
	 * @param doMapping
	 *            Whether or not to initialise the mapping with information that
	 *            can be deducted from the input. This boolean has no effect if
	 *            doReadFiles is set to false.
	 */
	protected Mapping(String logfile, String netfile, boolean doReadFiles,
			boolean doMapping) {
		this.logPath = logfile;
		this.petrinetPath = netfile;
		mapping = new HashMap<String, MapElement>();
		if (doReadFiles) {
			readActivities();
			readTransitions();
			if (doMapping)
				doMapping();
			else
				for (String transition : transitions) {
					MapElement mapElement = new MapElement();
					mapElement.activity = null;
					mapElement.certaincy = 0.0f;
					mapElement.invisible = false;
					mapping.put(transition, mapElement);
				}
		}
	}

	/**
	 * Use heuristics to determine as much of the mapping as possible.
	 */
	protected void doMapping() {
		for (String transition : transitions)
			mapping.put(transition, getLikelyActivity(transition));
	}

	/**
	 * Return an array containing all activities that are known in this mapping.
	 * This includes activities that have been mapped on a transition as well as
	 * activities that have not been mapped. This array will not contain any
	 * duplicates since activities with the same identifier will be regarded as
	 * identical.
	 */
	public Activity[] getActivities() {
		return activities.toArray(new Activity[activities.size()]);
	}

	/**
	 * Return the activity to which the transition with the given activity
	 * corresponds. This method may return null, indicating the given transition
	 * is an unmapped transition.
	 * 
	 * @param transition
	 *            The transition whose activity should be requested. This string
	 *            should be equal to a string contained in the array returned by
	 *            {@link #getTransitions()}.
	 * @see #isInvisibleTransition(String)
	 */
	public Activity getActivity(String transition) {
		MapElement t = mapping.get(transition);
		if (t == null)
			return null;
		return t.activity;
	}

	/**
	 * Return the certaincy with which the transition is matched to its
	 * corresponding activity. This certaincy is a floating point value between
	 * (and including) 0.0f and 1.0f, where 0.0f incidates absolute uncertaincy
	 * and 1.0f indicates absolute certaincy.
	 * 
	 * @param transition
	 *            The transition whose activity certaincy should be requested.
	 *            This string should be equal to a string contained in the array
	 *            returned by {@link #getTransitions()}.
	 */
	public float getActivityCertaincy(String transition) {
		return mapping.get(transition).certaincy;
	}

	/**
	 * Return whether or not the given transition is invisible. Only an unmapped
	 * activity can be invisible, but an unmapped activity can be visible as
	 * well.
	 * 
	 * @param transition
	 *            The transition whose activity should be requested. This string
	 *            should be equal to a string contained in the array returned by
	 *            {@link #getTransitions()}.
	 */
	public boolean getActivityInvisible(String transition) {
		return mapping.get(transition).invisible;
	}

	/**
	 * Determine the activity that is most likely for the given transition. This
	 * method will not return null, but instead an activity object that
	 * corresponds to "I have no clue". (This would be activity = null and
	 * certaincy = 0.0f.)
	 */
	private MapElement getLikelyActivity(String transition) {
		// Empty stuff is always invisible
		MapElement mapElement = new MapElement();
		mapElement.activity = null;
		mapElement.certaincy = 0.0f;
		mapElement.invisible = false;
		if (transition.equals(""))
			return mapElement;
		// Iterate over the activities
		for (Activity activity : activities) {
			// Strings we like:
			// NAME
			// NAME + TYPE
			// NAME+TYPE
			// NAME (TYPE)
			// String mkey = activity.toString().replace("\n", "+").trim();
			String uTransition = transition.replace("\\n", "+");
			uTransition = uTransition.replace("\n", "+");
			uTransition = uTransition.trim();
			float f1 = prefixMatching(uTransition, activity.name);
			float f2 = prefixMatching(uTransition, activity.name + " + "
					+ activity.type);
			float f3 = prefixMatching(uTransition, activity.name + "+"
					+ activity.type);
			float f4 = prefixMatching(uTransition, activity.name + " ("
					+ activity.type + ")");
			// Perhaps you could add activity.toString(), but that's currently
			float f12 = Math.max(f1, f2);
			float f34 = Math.max(f3, f4);
			float f = Math.max(f12, f34);
			if (f == 1.0f) {
				mapElement.certaincy = 1.0f;
				mapElement.activity = activity;
				return mapElement;
			} else if (f > mapElement.certaincy) {
				mapElement.activity = activity;
				mapElement.certaincy = f;
			}
		}
		// If nothing has been found for this transition,
		// check it for likely invisibles
		if (mapElement.activity == null && mapElement.certaincy < 1.0f)
			if (transition.matches("^(t[0-9]+)+$")) {
				mapElement.certaincy = 1.0f;
				mapElement.invisible = true;
			}
		// Only if we're quite sure we'll give an answer
		if (mapElement.certaincy >= CERTAINCY_THRESHOLD)
			return mapElement;
		else {
			mapElement.certaincy = 0.0f;
			mapElement.activity = null;
			mapElement.invisible = false;
			return mapElement;
		}
	}

	/**
	 * Creates and returns an {@link XLog} object corresponding to the log that
	 * is being represented by this Mapping object. Note that this object is not
	 * referred to by this class.
	 * 
	 * Calling this methods multiple times will yield multiple objects
	 * containing the same data. Doing this may result in excessively high
	 * memory usage.
	 */
	public XLog getLog() {
		XLog log = null;
		boolean ok = LogTool.isLogFile(getLogPath());
		if (ok)
			try {
				log = LogTool.readLog(getLogPath());
			} catch (Exception e) {
				ok = false;
			}
		if (!ok)
			throw new IllegalArgumentException("Illegal logfile specified");
		return log;
	}

	/**
	 * Returns the path where the log can be found on the filesystem.
	 */
	public String getLogPath() {
		return logPath;
	}

	/**
	 * Creates and returns a {@link Petrinet} object corresponding to the
	 * petrinet that is being represented by this Mapping object. Note that this
	 * object is not referred to by this class.
	 * 
	 * Calling this methods multiple times will yield multiple objects
	 * containing the same data. Doing this may result in excessively high
	 * memory usage.
	 */
	public Petrinet getPetrinet() {
		Petrinet net = null;
		boolean ok = PetrinetTool.isPetrinetFile(getPetrinetPath());
		if (ok)
			try {
				net = PetrinetTool.readPetrinet(getPetrinetPath());
			} catch (Exception e) {
				e.printStackTrace();
				ok = false;
			}
		if (!ok)
			throw new IllegalArgumentException("Illegal netfile specified: "+getPetrinetPath());
		return net;
	}
	
	public Object[] getPetrinetWithMarking() {
		Object[] netmarking = null;
		boolean ok = PetrinetTool.isPetrinetFile(getPetrinetPath());
		if (ok)
			try {
				netmarking = PetrinetTool.openPNML(getPetrinetPath());
			} catch (Exception e) {
				e.printStackTrace();
				ok = false;
			}
		if (!ok)
			throw new IllegalArgumentException("Illegal netfile specified: "+getPetrinetPath());
		return netmarking;
	}

	/**
	 * Returns the path where the petrinet file can be found on the filesystem.
	 */
	public String getPetrinetPath() {
		return petrinetPath;
	}

	/**
	 * Returns an array containing all transitions known to this mapping. This
	 * array can include duplicate values for transitions that exist in the
	 * petrinet with the same activity.
	 */
	public String[] getTransitions() {
		return transitions.toArray(new String[transitions.size()]);
	}

	/**
	 * Returns a list of transitions that are mapped onto the given activity.
	 */
	public String[] getTransitions(Activity activity) {
		List<String> transitions = new ArrayList<String>();
		for (String transition : getTransitions()) {
			Activity a = getActivity(transition);
			if (activity == null || a == null) {
				if (activity == a)
					transitions.add(transition);
			} else if (activity.equals(a))
				transitions.add(transition);
		}
		return transitions.toArray(new String[transitions.size()]);
	}

	/**
	 * Add information to this mapping by copying the mapping information for
	 * the given transition from the given mapping if this information is
	 * determined to be more certain. Calling this method may overwrite some
	 * mapping information, but only if this information is more certain than
	 * the information that was already in this mapping.
	 */
	public void induceMapping(Mapping other, String transition) {
		MapElement mine = mapping.get(transition);
		MapElement his = other.mapping.get(transition);
		if (his == null)
			return;
		if (mine == null) {
			mine = new MapElement();
			mine.activity = null;
			mine.certaincy = 0.0f;
			mine.invisible = false;
		}
		if (his.certaincy * CERTAINCY_THRESHOLD > mine.certaincy) {
			mine.activity = his.activity;
			mine.certaincy = his.certaincy;
			mine.invisible = his.invisible;
		}
	}

	/**
	 * Add information to this mapping by copying mapping information from the
	 * given mapping. Calling this method may overwrite some mapping
	 * information, but only if this information is more certain than the
	 * information that was already in this mapping.
	 */
	public void induceMappingFrom(Mapping other) {
		for (String transition : transitions)
			induceMapping(other, transition);
	}
	
	/**
	 * Assign all unmapped transitions to be visible unmapped (blocking).
	 */
	public void assignUnmappedToVisible() {
		for (Entry<String, MapElement> e : mapping.entrySet()) {
			if (e.getValue().activity == null) {
				e.getValue().invisible = false;
				e.getValue().certaincy = 1;
			}
		}
	}

	/**
	 * Assign all unmapped transitions to be invisible unmapped (silent).
	 */
	public void assignUnmappedToInvisible() {
		for (Entry<String, MapElement> e : mapping.entrySet()) {
			if (e.getValue().activity == null) {
				e.getValue().invisible = true;
				e.getValue().certaincy = 1;
			}
		}
	}

	/**
	 * Returns true if and only if every transition is matched to an event with
	 * appropriate certaincy. In every other case, this method will return
	 * false.
	 */
	public boolean isComplete() {
		for (String transition : transitions)
			if (mapping.get(transition).certaincy < CERTAINCY_THRESHOLD)
				return false;
		return true;
	}

	/**
	 * Match two strings and return on a scale from 0 to 1 how much they match.
	 * Use prefix matching, because we're smart.
	 */
	private float prefixMatching(String one, String two) {
		int minLength = Math.min(one.length(), two.length());
		int nbMatched = 0;
		while (nbMatched < minLength
				&& one.charAt(nbMatched) == two.charAt(nbMatched))
			nbMatched++;
		int maxLength = Math.max(one.length(), two.length());
		float matched = (float) nbMatched / (float) maxLength;
		return matched;
	}

	/**
	 * Create the list of activities by reading the log.
	 */
	protected void readActivities() {
		XLog log = getLog();
		HashSet<Activity> activities = new HashSet<Activity>();
		for (XTrace trace : log)
			for (XEvent event : trace) {
				Activity activity = new Activity(event);
				if (!activities.contains(activity))
					activities.add(activity);
			}
		this.activities = new ArrayList<Activity>();
		this.activities.addAll(activities);
		Collections.sort(this.activities);
	}

	/**
	 * Create the list of transitions by reading them from the petrinet.
	 */
	protected void readTransitions() {
		Petrinet net = getPetrinet();
		transitions = new ArrayList<String>();
		for (Transition t : net.getTransitions())
			transitions.add(t.getLabel());
		Collections.sort(transitions);
	}

	/**
	 * Change the mapping by mapping the given transition on the given activity.
	 * Using this method will change the certaincy of this particular mapping to
	 * 1.0f (absolute certaincy).
	 * 
	 * @param transition
	 *            The transition whose activity should be requested. This string
	 *            should be equal to a string contained in the array returned by
	 *            {@link #getTransitions()}, or null to indicate no mapping.
	 * @param activity
	 *            The activity to which the given transition should correspond.
	 *            This string should be equal to a string contained in the array
	 *            returned by {@link #getActivities()}.
	 * @param invisible
	 *            Indicates whether or not the transition is invisible. A
	 *            transition cannot be both mapped (transition != null) and
	 *            invisible.
	 */
	public void setActivity(String transition, Activity activity, boolean invisible) {
		setActivity(transition, activity, invisible, 1.0f);
	}

	/**
	 * Change the mapping by mapping the given transition on the given activity.
	 * The certaincy of this mapping will be set to the given value.
	 * 
	 * @param transition
	 *            The transition whose activity should be requested. This string
	 *            should be equal to a string contained in the array returned by
	 *            {@link #getTransitions()}.
	 * @param activity
	 *            The activity to which the given transition should correspond.
	 *            This string should be equal to a string contained in the array
	 *            returned by {@link #getActivities()}.
	 * @param certaincy
	 *            The certaincy of this particular mapping. This should be a
	 *            float in the (inclusive) range 0.0f to 1.0f.
	 * @param invisible
	 *            Indicates whether or not the transition is invisible. A
	 *            transition cannot be both mapped (transition != null) and
	 *            invisible.
	 */
	public void setActivity(String transition, Activity activity, boolean invisible, float certaincy) {
		if (!transition.contains(transition))
			throw new IllegalArgumentException(
					"The given transition does not exist");
		if (activity != null && !activities.contains(activity))
			throw new IllegalArgumentException(
					"The given activity does not exist");
		if (certaincy < 0 || certaincy > 1)
			throw new IllegalArgumentException(
					"Certaincy must be between 0.0f and 1.0f");
		if (invisible && activity != null)
			throw new IllegalArgumentException(
					"A mapped transition cannot be invisible");
		MapElement a = mapping.get(transition);
		if (a == null) {
			a = new MapElement();
			mapping.put(transition, a);
		}
		a.certaincy = certaincy;
		a.activity = activity;
		a.invisible = invisible;
	}
	
	public void unsetActivity(String transition) {
		mapping.remove(transition);
	}

	/**
	 * Write out this mapping object to the given OutputStream. The information
	 * written out can afterwards be used by the method {@link #readMapping()}.
	 * 
	 * @param stream
	 *            The OutputStream on which to write the mapping object.
	 * @param trusted
	 *            Whether or not the storage on which the mapping is about to be
	 *            stored is considered 'safe'. This boils down to being able to
	 *            just copy over the entire object (safe storage) or let as much
	 *            checks happen as possible when the object is read again
	 *            (unsafe).
	 * @throws TransformerException
	 */
	public void writeMapping(OutputStream stream, boolean trusted)
			throws TransformerException {
		try {
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();

			// Create a root mapping object
			Element root = document.createElement("mapping");
			root.setAttribute("version", "1.0");
			if (trusted)
				root.setAttribute("trusted", "true");
			document.appendChild(root);

			// Write out the path to the logfile
			Element logpath = document.createElement("logpath");
			logpath.setAttribute("value", logPath);
			root.appendChild(logpath);

			// Write out the path to the netfile
			Element netpath = document.createElement("netpath");
			netpath.setAttribute("value", petrinetPath);
			root.appendChild(netpath);

			if (trusted) {
				// Write out the list of transitions
				for (String transition : transitions) {
					Element nTransition = document.createElement("transition");
					nTransition.setTextContent(transition);
					root.appendChild(nTransition);
				}
				// Write out the list of activities
				for (Activity activity : activities) {
					Element nActivity = document.createElement("activity");
					nActivity.setAttribute("name", activity.name);
					nActivity.setAttribute("type", activity.type);
					root.appendChild(nActivity);
				}
			}

			// Write out each mapped transition/activity
			for (Entry<String, MapElement> entry : mapping.entrySet()) {
				Element transition = document.createElement("map");
				if (entry.getValue().activity != null) {
					transition.setAttribute("activity-name",
							entry.getValue().activity.name);
					transition.setAttribute("activity-type",
							entry.getValue().activity.type);
				}
				transition.setAttribute("invisible",
						Boolean.toString(entry.getValue().invisible));
				transition.setAttribute("certaincy",
						Float.toString(entry.getValue().certaincy));
				transition.setAttribute("transition", entry.getKey());
				root.appendChild(transition);
			}

			// Write out the tree
			DOMSource source = new DOMSource(document);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult target = new StreamResult(stream);
			transformer.transform(source, target);
		} catch (ParserConfigurationException e) {
		} catch (TransformerConfigurationException e) {
		}
	}

	@Override
	public String toString() {
		String r = "";
		r += "Mapping Object [" + "LogPath: "+logPath+", PetriPath: "+petrinetPath+
				"#activities: "+activities.size()+", #transitions: "+transitions.size()+"]\n";
		for (Entry<String, MapElement> e : mapping.entrySet()) {
			if (e.getValue() == null || e.getValue().activity == null) {
				r += e.getKey() + "  -->  NULL";
			} else{ 
				r += e.getKey() + "  -->  " + e.getValue().activity.name;
				r += (e.getValue().invisible) ? "(inv)" : "";
			}
			r += "\n";
		}
		return r;
	}
	
}
