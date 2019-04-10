package be.kuleuven.econ.cbf.ui.metrics.precision;

import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.precision.ETConformance;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {ETConformance.class})
public class ETConformanceVisualizer extends AbstractMetricVisualizer {

	@Override
	protected Component getVisualizer(AbstractMetric m) {
		final ETConformance metric = (ETConformance) m;
		
		JButton applyButton = new JButton("Apply Settings");
		applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				metric.getSettings().setSettings();
				hideMetric(metric);
			}
		});
		
		JPanel content = new JPanel();
		UISettings.prettify(content);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		
		Component c = metric.getSettings().initComponents();
		UISettings.applyUI(c, true);
		
		JScrollPane scrollPane = new JScrollPane(c);
		scrollPane.setPreferredSize(new Dimension(300, 300));
		content.add(scrollPane);
		content.add(Box.createVerticalStrut(MARGIN));
		content.add(applyButton);
		
		return content;
	}
}

