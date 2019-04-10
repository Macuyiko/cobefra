package be.kuleuven.econ.cbf.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.utils.ExtensionFileFilter;

public class BeginFrame implements Runnable {
	private boolean complete;
	private boolean cancelled; 
	private Object completionSignal;
	
	private JFrame frame;
	private InputSet inputSet;
	private MetricSet metricSet;

	private NewJobPanel newJobPanel;
	private OpenJobPanel openJobPanel;
	
	public BeginFrame() {
		cancelled = false;
		complete = false;
		completionSignal = new Object();
	}
	
	public void run() {
		UISettings.applyLookAndFeel();
		
		newJobPanel = new NewJobPanel();
		openJobPanel = new OpenJobPanel();
		
		frame = new JFrame() {
			@Override public void dispose() {
				cancel();
				super.dispose();
			}
		};
		
		frame.setSize(500, 300);
		frame.setTitle("CoBeFra -- The comprehensive benchmarking framework");
		frame.setLocationByPlatform(true);
		frame.setResizable(false);
		frame.getContentPane().setBackground(UISettings.COLOUR_SUBITEM_BACKGROUND);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		
		frame.add(newJobPanel);
		frame.add(Box.createRigidArea(new Dimension(0, 20)));
		frame.add(openJobPanel);
		frame.add(Box.createVerticalGlue());
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	private void jobNew() {
		inputSet = new InputSet();
		metricSet = new MetricSet();
		disableSelf("Please wait while loading...");
		signalCompletion();
	}
	
	private void jobOpen() {
		inputSet = new InputSet();
		metricSet = new MetricSet();
		
		File iset = new File(openJobPanel.getCbfi().getFile());
		File mset = new File(openJobPanel.getCbfm().getFile());
		
		try {
			if (!iset.exists() && !mset.exists())
				throw new IllegalArgumentException("At least one of the files must be set");
			if (iset.exists())
				inputSet = InputSet.readInputSet(iset);
			if (mset.exists())
				metricSet = MetricSet.readMetricSet(new FileInputStream(mset));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please select valid file(s). At least one input must be set.",
					"Error", 1);
			return;
		}
		
		disableSelf("Please wait while loading...");
		signalCompletion();
	}
	
	public InputSet getInputSet() {
		return inputSet;
	}

	public MetricSet getMetricSet() {
		return metricSet;
	}
	
	private void disableSelf(String message) {
		frame.getContentPane().removeAll();
		
		JLabel lblMessage = new JLabel(message);
		lblMessage.setFont(lblMessage.getFont().deriveFont(Font.BOLD, 16));
		lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		frame.getContentPane().add(Box.createRigidArea(new Dimension(0, 40)));
		frame.getContentPane().add(lblMessage);
		frame.getContentPane().add(Box.createRigidArea(new Dimension(0, 50)));
		frame.getContentPane().add(progressBar);
		
		frame.paintAll(frame.getGraphics());
	}
	
	public void close() {
		frame.dispose();
	}

	private class NewJobPanel extends JPanel {
		public NewJobPanel() {
			this.setBackground(UISettings.COLOUR_SUBITEM_BACKGROUND);
			this.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "New"));
			JButton button = new JButton("New Benchmark Job");
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					jobNew();
				}
			});
			button.setPreferredSize(new Dimension(300, 40));
			this.add(button);
		}
	}
	
	private class OpenJobPanel extends JPanel {
		JFileSelectionBox cbfi;
		JFileSelectionBox cbfm;
		
		public OpenJobPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JPanel left = new JPanel();
			JPanel right = new JPanel();
			left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
			right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
			left.setBackground(UISettings.COLOUR_SUBITEM_BACKGROUND);
			right.setBackground(UISettings.COLOUR_SUBITEM_BACKGROUND);
			
			this.setBackground(UISettings.COLOUR_SUBITEM_BACKGROUND);
			this.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Open"));
			
			JLabel lblInput = new JLabel("Select an input file:");
			JLabel lblMetric = new JLabel("Select a metrics file:");
			lblInput.setAlignmentX(Component.LEFT_ALIGNMENT);
			lblMetric.setAlignmentX(Component.LEFT_ALIGNMENT);
			cbfi = new JFileSelectionBox(new ExtensionFileFilter("cbfi"));
			cbfm = new JFileSelectionBox(new ExtensionFileFilter("cbfm"));
			
			left.add(lblInput);
			left.add(cbfi);
			
			right.add(lblMetric);
			right.add(cbfm);
			
			this.add(left);
			this.add(Box.createRigidArea(new Dimension(0, 10)));
			this.add(right);
			
			JButton button = new JButton("Open Benchmark Job");
			button.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					jobOpen();
				}
			});
			this.add(Box.createRigidArea(new Dimension(0, 20)));
			this.add(button);
		}
		
		public JFileSelectionBox getCbfi() {
			return cbfi;
		}

		public JFileSelectionBox getCbfm() {
			return cbfm;
		}
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
	
	public synchronized void cancel() {
		cancelled = true;
		signalCompletion();
	}

	public boolean isCancelled() {
		return cancelled;
	}
}