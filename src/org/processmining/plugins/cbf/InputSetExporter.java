package org.processmining.plugins.cbf;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.input.InputSet;

@Plugin(
		name = "CBF Input Set Export",
		returnLabels = {},
		returnTypes = {},
		parameterLabels = { "Input Set", "File" },
		userAccessible = true)
@UIExportPlugin(description = "CBF Input Set", extension = "cbfi")
public class InputSetExporter {

	@PluginVariant(
			variantLabel = "CBF Input Set Export",
			requiredParameterLabels = { 0, 1 })
	public void export(UIPluginContext context, InputSet input, File file) {
		try {
			input.writeInputSet(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
