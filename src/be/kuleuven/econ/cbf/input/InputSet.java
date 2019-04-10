package be.kuleuven.econ.cbf.input;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerException;

public class InputSet extends ArrayList<Mapping> {

	private static final long serialVersionUID = -5892245142388328165L;

	public void writeInputSet(File file) throws IOException {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		ZipOutputStream zos = new ZipOutputStream(fos);
		int index = 0;
		try {
			for (Mapping m : this) {
				zos.putNextEntry(new ZipEntry(Integer.toString(index)));
				index++;
				m.writeMapping(zos, false);
			}
		} catch (TransformerException e) {
			throw new IOException(e);
		}
		zos.closeEntry();
		zos.close();
	}

	public static InputSet readInputSet(File file) throws IOException {
		// There are some problems with the xml parser trying to close the
		// stream, so we'll make sure he can't do that.
		class MZipInputStream extends ZipInputStream {

			public MZipInputStream(InputStream in) {
				super(in);
			}

			@Override
			public void close() {
			}

			public void actuallyClose() throws IOException {
				super.close();
			}
		}
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		MZipInputStream zis = new MZipInputStream(bis);
		InputSet set = new InputSet();
		while (zis.getNextEntry() != null) {
			Mapping m = Mapping.readMapping(new BufferedInputStream(zis));
			set.add(m);
		}
		zis.actuallyClose();
		return set;
	}
}
