package be.kuleuven.econ.cbf.metrics.weighed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.MetricChangeListener;
import be.kuleuven.econ.cbf.metrics.SubmetricList;

@Metric(genericName = "Free weigher",
		classification = "Other",
		author = "CoBeFra",
		isWeighted = true)
public class FreeWeigher extends AbstractMetric {

	private boolean loaded = false;
	private List<AbstractMetric> metrics = new ArrayList<AbstractMetric>();
	private int nbEmptyMetrics = 2;
	private MetricChangeListener submetricListener;
	private List<Double> weight = new ArrayList<Double>();
	
	public FreeWeigher() {
		loaded = false;
		metrics = new ArrayList<AbstractMetric>();
		weight = new ArrayList<Double>();
		nbEmptyMetrics = 2;
		submetricListener = new MetricChangeListener() {
			
			@Override
			public void completenessChanged() {
				fireCompletenessChanged();
			}
			
			@Override
			public void nameChanged() {
			}
			
			@Override
			public void submetricsChanged() {
			}
		};
	}

	@Override
	public void add(Class<? extends AbstractMetric> m) {
		if (nbEmptyMetrics > 0)
			nbEmptyMetrics--;
		AbstractMetric am = AbstractMetric.createInstance(m);
		am.addChangeListener(submetricListener);
		metrics.add(am);
		weight.add(0.0);
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}

	@Override
	public void addEmpty() {
		nbEmptyMetrics++;
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}

	@Override
	public synchronized void calculate() throws IllegalStateException {
		if (!loaded)
			throw new IllegalStateException();
		double result = 0.0;
		for (int i = 0; i < metrics.size(); i++) {
			metrics.get(i).calculate();
			result += metrics.get(i).getResult() * weight.get(i);
		}
		setResult(result);
	}

	@Override
	public boolean canAddMetric() {
		return true;
	}

	@Override
	public boolean canRemoveEmpty() {
		int size = nbEmptyMetrics + metrics.size();
		return size > 2;
	}

	public int getNbSubmetrics() {
		return metrics.size() + nbEmptyMetrics;
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		for (int i = 0; i < weight.size(); i++)
			map.put("weight-" + i, Double.toString(weight.get(i)));
		return map;
	}

	@Override
	public SubmetricList getSubmetrics() {
		return new SubmetricList() {
			@Override
			public int getNbSubmetrics() {
				int size = metrics.size();
				size += nbEmptyMetrics;
				return size;
			}

			@Override
			public AbstractMetric getSubmetric(int i) {
				if (metrics.size() <= i)
					return null;
				return metrics.get(i);
			}

			@Override
			public String getUninstantiatedSubmetricType(int i) {
				return null;
			}

			@Override
			public boolean isInstantiated(int i) {
				if (metrics.size() <= i)
					return false;
				else
					return true;
			}
		};
	}

	public double getWeight(int index) {
		if (index < weight.size())
			return weight.get(index);
		else
			return 0.0;
	}

	@Override
	public boolean isComplete() {
		// 1. there are no empties
		boolean empties = nbEmptyMetrics == 0;
		// 2. all submetrics are complete
		boolean subs = true;
		for (AbstractMetric m : metrics)
			subs = subs && m.isComplete();
		// 3. the sum of weights is 1
		double weight = 0;
		for (int i = 0; i < metrics.size(); i++)
			weight += this.weight.get(i);
		boolean sum = weight == 1.0;
		return empties && subs && sum;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		clearResult();
		for (AbstractMetric m : metrics)
			m.load(mapping);
		loaded = true;
	}

	@Override
	public void remove(AbstractMetric m) {
		int index = metrics.indexOf(m);
		if (index < 0)
			return;
		m.removeChangeListener(submetricListener);
		metrics.remove(index);
		weight.remove(index);
		nbEmptyMetrics++;
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}

	@Override
	public void removeEmpty() {
		if (canRemoveEmpty())
			nbEmptyMetrics--;
		fireCompletenessChanged();
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		try {
			for (Entry<String, String> entry : properties.entrySet())
				if (entry.getKey().matches("^weight-[0-9]+$")) {
					int index = Integer.parseInt(entry.getKey().substring(7));
					double weight = Double.parseDouble(entry.getValue());
					this.weight.set(index, weight);
				}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse properties", e);
		}
		fireCompletenessChanged();
	}

	@Override
	public void setSubmetrics(SubmetricList list) {
		metrics = new ArrayList<AbstractMetric>();
		weight = new ArrayList<Double>();
		nbEmptyMetrics = 0;
		for (int i = 0; i < list.getNbSubmetrics(); i++) {
			list.getSubmetric(i).addChangeListener(submetricListener);
			metrics.add(list.getSubmetric(i));
			weight.add(0.0);
		}
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}

	public void setWeight(int index, double weight) {
		while (this.weight.size() <= index)
			this.weight.add(0.0);
		this.weight.set(index, weight);
		fireCompletenessChanged();
	}
}
