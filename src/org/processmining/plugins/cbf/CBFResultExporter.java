package org.processmining.plugins.cbf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.result.CBFResult;

@Plugin(
		name = "CBF ResultValue Export",
		returnLabels = {},
		returnTypes = {},
		parameterLabels = { "CBF ResultValue", "File" },
		userAccessible = true)
@UIExportPlugin(description = "CSV file", extension = "csv")
public class CBFResultExporter {

	@PluginVariant(
			variantLabel = "CBF Input Set Export",
			requiredParameterLabels = { 0, 1 })
	public void export(UIPluginContext context, CBFResult result, File file) {
		try {
			result.writeTo(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
