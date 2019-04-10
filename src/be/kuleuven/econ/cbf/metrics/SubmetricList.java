package be.kuleuven.econ.cbf.metrics;

public interface SubmetricList {

	public int getNbSubmetrics();

	public AbstractMetric getSubmetric(int i);

	/**
	 * Null means anything works.
	 */
	public String getUninstantiatedSubmetricType(int i);

	public boolean isInstantiated(int i);
}
