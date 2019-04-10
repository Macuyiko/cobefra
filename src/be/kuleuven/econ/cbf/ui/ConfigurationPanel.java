package be.kuleuven.econ.cbf.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.metrics.MetricSet;

public class ConfigurationPanel extends JPanel {

	private static final long serialVersionUID = 4963568008634158299L;
	private ButtonGroup tabButtonGroup;
	private JButton btnCalculate;
	private JToggleButton btnInput;
	private JToggleButton btnMetrics;
	private InputConfigurationPanel pnlInput;
	private MetricsConfigurationPanel pnlMetrics;
	private JPanel pnlTop;
	private JScrollPane spnBottom;
	private boolean complete = false;
	private Object completer = new Object();

	public ConfigurationPanel(InputSet input, MetricSet metrics) {
		tabButtonGroup = new ButtonGroup();
		initBtnInput();
		initBtnMetrics();
		initBtnCalculate();

		initPnlInput(input);
		initPnlMetrics(metrics);
		initPnlTop();
		initSpnBottom();

		this.setLayout(new BorderLayout());
		this.add(pnlTop, BorderLayout.NORTH);
		this.add(spnBottom, BorderLayout.CENTER);
	}

	private void initBtnCalculate() {
		btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!pnlInput.isComplete()) {
					JOptionPane
							.showMessageDialog(
									null,
									"<html><p>Not all input has been correctly entered. Please check the input tab.</p></html>",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (!pnlMetrics.isComplete()) {
					JOptionPane
							.showMessageDialog(
									null,
									"<html><p>Not all metrics has been correctly entered. Please check the metric tab.</p></html>",
									"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				complete = true;
				synchronized (completer) {
					completer.notifyAll();
				}
			}
		});
	}

	private void initBtnInput() {
		btnInput = new JToggleButton("Input data");
		btnInput.setSelected(true);
		tabButtonGroup.add(btnInput);
		btnInput.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spnBottom.getViewport().removeAll();
				spnBottom.getViewport().add(pnlInput);
			}
		});
	}

	private void initBtnMetrics() {
		btnMetrics = new JToggleButton("Metrics");
		tabButtonGroup.add(btnMetrics);
		btnMetrics.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				spnBottom.getViewport().removeAll();
				spnBottom.getViewport().add(pnlMetrics);
			}
		});
	}

	private void initPnlInput(InputSet input) {
		pnlInput = new InputConfigurationPanel(input);
	}

	private void initPnlMetrics(MetricSet metrics) {
		pnlMetrics = new MetricsConfigurationPanel(metrics);
	}

	private void initPnlTop() {
		pnlTop = new JPanel();
		pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.X_AXIS));
		pnlTop.add(btnInput);
		pnlTop.add(btnMetrics);
		pnlTop.add(Box.createHorizontalGlue());
		pnlTop.add(btnCalculate);
	}

	private void initSpnBottom() {
		spnBottom = new JScrollPane();
		spnBottom.getViewport().add(pnlInput);
	}

	public void waitForCompletion() throws InterruptedException {
		if (complete)
			return;
		else
			synchronized (completer) {
				completer.wait();
			}
	}

	public InputSet getInputSet() {
		return pnlInput.getInputSet();
	}

	public MetricSet getMetricSet() {
		return pnlMetrics.getMetricSet();
	}
}
