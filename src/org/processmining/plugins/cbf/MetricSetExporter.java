package org.processmining.plugins.cbf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.metrics.MetricSet;

@Plugin(
		name = "CBF Metric Set Export",
		returnLabels = {},
		returnTypes = {},
		parameterLabels = { "Metric Set", "File" },
		userAccessible = true)
@UIExportPlugin(description = "CBF Metric Set", extension = "cbfm")
public class MetricSetExporter {

	@PluginVariant(
			variantLabel = "CBF Metric Set Export",
			requiredParameterLabels = { 0, 1 })
	public void export(UIPluginContext context, MetricSet metrics, File file) {
		try {
			metrics.writeMetricSet(new FileOutputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
