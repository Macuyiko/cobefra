package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

public class AryaMarkingConfiguration extends JComponent {
	private JCheckBox initialM, finalM;

	public AryaMarkingConfiguration() {
		this(false, false);
	}

	public AryaMarkingConfiguration(boolean createInitial, boolean createFinal) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		add(new JLabel("Marking options:"));
		add(Box.createRigidArea(new Dimension(0, 10)));

		initialM = new JCheckBox("Create initial marking if not present (recommended)", createInitial);
		finalM = new JCheckBox("Create final marking if not present", createFinal);

		add(initialM);
		add(finalM);

	}

	public boolean isCreateInitial() {
		return initialM.isSelected();
	}

	public boolean isCreateFinal() {
		return finalM.isSelected();
	}
}