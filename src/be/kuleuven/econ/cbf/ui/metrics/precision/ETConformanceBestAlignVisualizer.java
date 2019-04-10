package be.kuleuven.econ.cbf.ui.metrics.precision;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.PluginContext;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.precision.ETConformanceBestAlign;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.other.AryaMarkingConfiguration;
import be.kuleuven.econ.cbf.ui.metrics.other.WizardVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.recall.AryaFitnessVisualizer;

@MetricVisualizer(metrics = {ETConformanceBestAlign.class})
public class ETConformanceBestAlignVisualizer extends WizardVisualizer {
	private ETConformanceBestAlign metric;
	private boolean booted; 
	private static int BOOT_STEP = 1;
	
	private void performBootSetup(int panelIndex) {
		if (booted) return;
		booted = true;
		
		btnPrevious.setEnabled(false);
		btnNext.setEnabled(false);
		JLabel lblBooting = new JLabel("Retrieving algorithms, please wait...");
		lblBooting.setHorizontalAlignment(SwingConstants.CENTER);  
		
		scrollPane.setViewportView(lblBooting);
		content.paintAll(content.getGraphics());
		
		try {
			Boot.boot(AryaFitnessVisualizer.class, PluginContext.class);
		} catch (Exception e) {
			lblBooting.setText("Uh oh, something's gone wrong");
			e.printStackTrace();
		}
		
		btnPrevious.setEnabled(true);
		btnNext.setEnabled(true);
		
		AryaAlgorithmPNMIChooser algorithmStep = new AryaAlgorithmPNMIChooser(metric.getChosenAlgorithm() == null 
				? null : metric.getChosenAlgorithm().getClass());
		panels.set(panelIndex, algorithmStep);
		
		currentStep = panelIndex;
		
		showPanel(currentStep);
	}
	
	@Override
	protected void showPanel(int number) {
		if (currentStep == BOOT_STEP && !booted) {
			performBootSetup(currentStep);
			return;
		}
		super.showPanel(number);
	}
	
	@Override
	protected void buildPanels() {
		panels.add(new AryaMarkingConfiguration(metric.isCreateInitialMarking(), metric.isCreateFinalMarking()));
		panels.add(null); // need to boot
		panels.add(new ETCAfterAlignmentConfiguration(metric.getGamma()));
	}

	@Override
	protected void applyParameters() {
		metric.setCreateInitialMarking(((AryaMarkingConfiguration)panels.get(0)).isCreateInitial());
		metric.setCreateFinalMarking(((AryaMarkingConfiguration)panels.get(0)).isCreateFinal());
		metric.setChosenAlgorithm(((AryaAlgorithmPNMIChooser)panels.get(1)).getAlgorithm());
		metric.setGamma(((ETCAfterAlignmentConfiguration)panels.get(2)).getGamma());
	}

	@Override
	protected void performSetup(AbstractMetric m) {
		metric = (ETConformanceBestAlign) m;
	}

}
