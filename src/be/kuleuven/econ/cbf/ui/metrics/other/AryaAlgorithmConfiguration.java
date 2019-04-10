package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;


public class AryaAlgorithmConfiguration extends JComponent {
	
	private IPNReplayParamProvider paramProvider;
	private JComponent paramUI;
	
	public AryaAlgorithmConfiguration(IPNReplayParamProvider paramProvider) {
		this(paramProvider, null);
	}
	
	public AryaAlgorithmConfiguration(IPNReplayParamProvider paramProvider, JComponent ui) {
		this.paramProvider = paramProvider;
		this.setLayout(new BorderLayout());
		
		if (ui != null) paramUI = ui;
		else paramUI = paramProvider.constructUI();
		
		add(paramUI, BorderLayout.CENTER);
	}
	
	public IPNReplayParamProvider getParamProvider() {
		return paramProvider;
	}
	
	public JComponent getParamUI() {
		return paramUI;
		
	}
}
