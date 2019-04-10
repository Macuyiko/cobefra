package org.processmining.plugins.cbf;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import be.kuleuven.econ.cbf.CBFUIController;
import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.result.CBFResult;

@Plugin(
		name = "Comprehensive Benchmark Framework",
		parameterLabels = { "CBF Metrics Set", "CBF Input Set" },
		returnLabels = { "CBF Result", "CBF Metrics Set", "CBF Input Set" },
		returnTypes = { CBFResult.class, MetricSet.class, InputSet.class },
		userAccessible = true,
		help = "Framework to allow and accellerate the calculation of metrics on sets of input")
public class CBF {

	public final static String affiliation = "KULeuven";
	public final static String author = "Jochen De Weerdt & Seppe vanden Broucke";
	public final static String email = "Jochen.DeWeerdt@econ.kuleuven.be & Seppe.vandenBroucke@kuleuven.be";
	public final static String website = "http://econ.kuleuven.be";

	@UITopiaVariant(
			affiliation = affiliation,
			author = author,
			email = email,
			website = website)
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public Object[] main(UIPluginContext context) {
		return main(context, new MetricSet(), new InputSet());
	}

	@UITopiaVariant(
			affiliation = affiliation,
			author = author,
			email = email,
			website = website)
	@PluginVariant(
			variantLabel = "Metrics Set known",
			requiredParameterLabels = { 0 })
	public Object[] main(UIPluginContext context, MetricSet metrics) {
		return main(context, metrics, new InputSet());
	}

	@UITopiaVariant(
			affiliation = affiliation,
			author = author,
			email = email,
			website = website)
	@PluginVariant(
			variantLabel = "Input Set known",
			requiredParameterLabels = { 1 })
	public Object[] main(UIPluginContext context, InputSet input) {
		return main(context, new MetricSet(), input);
	}

	@UITopiaVariant(
			affiliation = affiliation,
			author = author,
			email = email,
			website = website)
	@PluginVariant(
			variantLabel = "Metrics Set and Input Set known",
			requiredParameterLabels = { 0, 1 })
	public Object[] main(UIPluginContext context, MetricSet metrics,
			InputSet input) {
		context.log("Starting...");
		CBFUIController myController = new CBFUIController();
		myController.preload();
		context.log("Loaded");
		myController.setInputSet(input);
		myController.setMetricSet(metrics);
		new Thread(myController).start();
		context.log("Activated");
		try {
			myController.waitForCompletion();
			context.log("Completed");
		} catch (InterruptedException e) {
			myController.cancel();
			context.log("Cancelled");
		}
		if (myController.hasResult()) {
			Object[] output = new Object[3];
			output[0] = myController.getResult();
			output[1] = myController.getMetricSet();
			output[2] = myController.getInputSet();
			return output;
		} else {
			context.getFutureResult(1).cancel(false);
			context.getFutureResult(2).cancel(false);
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}
}
