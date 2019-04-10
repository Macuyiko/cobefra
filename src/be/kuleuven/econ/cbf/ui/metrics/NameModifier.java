package be.kuleuven.econ.cbf.ui.metrics;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;

class NameModifier extends JPanel {

	private JTextField field;
	private AbstractMetric metric;

	public NameModifier(AbstractMetric m) {
		this.metric = m;
		this.setBackground(COLOUR_ITEM_BACKGROUND);

		JLabel lblEnterTheMetrics = new JLabel("The metric's result name:");

		field = new JTextField();
		field.setColumns(10);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(
				Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								groupLayout
										.createParallelGroup(Alignment.LEADING)
										.addComponent(field,
												GroupLayout.DEFAULT_SIZE, 430,
												Short.MAX_VALUE)
										.addComponent(lblEnterTheMetrics))
						.addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
				Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblEnterTheMetrics)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(field, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
		setLayout(groupLayout);

		field.setText(m.getName());
		field.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				rename();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				rename();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				rename();
			}
		});
	}

	private void rename() {
		this.metric.setName(field.getText());
	}
}