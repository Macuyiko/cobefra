import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import be.kuleuven.econ.cbf.CBFUIController;
import be.kuleuven.econ.cbf.input.InputSet;
import be.kuleuven.econ.cbf.metrics.MetricSet;
import be.kuleuven.econ.cbf.ui.BeginFrame;
import be.kuleuven.econ.cbf.ui.ResultFrame;

public class Start {
	public static InputSet inputSet;
	public static MetricSet metricSet;
	
	public static void main(String[] args) {
		File promIni = new File("./ProM.ini");
		if (!promIni.exists()) {
			InputStream in = Start.class.getResourceAsStream("/ProM.ini");
			if (in != null) {
				try (	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
						BufferedWriter writer = new BufferedWriter(new FileWriter(promIni));) {
					String line;
					while((line = reader.readLine()) != null)
						writer.write(line + System.lineSeparator());
				} catch (IOException e) {}
			}
		}
			
		BeginFrame beginFrame = new BeginFrame();
		
		new Thread(beginFrame).start();
		try {
			beginFrame.waitForCompletion();
		} catch (InterruptedException e) {
			beginFrame.cancel();
			System.err.println(e.getStackTrace());
		}
		if (beginFrame.isCancelled())
			System.exit(1);
		
		inputSet = beginFrame.getInputSet();
		metricSet = beginFrame.getMetricSet();
		
		CBFUIController myController = new CBFUIController();
		myController.preload();
		myController.setInputSet(inputSet);
		myController.setMetricSet(metricSet);
		
		new Thread(myController).start();
		beginFrame.close(); // Close begin frame once next frame has loaded
		
		try {
			myController.waitForCompletion();
		} catch (InterruptedException e) {
			myController.cancel();
			System.err.println(e.getStackTrace());
		}
		
		if (myController.hasResult()) {
			ResultFrame resultFrame = new ResultFrame(myController);
			new Thread(resultFrame).start();
			try {
				resultFrame.waitForCompletion();
			} catch (InterruptedException e) {
				resultFrame.cancel();
				System.err.println(e.getStackTrace());
			}
			if (resultFrame.isCancelled())
				System.exit(1);
		}
	}
}
