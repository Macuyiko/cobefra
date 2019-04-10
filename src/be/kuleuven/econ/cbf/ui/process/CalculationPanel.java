package be.kuleuven.econ.cbf.ui.process;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ERROR_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_OK_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_WARNING_DARK;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import be.kuleuven.econ.cbf.process.MetricCalculator;
import be.kuleuven.econ.cbf.process.MetricCalculator.Status;
import be.kuleuven.econ.cbf.utils.FinishedListener;

public class CalculationPanel extends JPanel implements
		FinishedListener<MetricCalculator> {

	private Component spacing;
	private JLabel lblStatus;
	private JLabel lblRunningTime;
	private JLabel lblNbRunningTime;
	private JButton btnCancel;
	private JButton btnShowLog;
	private JLabel lblNewLogInfo;

	public CalculationPanel(final MetricCalculator calculator, Component spacing) {
		this.spacing = spacing;

		setBackground(COLOUR_ITEM_BACKGROUND);
		setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));

		JLabel lblMetricname = new JLabel(calculator.getMetric().getName());
		lblMetricname.setFont(new Font("Tahoma", Font.BOLD, 12));

		JLabel lblLogFile = new JLabel("Log file:");

		JLabel lblPetrinetFile = new JLabel("Petrinet file:");

		JLabel lblPetrinetpath = new JLabel(calculator.getMapping()
				.getPetrinetPath());

		JLabel lblLogpath = new JLabel(calculator.getMapping().getLogPath());

		lblRunningTime = new JLabel("Current running time:");

		lblNbRunningTime = new JLabel("0:00");

		lblStatus = new JLabel("calculating");
		lblStatus.setForeground(COLOUR_WARNING_DARK);
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 12));

		btnCancel = new JButton("cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				calculator.cancel();
			}
		});

		btnShowLog = new JButton("show errors");
		btnShowLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new ErrorFrame(calculator.getErrors());
			}
		});
		btnShowLog.setVisible(false);

		lblNewLogInfo = new JLabel("new error info available");
		lblNewLogInfo.setVisible(false);

		GroupLayout groupLayout = new GroupLayout(this);
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
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								lblPetrinetFile)
																						.addComponent(
																								lblLogFile))
																		.addGap(16)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.TRAILING)
																						.addComponent(
																								lblPetrinetpath,
																								GroupLayout.DEFAULT_SIZE,
																								338,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblLogpath,
																								GroupLayout.DEFAULT_SIZE,
																								338,
																								Short.MAX_VALUE)))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addContainerGap()
																		.addComponent(
																				lblNewLogInfo))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblMetricname)
																		.addPreferredGap(
																				ComponentPlacement.RELATED,
																				285,
																				Short.MAX_VALUE)
																		.addComponent(
																				lblStatus))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblRunningTime)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblNbRunningTime,
																				GroupLayout.DEFAULT_SIZE,
																				311,
																				Short.MAX_VALUE))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				btnCancel)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				btnShowLog)))
										.addContainerGap()));
		groupLayout
				.setVerticalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblMetricname)
														.addComponent(lblStatus))
										.addGap(7)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblNewLogInfo))
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblLogFile)
														.addComponent(
																lblLogpath))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblPetrinetFile)
														.addComponent(
																lblPetrinetpath))
										.addGap(6)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblRunningTime)
														.addComponent(
																lblNbRunningTime))
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(btnCancel)
														.addComponent(
																btnShowLog))));
		setLayout(groupLayout);
		startFollower(calculator);
		calculator.addFinishedListener(this);
		if (calculator.isCompleted())
			ended(calculator);
	}

	@Override
	public void ended(MetricCalculator origin) {
		lblRunningTime.setText("Final running time:");
		btnCancel.setVisible(false);
		CalculationPanel.this.revalidate();
		if (origin.getStatus() == Status.FINISHED)
			removeSelf(2000);
	}

	public void removeSelf(final int sleep) {
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
				}
				Container parent = CalculationPanel.this.getParent();
				CalculationPanel.this.setVisible(false);
				spacing.setVisible(false);
				parent.revalidate();
				parent.remove(CalculationPanel.this);
				parent.remove(spacing);
			}
		}.start();
	}

	public void startFollower(final MetricCalculator c) {
		new Thread() {

			private int errorLength = 0;

			@Override
			public void run() {
				boolean oneBehind = true;
				while (oneBehind) {
					// We want to update once after the calculator stopped
					// working.
					oneBehind = !c.isCompleted();

					// Update the time
					long time = c.getTime();
					int seconds = (int) (time / 1000l) % 60;
					int minutes = (int) (time / 60000l);
					String pre;
					if (seconds < 10)
						pre = "0";
					else
						pre = "";
					lblNbRunningTime.setText(minutes + ":" + pre + seconds);
					lblNbRunningTime.revalidate();

					// Check for errors
					String errors = c.getErrors();
					int newErrorLength = errors.length();
					if (newErrorLength != 0 && errorLength == 0) {
						btnShowLog.setVisible(true);
						btnShowLog.revalidate();
					}
					if (newErrorLength > errorLength) {
						lblNewLogInfo.setVisible(true);
						lblNewLogInfo.revalidate();
					}
					errorLength = newErrorLength;

					// Update the status
					Status status = c.getStatus();
					lblStatus.setText(status.name());
					switch (status) {
					case CANCELLED:
					case CANCELLING:
					case FAILED:
					case FAILING:
						lblStatus.setForeground(COLOUR_ERROR_DARK);
						break;
					case FINISHED:
						lblStatus.setForeground(COLOUR_OK_DARK);
						break;
					case FINISHING:
					case LOADING:
					case READY:
					case RUNNING:
						lblStatus.setForeground(COLOUR_WARNING_DARK);
						break;
					default:
						throw new IllegalStateException(
								"The domain has been changed");
					}

					// Wait a while
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			}
		}.start();
	}
}
