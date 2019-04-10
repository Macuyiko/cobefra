package be.kuleuven.econ.cbf.result;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import be.kuleuven.econ.cbf.input.Mapping;
import be.kuleuven.econ.cbf.metrics.AbstractMetric;
import be.kuleuven.econ.cbf.metrics.SubmetricList;
import be.kuleuven.econ.cbf.process.CalculationManager;
import be.kuleuven.econ.cbf.process.MetricCalculator;

public class CBFResult {

	public class InputID implements Comparable<InputID> {
		public final String logfile;
		public final String netfile;

		public InputID(String log, String net) {
			this.logfile = log;
			this.netfile = net;
		}

		@Override
		public int compareTo(InputID arg0) {
			return (logfile + netfile).compareTo(arg0.logfile + arg0.netfile);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof InputID)
				return compareTo((InputID) obj) == 0;
			else
				return false;
		}
	}

	public class MetricID implements Comparable<MetricID> {
		public final String metric;

		public MetricID(String metric) {
			this.metric = metric;
		}

		@Override
		public int compareTo(MetricID o) {
			return metric.compareTo(o.metric);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MetricID)
				return compareTo((MetricID) obj) == 0;
			else
				return false;
		}
	}

	public class ResultValue {

		/**
		 * Will be null when status is SUBMETRIC.
		 */
		public final String errors;
		public final double result;

		/**
		 * Will be 0 when status is SUBMETRIC.
		 */
		public final float runtime;
		public final Status status;

		public ResultValue(double result, float runtime, Status status,
				String errors) {
			this.result = result;
			this.runtime = runtime;
			this.status = status;
			this.errors = errors;
		}
	}

	public enum Status {
		ERROR, OK, SUBMETRIC, WARNING;

		public static Status fromCalculator(MetricCalculator c) {
			switch (c.getStatus()) {
			case CANCELLED:
			case FAILED:
			case RUNNING:
			case LOADING:
			case READY:
				return ERROR;
			case FINISHED:
				if (c.getErrors().length() == 0)
					return OK;
				else
					return WARNING;
			default:
				return ERROR;
			}
		}
	}

	private static String toCSVField(String s) {
		s = s.replace("\"", "\"\"");
		s = '"' + s + '"';
		return s;
	}

	private Map<InputID, Map<MetricID, ResultValue>> contents;
	private List<MetricID> metrics;

	public CBFResult(CalculationManager manager) {
		contents = new TreeMap<InputID, Map<MetricID, ResultValue>>();
		metrics = new ArrayList<MetricID>();
		for (MetricCalculator c : manager.getFinishedCalculators()) {
			Mapping mapping = c.getMapping();
			AbstractMetric metric = c.getMetric();
			float runtime = (float) (c.getTime() / 1000.0);
			Status status = Status.fromCalculator(c);
			double result;
			result = c.getResult();

			InputID iKey = new InputID(mapping.getLogPath(),
					mapping.getPetrinetPath());
			MetricID mKey = new MetricID(metric.getName());
			ResultValue resultValue = new ResultValue(result, runtime, status,
					c.getErrors());

			Map<MetricID, ResultValue> map = contents.get(iKey);
			if (map == null) {
				map = new TreeMap<MetricID, ResultValue>();
				contents.put(iKey, map);
			}
			map.put(mKey, resultValue);

			if (!metrics.contains(mKey))
				metrics.add(mKey);

			SubmetricList list = metric.getSubmetrics();
			for (int i = 0; i < list.getNbSubmetrics(); i++) {
				MetricID m = new MetricID(list.getSubmetric(i).getName());
				ResultValue r = new ResultValue(c.getSubresult(i), 0,
						Status.SUBMETRIC, null);
				map.put(m, r);

				if (!metrics.contains(m))
					metrics.add(m);
			}
		}
	}

	public Iterator<InputID> getInputIDIterator() {
		return contents.keySet().iterator();
	}

	public InputID[] getInputIDs() {
		Set<InputID> keySet = contents.keySet();
		return keySet.toArray(new InputID[keySet.size()]);
	}

	public Iterator<MetricID> getMetricIDIterator() {
		return metrics.iterator();
	}

	public MetricID[] getMetricIDs() {
		return metrics.toArray(new MetricID[metrics.size()]);
	}

	public int getNbMetricIDs() {
		return metrics.size();
	}

	public int getNbInputIDs() {
		return contents.size();
	}

	public ResultValue getValue(InputID iKey, MetricID mKey) {
		return contents.get(iKey).get(mKey);
	}

	public void writeTo(FileOutputStream fstream) {
		BufferedOutputStream bos = new BufferedOutputStream(fstream);
		PrintStream stream = new PrintStream(bos);
		stream.println("logfile path,netfile path,metric activity,result,runtime in seconds,status");
		for (Entry<InputID, Map<MetricID, ResultValue>> entry : contents
				.entrySet()) {
			InputID iKey = entry.getKey();
			for (Entry<MetricID, ResultValue> subentry : entry.getValue()
					.entrySet()) {
				MetricID mKey = subentry.getKey();
				ResultValue resultValue = subentry.getValue();
				// logfile
				stream.print(toCSVField(iKey.logfile));
				stream.print(',');
				// netfile
				stream.print(toCSVField(iKey.netfile));
				stream.print(',');
				// metric
				stream.print(toCSVField(mKey.metric));
				stream.print(',');
				// result
				stream.print(resultValue.result);
				stream.print(',');
				// runtime
				if (resultValue.status != Status.SUBMETRIC)
					stream.print(resultValue.runtime);
				stream.print(',');
				// status
				stream.print(resultValue.status);
				stream.println();
			}
		}
		stream.flush();
		stream.close();
	}
}