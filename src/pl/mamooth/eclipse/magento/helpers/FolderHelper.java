package pl.mamooth.eclipse.magento.helpers;

import java.util.Arrays;

public class FolderHelper {
	public static final String SYSTEM_FILE = "system.xml";
	public static final String STRUCT_FILE = "structure.xml";
	public static final String PLUGIN_CACHE_FOLDER = ".magento-plugin-cache";
	public static final String MODEL_FOLDER = "Model";
	public static final String HELPER_FOLDER = "Helper";
	public static final String BLOCK_FOLDER = "Block";
	public static final String RESOUCE_FOLDER = "Resource";
	public static final String APP_FOLDER = "app";
	public static final String CODE_FOLDER = "code";
	public static final String SOURCE_FOLDER = "src";
	public static final String ETC_FOLDER = "etc";
	public static final String CONTROLLERS_FOLDER = "controllers";
	public static final String ADMIN_CONTROLLERS_SUBFOLDER = "Adminhtml";
	public static final String SETUP_FOLDER = "sql";
	public static final String BUILDPATH_FILE = ".buildpath";
	public static final String MODULES_FOLDER = "modules";
	public static final String CONFIG_FILE = "config.xml";
	public static final String XML_EXTENSION = ".xml";
	public static final String PHP_EXTENSION = ".php";
	public static final String CSV_EXTENSION = ".csv";
	public static final String LOCALE_FOLDER = "locale";
	public static final String LAYOUT_FILE_PREFIX = "layout_";
	public static final String LAYOUT_FOLDER = "layout";
	public static final String DESIGN_FOLDER = "design";
	public static final String COLECTION_FILE = "Collection.php";
	public static final String TEMPLATE_FOLDER = "template";
	public static final char DIRECTORY_SEPATATOR = '/';

	public static String[] getTrailingPath(String path, String projectName, String[] compareToPath) {
		String[] basePath = path.split("/");
		int i = 0;
		int j = 0;
		if (basePath.length == 0)
			return null;
		if (basePath[0].length() == 0) {
			++i;
		}
		if (!basePath[i++].equals(projectName)) {
			return null;
		}
		boolean ok = true;
		while (i < basePath.length && j < compareToPath.length) {
			if (!basePath[i].equals(compareToPath[j])) {
				ok = false;
				break;
			}
			++i;
			++j;
		}
		if (!ok || compareToPath.length != j) {
			return null;
		}
		String[] result = Arrays.copyOfRange(basePath, i, basePath.length);
		return result;
	}
}
