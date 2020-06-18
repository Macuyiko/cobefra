package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Abs;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Opd;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class MarkovianMetricConfigPanel extends JComponent {	
	
	private JComboBox<?> abs_box;
	private JComboBox<?> opd_box;
	private NiceIntegerSlider order;
	
	public MarkovianMetricConfigPanel() {
		TableLayout tl = new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 40, 40, 40, 40, 40 } });
		setLayout(tl);

		SlickerFactory slickerFactoryInstance = SlickerFactory.instance();
		
		abs_box = slickerFactoryInstance.createComboBox(
				Abs.values()
		);
		
		opd_box = slickerFactoryInstance.createComboBox(
				Opd.values()
		);
		
		add(slickerFactoryInstance.createLabel("Type:"), "0, 0, l, t");
		add(abs_box, "0, 1, l, t");
		
		add(slickerFactoryInstance.createLabel("Cost function (SPL or HUN recommended):"), "0, 2, l, t");
		add(opd_box, "0, 3, l, t");

		order = slickerFactoryInstance.createNiceIntegerSlider("Order", 1, 10, 3, Orientation.HORIZONTAL);
		order.setPreferredSize(new Dimension(700, 20));
		order.setMaximumSize(new Dimension(700, 20));
		order.setMinimumSize(new Dimension(700, 20));
		add(order, "0, 4, c, t");
	}
	
	public void setAbs(Abs abs) {
		abs_box.setSelectedItem(abs);
	}

	public void setOpd(Opd opd) {
		opd_box.setSelectedItem(opd);
	}
	
	public void setOrder(int order_value) {
		order.setValue(order_value);
	}
	
	public Abs getAbs() {
		return (Abs) abs_box.getSelectedItem();
	}
	
	public Opd getOpd() {
		return (Opd) opd_box.getSelectedItem();
	}
	
	public int getOrder() {
		return order.getValue();
	}
	

}