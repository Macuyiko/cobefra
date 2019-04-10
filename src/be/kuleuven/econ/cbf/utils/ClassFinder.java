package be.kuleuven.econ.cbf.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import be.kuleuven.econ.cbf.CBFUIController;

public abstract class ClassFinder {

	/**
	 * Create and return a list of all classes on the current classpath that
	 * meet the constraints given by {@link #isIncluded(Class)}.
	 * 
	 * Calling this method may take a considerable amount of time since it will
	 * require iterating over each class on the classpath. Note that this method
	 * may also take a considerable amount of memory (notably PermGen memory) if
	 * a lot of classes match the criteria of {@link #isIncluded(Class)}.
	 * 
	 * The result of this method is a list containing all {@link Class} objects
	 * matching the constraints given by {@link #isIncluded(Class)}. Each of
	 * these classes will be loaded and take up an amount of space in the
	 * PermGen memory. Note that this may cause excessive memory usage and the
	 * PermGen memory is by default very limited. Classes can however qualify to
	 * be unloaded again by simply discarding the reference to their
	 * {@link Class} object.
	 */
	public Set<Class<?>> getAllClasses(String prefixFilter) {
		String path[] = System.getProperty("java.class.path").split(File.pathSeparator);
		List<Class<?>> list = new ArrayList<Class<?>>();
		for (int i = 0; i < path.length; i++) {
			File place = new File(path[i]);
			if (place.isDirectory())
				list.addAll(scanDirectory(place, prefixFilter));
			else if (path[i].endsWith(".jar"))
				list.addAll(scanJar(place, prefixFilter));
			else
				System.err.println("ClassFinder: can't work with path "+ path[i]);		
		}
		
		String selfPath = CBFUIController.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = null;
		try {
			decodedPath = URLDecoder.decode(selfPath.toString(), "UTF-8");
			list.addAll(scanDirectory(new File(decodedPath), prefixFilter));
		} catch (UnsupportedEncodingException e) {
		}
		
		Set<Class<?>> set = new HashSet<Class<?>>(list);
		return set;
	}

	/**
	 * This method should return true if the given class should be included in
	 * the results of this class. You should make sure no reference to the value
	 * of type is maintained in order to avoid excessive memory leaks. (Just
	 * don't store the value of type anywhere.)
	 */
	public abstract boolean isIncluded(Class<?> type);

	/**
	 * Load a class and add it to the given list if it is included according to
	 * {{@link #isIncluded(Class)} .
	 */
	private void loadAndAdd(String name, List<Class<?>> list) {
		try {
			// TODO: Figure out the whole class loading stuff later, we probably should work towards a ProM-inspired
			// booting class.
			Class<?> sysLoaded = Class.forName(name);
			//Class<?> sysLoaded = ((URLClassLoader) ClassLoader.getSystemClassLoader()).loadClass(name);
			if (isIncluded(sysLoaded)) {
				list.add(sysLoaded);
			}
		} catch (IllegalAccessError e) {
		} catch (Throwable t) {
		}
	}
	
	/**
	 * Create and return a list of all classes in the given directory that meet
	 * the constraints given by {@link #isIncluded(Class)}. This method works
	 * recursively.
	 */
	public List<Class<?>> scanDirectory(File dir, String prefixFilter) {
		ArrayList<Class<?>> list = new ArrayList<Class<?>>();
		try {
			LinkedList<File> files = new LinkedList<File>();
			files.add(dir);
			while (!files.isEmpty()) {
				File current = files.pop();
				if (current.isDirectory()) {
					File[] sub = current.listFiles();
					for (File f : sub)
						if (f.getAbsolutePath().endsWith(".class"))
							files.push(f);
						else if (f.getAbsolutePath().endsWith(".jar"))
							files.push(f);
						else if (f.isDirectory())
							files.push(f);
				} else {
					String relative = current.getAbsolutePath();
					relative = relative.substring(dir.getAbsolutePath().length());
					if (relative.endsWith(".class")) {
						String name = relative.substring(1, relative.length() - 6);
						name = name.replace(File.separatorChar, '.');
						if (name.startsWith(prefixFilter))
							loadAndAdd(name, list);
					} else {
						list.addAll(scanJar(current, prefixFilter));
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return list;
	}

	/**
	 * Create and return a list of all classes in the given jar-file that meet
	 * the constraints given by {@link #isIncluded(Class)}.
	 */
	public List<Class<?>> scanJar(File jar, String prefixFilter) {
		ArrayList<Class<?>> list = new ArrayList<Class<?>>();
		try {
			JarInputStream jis = new JarInputStream(new FileInputStream(jar));
			JarEntry current;
			while ((current = jis.getNextJarEntry()) != null) {
				if (!current.isDirectory() && current.getName().endsWith(".class")) {
					String relative = current.getName();
					String name = relative.substring(0, relative.length() - 6);
					name = name.replace('/', '.');
					if (name.startsWith(prefixFilter))
						loadAndAdd(name, list);
				}
			}
			jis.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return list;
	}

	public static Class<?> getClass(String type) throws ClassNotFoundException {
		ClassFinder finder = new ClassFinder(){
			@Override
			public boolean isIncluded(Class<?> type) {
				return true;
			}
		};
		Set<Class<?>> classes = finder.getAllClasses(type);
		for (Class<?> cl : classes) {
			if (cl.getCanonicalName().equals(type))
				return cl;
		}
		throw new ClassNotFoundException();
	}

}
