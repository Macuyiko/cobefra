package be.kuleuven.econ.cbf.ui.metrics.precision;

import java.awt.Dimension;
import java.awt.FlowLayout;


import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import be.kuleuven.econ.cbf.ui.UISettings;


public class ETCAfterAlignmentConfiguration extends JComponent {
		private JSpinner gamma;
	
		public ETCAfterAlignmentConfiguration() {
			this(0D);
		}
		
		public ETCAfterAlignmentConfiguration(double gammaValue) {
			this.setLayout(new FlowLayout());
			
			JLabel lblNewLabel = new JLabel("Escaping states threshold (gamma):");
			SpinnerModel model = new SpinnerNumberModel(0D, 0D, 1D, .01D);
			gamma = new JSpinner(model);
			gamma.setValue(gammaValue);
			Dimension d = gamma.getPreferredSize();  
	        d.width = UISettings.SPINNER_FIXED_WIDTH;  
	        gamma.setPreferredSize(d);  
	   
			add(lblNewLabel);
			add(gamma);			
		}
		
		public double getGamma() {
			return (double) gamma.getValue();
		}

	}