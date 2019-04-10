package be.kuleuven.econ.cbf.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Metric {

	public String genericName();
	public String author();

	public String classification();

	public boolean isWeighted() default false;
}
