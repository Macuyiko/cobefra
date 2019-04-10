package be.kuleuven.econ.cbf.ui.metrics.recall;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.processmining.plugins.behavioralconformance.BehavioralConformanceChecker.ComplianceMetrics;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.recall.BehavioralConformance;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {BehavioralConformance.class})
public class BehavioralConformanceVisualizer extends AbstractMetricVisualizer {
	
	@Override
	protected Component getVisualizer(AbstractMetric m) {
		final BehavioralConformance metric = (BehavioralConformance) m;
		final ConfigPanel c = new ConfigPanel(metric);
		
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		UISettings.prettify(content);
		
		UISettings.applyUI(c, true);
		JScrollPane scrollPane = new JScrollPane(c);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		content.add(scrollPane);
		
		JButton applyButton = new JButton("Apply Settings");
		applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				c.applySettings();
			}
		});
		content.add(applyButton);
		
		return content;
	}
	
	class ConfigPanel extends TwoColumnParameterPanel {
		private BehavioralConformance metric;
		private JCheckBox checkImplementation;
		private JRadioButton radioAvg, radioSd, radioMin, radioMax, radioOne;
		private JRadioButton radioC, radioEC, radioMC, radioCC,
			radioCBC, radioMBC, radioCCC, radioMCC, radioICC, radioIMC;

		private ConfigPanel(BehavioralConformance metric) {
			super(21);
			this.metric = metric;
			init();
		}
		
		protected void init() {
			int r = 1;
			this.addDoubleLabel("Implementation", r++);
			checkImplementation = this.addCheckbox("Use only jBPT", metric.isUseOriginalImplementation(), r++, true);
			r++;
	    
			this.addDoubleLabel("Compliance metric", r++);
			radioC = this.addRadiobutton("Log Compliance (C)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.C_LOG_COMPLIANCE), r++, true);
			radioEC = this.addRadiobutton("Execution Order Compliance (EC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.EC_EXECUTION_ORDER), r++, true);
			radioMC = this.addRadiobutton("Mandatory Execution Compliance (MC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.MC_MANDATORY_EXECUTION), r++, true);
			radioCC = this.addRadiobutton("Causal Coupling Compliance (CC" + ("C") + ")", 
					metric.getComplianceMetric().equals(ComplianceMetrics.CC_CAUSAL_COUPLING), r++, true);
			
			radioICC = this.addRadiobutton("Constraint-relative Case Compliance (CC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.CC_CONSTRAINT_RELATIVE), r++, true);
			radioIMC = this.addRadiobutton("Model-relative Case Compliance (MC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.MC_MODEL_RELATIVE), r++, true);
			radioCBC = this.addRadiobutton("Constraint-relative Behavioral Profile Compliance (CBC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.CBC_CONSTRAINT_RELATIVE), r++, true);
			radioMBC = this.addRadiobutton("Model-relative Behavioral Profile Compliance (MBC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.MBC_MODEL_RELATIVE), r++, true);
			radioCCC = this.addRadiobutton("Constraint-relative Co-occurrence Compliance (CCC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.CCC_CONSTRAINT_RELATIVE), r++, true);
			radioMCC = this.addRadiobutton("Model-relative Co-occurrence Compliance (MCC)", 
					metric.getComplianceMetric().equals(ComplianceMetrics.MCC_MODEL_RELATIVE), r++, true);
			r++;
			
			this.addDoubleLabel("Return value", r++);
			radioAvg = this.addRadiobutton("Average value", metric.getValueToReturn() == 0, r++, true);
			radioSd = this.addRadiobutton("Standard deviation", metric.getValueToReturn() == 1, r++, true);
			radioMin = this.addRadiobutton("Min value", metric.getValueToReturn() == 2, r++, true);
			radioMax = this.addRadiobutton("Max value", metric.getValueToReturn() == 3, r++, true);
			radioOne = this.addRadiobutton("Perfect trace %", metric.getValueToReturn() == 4, r++, true);
			
			ButtonGroup group1 = this.addButtongroup();
			group1.add(radioAvg);
			group1.add(radioSd);
			group1.add(radioMin);
			group1.add(radioMax);
			group1.add(radioOne);
			
			ButtonGroup group2 = this.addButtongroup();
			group2.add(radioC);
			group2.add(radioEC);
			group2.add(radioMC);
			group2.add(radioCC);
			group2.add(radioICC);
			group2.add(radioIMC);
			group2.add(radioCBC);
			group2.add(radioMBC);
			group2.add(radioCCC);
			group2.add(radioMCC);
						
			this.updateFields();
		}
		
		@Override
		protected void updateFields() {
			radioC.setEnabled(!checkImplementation.isSelected());
			radioEC.setEnabled(!checkImplementation.isSelected());
			radioMC.setEnabled(!checkImplementation.isSelected());
			radioCC.setEnabled(!checkImplementation.isSelected());
			
			if (checkImplementation.isSelected() 
					&& (radioC.isSelected() || radioEC.isSelected()
							|| radioMC.isSelected() || radioCC.isSelected()))
				radioIMC.setSelected(true);
		}
		
		public void applySettings() {
			metric.setUseOriginalImplementation(checkImplementation.isSelected());
			
			if (radioAvg.isSelected()) metric.setValueToReturn(0);
			if (radioSd.isSelected()) metric.setValueToReturn(1);
			if (radioMin.isSelected()) metric.setValueToReturn(2);
			if (radioMax.isSelected()) metric.setValueToReturn(3);
			if (radioOne.isSelected()) metric.setValueToReturn(4);
			
			if (radioC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.C_LOG_COMPLIANCE);
			if (radioEC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.EC_EXECUTION_ORDER);
			if (radioMC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.MC_MANDATORY_EXECUTION);
			if (radioCC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.CC_CAUSAL_COUPLING);
			if (radioICC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.CC_CONSTRAINT_RELATIVE);
			if (radioIMC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.MC_MODEL_RELATIVE);
			if (radioCBC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.CBC_CONSTRAINT_RELATIVE);
			if (radioMBC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.MBC_MODEL_RELATIVE);
			if (radioCCC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.CCC_CONSTRAINT_RELATIVE);
			if (radioMCC.isSelected()) metric.setComplianceMetric(ComplianceMetrics.MCC_MODEL_RELATIVE);
			
			hideMetric(metric);
		}
	}

}
