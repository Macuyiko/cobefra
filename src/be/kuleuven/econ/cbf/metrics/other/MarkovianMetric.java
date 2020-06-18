package be.kuleuven.econ.cbf.metrics.other;

import java.util.Map;
import java.util.TreeMap;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Abs;
import au.edu.unimelb.processmining.accuracy.MarkovianAccuracyCalculator.Opd;
import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractSimpleMetric;
import au.edu.qut.processmining.log.LogParser;
import au.edu.qut.processmining.log.SimpleLog;
import au.edu.unimelb.processmining.accuracy.abstraction.Abstraction;
import au.edu.unimelb.processmining.accuracy.abstraction.LogAbstraction;
import au.edu.unimelb.processmining.accuracy.abstraction.ProcessAbstraction;
import au.edu.unimelb.processmining.accuracy.abstraction.distances.ConfusionMatrix;
import au.edu.unimelb.processmining.accuracy.abstraction.intermediate.AutomatonAbstraction;
import au.edu.unimelb.processmining.accuracy.abstraction.subtrace.SubtraceAbstraction;
import de.drscc.automaton.Automaton;
import de.drscc.importer.ImportProcessModel;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;

public abstract class MarkovianMetric extends AbstractSimpleMetric {
	protected Petrinet petrinet = null;
	protected XLog log = null;
	protected Marking initialMarking = null;

	protected Abs type;
	protected Opd opd;
	protected int order;

	public MarkovianMetric() {
		type = Abs.MARK;
		opd = Opd.SPL;
		order = 3;
	}

	@Override
	public boolean isComplete() {
		return true;
	}
		
	public double calculate(boolean fitness) {
		MarkovianAccuracyCalculator calc = new MarkovianAccuracyCalculator();
		calc.order = this.order;
		calc.importLog(log, type);
		calc.importPetrinet(petrinet, initialMarking, type);
		if (fitness) return calc.computeFitness(opd);
		else return calc.computePrecision(opd);
	}

	@Override
	public synchronized void load(Mapping mapping) {
		Object[] petrimarking = mapping.getPetrinetWithMarking();
		petrinet = (Petrinet) petrimarking[0];
		initialMarking = (Marking) petrimarking[1];
		log = mapping.getLog();
		applyMappingToPetrinet(mapping, petrinet);

	}

	protected void applyMappingToPetrinet(Mapping mapping, Petrinet targetNet) {
		for (Transition t : targetNet.getTransitions()) {
			t.setInvisible(mapping.getActivityInvisible(t.getLabel()));
			if (mapping.getActivityInvisible(t.getLabel())) {
				t.getAttributeMap().put(AttributeMap.LABEL, "tau");
			} else {
				if (mapping.getActivity(t.getLabel()) == null)
					t.getAttributeMap().put(AttributeMap.LABEL, "__UNMAPPED__VISIBLE__");
				else
					t.getAttributeMap().put(AttributeMap.LABEL, mapping.getActivity(t.getLabel()).getName());
			}
		}
	}

	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("type", type.name());
		map.put("opd", opd.name());
		map.put("order", "" + order);
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		type = Abs.valueOf(properties.get("type"));
		opd = Opd.valueOf(properties.get("opd"));
		order = Integer.parseInt(properties.get("order"));
		fireCompletenessChanged();
	}
	
	public Petrinet getPetrinet() {
		return petrinet;
	}

	public Marking getInitialMarking() {
		return initialMarking;
	}

	public Abs getType() {
		return type;
	}

	public void setType(Abs type) {
		this.type = type;
	}

	public Opd getOpd() {
		return opd;
	}

	public void setOpd(Opd opd) {
		this.opd = opd;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public class MarkovianAccuracyCalculator {
		static final boolean includeLTT = false;
		SimpleLog log;
		Automaton automaton;
		AutomatonAbstraction automatonAbstraction;
		Abstraction logAbstraction;
		Abstraction processAbstraction;
		ConfusionMatrix matrix;
		int order;

		double computeFitness(Opd opd) {
			double fitness = -1.0D;
			switch (opd) {
			case STD:
			case SPL:
				fitness = this.logAbstraction.minus(this.processAbstraction);
				break;
			case HUN:
				fitness = this.logAbstraction.minusHUN(this.processAbstraction);
				break;
			case GRD:
				fitness = this.logAbstraction.minusGRD(this.processAbstraction);
				break;
			case CFM:
				if (this.logAbstraction instanceof SubtraceAbstraction) {
					this.matrix = ((SubtraceAbstraction) this.logAbstraction).confusionMatrix(this.processAbstraction);
					fitness = this.matrix.getTP() / (this.matrix.getFN() + this.matrix.getTP());
				}
				break;
			case UHU:
				if (this.logAbstraction instanceof SubtraceAbstraction)
					fitness = ((SubtraceAbstraction) this.logAbstraction).minusUHU(this.processAbstraction);
				break;
			default:
				break;
			}
			return fitness;
		}

		double computePrecision(Opd opd) {
			double precision = -1.0D;
			switch (opd) {
			case STD:
			case SPL:
				precision = this.processAbstraction.minus(this.logAbstraction);
				break;
			case HUN:
				precision = this.processAbstraction.minusHUN(this.logAbstraction);
				break;
			case GRD:
				precision = this.processAbstraction.minusGRD(this.logAbstraction);
				break;
			case CFM:
				if (this.logAbstraction instanceof SubtraceAbstraction) {
					this.matrix = ((SubtraceAbstraction) this.logAbstraction).confusionMatrix(this.processAbstraction);
					precision = this.matrix.getTP() / (this.matrix.getFP() + this.matrix.getTP());
				}
				break;
			case UHU:
				if (this.processAbstraction instanceof SubtraceAbstraction)
					precision = ((SubtraceAbstraction) this.processAbstraction).minusUHU(this.logAbstraction);
				break;
			default:
				break;
			}
			return precision;
		}

		boolean importLog(XLog xlog, Abs type) {
			try {
				this.log = LogParser.getSimpleLog(xlog, (XEventClassifier) new XEventNameClassifier());
				switch (type) {
				case MARK:
					this.logAbstraction = (Abstraction) LogAbstraction.markovian(this.log, this.order);
					break;
				case STA:
					this.logAbstraction = (Abstraction) LogAbstraction.subtrace(this.log, this.order);
					break;
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		boolean importPetrinet(Petrinet petrinet, Marking initialMarking, Abs type) {
			ImportProcessModel importer = new ImportProcessModel();
			try {
				this.automaton = importer.createFSMfromPetrinet(petrinet, initialMarking, null, null);
				this.automatonAbstraction = new AutomatonAbstraction(this.automaton, this.log);
				switch (type) {
				case MARK:
					this.processAbstraction = (Abstraction) (new ProcessAbstraction(this.automatonAbstraction))
							.markovian(this.order);
					break;
				case STA:
					this.processAbstraction = (Abstraction) (new ProcessAbstraction(this.automatonAbstraction))
							.subtrace(this.order);
					break;
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	

}
