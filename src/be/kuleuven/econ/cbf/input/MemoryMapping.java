package be.kuleuven.econ.cbf.input;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

/**
 * This class provides identical functionality as {@link Mapping}. However,
 * instead of maintaining as little memory usage as possible and maintaining
 * only references to the files containing the represented petrinet and log,
 * this class allows to build a mapping for an in-memory petrinet and log.
 * 
 * This allows you to avoid the overhead of the filesystem.
 */
public class MemoryMapping extends Mapping {

	private XLog log;
	private Petrinet net;

	/**
	 * Create a new {@link Mapping} for the given log and petrinet. Upon
	 * completion of this constructor the mapping is initialized with as much
	 * information as possible. This initial mapping is deducted only from the
	 * given net and log.
	 */
	public MemoryMapping(XLog log, Petrinet net) {
		super(null, null, false, false);
		this.log = log;
		this.net = net;

		readActivities();
		readTransitions();
		doMapping();
	}

	@Override
	public Petrinet getPetrinet() {
		return net;
	}

	@Override
	public XLog getLog() {
		return log;
	}
}
