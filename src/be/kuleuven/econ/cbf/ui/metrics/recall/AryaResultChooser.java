package be.kuleuven.econ.cbf.ui.metrics.recall;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import be.kuleuven.econ.cbf.metrics.recall.AryaFitness;

public class AryaResultChooser extends JComponent {	
	private JComboBox<String> resultTypeCombo;

	public AryaResultChooser() {
		this(0);
	}

	public AryaResultChooser(int resultIndex) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JLabel lblResult= new JLabel("Select a result type:");
		lblResult.setAlignmentX(LEFT_ALIGNMENT);
		
		resultTypeCombo = new JComboBox<String>(AryaFitness.resultTypes);
		
		resultTypeCombo.setPreferredSize(new Dimension(this.getWidth(), 25));
		resultTypeCombo.setAlignmentX(LEFT_ALIGNMENT);
		setMaxSize(resultTypeCombo);
		resultTypeCombo.setSelectedItem(resultIndex);

		add(Box.createRigidArea(new Dimension(0,20)));
		add(lblResult);
		add(resultTypeCombo);
		
		add(Box.createRigidArea(new Dimension(0,20)));
		add(new JLabel("<html>Make sure to select the correct result type for the algorithm.<br>" +
				"The algorithm is chosen in next step.</html>"));
	}

	private void setMaxSize(JComponent jc) {
		Dimension max = jc.getMaximumSize();
		Dimension pref = jc.getPreferredSize();
		max.height = pref.height;
		jc.setMaximumSize(max);
	}

	public int getResultType() {
		return resultTypeCombo.getSelectedIndex();
	}
	

}