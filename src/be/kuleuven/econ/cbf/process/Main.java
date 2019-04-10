package be.kuleuven.econ.cbf.process;

import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.zip.ZipInputStream;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.metrics.SubmetricList;
import be.kuleuven.econ.cbf.utils.NullOutputStream;

public class Main {

	/**
	 * This program expects no command line arguments. The expected input on the
	 * standard input stream consists of a serialised mapping and abstract
	 * metric. This stream will be considered to be a ZipInputStream where those
	 * two objects are put on as two different entries.
	 * 
	 * The output of this program will be moved to System.out, where it will
	 * consist of two Longs and Double that have been written using an
	 * ObjectOutputStream. The first Object is a Long representing the start
	 * time of the calculation as given by {@link System#currentTimeMillis()}.
	 * The second Object is again a Long, representing the end time of the
	 * calculation as given by {@link System#currentTimeMillis()}. The last
	 * Object is a Double containing the result of the calculation.
	 * 
	 * The main use of this class is to function as a starting point when
	 * creating a new process in which to perform a metric calculation.
	 */
	public static void main(String[] args) {
		try {
			// Create a zip input stream, override the close method so nobody
			// can accidentally close it beforehand.
			ZipInputStream zip = new ZipInputStream(System.in) {

				@Override
				public void close() {
				}
			};
			// Create the output stream
			ObjectOutputStream oos = new ObjectOutputStream(System.out);

			// Ensure nobody touches System.out
			// Otherwise our output will get messed up
			System.setOut(new PrintStream(new NullOutputStream()));

			// Read the mapping
			zip.getNextEntry();
			Mapping mapping = Mapping.readMapping(zip);

			// Read the metric (first as set)
			zip.getNextEntry();
			MetricSet metricset = MetricSet.readMetricSet(zip);
			AbstractMetric metric = metricset.get(0);
			System.in.close();

			// Load the required information
			metric.load(mapping);

			// Send out the current time as start time
			oos.writeLong(System.currentTimeMillis());
			oos.flush();

			// Do the calculation
			metric.calculate();

			// Send out the current time as end time
			oos.writeLong(System.currentTimeMillis());

			// Write our result
			oos.writeDouble(metric.getResult());

			// Write our subresults
			SubmetricList l = metric.getSubmetrics();
			for (int i = 0; i < l.getNbSubmetrics(); i++)
				oos.writeDouble(l.getSubmetric(i).getResult());

			// Clean up
			oos.flush();
			System.err.write('\0');
			System.err.flush();
			System.exit(0);
		} catch (Exception e) {
			// Folks, we're having some technical difficulties...
			e.printStackTrace();
			System.exit(1);
		}
	}
}
