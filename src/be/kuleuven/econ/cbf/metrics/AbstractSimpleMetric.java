package be.kuleuven.econ.cbf.metrics;

public abstract class AbstractSimpleMetric extends AbstractMetric {

	@Override
	public void add(Class<? extends AbstractMetric> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addEmpty() {
		throw new UnsupportedOperationException();
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
				return 0;
			}

			@Override
			public AbstractMetric getSubmetric(int i) {
				throw new IllegalArgumentException("No such submetric");
			}

			@Override
			public String getUninstantiatedSubmetricType(int i) {
				throw new IllegalArgumentException("No such submetric");
			}

			@Override
			public boolean isInstantiated(int i) {
				throw new IllegalArgumentException("No such submetric");
			}
		};
	}

	@Override
	public void remove(AbstractMetric m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSubmetrics(SubmetricList list) {
		if (list.getNbSubmetrics() != 0)
			throw new IllegalArgumentException(
					"This metric does not support submetrics");
	}
}
