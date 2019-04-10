package be.kuleuven.econ.cbf.utils;

import java.io.File;
import java.util.List;

import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;

public class LogTool {

	public static XLog readLog(String path) {
		try {
			File file = new File(path);
			if (file.isFile() && file.exists()) {
				List<XLog> logs = null;
				if (file.getAbsolutePath().endsWith(".mxml"))
					logs = openMxmlLog(file);
				else if (file.getAbsolutePath().endsWith(".xes"))
					logs = openXesLog(file);
				if (logs == null)
					throw new Exception();
				if (logs.size() > 1)
					System.err
							.println("This tool does not work with files containing multiple logs. All but the first log in the file "
									+ path + " will be ignored.");
				return logs.get(0);
			} else
				throw new Exception();
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid file to read");
		}
	}

	public static List<XLog> openMxmlLog(File file) throws Exception {
		XMxmlParser p = new XMxmlParser();
		List<XLog> logs = p.parse(file);
		return logs;
	}

	public static List<XLog> openXesLog(File file) throws Exception {
		XesXmlParser p = new XesXmlParser();
		List<XLog> logs = p.parse(file);
		return logs;
	}

	public static boolean isLogFile(String path) {
		File file = new File(path);
		boolean ok = false;
		if (file.getAbsolutePath().endsWith(".xes"))
			ok = true;
		else if (file.getAbsolutePath().endsWith(".mxml"))
			ok = true;
		if (!file.isFile())
			ok = false;
		return ok;
	}
}
