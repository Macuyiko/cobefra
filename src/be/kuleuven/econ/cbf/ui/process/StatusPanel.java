package be.kuleuven.econ.cbf.ui.process;

import static be.kuleuven.econ.cbf.ui.UISettings.COLOUR_ITEM_BACKGROUND;
import static be.kuleuven.econ.cbf.ui.UISettings.MARGIN;

import java.awt.Font;
import java.util.Calendar;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import be.kuleuven.econ.cbf.process.CalculationManager;
import be.kuleuven.econ.cbf.process.MetricCalculator;
import be.kuleuven.econ.cbf.utils.FinishedListener;

public class StatusPanel extends JPanel implements
		FinishedListener<MetricCalculator> {

	private CalculationManager manager;
	private int nbCalculations;
	private int nbCompleted;
	private int nbFailed;
	private JLabel lblPrRemaining;
	private JLabel lblNbFailed;
	private JLabel lblNbRemaining;
	private JLabel lblNbCompleted;
	private JLabel lblPrFailed;
	private JLabel lblPrCompleted;
	private JLabel lblFinishedAt;
	private JLabel lblFinishedAtTime;

	public StatusPanel(CalculationManager manager) {
		this.nbCalculations = manager.getNbCalculations();
		this.manager = manager;
		nbCompleted = 0;
		nbFailed = 0;
		setBackground(COLOUR_ITEM_BACKGROUND);
		setBorder(new EmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));

		JLabel lblCurrentlyCalculating = new JLabel("Currently calculating:");
		lblCurrentlyCalculating.setFont(new Font("Tahoma", Font.BOLD, 12));

		JLabel lblCompleted = new JLabel("Completed:");
		JLabel lblRemaining = new JLabel("Remaining:");
		JLabel lblFailed = new JLabel("Failed:");

		lblNbCompleted = new JLabel("0/" + nbCalculations);
		lblNbCompleted.setHorizontalAlignment(SwingConstants.RIGHT);

		lblNbRemaining = new JLabel(nbCalculations + "/" + nbCalculations);
		lblNbRemaining.setHorizontalAlignment(SwingConstants.RIGHT);

		lblNbFailed = new JLabel("0/" + nbCalculations);
		lblNbFailed.setHorizontalAlignment(SwingConstants.RIGHT);

		lblPrCompleted = new JLabel("(0%)");
		lblPrRemaining = new JLabel("(100%)");
		lblPrFailed = new JLabel("(0%)");

		JLabel lblNumberOfParallel = new JLabel(
				"Number of parallel calculations:");

		final JSpinner spinnerCalculations = new JSpinner();
		spinnerCalculations.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				StatusPanel.this.manager
						.setNbParallel((Integer) spinnerCalculations.getValue());
			}
		});
		spinnerCalculations.setModel(new SpinnerNumberModel(this.manager.getNbParallel(), new Integer(1), null, new Integer(1)));

		JLabel lblRunningSince = new JLabel("Started at:");

		JLabel lblStarttime = new JLabel(getTime());

		lblFinishedAt = new JLabel("Finished at:");
		lblFinishedAt.setVisible(false);

		lblFinishedAtTime = new JLabel("<time>");
		lblFinishedAtTime.setVisible(false);

		JLabel lblAutomaticCancelAfter = new JLabel("Automatic cancel after:");

		final JSpinner spinnerAutocancel = new JSpinner();
		spinnerAutocancel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				StatusPanel.this.manager
						.setCancelAfter((Integer) spinnerAutocancel.getValue());
			}
		});
		spinnerAutocancel.setModel(new SpinnerNumberModel(new Integer(0),
				new Integer(0), null, new Integer(1)));
		spinnerAutocancel
				.setToolTipText("Time (in minutes) after which any calculation should automatically be cancelled. " +
						"Enter a value of 0 minutes to disable this feature.");

		JLabel lblMinutes = new JLabel("minutes");

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
														.addComponent(
																lblCurrentlyCalculating)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								lblFailed)
																						.addComponent(
																								lblCompleted)
																						.addComponent(
																								lblRemaining))
																		.addGap(18)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.TRAILING)
																						.addComponent(
																								lblNbRemaining,
																								GroupLayout.DEFAULT_SIZE,
																								70,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblNbCompleted,
																								GroupLayout.DEFAULT_SIZE,
																								70,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblNbFailed,
																								GroupLayout.DEFAULT_SIZE,
																								70,
																								Short.MAX_VALUE))
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								lblPrFailed,
																								GroupLayout.DEFAULT_SIZE,
																								70,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblPrRemaining,
																								GroupLayout.DEFAULT_SIZE,
																								70,
																								Short.MAX_VALUE)
																						.addComponent(
																								lblPrCompleted,
																								GroupLayout.DEFAULT_SIZE,
																								70,
																								Short.MAX_VALUE)))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblAutomaticCancelAfter)
																		.addGap(55)
																		.addComponent(
																				spinnerAutocancel,
																				GroupLayout.PREFERRED_SIZE,
																				50,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblMinutes))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblNumberOfParallel)
																		.addGap(18)
																		.addComponent(
																				spinnerCalculations,
																				GroupLayout.PREFERRED_SIZE,
																				50,
																				GroupLayout.PREFERRED_SIZE))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblRunningSince)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblStarttime))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblFinishedAt)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblFinishedAtTime)))
										.addContainerGap(232, Short.MAX_VALUE)));
		groupLayout
				.setVerticalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addComponent(lblCurrentlyCalculating)
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblCompleted)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblRemaining)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.BASELINE)
																						.addComponent(
																								lblFailed)
																						.addComponent(
																								lblNbFailed)
																						.addComponent(
																								lblPrFailed)))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblPrCompleted)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				lblPrRemaining))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblNbCompleted,
																				GroupLayout.PREFERRED_SIZE,
																				16,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(ComponentPlacement.RELATED)
																		.addComponent(lblNbRemaining)))
										.addGap(18)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblAutomaticCancelAfter)
														.addComponent(
																spinnerAutocancel,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																lblMinutes))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblNumberOfParallel)
														.addComponent(
																spinnerCalculations,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addGap(18)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblRunningSince)
														.addComponent(
																lblStarttime))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																lblFinishedAt)
														.addComponent(
																lblFinishedAtTime))));
		setLayout(groupLayout);
	}

	private void setNbCompleted(int n) {
		nbCompleted = n;
		lblNbCompleted.setText(nbCompleted + "/" + nbCalculations);
		int pr = (nbCompleted * 100) / nbCalculations;
		lblPrCompleted.setText("(" + pr + "%)");
		setNbRemaining();
	}

	private void setNbFailed(int n) {
		nbFailed = n;
		lblNbFailed.setText(nbFailed + "/" + nbCalculations);
		int pr = (nbFailed * 100) / nbCalculations;
		lblPrFailed.setText("(" + pr + "%)");
		setNbRemaining();
	}

	private void setNbRemaining() {
		int nbRemaining = nbCalculations - nbCompleted - nbFailed;
		lblNbRemaining.setText(nbRemaining + "/" + nbCalculations);
		int pr = (nbRemaining * 100) / nbCalculations;
		lblPrRemaining.setText("(" + pr + "%)");
	}

	@Override
	public void ended(MetricCalculator origin) {
		switch (origin.getStatus()) {
		case FAILED:
		case CANCELLED:
			setNbFailed(nbFailed + 1);
			break;
		case FINISHED:
			setNbCompleted(nbCompleted + 1);
			break;
		default:
			throw new IllegalStateException("The domain has been changed");
		}
		if (nbFailed + nbCompleted == nbCalculations) {
			lblFinishedAt.setVisible(true);
			lblFinishedAtTime.setText(getTime());
			lblFinishedAtTime.setVisible(true);
		}
	}

	public String getTime() {
		StringBuilder sb = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		sb.append(calendar.get(Calendar.YEAR));
		sb.append("-");
		int month = calendar.get(Calendar.MONTH) + 1;
		if (month < 10)
			sb.append('0');
		sb.append(month);
		sb.append("-");
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (day < 10)
			sb.append('0');
		sb.append(day);
		sb.append(" ");
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 10)
			sb.append('0');
		sb.append(hour);
		sb.append(":");
		int minute = calendar.get(Calendar.MINUTE);
		if (minute < 10)
			sb.append('0');
		sb.append(minute);
		return sb.toString();
	}
}
