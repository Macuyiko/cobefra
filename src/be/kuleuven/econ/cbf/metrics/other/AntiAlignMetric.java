package be.kuleuven.econ.cbf.metrics.other;

import java.io.File;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import org.processmining.antialignments.ilp.antialignment.AntiAlignmentParameters;
import org.processmining.antialignments.ilp.antialignment.AntiAlignmentValues;
import org.processmining.antialignments.ilp.antialignment.HeuristicAntiAlignmentAlgorithm;
import org.processmining.antialignments.ilp.util.AntiAlignments;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.packages.UnknownPackageException;
import org.processmining.framework.packages.UnknownPackageTypeException;
import org.processmining.framework.packages.impl.CancelledException;
import org.processmining.framework.util.PathHacker;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;

import nl.tue.astar.AStarException;

public abstract class AntiAlignMetric extends AryaMetric {
	protected int cutOffLength;
	protected double maxFactor;
	protected int backtrackLimit;
	protected double backtrackThreshold;
	protected int resultType;
	
	public AntiAlignMetric() {
		cutOffLength = 5;
		maxFactor = 1.0;
		backtrackLimit = 1;
		backtrackThreshold = 2.0;
		resultType = 0;
		createFinalMarking = true;
	}
	
	@Override
	protected Map<String, String> getProperties() {
		Map<String, String> map = super.getProperties();
		map.put("cutOffLength", cutOffLength + "");
		map.put("maxFactor", maxFactor + "");
		map.put("backtrackLimit", backtrackLimit + "");
		map.put("backtrackThreshold", backtrackThreshold + "");
		map.put("resultType", resultType + "");
		return map;
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		super.setProperties(properties);
		maxFactor = Double.parseDouble(properties.get("maxFactor"));
		backtrackThreshold = Double.parseDouble(properties.get("backtrackThreshold"));
		cutOffLength = Integer.parseInt(properties.get("cutOffLength"));
		backtrackLimit = Integer.parseInt(properties.get("backtrackLimit"));
		resultType = Integer.parseInt(properties.get("resultType"));
		fireCompletenessChanged();
	}
	
	@Override
	public void replayLog() throws AStarException {
		super.replayLog();
		context = new FakePluginContext();
		
		try {
			PackageManager.getInstance().cleanPackageCache();
			PackageManager.getInstance().update(false, Level.ALL);
			PackageManager.getInstance().findOrInstallPackages("LpSolve");
		} catch (BackingStoreException | CancelledException | UnknownPackageTypeException | UnknownPackageException e) {
			System.err.println("Fatal Package Manager Error: Could not install LpSolve");
		}
		
		File dir = PackageManager.getInstance().getPackagesDirectory();
		for (File d : dir.listFiles()) {
			if (!d.exists() || !d.isDirectory())
				continue;
			for (File f : d.listFiles()) {
				if (f.getAbsolutePath().toLowerCase().endsWith(".jar"))
					PathHacker.addJar(f);
				if (f.isDirectory()) {
					for (File f2 : f.listFiles()) {
						if (f2.getAbsolutePath().toLowerCase().endsWith(".jar"))
							PathHacker.addJar(f2);
					}
				}
			}
		}
		PathHacker.addLibraryPathFromDirectory(dir);
		
		HeuristicAntiAlignmentAlgorithm algorithm = new HeuristicAntiAlignmentAlgorithm(
				petrinet, initialMarking, finalMarking, 
	    		log, repResult, logMapper);
		AntiAlignmentParameters parameters = new AntiAlignmentParameters(cutOffLength, maxFactor, backtrackLimit, backtrackThreshold);
	    AntiAlignments aa = algorithm.computeAntiAlignments(context.getProgress(), parameters);
	    AntiAlignmentValues values = algorithm.computePrecisionAndGeneralization(aa);
	    repResult = algorithm.getPNRepResult(aa, values, parameters);	    
	}

	public int getCutOffLength() {
		return cutOffLength;
	}

	public void setCutOffLength(int cutOffLength) {
		this.cutOffLength = cutOffLength;
	}

	public double getMaxFactor() {
		return maxFactor;
	}

	public void setMaxFactor(double maxFactor) {
		this.maxFactor = maxFactor;
	}

	public int getBacktrackLimit() {
		return backtrackLimit;
	}

	public void setBacktrackLimit(int backtrackLimit) {
		this.backtrackLimit = backtrackLimit;
	}

	public double getBacktrackThreshold() {
		return backtrackThreshold;
	}

	public void setBacktrackThreshold(double backtrackThreshold) {
		this.backtrackThreshold = backtrackThreshold;
	}

	public int getResultType() {
		return resultType;
	}

	public void setResultType(int resultType) {
		this.resultType = resultType;
	}


}
