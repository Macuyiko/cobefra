package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import be.kuleuven.econ.cbf.metrics.precision.AryaPrecision;
import be.kuleuven.econ.cbf.metrics.generalization.AryaGeneralization;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {AryaPrecision.class, AryaGeneralization.class})
public class AryaPrecisionGeneralizationVisualizer extends AryaMetricVisualizer {
	@Override
	protected void buildPanels() {
		super.buildPanels();
		// Add the final configuration panel
		if (metric instanceof AryaPrecision)
			panels.add(new AryaPrecisionGeneralizationConfiguration(((AryaPrecision) metric).isTraceGrouped()));
		else if (metric instanceof AryaGeneralization)
			panels.add(new AryaPrecisionGeneralizationConfiguration(((AryaGeneralization) metric).isTraceGrouped()));
	}

	@Override
	protected void applyParameters() {
		super.applyParameters();
		if (metric instanceof AryaPrecision)
			((AryaPrecision) metric).setTraceGrouped(
					((AryaPrecisionGeneralizationConfiguration) panels.get(panels.size()-1)).isGroupTraces());
		if (metric instanceof AryaGeneralization)
			((AryaGeneralization) metric).setTraceGrouped(
					((AryaPrecisionGeneralizationConfiguration) panels.get(panels.size()-1)).isGroupTraces());
	}
	
	public class AryaPrecisionGeneralizationConfiguration extends JPanel {
		private JCheckBox groupT;
	
		public AryaPrecisionGeneralizationConfiguration() {
			this(false);
		}
		
		public AryaPrecisionGeneralizationConfiguration(boolean groupTraces) {
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(new JLabel("Precision/generalization checking options:"));
			add(Box.createRigidArea(new Dimension(0,10)));
			groupT = new JCheckBox("Group trace with same event class sequence", groupTraces);
			add(groupT);
		}

		public boolean isGroupTraces() {
			return groupT.isSelected();
		}

	}
}
