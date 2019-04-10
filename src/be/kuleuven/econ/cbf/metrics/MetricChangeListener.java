package be.kuleuven.econ.cbf.metrics;

public interface MetricChangeListener {

	public void nameChanged();

	public void submetricsChanged();

	public void completenessChanged();
}
