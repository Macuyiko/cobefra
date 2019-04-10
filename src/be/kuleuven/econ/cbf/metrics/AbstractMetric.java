package be.kuleuven.econ.cbf.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.utils.ClassFinder;

@SuppressWarnings("unchecked")
public abstract class AbstractMetric {

	private static List<Class<? extends AbstractMetric>> allMetrics = null;
	private static Map<String, Integer> nbGenericInstances = new HashMap<String, Integer>();

	/**
	 * Create and return an instance of the specified metric type.
	 */
	public static AbstractMetric createInstance(
			Class<? extends AbstractMetric> type) {
		if (!isMetricClass(type))
			throw new InvalidMetricException();
		try {
			return type.getConstructor().newInstance();
		} catch (Exception e) {
			throw new InvalidMetricException();
		}
	}

	/**
	 * Returns a list of all known types of metrics.
	 */
	public static List<Class<? extends AbstractMetric>> getAllMetrics() {
		if (allMetrics == null)
			loadClass();
		ArrayList<Class<? extends AbstractMetric>> list = new ArrayList<Class<? extends AbstractMetric>>();
		list.addAll(allMetrics);
		return list;
	}

	/**
	 * Return the classification of the specified type.
	 */
	public static String getClassification(Class<? extends AbstractMetric> type) {
		if (!isMetricClass(type))
			throw new InvalidMetricException();
		Metric m = type.getAnnotation(Metric.class);
		return m.classification();
	}

	/**
	 * Returns the generic name of the specified metric type.
	 */
	public static String getGenericName(Class<? extends AbstractMetric> type) {
		if (!isMetricClass(type))
			throw new InvalidMetricException();
		Metric m = type.getAnnotation(Metric.class);
		return m.genericName();
	}
	
	/**
	 * Returns the author of the specified metric type.
	 */
	public static String getAuthor(Class<? extends AbstractMetric> type) {
		if (!isMetricClass(type))
			throw new InvalidMetricException();
		Metric m = type.getAnnotation(Metric.class);
		return m.author();
	}

	/**
	 * Returns true if and only if the specified class contains a metric.
	 */
	private static boolean isMetricClass(Class<?> type) {
		if (!AbstractMetric.class.isAssignableFrom(type)) {
			return false;
		}
		if (!type.isAnnotationPresent(Metric.class)) {
			return false;
		}
		try {
			type.getConstructor();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * This method should return true if this metric consists of any number of
	 * submetrics.
	 */
	public static boolean isWeighted(Class<?> type) {
		if (!isMetricClass(type))
			throw new InvalidMetricException();
		Metric m = type.getAnnotation(Metric.class);
		return m.isWeighted();
	}

	/**
	 * Ensure all information is loaded to work with this class.
	 */
	public static void loadClass() {
		allMetrics = new ArrayList<Class<? extends AbstractMetric>>();
		ClassFinder finder = new ClassFinder() {
			@Override
			public boolean isIncluded(Class<?> type) {
				return isMetricClass(type);
			}
		};

		Set<Class<?>> types = finder.getAllClasses("be.kuleuven.econ.cbf");
		for (Class<?> type : types)
			allMetrics.add((Class<? extends AbstractMetric>) type);
	}

	public static AbstractMetric readMetric(Node node) {
		String exceptionMessage = "This node does not contain a Metric";
		if (!node.getNodeName().equals("metric"))
			throw new IllegalArgumentException(exceptionMessage);
		NamedNodeMap attributes = node.getAttributes();

		// Read metric activity
		Node nName = attributes.getNamedItem("activity");
		if (nName == null)
			throw new IllegalArgumentException(exceptionMessage);
		String name = nName.getNodeValue();

		// Read metric type
		Node nType = attributes.getNamedItem("type");
		if (nType == null)
			throw new IllegalArgumentException(exceptionMessage);
		String type = nType.getNodeValue();

		// Read all child nodes
		NodeList children = node.getChildNodes();
		Map<String, String> properties = new TreeMap<String, String>();
		FlexibleSubmetricList list = new FlexibleSubmetricList();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals("metric")) {
				// Child node
				Node nIndex = child.getAttributes().getNamedItem("index");
				if (nIndex == null)
					throw new IllegalArgumentException(exceptionMessage);
				int index = Integer.parseInt(nIndex.getNodeValue());
				list.setSubmetric(index, readMetric(child));
			} else if (child.getNodeName().equals("property")) {
				// Property
				Node npName = child.getAttributes().getNamedItem("activity");
				if (npName == null)
					throw new IllegalArgumentException(exceptionMessage);
				String pName = npName.getNodeValue();
				String pValue = child.getTextContent();
				properties.put(pName, pValue);
			}
		}

		// Time to load the metric
		Class<?> rtt = null;
		try {
			rtt = Class.forName(type);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Unknown type, perhaps a metric type that is not available in this environment",
					e);
		}
		if (!AbstractMetric.isMetricClass(rtt))
			throw new IllegalArgumentException(
					"Metric type is known, but not an actual metric type: "
							+ type);
		AbstractMetric metric = AbstractMetric.createInstance((Class<? extends AbstractMetric>) rtt);
		metric.setName(name);
		metric.setSubmetrics(list);
		metric.setProperties(properties);
		return metric;
	}

	private ArrayList<MetricChangeListener> changeListeners;
	private boolean isResultSet;
	private String name;
	private double result;

	public AbstractMetric() {
		String genericName = getGenericName(this.getClass());
		if (!nbGenericInstances.containsKey(genericName))
			nbGenericInstances.put(genericName, 0);
		nbGenericInstances.put(genericName, nbGenericInstances.get(genericName) + 1);
		name = genericName + " #" + nbGenericInstances.get(genericName);
		changeListeners = new ArrayList<MetricChangeListener>();
		isResultSet = false;
		result = -1.0;
	}

	public abstract void add(Class<? extends AbstractMetric> m);

	public void addChangeListener(MetricChangeListener l) {
		if (!changeListeners.contains(l))
			changeListeners.add(l);
	}

	public abstract void addEmpty();

	/**
	 * Performs the calculation of the metric based on the values that have been
	 * loaded by {@link #load(Mapping)}. The result of this calculation will be
	 * made available though {@link #getResult()}.
	 * 
	 * Implementations should always synchronise this method.
	 * 
	 * @throws IllegalStateException
	 *             If {@link #load(Mapping)} has not been called before this
	 *             method.
	 */
	public abstract void calculate() throws IllegalStateException;

	public abstract boolean canAddMetric();

	public abstract boolean canRemoveEmpty();

	/**
	 * Clears the result data from this metric. Implementations should call this
	 * method in their {@link #load(Mapping)} method.
	 */
	public void clearResult() {
		isResultSet = false;
		result = -1.0;
	}

	protected void fireCompletenessChanged() {
		for (int i = 0; i < changeListeners.size(); i++)
			changeListeners.get(i).completenessChanged();
	}

	private void fireNameChanged() {
		for (int i = 0; i < changeListeners.size(); i++)
			changeListeners.get(i).nameChanged();
	}

	protected void fireSubmetricsChanged() {
		for (int i = 0; i < changeListeners.size(); i++)
			changeListeners.get(i).submetricsChanged();
	}

	public String getName() {
		return name;
	}

	/**
	 * Return a map of all properties for this metric. The properties specified
	 * by this map should allow one to bring a newly created metric of the same
	 * type to the same state as the current metric.
	 * 
	 * @see #setProperties(Map)
	 */
	protected abstract Map<String, String> getProperties();

	public double getResult() {
		if (!isResultSet)
			throw new IllegalStateException("No result has been set");
		return result;
	}

	/**
	 * Array with zero elements if there are no submetrics. If the array
	 * contains null values, these are values that ought to be filled in.
	 */
	public abstract SubmetricList getSubmetrics();

	/**
	 * Returns whether or not this metric is complete. An instantiated metric
	 * can change its completeness at runtime, for example when configuration
	 * changes occur.
	 * 
	 * Complete metrics should at any time be serializable through
	 * {@link #writeMetric(Document, int)} and be computable.
	 */
	public abstract boolean isComplete();

	/**
	 * Returns true if and only if this metric has a result. This result would
	 * probably be calculated through {@link #calculate()}.
	 */
	public boolean isResultSet() {
		return isResultSet;
	}

	/**
	 * Ensure all information required for the actual calculation of the metric
	 * is available. This method should always be called before
	 * {@link #calculate()}.
	 * 
	 * It is possible to call this method multiple times with the same or
	 * different parameters. In this case, after each call the object will be in
	 * the same state as if this method had only been called once with the last
	 * parameters used.
	 * 
	 * Implementations should always synchronise this method and include a call
	 * to {@link #clearResult()}.
	 */
	public abstract void load(Mapping mapping);

	public abstract void remove(AbstractMetric m);

	public void removeChangeListener(MetricChangeListener l) {
		changeListeners.remove(l);
	}

	public abstract void removeEmpty();

	/**
	 * Set the activity of this metric to the given activity.
	 */
	public void setName(String name) {
		this.name = name;
		fireNameChanged();
	}

	/**
	 * Read properties from the given map and load them in for this metric. This
	 * is the dual method of {@link #getProperties()}. For any type of
	 * {@link AbstractMetric}, all information that can be obtained through
	 * {@link #getProperties()} should be valid arguments for this method.
	 * 
	 * @see #getProperties()
	 */
	public abstract void setProperties(Map<String, String> properties);

	protected synchronized void setResult(double result) {
		isResultSet = true;
		this.result = result;
	}

	public abstract void setSubmetrics(SubmetricList list);

	/**
	 * Write this metric to an {@link Element} for the given {@link Document}.
	 * This method will write away any information required to recover an
	 * equivalent object from the returned element.
	 */
	public Element writeMetric(Document document, int index) {
		Element xml = document.createElement("metric");
		xml.setAttribute("activity", getName());
		xml.setAttribute("type", getClass().getName());
		xml.setAttribute("index", Integer.toString(index));

		SubmetricList l = getSubmetrics();
		for (int i = 0; i < l.getNbSubmetrics(); i++)
			xml.appendChild(l.getSubmetric(i).writeMetric(document, i));

		Map<String, String> map = getProperties();
		for (Entry<String, String> entry : map.entrySet()) {
			Element property = document.createElement("property");
			property.setAttribute("activity", entry.getKey());
			property.setTextContent(entry.getValue());
			xml.appendChild(property);
		}

		return xml;
	}

}
