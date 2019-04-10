package be.kuleuven.econ.cbf.metrics.weighed;

import java.util.Map;
import java.util.TreeMap;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.Metric;
import be.kuleuven.econ.cbf.metrics.MetricChangeListener;
import be.kuleuven.econ.cbf.metrics.SubmetricList;

@Metric(genericName = "F-Score", author = "De Weerdt et al.", classification = "Other", isWeighted = true)
public class FScore extends AbstractMetric {

	private double beta;
	private boolean loaded;
	private AbstractMetric precision;
	private AbstractMetric recall;
	private MetricChangeListener submetricListener;

	public FScore() {
		this.beta = 1;
		this.loaded = false;
		this.submetricListener = new MetricChangeListener() {

			@Override
			public void submetricsChanged() {
			}

			@Override
			public void nameChanged() {
			}

			@Override
			public void completenessChanged() {
				fireCompletenessChanged();
			}
		};
	}

	@Override
	public void add(Class<? extends AbstractMetric> m) {
		String classification = AbstractMetric.getClassification(m);
		if (classification.equals("Recall")) {
			if (recall == null) {
				recall = AbstractMetric.createInstance(m);
				recall.addChangeListener(submetricListener);
			} else
				throw new IllegalStateException(
						"This F-Score already contains a recall metric");
		} else if (classification.equals("Precision"))
			if (precision == null) {
				precision = AbstractMetric.createInstance(m);
				precision.addChangeListener(submetricListener);
			} else
				throw new IllegalStateException(
						"This F-Score already contains a precision metric");
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}

	@Override
	public void addEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void calculate() throws IllegalStateException {
		if (!loaded)
			throw new IllegalStateException("This method has not been loaded");

		this.recall.calculate();
		this.precision.calculate();
		double recall = this.recall.getResult();
		double precision = this.precision.getResult();

		double beta2 = beta * beta;
		double f = 0;
		f += (1 + beta2) * precision * recall;
		f /= beta2 * precision + recall;

		setResult(f);
	}

	@Override
	public boolean canAddMetric() {
		return false;
	}

	@Override
	public boolean canRemoveEmpty() {
		return false;
	}

	@Override
	public SubmetricList getSubmetrics() {
		return new SubmetricList() {

			@Override
			public int getNbSubmetrics() {
				return 2;
			}

			@Override
			public AbstractMetric getSubmetric(int i) {
				if (i == 0)
					return recall;
				else if (i == 1)
					return precision;
				else
					throw new IllegalArgumentException("No such submetric");
			}

			@Override
			public String getUninstantiatedSubmetricType(int i) {
				if (i == 0)
					return "Recall";
				else if (i == 1)
					return "Precision";
				else
					throw new IllegalArgumentException("No such submetric");
			}

			@Override
			public boolean isInstantiated(int i) {
				if (i == 0)
					return recall != null;
				else if (i == 1)
					return precision != null;
				else
					throw new IllegalArgumentException("No such submetric");
			}
		};
	}

	@Override
	public boolean isComplete() {
		// fitness & precision are filled in
		boolean filled = recall != null && precision != null;
		if (!filled)
			return false;
		boolean completed = recall.isComplete() && precision.isComplete();
		return completed;
	}

	@Override
	public synchronized void load(Mapping mapping) {
		loaded = true;
		clearResult();
		recall.load(mapping);
		precision.load(mapping);
	}

	@Override
	public void remove(AbstractMetric m) {
		m.removeChangeListener(submetricListener);
		if (recall == m)
			recall = null;
		else if (precision == m)
			precision = null;
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}

	@Override
	public void removeEmpty() {
	}

	public double getBeta() {
		return beta;
	}

	public void setBeta(double beta) {
		if (beta >= 0)
			this.beta = beta;
		else
			throw new IllegalArgumentException(
					"Beta has to be a positive double");
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> properties = new TreeMap<String, String>();
		properties.put("beta", Double.toString(beta));
		return properties;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		String beta = properties.get("beta");
		setBeta(Double.parseDouble(beta));
	}

	@Override
	public void setSubmetrics(SubmetricList list) {
		recall = null;
		precision = null;
		if (list.getNbSubmetrics() != 2)
			throw new IllegalArgumentException(
					"An F-Score always needs 2 submetrics");
		for (int i = 0; i < list.getNbSubmetrics(); i++) {
			AbstractMetric m = list.getSubmetric(i);
			String classification = AbstractMetric.getClassification(m
					.getClass());
			if (classification.equals("Recall")) {
				if (recall == null)
					recall = m;
			} else if (classification.equals("Precision")) {
				if (precision == null)
					precision = m;
			} else
				throw new IllegalArgumentException(
						"Only precision and recall metrics are allowed in an F-Score");
			m.addChangeListener(submetricListener);
		}
		if (recall == null || precision == null)
			throw new IllegalArgumentException(
					"Both a precision and a recall metric have to be supplied");
		fireSubmetricsChanged();
		fireCompletenessChanged();
	}
}
