package pl.mamooth.eclipse.magento.wizards.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import org.eclipse.core.runtime.FileLocator;

import pl.mamooth.eclipse.magento.MagentoEclipsePlugin;

public class ComboSource {
	public static String[] getCodePools() {
		return new String[] { "local", "community", "core" };
	}

	public static String[] getResourceBases() {
		return new String[] { "mysql4", "entity" };
	}

	protected static LinkedList<String> baseVersionsPaths = null;
	protected static LinkedList<String> baseVersionsNames = null;

	public static String getBaseVersionPath(int i) {
		if (baseVersionsPaths == null)
			return null;
		return baseVersionsPaths.get(i);
	}

	public static String[] baseVersionFiles = { "config.xml", "layout_frontend.xml", "layout_adminhtml.xml", "structure.xml", "system.xml", "version" };

	protected static void testBaseVersion(String path, String name) throws IOException {
		File container = new File(path + "/" + name);
		if (container.exists() && container.isDirectory()) {
			for (String subfile : baseVersionFiles) {
				File version = new File(container.getPath() + "/" + subfile);
				if (!version.exists())
					return;
			}
			File version = new File(container.getPath() + "/version");
			FileInputStream fis = new FileInputStream(version);
			InputStreamReader in = new InputStreamReader(fis, "UTF-8");
			char[] buff = new char[120];
			in.read(buff);
			baseVersionsNames.push(new String(buff).trim());
			baseVersionsPaths.push(container.getPath());
		}
	}

	public static String[] getBaseVersions() {
		try {
			if (baseVersionsNames == null)
				baseVersionsNames = new LinkedList<String>();
			else
				return baseVersionsNames.toArray(new String[0]);
			if (baseVersionsPaths == null)
				baseVersionsPaths = new LinkedList<String>();

			URL configURL = MagentoEclipsePlugin.getDefault().getContext().getBundle().getResource("resources/versions");
			File configFile = new File(FileLocator.toFileURL(configURL).getPath());
			if (configFile.exists() && configFile.isDirectory()) {
				for (String subelement : configFile.list()) {
					testBaseVersion(configFile.getPath(), subelement);
				}
			}

			String path = MagentoEclipsePlugin.getDefault().getStateLocation().toString() + "/versions";
			configFile = new File(path);
			if (configFile.exists() && configFile.isDirectory()) {
				for (String subelement : configFile.list()) {
					testBaseVersion(configFile.getPath(), subelement);
				}
			}

		} catch (IOException e) {
			if (baseVersionsNames == null)
				baseVersionsNames = new LinkedList<String>();
			if (baseVersionsPaths == null)
				baseVersionsPaths = new LinkedList<String>();
		}
		return baseVersionsNames.toArray(new String[0]);
	}
}
