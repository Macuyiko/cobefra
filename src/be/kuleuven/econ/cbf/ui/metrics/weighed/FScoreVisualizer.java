package be.kuleuven.econ.cbf.ui.metrics.weighed;

import java.awt.Component;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.weighed.FScore;
import be.kuleuven.econ.cbf.ui.JAutomaticTextField;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

@MetricVisualizer(metrics = {FScore.class})
public class FScoreVisualizer extends AbstractMetricVisualizer {

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	protected Component getVisualizer(AbstractMetric m) {
		final FScore metric = (FScore) m;
		JPanel panel = new JPanel();
		UISettings.prettify(panel);

		JLabel lblNewLabel = new JLabel("\u03B2 (beta)");

		JLabel lblNewLabel_1 = new JLabel(
				"<html><p>The \u03B2 (beta) value of a metric should be a positive double.</p></html>");

		JAutomaticTextField textField = new JAutomaticTextField() {

			@Override
			protected void valueChanged() {
				double d = Double.parseDouble(getText());
				metric.setBeta(d);
			}

			@Override
			protected boolean verify() {
				String text = getText();
				try {
					double d = Double.parseDouble(text);
					return d >= 0;
				} catch (NumberFormatException e) {
					return false;
				}
			}
		};
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		textField.setColumns(10);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel
				.createParallelGroup(Alignment.LEADING)
				.addComponent(lblNewLabel_1, GroupLayout.DEFAULT_SIZE, 450,
						Short.MAX_VALUE)
				.addGroup(
						gl_panel.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblNewLabel)
								.addGap(18)
								.addComponent(textField,
										GroupLayout.DEFAULT_SIZE, 383,
										Short.MAX_VALUE)));
		gl_panel.setVerticalGroup(gl_panel
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_panel.createSequentialGroup()
								.addComponent(lblNewLabel_1)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										gl_panel.createParallelGroup(
												Alignment.BASELINE)
												.addComponent(lblNewLabel)
												.addComponent(
														textField,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addContainerGap(266, Short.MAX_VALUE)));
		panel.setLayout(gl_panel);

		textField.setText(Double.toString(metric.getBeta()));

		return panel;
	}
}
