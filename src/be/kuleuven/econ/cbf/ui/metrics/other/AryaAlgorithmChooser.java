package be.kuleuven.econ.cbf.ui.metrics.other;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.astar.petrinet.PrefixBasedPetrinetReplayer;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.behavapp.BehavAppPruneAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteMarkEquationPrune;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompletePruneAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedprefix.CostBasedPrefixAlg;
import org.processmining.plugins.petrinet.replayer.algorithms.syncproduct.SyncProductAlg;
import org.processmining.plugins.petrinet.replayer.annotations.PNReplayAlgorithm;


public class AryaAlgorithmChooser extends JComponent {
		private JList<IPNReplayAlgorithm> algorithmList;
		private JComboBox<IPNReplayAlgorithm> combo;
		private final JLabel label = new JLabel(""); // Help text currently unused
		private IPNReplayAlgorithm[] availableAlgorithms;

		public AryaAlgorithmChooser(PetrinetGraph net, XLog log, TransEvClassMapping mapping) {
			this(net, log, mapping, null);
		}
		
		public AryaAlgorithmChooser(PetrinetGraph net, XLog log, TransEvClassMapping mapping,
				Class<?> chosenAlgorithm) {
			
			PluginContext context = new FakePluginContext();
			
			if (chosenAlgorithm == null)
				chosenAlgorithm = PetrinetReplayerWithoutILP.class;
			
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			JLabel lblTitle = new JLabel("What is the purpose of your replay?");
			lblTitle.setAlignmentX(LEFT_ALIGNMENT);
			add(lblTitle);
			
			JRadioButton fitnessRB = new JRadioButton("Measuring fitness", true);
			JRadioButton fitnessCompleteRB = new JRadioButton("    Penalize improper completion", true);
			JRadioButton fitnessNotCompleteRB = new JRadioButton("    Don't penalize improper completion", false);
			JRadioButton behavAppRB = new JRadioButton("Measuring behavioral appropriateness", false);
			
			fitnessRB.setAlignmentX(LEFT_ALIGNMENT);
			fitnessCompleteRB.setAlignmentX(LEFT_ALIGNMENT);
			fitnessNotCompleteRB.setAlignmentX(LEFT_ALIGNMENT);
			behavAppRB.setAlignmentX(LEFT_ALIGNMENT);
			
			add(fitnessRB);
			add(fitnessCompleteRB);
			add(fitnessNotCompleteRB);
			add(behavAppRB);
			
			DefActListener defaultAction = new DefActListener(context, fitnessRB, fitnessCompleteRB, net, log, mapping);

			ButtonGroup algTypeSelection = new ButtonGroup();
			fitnessRB.addActionListener(defaultAction);
			behavAppRB.addActionListener(defaultAction);
			algTypeSelection.add(fitnessRB);
			algTypeSelection.add(behavAppRB);

			ButtonGroup fitnessTypeSelection = new ButtonGroup();
			fitnessCompleteRB.addActionListener(defaultAction);
			fitnessNotCompleteRB.addActionListener(defaultAction);
			fitnessTypeSelection.add(fitnessCompleteRB);
			fitnessTypeSelection.add(fitnessNotCompleteRB);

			add(Box.createRigidArea(new Dimension(0,20)));
			
			JLabel lblSuggest = new JLabel("I would suggest the following (double click to select):");
			lblSuggest.setAlignmentX(LEFT_ALIGNMENT);
			add(lblSuggest);
			
			algorithmList = new JList<IPNReplayAlgorithm>();
			algorithmList.setAlignmentX(LEFT_ALIGNMENT);
			algorithmList.setPreferredSize(new Dimension(this.getWidth(), 60));
			add(algorithmList);
			algorithmList.addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent evt) {
			        @SuppressWarnings("unchecked")
					JList<IPNReplayAlgorithm> list = (JList<IPNReplayAlgorithm>)evt.getSource();
			        if (evt.getClickCount() == 2) {
			        	IPNReplayAlgorithm algo = list.getSelectedValue();
			        	if (algo == null)
			        		return;
			        	for (int i = 0; i < combo.getItemCount(); i++)
			        		if (combo.getItemAt(i).getClass().equals(algo.getClass()))
			        			combo.setSelectedIndex(i);
			        }
			    }
			});
			
			// If ProM was booted:
			Set<Class<?>> coverageEstimatorClasses = 
					context.getPluginManager().getKnownClassesAnnotatedWith(PNReplayAlgorithm.class);
			
			availableAlgorithms = null;
			IPNReplayAlgorithm selectedAlgo = null;
			if (coverageEstimatorClasses != null) {
				List<IPNReplayAlgorithm> algList = new LinkedList<IPNReplayAlgorithm>();
				for (Class<?> coverClass : coverageEstimatorClasses) {
					try {
						IPNReplayAlgorithm alg = (IPNReplayAlgorithm) coverClass.newInstance();
						if (alg.isReqWOParameterSatisfied(context, net, log, mapping)) {
							algList.add(alg);
							if (chosenAlgorithm != null && coverClass.equals(chosenAlgorithm))
								selectedAlgo = alg;
						}
					} catch (Exception exc) {
						// Ignore, there are a bunch of mismatches here...
					}
				}
				Collections.sort(algList, new Comparator<IPNReplayAlgorithm>() {
					@Override
					public int compare(IPNReplayAlgorithm o1,
							IPNReplayAlgorithm o2) {
						return o1.toString().compareTo(o2.toString());
					}
				});
				availableAlgorithms = (IPNReplayAlgorithm[]) algList
						.toArray(new IPNReplayAlgorithm[algList.size()]);
			}
			
			add(Box.createRigidArea(new Dimension(0,40)));
			
			JLabel lblSelect = new JLabel("Select an algorithm:");
			lblSelect.setAlignmentX(LEFT_ALIGNMENT);
			add(lblSelect);
			
			combo = new JComboBox<IPNReplayAlgorithm>(availableAlgorithms);
			combo.setPreferredSize(new Dimension(this.getWidth(), 25));
			combo.setAlignmentX(LEFT_ALIGNMENT);
			setMaxSize(combo);
			add(combo);
			combo.setSelectedItem(selectedAlgo);
			
			add(Box.createVerticalGlue());
			
			populateBasicAlgorithms(context, 
					fitnessRB.isSelected(),
					fitnessCompleteRB.isSelected(), 
					net, log, mapping);
			
			combo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					label.setText(((IPNReplayAlgorithm) combo.getSelectedItem()).getHTMLInfo());
				}
			});
			
		}
		
		private void setMaxSize(JComponent jc) {
			Dimension max = jc.getMaximumSize();
			Dimension pref = jc.getPreferredSize();
			max.height = pref.height;
			jc.setMaximumSize(max);
		}

		private void populateBasicAlgorithms(PluginContext context,
				boolean isFitness, boolean isComplete, PetrinetGraph net,
				XLog log, TransEvClassMapping mapping) {

			// So we use the pluginmanager to fill up the combobox, but manually add
			// all algorithms here?
			algorithmList.removeAll();
			List<IPNReplayAlgorithm> listAlgorithms = new LinkedList<IPNReplayAlgorithm>();
			
			if (isFitness && isComplete) {
				try {
					Collection<FinalMarkingConnection> conns =
							context.getConnectionManager().getConnections(FinalMarkingConnection.class,
									context, new Object[] { net });
					if (conns != null) {
						for (FinalMarkingConnection conn : conns) {
							if (conn.getObjectWithRole("Marking") != null) {
								@SuppressWarnings("rawtypes")
								AbstractPetrinetReplayer express = new PetrinetReplayerWithILP();
								if (!express.isReqWOParameterSatisfied(
										context, net, log, mapping))
									break;
								listAlgorithms.add(express);
								break;
							}
						}

					}
				} catch (ConnectionCannotBeObtained e) {
				}
				
				AbstractPetrinetReplayer<?, ?> pnReplayerWOILP = new PetrinetReplayerWithoutILP();
				if (pnReplayerWOILP.isReqWOParameterSatisfied(context, net, log,
						mapping)) {
					listAlgorithms.add(pnReplayerWOILP);
				}
	
				CostBasedCompletePruneAlg prune = new CostBasedCompletePruneAlg();
				if (prune.isReqWOParameterSatisfied(context, net, log, mapping)) {
					listAlgorithms.add(prune);
				}
	
				CostBasedCompleteMarkEquationPrune markEqPrune = new CostBasedCompleteMarkEquationPrune();
				if (markEqPrune.isReqWOParameterSatisfied(context, net, log,
						mapping)) {
					listAlgorithms.add(markEqPrune);
				}
			} else if (isFitness && !isComplete) {
				// check prefix replayer
				PrefixBasedPetrinetReplayer prefixReplayer = new PrefixBasedPetrinetReplayer();
				if (prefixReplayer.isReqWOParameterSatisfied(context, net, log,
						mapping)) {
					listAlgorithms.add(prefixReplayer);
				}
	
				// check cost based
				CostBasedPrefixAlg costBased = new CostBasedPrefixAlg();
				if (costBased.isReqWOParameterSatisfied(context, net, log, mapping)) {
					listAlgorithms.add(costBased);
				}
	
				// check synchronous product
				SyncProductAlg syncProduct = new SyncProductAlg();
				if (syncProduct.isReqWOParameterSatisfied(context, net, log,
						mapping)) {
					listAlgorithms.add(syncProduct);
				}
			} else if (!isFitness) {
				listAlgorithms.add(new BehavAppPruneAlg());
			}
			
			algorithmList.setListData(listAlgorithms.toArray(new IPNReplayAlgorithm[]{}));
		}

		public IPNReplayAlgorithm getAlgorithm() {
			return (IPNReplayAlgorithm) combo.getSelectedItem();
		}

		class DefActListener implements ActionListener {
			private AbstractButton fitnessRB;
			private AbstractButton fitnessCompleteRB;
			private PetrinetGraph net;
			private XLog log;
			private TransEvClassMapping mapping;
			private PluginContext context;

			public DefActListener(PluginContext context,
					AbstractButton fitnessRB, AbstractButton fitnessCompleteRB,
					PetrinetGraph net, XLog log, TransEvClassMapping mapping) {
				this.context = context;
				this.fitnessRB = fitnessRB;
				this.fitnessCompleteRB = fitnessCompleteRB;
				this.net = net;
				this.log = log;
				this.mapping = mapping;
			}

			public void actionPerformed(ActionEvent e) {
				populateBasicAlgorithms(this.context,
						this.fitnessRB.isSelected(),
						this.fitnessCompleteRB.isSelected(), 
						this.net,
						this.log, 
						this.mapping);
			}
		}

	}