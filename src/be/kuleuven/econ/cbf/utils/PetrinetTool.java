package be.kuleuven.econ.cbf.utils;

import java.io.File;
import java.io.FileInputStream;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;

public class PetrinetTool {

	public static Object[] openPNML(String path) throws Exception {
		File pnmlFile = new File(path);
		PnmlImportUtils utils = new PnmlImportUtils();
		FileInputStream input;
		Pnml pnml = null;
		input = new FileInputStream(pnmlFile);
		pnml = utils.importPnmlFromStream(null, input, null, 0);
		input.close();
		Petrinet net = PetrinetFactory.newPetrinet(pnml.getLabel() + " imported");
		Marking marking = new Marking();
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		pnml.convertToNet(net, marking, layout);
		return new Object[]{net, marking};
	}
	
	public static Petrinet readPetrinet(String path) {
		try {
			Object[] petriAndMarking = openPNML(path);
			return (Petrinet) petriAndMarking[0];
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to read pnml file", e);
		}
	}
	
	public static Marking readMarking(String path) {
		try {
			Object[] petriAndMarking = openPNML(path);
			return (Marking) petriAndMarking[1];
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to read pnml file", e);
		}
	}

	public static boolean isPetrinetFile(String path) {
		File file = new File(path);
		boolean ok = false;
		if (file.getAbsolutePath().endsWith(".pnml"))
			ok = true;
		if (!file.isFile())
			ok = false;
		return ok;
	}
}