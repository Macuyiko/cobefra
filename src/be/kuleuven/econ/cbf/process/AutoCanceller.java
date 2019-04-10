package be.kuleuven.econ.cbf.process;

import java.util.ArrayList;
import java.util.List;

class AutoCanceller implements Runnable {

	private static AutoCanceller instance = null;

	private List<Entry> entries;

	private class Entry {
		MetricCalculator calculator;
		long time;
	}

	private AutoCanceller() {
		entries = new ArrayList<Entry>();
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	/**
	 * Schedule a MetricCalculator to be cancelled at the given time. The given
	 * time is in the same format as given by {@link System#currentTimeMillis()}
	 * .
	 */
	public void setCancelAt(MetricCalculator calculator, long time) {
		Entry e = new Entry();
		e.calculator = calculator;
		e.time = time;
		synchronized (entries) {
			// Remove a (possible) old entry
			for (int i = 0; i < entries.size(); i++)
				if (entries.get(i).calculator == calculator) {
					entries.remove(i);
					break;
				}
			// Insert the new entry at the correct position
			int pointer = 0;
			while (pointer < entries.size() && entries.get(pointer).time < time)
				pointer++;
			entries.add(pointer, e);
			entries.notify();
		}
	}

	/**
	 * Cancel the automatic cancellation of the given calculator. This method
	 * has no effect if the given calculator is not scheduled to be cancelled.
	 */
	public void removeCancel(MetricCalculator calculator) {
		synchronized (entries) {
			for (int i = 0; i < entries.size(); i++)
				if (entries.get(i).calculator == calculator) {
					entries.remove(i);
					break;
				}
		}
	}

	public static AutoCanceller getInstance() {
		if (instance == null)
			instance = new AutoCanceller();
		return instance;
	}

	@Override
	public void run() {
		synchronized (entries) {
			try {
				while (true) {
					// Determine how long we have to wait
					// = 0 => no entries, wait until we get notified
					// < 0 => something ran out of time
					// > 0 => everything has time left
					long timeout = 0;
					if (entries.size() > 0) {
						timeout = entries.get(0).time
								- System.currentTimeMillis();
						if (timeout == 0)
							timeout = -1;
					}
					if (timeout >= 0)
						entries.wait(timeout);
					// If there are entries, check the first one and cancel if
					// required. Upon re-entering the loop we'll be checking
					// every other entry with the same time field.
					if (entries.size() > 0) {
						Entry e = entries.get(0);
						if (e.time - System.currentTimeMillis() <= 0) {
							if (e.calculator.isWorking())
								e.calculator.cancel();
							entries.remove(0);
						}
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}
}
