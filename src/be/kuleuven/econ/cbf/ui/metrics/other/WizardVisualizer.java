package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;

public abstract class WizardVisualizer extends AbstractMetricVisualizer {
	protected JPanel content;
	protected JScrollPane scrollPane;
	
	protected JLabel lblHeading;
	protected JButton btnPrevious;
	protected JButton btnNext;
	
	protected int currentStep = 0;
	protected int nrSteps;
	protected List<JComponent> panels;
	
	protected AbstractMetric metric;
	
	private void buildVisualizer() {
		panels = new ArrayList<JComponent>();
		currentStep = 0;
		
		content = new JPanel();
		content.setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(400, 400));
		content.add(scrollPane, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		
		lblHeading = new JLabel("Please wait...");
		lblHeading.setFont(new Font(null, Font.BOLD, 14));
		btnPrevious = new JButton("");
		btnNext = new JButton("");
		btnPrevious.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				go(-1);
			}
		});
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				go(1);
			}
		});
		btnPrevious.setEnabled(false);
		btnNext.setEnabled(false);
		
		bottomPanel.add(btnPrevious);
		bottomPanel.add(btnNext);
		content.add(lblHeading, BorderLayout.PAGE_START);
		content.add(bottomPanel, BorderLayout.PAGE_END);
		
		buildPanels();
		nrSteps = panels.size();
		
		showPanel(0);
		go(0);
	}
	
	private void go(int move) {
		int newStep = currentStep + move;
		
		if (newStep == nrSteps) {
			applyParameters();
			hideMetric(metric);
		}
		
		if (newStep < 0 || newStep >= nrSteps)
			newStep = currentStep;
		
		lblHeading.setText("Configuration step "+(newStep+1)+" / "+nrSteps);
		btnPrevious.setEnabled(newStep > 0);
		btnNext.setEnabled(newStep < nrSteps);
		btnPrevious.setText("\u2190");
		btnNext.setText("\u2192");
		
		if (newStep == nrSteps - 1)
			btnNext.setText("Apply All Settings");
		
		if (newStep == currentStep)
			return;
		
		currentStep = newStep;
		showPanel(currentStep);
	}
	
	protected void showPanel(int number) {
		scrollPane.setViewportView(panels.get(number));
	}
	protected abstract void buildPanels();
	protected abstract void applyParameters();
	protected abstract void performSetup(AbstractMetric m);

	@Override
	protected Component getVisualizer(AbstractMetric m) {
		metric = m;
		performSetup(metric);
		buildVisualizer();
		return content;
	}

}
