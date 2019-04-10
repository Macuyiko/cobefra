package be.kuleuven.econ.cbf.ui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import be.kuleuven.econ.cbf.input.Mapping;

public class GraphicalMappingLoader extends JWindow {

	private JLabel lblNewLabel;
	private JLabel lblLoadingMappings;
	private JLabel lblThisMayTake;
	private JLabel lblCurrentlyLoading;
	private JLabel lblLogfile;
	private JLabel lblMappedOnto;
	private JLabel lblNetfile;
	private JLabel lblFramework;
	private JProgressBar progressBar;

	public static Mapping create(String logfile, String netfile) {
		GraphicalMappingLoader loader = new GraphicalMappingLoader();
		loader.progressBar.setIndeterminate(true);
		loader.setVisible(true);
		loader.setLogfile(logfile);
		loader.setNetfile(netfile);
		Mapping out = new Mapping(logfile, netfile);
		loader.setVisible(false);
		loader.dispose();
		return out;
	}

	public static Mapping[] create(String logfile, String[] netfiles) {
		if (netfiles.length == 1)
			return new Mapping[] { create(logfile, netfiles[0]) };
		GraphicalMappingLoader loader = new GraphicalMappingLoader();
		loader.progressBar.setMaximum(netfiles.length - 1);
		loader.setVisible(true);
		loader.setLogfile(logfile);
		Mapping[] out = new Mapping[netfiles.length];
		for (int i = 0; i < netfiles.length; i++) {
			loader.progressBar.setValue(i);
			loader.setNetfile(netfiles[i]);
			out[i] = new Mapping(logfile, netfiles[i]);
		}
		loader.setVisible(false);
		loader.dispose();
		return out;
	}

	private GraphicalMappingLoader() {
		getContentPane().setBackground(new Color(102, 153, 204));
		setBounds(100, 100, 450, 300);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);

		lblNewLabel = new JLabel("Comprehensive Benchmark");
		lblNewLabel.setForeground(Color.BLACK);
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 28));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);

		lblLoadingMappings = new JLabel("loading mappings...");
		lblLoadingMappings.setFont(new Font("SansSerif", Font.PLAIN, 16));

		lblThisMayTake = new JLabel("This may take a while, please wait...");
		lblThisMayTake.setFont(new Font("SansSerif", Font.ITALIC, 10));

		lblCurrentlyLoading = new JLabel("currently loading:");
		lblCurrentlyLoading.setFont(new Font("SansSerif", Font.PLAIN, 16));

		lblLogfile = new JLabel("<logfile>");
		lblLogfile.setFont(new Font("SansSerif", Font.PLAIN, 16));

		lblMappedOnto = new JLabel("mapped onto");
		lblMappedOnto.setFont(new Font("SansSerif", Font.ITALIC, 10));

		lblNetfile = new JLabel("<netfile>");
		lblNetfile.setFont(new Font("SansSerif", Font.PLAIN, 16));

		lblFramework = new JLabel("Framework");
		lblFramework.setForeground(Color.BLACK);
		lblFramework.setHorizontalAlignment(SwingConstants.CENTER);
		lblFramework
				.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 28));

		progressBar = new JProgressBar();
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																lblNewLabel,
																GroupLayout.DEFAULT_SIZE,
																449,
																Short.MAX_VALUE)
														.addComponent(
																lblFramework,
																GroupLayout.DEFAULT_SIZE,
																449,
																Short.MAX_VALUE)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGap(40)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addGroup(
																												groupLayout
																														.createParallelGroup(
																																Alignment.LEADING)
																														.addComponent(
																																lblThisMayTake)
																														.addComponent(
																																lblLoadingMappings))
																										.addPreferredGap(
																												ComponentPlacement.RELATED,
																												92,
																												Short.MAX_VALUE))
																						.addComponent(
																								lblCurrentlyLoading)
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addGap(20)
																										.addGroup(
																												groupLayout
																														.createParallelGroup(
																																Alignment.LEADING)
																														.addGroup(
																																groupLayout
																																		.createSequentialGroup()
																																		.addGap(40)
																																		.addComponent(
																																				lblMappedOnto))
																														.addComponent(
																																lblLogfile,
																																GroupLayout.DEFAULT_SIZE,
																																162,
																																Short.MAX_VALUE)
																														.addComponent(
																																lblNetfile,
																																GroupLayout.DEFAULT_SIZE,
																																390,
																																Short.MAX_VALUE))))))
										.addGap(10))
						.addComponent(progressBar, GroupLayout.DEFAULT_SIZE,
								450, Short.MAX_VALUE));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
				Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addComponent(lblNewLabel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblFramework)
						.addGap(20)
						.addComponent(lblLoadingMappings)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblThisMayTake)
						.addGap(22)
						.addComponent(lblCurrentlyLoading)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblLogfile)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblMappedOnto)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblNetfile)
						.addPreferredGap(ComponentPlacement.RELATED, 23,
								Short.MAX_VALUE)
						.addComponent(progressBar, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE)));
		getContentPane().setLayout(groupLayout);
	}

	public void setLogfile(String s) {
		int width = lblLogfile.getWidth();
		int cutoff = 0;
		String display = s;
		while (cutoff < s.length()) {
			if (cutoff > 0)
				display = "..." + s.substring(cutoff);
			int length = lblLogfile.getFontMetrics(lblLogfile.getFont())
					.stringWidth(display);
			if (length > width)
				if (cutoff == 0)
					cutoff = 4;
				else
					cutoff++;
			else
				break;
		}
		lblLogfile.setText(display);
	}

	public void setNetfile(String s) {
		int width = lblNetfile.getWidth();
		int cutoff = 0;
		String display = s;
		while (cutoff < s.length()) {
			if (cutoff > 0)
				display = "..." + s.substring(cutoff);
			int length = lblNetfile.getFontMetrics(lblNetfile.getFont())
					.stringWidth(display);
			if (length > width)
				if (cutoff == 0)
					cutoff = 4;
				else
					cutoff++;
			else
				break;
		}
		lblNetfile.setText(display);
	}
}
