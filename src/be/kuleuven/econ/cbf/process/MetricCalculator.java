package be.kuleuven.econ.cbf.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerException;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.utils.FinishedListener;

public class MetricCalculator implements Runnable {

	public enum Status {

		/**
		 * The calculation has been cancelled. This can be done either by the
		 * user or automatically.
		 */
		CANCELLED,

		/**
		 * The calculation has been cancelled, but the calculator is still
		 * running. This status occurs after the calculator has started
		 * processing a cancel signal, but before the cancellation has been
		 * completed.
		 */
		CANCELLING,

		/**
		 * The calculation has failed.
		 */
		FAILED,

		/**
		 * It is certain that the calculation has failed, but the calculator has
		 * not yet fully exited.
		 */
		FAILING,

		/**
		 * The calculation has been successfully completed.
		 */
		FINISHED,

		/**
		 * The calculation has been completed successfully, but the calculator
		 * is not completely ready yet.
		 */
		FINISHING,

		/**
		 * The calculation is currently loading the resources it requires to
		 * run.
		 */
		LOADING,

		/**
		 * The calculator is ready to be started.
		 */
		READY,

		/**
		 * The actual calculation is currently in progress.
		 */
		RUNNING;
	}

	private ByteArrayOutputStream errors;
	private boolean errorStreamOpen;
	private List<FinishedListener<MetricCalculator>> finishedListeners;
	private boolean hasCalledFinishedListeners;
	private Thread main;
	private boolean mainRunning;
	private Mapping mapping;
	private AbstractMetric metric;
	private double result;
	private Thread shutdownHook;
	private Object startNotifier;
	private Status status;
	private long startTime;
	private double[] subresults;
	private long endTime;

	public MetricCalculator(Mapping mapping, AbstractMetric metric) {
		this.metric = metric;
		this.mapping = mapping;
		this.main = null;
		this.errors = new ByteArrayOutputStream();
		this.finishedListeners = new ArrayList<FinishedListener<MetricCalculator>>();
		this.hasCalledFinishedListeners = false;
		this.startNotifier = new Object();
		this.shutdownHook = new Thread() {

			@Override
			public void run() {
				try {
					MetricCalculator.this.finalize();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
		this.errorStreamOpen = false;
		this.result = 0.0;
		this.startTime = -1;
		this.endTime = -1;
		setStatus(Status.READY);
	}

	public void addFinishedListener(FinishedListener<MetricCalculator> l) {
		finishedListeners.add(l);
	}

	public void cancel() {
		if (!isWorking())
			throw new IllegalStateException(
					"The calculator is currently not working");
		main.interrupt();
	}

	/**
	 * Set the maximum time (in minutes) after which the calculation should be
	 * cancelled if it is still running. Setting this value to 0 will disable
	 * automatic cancellation.
	 * 
	 * Automatic cancelling will not take effect if the calculation has already
	 * exceeded the given amount of minutes.
	 * 
	 * @throws IllegalStateException
	 *             If the calculator is not currently running. Be aware this can
	 *             cause race conditions. For example, using
	 *             <p>
	 *             <code>
	 *             calculator.start();<br>
	 *             calculator.cancelAfter(10);
	 *             </code>
	 *             </p>
	 *             can cause an IllegalStateException. You can avoid this by
	 *             wrapping cancelAfter(int) inside a catch block, such as
	 *             <p>
	 *             <code>
	 *             calculator.start();<br>
	 *             try {<br>
	 *             calculator.cancelAfter(10);<br>
	 *             } catch (IllegalStateException e) {<br>
	 *             }
	 *             <code>
	 *             </p>
	 * @throws IllegalArgumentException
	 *             If minutes is a negative number.
	 * @see #isRunning()
	 */
	public void cancelAfter(int minutes) {
		if (!isWorking())
			throw new IllegalStateException("The calculator must be working");
		if (minutes < 0)
			throw new IllegalStateException("Timeout must be positive or zero");
		long time = System.currentTimeMillis() - getTime() + 60000 * minutes;
		if (time <= System.currentTimeMillis())
			AutoCanceller.getInstance().removeCancel(this);
		else
			AutoCanceller.getInstance().setCancelAt(this, time);
	}

	private Process createProcess() throws IOException {
		// program command:
		// [java.home]/bin/java -cp [java.class.path] Main
		String separator = System.getProperty("file.separator");
		String executable =
		System.getProperty("java.home")
				+ separator + "bin"
				+ separator + "java";
		String classpath = System.getProperty("java.class.path");
		String main = Main.class.getCanonicalName();
		// TODO: add possibility to change memory
		String[] command = new String[] { executable, "-Xmx4G", "-cp", classpath, main };
		return Runtime.getRuntime().exec(command);
	}

	private void feedInformation(Process process) throws IOException,
			TransformerException {
		ZipOutputStream stream = new ZipOutputStream(process.getOutputStream());
		// Feed mapping
		stream.putNextEntry(new ZipEntry("mapping"));
		mapping.writeMapping(stream, true);
		// Feed metric
		stream.putNextEntry(new ZipEntry("metric"));
		MetricSet set = new MetricSet();
		set.add(metric);
		set.writeMetricSet(stream);
		stream.close();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (isWorking())
			cancel();
	}

	/**
	 * Can be called multiple times. By doing this, it is possible to put this
	 * method wherever a change from false to true in {{@link #isFinished()} can
	 * occur.
	 */
	private synchronized void finished() {
		if (!mainRunning && !errorStreamOpen && !hasCalledFinishedListeners) {
			switch (getStatus()) {
			case FINISHING:
				setStatus(Status.FINISHED);
				break;
			case FAILING:
				setStatus(Status.FAILED);
				break;
			case CANCELLING:
				setStatus(Status.CANCELLED);
				break;
			default:
				throw new IllegalStateException("Cannot finish from status "
						+ getStatus());
			}
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			} catch (IllegalStateException e) {
			}
			shutdownHook = null;
			for (FinishedListener<MetricCalculator> l : finishedListeners)
				l.ended(this);
			hasCalledFinishedListeners = true;
		}
	}

	public String getErrors() {
		return errors.toString();
	}

	/**
	 * @return the mapping
	 */
	public Mapping getMapping() {
		return mapping;
	}

	/**
	 * @return the metric
	 */
	public AbstractMetric getMetric() {
		return metric;
	}

	public double getResult() {
		return result;
	}

	public Status getStatus() {
		return status;
	}

	/**
	 * Get the current running time of this calculator. When this calculator is
	 * still loading information, the current running time is defined as time
	 * since starting the calculator. As soon as loading is completed, it is
	 * defined to be the time that has passed since the calculator has finished
	 * loading. When the calculator has stopped working, it is the total time
	 * that had passed between the end of the loading phase and the end of the
	 * actual calculation.
	 * 
	 * This information is represented in milliseconds. The result of this
	 * method may go up or down over time, there are no guarantees.
	 */
	public long getTime() {
		if (startTime == -1)
			return 0l;
		else if (endTime == -1)
			return System.currentTimeMillis() - startTime;
		else
			return endTime - startTime;
	}

	public boolean isCompleted() {
		switch (getStatus()) {
		case FAILED:
		case CANCELLED:
		case FINISHED:
			return true;
		default:
			return false;
		}
	}

	public boolean isWorking() {
		switch (getStatus()) {
		case LOADING:
		case RUNNING:
		case FAILING:
		case CANCELLING:
		case FINISHING:
			return true;
		default:
			return false;
		}
	}

	public void join() throws InterruptedException {
		if (main == null)
			throw new IllegalStateException("This calculator has yet to start");
		if (main.isAlive())
			main.join();
	}

	/**
	 * When a '\0' has been read on the stream, it's considered to be closed.
	 * 
	 * This could be improved if '\0' is expected to occur on the stream.
	 */
	private void monitorErrors(final Process process) {
		errorStreamOpen = true;
		Thread t = new Thread() {

			@Override
			public void run() {
				InputStream in = process.getErrorStream();
				try {
					int c;
					while ((c = in.read()) != -1)
						if (c != 0)
							errors.write(c);
						else
							break;
				} catch (IOException e) {
				}
				errorStreamOpen = false;
				finished();
			}
		};
		t.setName("MetricCalculator error monitor - " + getMetric().getName());
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void run() {
		startTime = System.currentTimeMillis();
		mainRunning = true;
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		boolean failed = true;
		setStatus(Status.LOADING);
		synchronized (this) {
			this.notify();
		}
		Process process = null;
		try {
			// Create a process
			process = createProcess();
			// Set up the error reading
			monitorErrors(process);
			// Feed the process information
			feedInformation(process);
			// Create our result stream
			final ObjectInputStream ois = new ObjectInputStream(
					process.getInputStream());
			boolean eof = false;
			try {
				// Read the start time, when we have that we're actually running
				// This must be done in another thread so we can still catch our
				// interrupts
				// The synchronize stuff makes sure 'time' is running before
				// continuing.
				final Object synch = new Object();
				Thread time = new Thread() {

					@Override
					public void run() {
						synchronized (synch) {
							synch.notify();
						}
						try {
							startTime = ois.readLong();
							setStatus(Status.RUNNING);
						} catch (Exception e) {
						}
					}
				};
				time.setDaemon(true);
				time.setName("MetricCalculator start time reader - "
						+ getMetric().getName());
				synchronized (synch) {
					time.start();
					synch.wait();
				}

				// Wait for the process to finish
				process.waitFor();
				// Read end time
				endTime = ois.readLong();
				// Get the main metric's output
				result = ois.readDouble();
				// Get the submetrics' output
				int nbSubmetrics = getMetric().getSubmetrics()
						.getNbSubmetrics();
				subresults = new double[nbSubmetrics];
				for (int i = 0; i < nbSubmetrics; i++)
					subresults[i] = ois.readDouble();
			} catch (IOException e2) {
				eof = true;
			}
			// Determine result correctness
			failed = eof || process.exitValue() != 0;
		} catch (InterruptedException e) {
			// Interesting scenario:
			// The thread gets interrupted if it has to destroy the process
			setStatus(Status.CANCELLING);
			failed = false;
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} finally {
			if (failed)
				setStatus(Status.FAILING);
			else if (getStatus() != Status.CANCELLING)
				setStatus(Status.FINISHING);
			tryClose(process.getInputStream());
			tryClose(process.getOutputStream());
			mainRunning = false;
			finished();
		}
	}

	private void tryClose(InputStream s) {
		try {
			s.close();
		} catch (IOException e) {
		}
	}

	private void tryClose(OutputStream s) {
		try {
			s.close();
		} catch (IOException e) {
		}
	}

	private synchronized void setStatus(Status status) {
		this.status = status;
	}

	public synchronized void start() {
		if (getStatus() != Status.READY)
			throw new IllegalStateException("This calculator is not ready");
		main = new Thread(this);
		main.setName("MetricCalculator - " + getMetric().getName());
		synchronized (startNotifier) {
			// Deadlock-free since the main thread can't get owernship of
			// the monitor while we have it. This way the main
			// thread can't have already issued notify(), since it cannot own
			// its monitor until we release it with
			// wait().
			main.start();
			boolean interrupted = false;
			try {
				this.wait();
			} catch (InterruptedException e) {
				interrupted = true;
			}
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	/**
	 * Return the result of the index'th submetric of the calculation's metric.
	 * 
	 * @see #getMetric()
	 * @see AbstractMetric#getSubmetrics()
	 */
	public double getSubresult(int index) {
		if (index < 0 || index >= subresults.length)
			throw new IllegalArgumentException("Bad index (" + index
					+ ") specified");
		return subresults[index];
	}
}
