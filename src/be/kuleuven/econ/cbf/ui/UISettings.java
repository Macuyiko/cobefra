package be.kuleuven.econ.cbf.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.ScrollPaneUI;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class UISettings {
	public static LookAndFeel LOOK_AND_FEEL = new NimbusLookAndFeel();
	
	public static final Color COLOUR_BACKGROUND = new Color(150, 150, 150);
	public static final Color COLOUR_ITEM_BACKGROUND = new Color(200, 200, 200);
	public static final Color COLOUR_SUBITEM_BACKGROUND = new Color(180, 180,
			180);

	public static final Color COLOUR_ERROR_DARK = new Color(188, 30, 6);
	public static final Color COLOUR_WARNING_DARK = new Color(222, 122, 16);
	public static final Color COLOUR_OK_DARK = new Color(5, 145, 7);

	public static final Color COLOUR_ERROR_LIGHT = new Color(251, 125, 111);
	public static final Color COLOUR_WARNING_LIGHT = new Color(248, 232, 144);
	public static final Color COLOUR_OK_LIGHT = new Color(170, 250, 124);

	public static final int FRAME_WIDTH = 800;
	public static final int FRAME_HEIGHT = 500;

	public static final int LINE_COMPONENT_HEIGHT_MAX = 30;
	public static final int SPINNER_FIXED_WIDTH = 80;
	public static final int MARGIN = 10;
	
	public static void applyLookAndFeel() {
        try {
			SwingUtilities.invokeAndWait(new Runnable() {
			    public void run() {
			    	try {
						UIManager.setLookAndFeel(LOOK_AND_FEEL);
					} catch (UnsupportedLookAndFeelException e) {}
			    }
			});
		} catch (Exception e) {
		}
	}
	
	public static void applyUI(Component c, boolean recursive) {
		if (c instanceof JComponent) {		
			LOOK_AND_FEEL.initialize();
			ComponentUI defaultUI = LOOK_AND_FEEL.getDefaults().getUI((JComponent) c);
			
			if (c instanceof JCheckBox) {
				((JCheckBox) c).setUI((ButtonUI) defaultUI);
			} else if(c instanceof JComboBox) {
				((JComboBox<?>) c).setUI((ComboBoxUI) defaultUI);
			} else if(c instanceof JProgressBar) {
				((JProgressBar) c).setUI((ProgressBarUI) defaultUI);
			} else if(c instanceof JLabel) {
				((JLabel) c).setUI((LabelUI) defaultUI);
			} else if(c instanceof JRadioButton) {
				((JRadioButton) c).setUI((ButtonUI) defaultUI);
			} else if(c instanceof JScrollBar) {
				((JScrollBar) c).setUI((ScrollBarUI) defaultUI);
			} else if(c instanceof JScrollPane) {
				((JScrollPane) c).setUI((ScrollPaneUI) defaultUI);
			} else if(c instanceof JSlider) {
				((JSlider) c).setUI((SliderUI) defaultUI);
			} else if(c instanceof JTextField) {
				Dimension d = c.getPreferredSize();  
		        d.height = UISettings.LINE_COMPONENT_HEIGHT_MAX;  
		        c.setPreferredSize(d);  
			} else if(c instanceof JSpinner) {
				Dimension d1 = ((JSpinner) c).getEditor().getPreferredSize();  
		        d1.height = UISettings.LINE_COMPONENT_HEIGHT_MAX;  
		        ((JSpinner) c).getEditor().setPreferredSize(d1);  
		        Dimension d2 = c.getPreferredSize();  
		        d2.height = UISettings.LINE_COMPONENT_HEIGHT_MAX;  
		        c.setPreferredSize(d2);  
		        recursive = false;
			} else if(c instanceof JPanel) {
				((JPanel) c).setUI((PanelUI) defaultUI);
				prettify((JPanel) c);
			}
		}
		
		if (recursive) {
			if (c instanceof Container) {
				for (Component subc : ((Container) c).getComponents())
					applyUI((JComponent) subc, recursive);
			}
		}
	}
	
	public static void prettify(JPanel panel) {
		panel.setBackground(COLOUR_ITEM_BACKGROUND);
		panel.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));
	}

	
}
