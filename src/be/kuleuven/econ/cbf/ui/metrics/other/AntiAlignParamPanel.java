package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Dimension;

import javax.swing.JComponent;
import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class AntiAlignParamPanel extends JComponent {	
	private final NiceIntegerSlider cutOffLengthSlider;
	private final NiceDoubleSlider maxFactorSlider;
	private NiceDoubleSlider backtrackThreshold;
	private NiceIntegerSlider backtrackMax;

	public AntiAlignParamPanel() {
		this(5, 1, 1, 2);
	}
	
	public AntiAlignParamPanel(int cutOffLength, double maxFactor, int backtrackLimit, double backtrackThresh) {
		TableLayout tl = new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 80, 40, 40, 40, 40 } });
		setLayout(tl);

		SlickerFactory slickerFactoryInstance = SlickerFactory.instance();
		
		// max modelMoves
		cutOffLengthSlider = slickerFactoryInstance.createNiceIntegerSlider("<html><h4>Prefix length</h4></html>", 0, 100, cutOffLength, Orientation.HORIZONTAL);
		cutOffLengthSlider.setPreferredSize(new Dimension(700, 20));
		cutOffLengthSlider.setMaximumSize(new Dimension(700, 20));
		cutOffLengthSlider.setMinimumSize(new Dimension(700, 20));
		add(cutOffLengthSlider, "0, 1, c, t");

		maxFactorSlider = slickerFactoryInstance.createNiceDoubleSlider("<html><h4>Maximum length factor</h4></html>", 1.0, 5.0, maxFactor, Orientation.HORIZONTAL);
		maxFactorSlider.setPreferredSize(new Dimension(700, 20));
		maxFactorSlider.setMaximumSize(new Dimension(700, 20));
		maxFactorSlider.setMinimumSize(new Dimension(700, 20));
		add(maxFactorSlider, "0, 2, c, t");

		// max backtrack
		backtrackMax = slickerFactoryInstance.createNiceIntegerSlider("<html><h4># Maximum number of backtrack steps.</h4></html>", 0, 10, backtrackLimit, Orientation.HORIZONTAL);
		backtrackMax.setPreferredSize(new Dimension(700, 20));
		backtrackMax.setMaximumSize(new Dimension(700, 20));
		backtrackMax.setMinimumSize(new Dimension(700, 20));
		add(backtrackMax, "0, 3, c, t");

		// max backtrackThreshold
		backtrackThreshold = slickerFactoryInstance.createNiceDoubleSlider("<html><h4># Backtracking threshold.</h4></html>", 0, 4.0, backtrackThresh, Orientation.HORIZONTAL);
		backtrackThreshold.setPreferredSize(new Dimension(700, 20));
		backtrackThreshold.setMaximumSize(new Dimension(700, 20));
		backtrackThreshold.setMinimumSize(new Dimension(700, 20));
		add(backtrackThreshold, "0, 4, c, t");

	}

	protected void setTitle(SlickerFactory slickerFactoryInstance, String title) {
		add(slickerFactoryInstance.createLabel(title), "0, 0, l, t");
	}

	public double getMaxFactor() {
		return maxFactorSlider.getValue();
	}

	public int getBacktrackLimit() {
		return backtrackMax.getValue();
	}

	public double getBacktrackThreshold() {
		return backtrackThreshold.getValue();
	}

	public int getCutoffLength() {
		return cutOffLengthSlider.getValue();
	}
	
	public void setMaxFactor(double val) {
		maxFactorSlider.setValue(val);
	}

	public void setBacktrackLimit(int val) {
		backtrackMax.setValue(val);
	}

	public void setBacktrackThreshold(double val) {
		backtrackThreshold.setValue(val);
	}

	public void setCutoffLength(int val) {
		cutOffLengthSlider.setValue(val);
	}


}