package org.processmining.plugins.cbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.metrics.MetricSet;

@Plugin(
		name = "Import CBF Metric Set",
		parameterLabels = { "Filename" },
		returnLabels = { "CBF Metric Set" },
		returnTypes = { MetricSet.class })
@UIImportPlugin(description = "CBF Metric Sets", extensions = { "cbfm" })
public class MetricSetImporter {

	@PluginVariant(
			variantLabel = "CBF Metric Set Import",
			requiredParameterLabels = { 0 })
	public MetricSet exportInputSet(UIPluginContext context, File file) {
		try {
			String name = file.getName();
			name = name.substring(0, name.length() - 5);
			context.getFutureResult(0).setLabel(name);
			return MetricSet.readMetricSet(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			context.getFutureResult(0).cancel(false);
			return null;
		}
	}
}
