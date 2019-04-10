package be.kuleuven.econ.cbf.ui.process;

import static be.kuleuven.econ.cbf.ui.UISettings.FRAME_HEIGHT;
import static be.kuleuven.econ.cbf.ui.UISettings.FRAME_WIDTH;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class ErrorFrame extends JFrame {

	public ErrorFrame(String errors) {
		this.setTitle("Metric Calculation Errors");
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setLocationByPlatform(true);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		JTextArea area = new JTextArea();
		area.setText(errors);
		area.setEditable(false);
		this.add(new JScrollPane(area));
		this.setVisible(true);
	}
}
