package be.kuleuven.econ.cbf.ui.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MetricVisualizer {

	public Class<? extends AbstractMetric>[] metrics();
	
}
