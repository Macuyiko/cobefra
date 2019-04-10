package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Component;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.simplicity.AverageNodeArcDegree;
import be.kuleuven.econ.cbf.metrics.simplicity.CountArcs;
import be.kuleuven.econ.cbf.metrics.simplicity.CountNodes;
import be.kuleuven.econ.cbf.metrics.simplicity.CountPlaces;
import be.kuleuven.econ.cbf.metrics.simplicity.CountTransitions;
import be.kuleuven.econ.cbf.metrics.simplicity.WeighedPlaceTransitionArcDegree;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {
		CountArcs.class, 
		AverageNodeArcDegree.class,
		CountPlaces.class,
		CountTransitions.class,
		CountNodes.class,
		WeighedPlaceTransitionArcDegree.class})
public class EmptyVisualizer extends AbstractMetricVisualizer {
	@Override
	protected Component getVisualizer(AbstractMetric m) {
		return null;
	}
}
