package be.kuleuven.econ.cbf.utils;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DebugWindow {
	private static JFrame frame;
	private static JTextArea text;

	private static void setup() {
		text = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(text);
		frame = new JFrame("Debug Window");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void showDebugLine(String line) {
		if (frame == null || text == null)
			setup();
		if (text.getText().length() > 800000)
			text.setText("");
		text.setText(line + "\n" + text.getText());
	}
}
