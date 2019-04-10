package be.kuleuven.econ.cbf.metrics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MetricSet extends ArrayList<AbstractMetric> {

	private static final long serialVersionUID = -2470348392511857058L;

	public void writeMetricSet(OutputStream stream) throws TransformerException {
		try {
			ZipOutputStream zos = new ZipOutputStream(stream);
			zos.putNextEntry(new ZipEntry("egg"));
			Document document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();

			Element root = document.createElement("metricset");
			root.setAttribute("version", "1.1");
			document.appendChild(root);

			for (int i = 0; i < this.size(); i++)
				root.appendChild(this.get(i).writeMetric(document, i));

			DOMSource source = new DOMSource(document);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult target = new StreamResult(zos);
			transformer.transform(source, target);
			zos.closeEntry();
			zos.close();
		} catch (ParserConfigurationException e) {
		} catch (TransformerConfigurationException e) {
		} catch (IOException e) {
		}
	}

	public static MetricSet readMetricSet(InputStream stream)
			throws IOException {
		MetricSet output = new MetricSet();
		String exceptionMessage = "Specified source does not contain a MetricSet object";
		try {
			ZipInputStream zis = new ZipInputStream(stream);
			ZipEntry entry = zis.getNextEntry();
			if (entry == null || !entry.getName().equals("egg"))
				throw new IllegalArgumentException(exceptionMessage);
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.parse(zis);

			Element root = document.getDocumentElement();
			if (root == null || !root.getNodeName().equals("metricset"))
				throw new IllegalArgumentException(exceptionMessage);
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				if (!n.getNodeName().equals("metric"))
					continue;
				Node nIndex = n.getAttributes().getNamedItem("index");
				if (nIndex == null)
					throw new IllegalArgumentException(exceptionMessage);
				int index = Integer.parseInt(nIndex.getNodeValue());
				while (output.size() <= index)
					output.add(null);
				AbstractMetric m = AbstractMetric.readMetric(n);
				output.set(index, m);
			}
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		}
		return output;
	}
}
