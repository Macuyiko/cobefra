package be.kuleuven.econ.cbf;

import static be.kuleuven.econ.cbf.ui.UISettings.FRAME_HEIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.FRAME_WIDTH;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.process.CalculationManager;
import be.kuleuven.econ.cbf.result.CBFResult;
import be.kuleuven.econ.cbf.ui.ConfigurationPanel;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.process.ProgressView;

public class CBFUIController implements Runnable {

	private boolean complete;
	private Object completionSignal;
	private boolean hasResult;
	private CBFResult result;
	private InputSet input;
	private MetricSet metrics;
	private JFrame frame;
	private Thread current;

	public CBFUIController() {
		complete = false;
		hasResult = false;
		completionSignal = new Object();
		current = null;
	}

	public void cancel() {
		frame.setVisible(false);
		frame.dispose();
		current.interrupt();
	}

	public InputSet getInputSet() {
		return input;
	}

	public MetricSet getMetricSet() {
		return metrics;
	}

	public CBFResult getResult() {
		return result;
	}

	public boolean hasResult() {
		return hasResult;
	}

	public boolean isComplete() {
		return complete;
	}

	public void preload() {
		AbstractMetric.loadClass();
		AbstractMetricVisualizer.loadClass();
	}

	public void setInputSet(InputSet input) {
		this.input = input;
	}

	public void setMetricSet(MetricSet metrics) {
		this.metrics = metrics;
	}

	private void signalCompletion() {
		complete = true;
		synchronized (completionSignal) {
			completionSignal.notifyAll();
		}
	}

	public CBFResult runNoGui() {
		CalculationManager manager = new CalculationManager(getInputSet(), getMetricSet());
		result = new CBFResult(manager);
		try {
			manager.perform();
		} catch (InterruptedException e) {
			signalCompletion();
			return null;
		}
		return result;
	}
	
	@Override
	public void run() {
		UISettings.applyLookAndFeel();

		current = Thread.currentThread();
		frame = new JFrame() {
			private static final long serialVersionUID = -747260670524866781L;
			@Override
			public void dispose() {
				super.dispose();
				current.interrupt();
			}
		};
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		frame.setTitle("Comprehensive Benchmark Framework");
		frame.setLocationByPlatform(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// Step one: configuring the plugin
		ConfigurationPanel configuration = new ConfigurationPanel(input, metrics);
		frame.add(configuration);
		frame.setVisible(true);
		try {
			configuration.waitForCompletion();
		} catch (InterruptedException e) {
			signalCompletion();
			return;
		}
		frame.remove(configuration);
		setInputSet(configuration.getInputSet());
		setMetricSet(configuration.getMetricSet());

		// Step two: calculations
		CalculationManager manager = new CalculationManager(getInputSet(),
				getMetricSet());
		ProgressView progress = new ProgressView(manager);
		frame.add(progress);
		frame.revalidate();
		try {
			manager.perform();
		} catch (InterruptedException e) {
			signalCompletion();
			return;
		}

		frame.setVisible(false);
		frame.dispose();

		result = new CBFResult(manager);
		hasResult = true;
		signalCompletion();
	}

	public void waitForCompletion() throws InterruptedException {
		if (complete)
			return;
		else
			synchronized (completionSignal) {
				completionSignal.wait();
			}
	}
}
