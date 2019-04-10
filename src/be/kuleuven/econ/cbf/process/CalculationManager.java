package be.kuleuven.econ.cbf.process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.utils.CreationListener;
import be.kuleuven.econ.cbf.utils.FinishedListener;

public class CalculationManager implements FinishedListener<MetricCalculator> {

	private List<MetricCalculator> busy;
	private boolean clean;
	private List<CreationListener<MetricCalculator>> creationListeners;
	private List<MetricCalculator> finished;
	private List<FinishedListener<MetricCalculator>> finishedListeners;
	private InputSet input;
	private MetricSet metrics;
	private int nbParallel;
	private Object notification;
	private int cancelAfter;

	public CalculationManager(InputSet input, MetricSet metrics) {
		this.input = input;
		this.metrics = metrics;
		this.nbParallel = 1;
		this.busy = new LinkedList<MetricCalculator>();
		this.finished = new ArrayList<MetricCalculator>();
		((ArrayList<MetricCalculator>) this.finished)
				.ensureCapacity(getNbCalculations());
		clean = true;
		notification = new Object();
		this.finishedListeners = new ArrayList<FinishedListener<MetricCalculator>>();
		this.creationListeners = new ArrayList<CreationListener<MetricCalculator>>();
		cancelAfter = 0;
	}

	public void addCreationListener(CreationListener<MetricCalculator> l) {
		synchronized (creationListeners) {
			creationListeners.add(l);
		}
	}

	public void addFinishedListener(FinishedListener<MetricCalculator> l) {
		synchronized (finishedListeners) {
			finishedListeners.add(l);
		}
	}

	private void created(MetricCalculator origin) {
		synchronized (creationListeners) {
			for (CreationListener<MetricCalculator> l : creationListeners)
				l.created(origin);
		}
	}

	@Override
	public void ended(MetricCalculator origin) {
		synchronized (busy) {
			busy.remove(origin);
		}
		synchronized (finished) {
			finished.add(origin);
		}
		synchronized (finishedListeners) {
			for (FinishedListener<MetricCalculator> l : finishedListeners)
				l.ended(origin);
		}
		synchronized (notification) {
			notification.notifyAll();
		}
	}

	public MetricCalculator[] getFinishedCalculators() {
		return finished.toArray(new MetricCalculator[finished.size()]);
	}

	public int getNbCalculations() {
		return input.size() * metrics.size();
	}

	/**
	 * This method will abort when the current thread is interrupted. If you do
	 * not want this behaviour you should wrap the execution of this command in
	 * a separate thread. This could, for example, be done like this:
	 * 
	 * <p>
	 * <code>
	 * Thread t = new Thread() {<br><br>
	 * &nbsp;@Override<br>
	 * &nbsp;public void run() {<br>
	 * &nbsp;&nbsp;manager.perform();
	 * &nbsp;}<br>
	 * }<br>
	 * try {<br>
	 * &nbsp;t.join()<br>
	 * } catch (InterruptedException e) {</br>
	 * &nbsp;//Handle your own interruption here<br>
	 * }
	 * </code>
	 * </p>
	 * Note that you will still be able to preempt the calculations by
	 * interrupting this new thread.
	 * 
	 * @throws InterruptedException
	 *             If the active thread has been interrupted.
	 */
	public synchronized void perform() throws InterruptedException {
		if (!clean)
			throw new IllegalStateException("Cannot reuse calculation manager");
		clean = false;
		try {
			for (Mapping mapping : input)
				for (AbstractMetric metric : metrics) {
					waitForStart();
					MetricCalculator calculator = new MetricCalculator(mapping,
							metric);
					created(calculator);
					calculator.addFinishedListener(this);
					calculator.start();
					try {
						calculator.cancelAfter(cancelAfter);
					} catch (IllegalStateException e) {
					}
					synchronized (busy) {
						busy.add(calculator);
					}
				}
			waitAll();
		} catch (InterruptedException e) {
			// Force-close all running calculators
			for (Object o : busy.toArray())
				try {
					((MetricCalculator) o).cancel();
				} catch (Exception e2) {
				}
			// We did our part, it's their turn now
			throw e;
		}
	}

	/**
	 * This method has the same effect as calling
	 * {@link MetricCalculator#cancelAfter(int)} on each currently active
	 * calculator and all calculators that will be created from now on.
	 */
	public void setCancelAfter(int minutes) {
		cancelAfter = minutes;
		synchronized (busy) {
			for (MetricCalculator calculator : busy)
				try {
					calculator.cancelAfter(minutes);
				} catch (IllegalStateException e) {
				}
		}
	}

	public int getNbParallel() {
		return nbParallel;
	}

	public void setNbParallel(int nbParallel) {
		if (nbParallel <= 0)
			throw new IllegalArgumentException(
					"Amount of parallel executions should be at least 1");
		this.nbParallel = nbParallel;
		synchronized (notification) {
			notification.notifyAll();
		}
	}

	private void waitAll() throws InterruptedException {
		synchronized (notification) {
			while (busy.size() != 0)
				notification.wait();
		}
	}

	private void waitForStart() throws InterruptedException {
		synchronized (notification) {
			while (busy.size() >= nbParallel)
				notification.wait();
		}
	}
}
