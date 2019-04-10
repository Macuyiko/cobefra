package be.kuleuven.econ.cbf.ui.process;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import be.kuleuven.econ.cbf.process.CalculationManager;
import be.kuleuven.econ.cbf.process.MetricCalculator;
import be.kuleuven.econ.cbf.utils.CreationListener;

public class ProgressView extends JScrollPane implements
		CreationListener<MetricCalculator> {

	private JPanel content;

	public ProgressView(CalculationManager manager) {
		this.setBackground(COLOUR_BACKGROUND);
		this.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN,
				MARGIN));
		this.getViewport().setOpaque(false);
		this.getVerticalScrollBar().setUnitIncrement(16);

		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setOpaque(false);
		this.getViewport().add(content);

		StatusPanel status = new StatusPanel(manager);
		manager.addFinishedListener(status);
		content.add(status);
		manager.addCreationListener(this);
	}

	@Override
	public void created(MetricCalculator origin) {
		Component box = Box.createVerticalStrut(MARGIN);
		content.add(box);
		content.add(new CalculationPanel(origin, box));
		content.revalidate();
	}
}
