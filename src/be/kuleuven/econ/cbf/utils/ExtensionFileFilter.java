package be.kuleuven.econ.cbf.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {
	private String extension;
	public ExtensionFileFilter(String ext) {
		this.extension = ext;
	}
	@Override
	public boolean accept(File arg0) {
		return arg0.isDirectory() || getExtension(arg0).equals(extension);
	}
	@Override
	public String getDescription() {
		return extension+"-files";
	}
	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1)
			ext = s.substring(i+1).toLowerCase();
		if(ext == null)
			return "";
		return ext;
	}
}
