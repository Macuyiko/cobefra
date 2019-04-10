import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.input.Mapping;

public class CmdLineExample {
	public static void main(String[] args) throws IOException {
		File inputsDir = new File("C:/Users/Seppe/Desktop/inputs/");
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xes");
			}
		};
		
		InputSet newInputSet = new InputSet();
		
		for (String logname : inputsDir.list(filter)) {
			logname = inputsDir.getAbsolutePath() + File.separator + logname;
			String netname = logname.replace(".xes", ".pnml");
			System.out.println(logname);
			System.out.println(netname);
			Mapping mapping = new Mapping(logname, netname);
			// Assume all unmapped activities should be invisible
			mapping.assignUnmappedToInvisible();
			newInputSet.add(mapping);
		}
		
		newInputSet.writeInputSet(new File(inputsDir + "mapping.cbfi"));
	}
}
