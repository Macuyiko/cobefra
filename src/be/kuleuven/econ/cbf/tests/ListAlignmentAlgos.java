package be.kuleuven.econ.cbf.tests;

import java.util.Set;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.petrinet.replayer.annotations.PNReplayAlgorithm;

import be.kuleuven.econ.cbf.ui.metrics.recall.AryaFitnessVisualizer;

public class ListAlignmentAlgos {

	public static void main(String[] args) {
		try {
			Boot.boot(AryaFitnessVisualizer.class, PluginContext.class);
			FakePluginContext context = new FakePluginContext();
			
			Set<Class<?>> PNReplayAlgorithmClasses = 
				context.getPluginManager().getKnownClassesAnnotatedWith(PNReplayAlgorithm.class);
			
			for (Class<?> c : PNReplayAlgorithmClasses)
				System.out.println(c.getName()+".class,");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
