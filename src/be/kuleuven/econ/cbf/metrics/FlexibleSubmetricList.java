package be.kuleuven.econ.cbf.metrics;

import java.util.ArrayList;

public class FlexibleSubmetricList implements SubmetricList {

	private ArrayList<AbstractMetric> list;

	public FlexibleSubmetricList() {
		list = new ArrayList<AbstractMetric>();
	}

	public void addSubmetric(AbstractMetric metric) {
		list.add(metric);
	}

	@Override
	public int getNbSubmetrics() {
		return list.size();
	}

	@Override
	public AbstractMetric getSubmetric(int i) {
		if (i < 0 || i >= list.size())
			return null;
		else
			return list.get(i);
	}

	@Override
	public String getUninstantiatedSubmetricType(int i) {
		return null;
	}

	@Override
	public boolean isInstantiated(int i) {
		if (i < 0 || i >= list.size())
			return false;
		else
			return true;
	}

	public void setSubmetric(int index, AbstractMetric metric) {
		while (list.size() <= index)
			list.add(null);
		list.set(index, metric);
	}
}
