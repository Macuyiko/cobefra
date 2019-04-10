package be.kuleuven.econ.cbf.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.processmining.plugins.cbf.CBFResultExporter;
import org.processmining.plugins.cbf.CBFResultVisualizer;
import org.processmining.plugins.cbf.InputSetExporter;
import org.processmining.plugins.cbf.MetricSetExporter;

import be.kuleuven.econ.cbf.CBFUIController;
import be.kuleuven.econ.cbf.utils.ExtensionFileFilter;

public class ResultFrame implements Runnable {
	JFileChooser chooser;
	private CBFUIController controller;
	
	private boolean complete;
	private boolean cancelled; 
	private Object completionSignal;
	
	private JFrame frame;
	private JComponent table;
	
	public ResultFrame(CBFUIController myController) {
		complete = false;
		cancelled = false;
		controller = myController;
		completionSignal = new Object();
	}
	
	public void run() {
		UISettings.applyLookAndFeel();
		
		chooser = new JFileChooser("");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		frame = new JFrame() {
			@Override public void dispose() {
				cancel();
				super.dispose();
			}
		};
		frame.setSize(500, 500);
		frame.setTitle("Benchmarking results");
		frame.setLocationByPlatform(true);
		frame.getContentPane().setBackground(UISettings.COLOUR_BACKGROUND);
		frame.setLayout(new BorderLayout());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		CBFResultVisualizer visualizer = new CBFResultVisualizer();
		table = visualizer.makeResultTable(controller.getResult());
		
		frame.add(table, BorderLayout.CENTER);
		
		JPanel actionPanel = new JPanel();
		JButton exportParams = new JButton("Save Benchmark Job");
		JButton exportCSV = new JButton("Export Results as CSV");
		exportParams.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportParams();
			}
		});
		exportCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportCSV();
			}
		});
		actionPanel.add(exportParams);
		actionPanel.add(exportCSV);
		
		frame.add(actionPanel, BorderLayout.PAGE_START);
		frame.revalidate();
	}
	
	private File fileWithExtension(File base, String extension) {
		String path = base.getAbsolutePath();
		if(!path.endsWith(extension))
			base = new File(path + "." + extension);
		return base;
	}
	
	protected void exportCSV() {
		File csvFile;
		
		chooser.setFileFilter(new ExtensionFileFilter("csv"));
		chooser.setCurrentDirectory((UIGlobals.lastLocation == null || !new File(
				UIGlobals.lastLocation).isDirectory()) ? null : new File(
				UIGlobals.lastLocation));
		chooser.setSelectedFile(new File("cbf-result.csv"));
		int rValue = chooser.showSaveDialog(null);
		if (rValue == JFileChooser.APPROVE_OPTION) {
			csvFile = fileWithExtension(chooser.getSelectedFile(), "csv");
			UIGlobals.lastLocation = chooser.getCurrentDirectory()
					.getAbsolutePath();
		} else {
			return;
		}
		
		CBFResultExporter exporter = new CBFResultExporter();
		exporter.export(null, controller.getResult(), csvFile);
	}

	protected void exportParams() {
		File cbfiFile;
		File cbfmFile;
		
		chooser.setFileFilter(new ExtensionFileFilter("cbfi"));
		chooser.setCurrentDirectory((UIGlobals.lastLocation == null || !new File(
				UIGlobals.lastLocation).isDirectory()) ? null : new File(
				UIGlobals.lastLocation));
		chooser.setSelectedFile(new File("cbf-inputs.cbfi"));
		int rValue = chooser.showSaveDialog(null);
		if (rValue == JFileChooser.APPROVE_OPTION) {
			cbfiFile = fileWithExtension(chooser.getSelectedFile(), "cbfi");
			UIGlobals.lastLocation = chooser.getCurrentDirectory()
					.getAbsolutePath();
		} else {
			return;
		}
		
		chooser.setFileFilter(new ExtensionFileFilter("cbfm"));
		chooser.setCurrentDirectory((UIGlobals.lastLocation == null || !new File(
				UIGlobals.lastLocation).isDirectory()) ? null : new File(
				UIGlobals.lastLocation));
		chooser.setSelectedFile(new File("cbf-metrics.cbfm"));
		rValue = chooser.showSaveDialog(null);
		if (rValue == JFileChooser.APPROVE_OPTION) {
			cbfmFile = fileWithExtension(chooser.getSelectedFile(), "cbfm");
			UIGlobals.lastLocation = chooser.getCurrentDirectory()
					.getAbsolutePath();
		} else {
			return;
		}
		
		InputSetExporter iExporter = new InputSetExporter();
		iExporter.export(null, controller.getInputSet(), cbfiFile);
		MetricSetExporter mExporter = new MetricSetExporter();
		mExporter.export(null, controller.getMetricSet(), cbfmFile);
	}

	public void close() {
		frame.dispose();
	}

	public void waitForCompletion() throws InterruptedException {
		if (complete)
			return;
		else
			synchronized (completionSignal) {
				completionSignal.wait();
			}
	}
	
	private void signalCompletion() {
		complete = true;
		synchronized (completionSignal) {
			completionSignal.notifyAll();
		}
	}
	
	public void cancel() {
		cancelled = true;
		signalCompletion();
	}

	public boolean isCancelled() {
		return cancelled;
	}

}