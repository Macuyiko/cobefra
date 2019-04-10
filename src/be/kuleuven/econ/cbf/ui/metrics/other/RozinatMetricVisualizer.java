package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Component;
import javax.swing.JPanel;

import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.other.RozinatMetric;
import be.kuleuven.econ.cbf.ui.UISettings;
import be.kuleuven.econ.cbf.ui.metrics.AbstractMetricVisualizer;
import be.kuleuven.econ.cbf.ui.metrics.MetricVisualizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@MetricVisualizer(metrics = { RozinatMetric.class })
public class RozinatMetricVisualizer extends AbstractMetricVisualizer {
	private boolean punishUnmapped;
	private int timeoutLogReplay;
	private int timeoutStateSpaceExploration;
	private int maxDepth;
	private boolean findBestShortestSequence;

	@Override
	protected Component getVisualizer(AbstractMetric m) {
		final RozinatMetric metric = (RozinatMetric) m;
		final Component c = getConfigPanel(metric);
		
		punishUnmapped = metric.isPunishUnmapped();
		timeoutLogReplay = metric.getTimeoutLogReplay();
		timeoutStateSpaceExploration = metric.getTimeoutStateSpaceExploration();
		maxDepth = metric.getMaxDepth();
		findBestShortestSequence = metric.getFindBestShortestSequence();

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		UISettings.prettify(content);
		
		content.add(c);
		
		JButton applyButton = new JButton("Apply Settings");
		applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				applySettings(metric);
			}
		});
		content.add(applyButton);
		
		return content;
	}
	
	private void applySettings(RozinatMetric metric) {
		metric.setPunishUnmapped(punishUnmapped);
		metric.setTimeoutLogReplay(timeoutLogReplay);
		metric.setTimeoutStateSpaceExploration(timeoutStateSpaceExploration);
		metric.setMaxDepth(maxDepth);
		metric.setFindBestShortestSequence(findBestShortestSequence);
		hideMetric(metric);
	}
	
	private Component getConfigPanel(AbstractMetric m) {
		final RozinatMetric metric = (RozinatMetric) m;
		JPanel panel = new JPanel();
		UISettings.prettify(panel);

		final JCheckBox chkUnmapped = new JCheckBox(
				"Punish unmapped activities");
		
		chkUnmapped.setOpaque(false);
		chkUnmapped.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				punishUnmapped = chkUnmapped.isSelected();
			}
		});
		
		final JSpinner spinner = new JSpinner();
		final JCheckBox chckbxRestrictSearch = new JCheckBox(
				"Restrict search depth for invisible tasks");
		
		chckbxRestrictSearch.setOpaque(false);
		chckbxRestrictSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (chckbxRestrictSearch.isSelected()) {
					spinner.setEnabled(true);
					maxDepth = (int) spinner.getValue();
				} else {
					spinner.setEnabled(false);
					maxDepth = -1;
				}
			}
		});
		
		JLabel lblMaximumDepth = new JLabel("Maximum depth:");
		final JCheckBox chckbxBestShortestSequence = new JCheckBox(
				"Choose best shortest sequence of invisible tasks");
		chckbxBestShortestSequence.setOpaque(false);
		chckbxBestShortestSequence.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findBestShortestSequence = chckbxBestShortestSequence.isSelected();
			}
		});
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (chckbxRestrictSearch.isSelected()) {
					maxDepth = (int) spinner.getValue();
				} else {
					maxDepth = -1;
				}
			}
		});
		spinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));

		chckbxBestShortestSequence.setSelected(metric.getFindBestShortestSequence());
		if (metric.getMaxDepth() == -1) {
			chckbxRestrictSearch.setSelected(false);
			spinner.setEnabled(false);
			spinner.setValue(0);
		} else {
			chckbxRestrictSearch.setSelected(true);
			spinner.setEnabled(true);
			spinner.setValue(metric.getMaxDepth());
		}

		JLabel lblSpaceStateExploration = new JLabel("State Space Exploration timeout:");

		final JSpinner spinnerSSEtime = new JSpinner();
		spinnerSSEtime.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				timeoutStateSpaceExploration = (int) spinnerSSEtime.getValue();
			}
		});
		spinnerSSEtime.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		spinnerSSEtime.setValue(metric.getTimeoutStateSpaceExploration());

		JLabel lblautomaticallyEndsThe = new JLabel(
				"<html><p>Automatically ends the execution of the state space exploration " +
				"after the given amount of minutes have passed since the start of the exploration." +
				" Set this value to zero to disable automatic cancellation.</p></html>");

		JLabel lblNewLabel = new JLabel("Log Replay timeout:");

		final JSpinner spinnerLRtime = new JSpinner();
		spinnerLRtime.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				timeoutLogReplay = (int) spinnerLRtime.getValue();
			}
		});
		spinnerLRtime.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		spinnerLRtime.setValue(metric.getTimeoutLogReplay());

		JLabel lblautomaticallyEndsThe_1 = new JLabel(
				"<html><p>Automatically ends the execution of the log replay after the given amount " +
				"of minutes have passed since the start of the replay. Set this value to zero to disable " +
				"automatic cancellation.</p><p>When the log replay has been cancelled, the metric will " +
				"<b>not</b> provide a result but instead just crash.</p></html>");

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.TRAILING).addGroup(gl_panel.createSequentialGroup()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(chkUnmapped).addComponent(chckbxBestShortestSequence)
						.addGroup(gl_panel.createSequentialGroup().addGap(31).addComponent(lblMaximumDepth)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(spinner, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
						.addComponent(chckbxRestrictSearch)
						.addGroup(gl_panel.createSequentialGroup().addContainerGap()
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
										.addComponent(lblSpaceStateExploration).addComponent(lblNewLabel))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(spinnerLRtime, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
										.addComponent(spinnerSSEtime, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))))
				.addGap(191))
				.addGroup(gl_panel.createSequentialGroup().addGap(20)
						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblautomaticallyEndsThe_1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
								.addComponent(lblautomaticallyEndsThe, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
						.addContainerGap()));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel
				.createSequentialGroup().addComponent(chkUnmapped).addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxRestrictSearch).addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblMaximumDepth).addComponent(
						spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxBestShortestSequence).addGap(18)
				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblSpaceStateExploration)
						.addComponent(spinnerSSEtime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblautomaticallyEndsThe)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblNewLabel).addComponent(
						spinnerLRtime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(ComponentPlacement.RELATED).addComponent(lblautomaticallyEndsThe_1)
				.addContainerGap(93, Short.MAX_VALUE)));
		panel.setLayout(gl_panel);

		return panel;
	}
}
