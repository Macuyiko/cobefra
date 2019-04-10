package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

public class AntiAlignResultChooser extends JComponent {	
	private JComboBox<String> resultTypeCombo;

	public AntiAlignResultChooser(String[] choices) {
		this(0, choices);
	}

	public AntiAlignResultChooser(int resultIndex, String[] choices) {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JLabel lblResult= new JLabel("Select a result type:");
		lblResult.setAlignmentX(LEFT_ALIGNMENT);
		
		resultTypeCombo = new JComboBox<String>(choices);
		
		resultTypeCombo.setPreferredSize(new Dimension(this.getWidth(), 25));
		resultTypeCombo.setAlignmentX(LEFT_ALIGNMENT);
		setMaxSize(resultTypeCombo);
		resultTypeCombo.setSelectedItem(resultIndex);

		add(Box.createRigidArea(new Dimension(0,20)));
		add(lblResult);
		add(resultTypeCombo);
		
		add(Box.createRigidArea(new Dimension(0,20)));
		add(new JLabel("<html>Make sure to select the correct result type.</html>"));
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