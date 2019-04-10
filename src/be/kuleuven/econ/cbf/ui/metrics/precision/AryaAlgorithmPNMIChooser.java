package be.kuleuven.econ.cbf.ui.metrics.precision;

import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.petrinet.replayer.annotations.PNReplayMultipleAlignmentAlgorithm;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.IPNMatchInstancesLogReplayAlgorithm;

import be.kuleuven.econ.cbf.ui.metrics.recall.AryaFitnessVisualizer;

public class AryaAlgorithmPNMIChooser extends JComponent {
		private JComboBox<IPNMatchInstancesLogReplayAlgorithm> combo;
		private IPNMatchInstancesLogReplayAlgorithm[] availableAlgorithms;
		
		public AryaAlgorithmPNMIChooser(boolean doboot) {
			this(null);
			if (doboot) {
				try {
					Boot.boot(AryaFitnessVisualizer.class, PluginContext.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public AryaAlgorithmPNMIChooser(Class<? extends IPNMatchInstancesLogReplayAlgorithm> chosenAlgorithm) {
			PluginContext context = new FakePluginContext();
			
			Set<Class<?>> coverageEstimatorClasses = context.getPluginManager()
					.getKnownClassesAnnotatedWith(PNReplayMultipleAlignmentAlgorithm.class);
			if (coverageEstimatorClasses != null) {
				List<IPNMatchInstancesLogReplayAlgorithm> algList = 
						new LinkedList<IPNMatchInstancesLogReplayAlgorithm>();
				for (Class<?> coverClass : coverageEstimatorClasses) {
					try {
						Object inst = coverClass.newInstance();
						if (inst instanceof IPNMatchInstancesLogReplayAlgorithm) {
							IPNMatchInstancesLogReplayAlgorithm alg = (IPNMatchInstancesLogReplayAlgorithm) inst;
							algList.add(alg);
						}
					} catch (Exception exc) {
						// do nothing
					}
				}
				Collections.sort(algList,
						new Comparator<IPNMatchInstancesLogReplayAlgorithm>() {
							public int compare(
									IPNMatchInstancesLogReplayAlgorithm o1,
									IPNMatchInstancesLogReplayAlgorithm o2) {
								return o1.toString().compareTo(o2.toString());
							}
						});
				availableAlgorithms = algList.toArray(new IPNMatchInstancesLogReplayAlgorithm[algList.size()]);
			}

		    // Preset selection
			if (chosenAlgorithm == null)
				chosenAlgorithm = availableAlgorithms[0].getClass();
			
			IPNMatchInstancesLogReplayAlgorithm selectedAlgo = null;
			for (int i = 0; i < availableAlgorithms.length; i++) {
				if (chosenAlgorithm != null && availableAlgorithms[i].getClass().equals(chosenAlgorithm))
					selectedAlgo = availableAlgorithms[i];
			}
			
			// Make components
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			JLabel lblSelect = new JLabel("Select an algorithm:");
			lblSelect.setAlignmentX(LEFT_ALIGNMENT);
			add(lblSelect);
			
			combo = new JComboBox<IPNMatchInstancesLogReplayAlgorithm>(availableAlgorithms);
			combo.setPreferredSize(new Dimension(this.getWidth(), 25));
			combo.setAlignmentX(LEFT_ALIGNMENT);
			setMaxSize(combo);
			add(combo);
			combo.setSelectedItem(selectedAlgo);			
		}
		
		private void setMaxSize(JComponent jc) {
			Dimension max = jc.getMaximumSize();
			Dimension pref = jc.getPreferredSize();
			max.height = pref.height;
			jc.setMaximumSize(max);
		}

		public IPNMatchInstancesLogReplayAlgorithm getAlgorithm() {
			return (IPNMatchInstancesLogReplayAlgorithm) combo.getSelectedItem();
		}

	}