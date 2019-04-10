package org.processmining.plugins.cbf;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.input.InputSet;

@Plugin(
		name = "Import CBF Input Set",
		parameterLabels = { "Filename" },
		returnLabels = { "CBF Input Set" },
		returnTypes = { InputSet.class })
@UIImportPlugin(description = "CBF Input Sets", extensions = { "cbfi" })
public class InputSetImporter {

	@PluginVariant(
			variantLabel = "CBF Input Set Import",
			requiredParameterLabels = { 0 })
	public InputSet exportInputSet(UIPluginContext context, File file) {
		try {
			String name = file.getName();
			name = name.substring(0, name.length() - 5);
			context.getFutureResult(0).setLabel(name);
			return InputSet.readInputSet(file);
		} catch (IOException e) {
			e.printStackTrace();
			context.getFutureResult(0).cancel(false);
			return null;
		}
	}
}
