package pl.mamooth.eclipse.magento.helpers;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import pl.mamooth.eclipse.magento.MagentoEclipsePlugin;
import pl.mamooth.eclipse.magento.MagentoModule;

public class ModuleHelper {
	private static final String PROPERTY_APPPATH = "app_path";
	private static final String PROPERTY_MODULES = "modules_list";

	public static int moduleCountAviableInPath(String path) throws CoreException {
		String[] modules = listModulesInPath(path);
		if (modules == null)
			return 0;
		return modules.length;
	}

	protected static String[] getModulesInProject(IProject project) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID;
		if (!project.exists())
			return null;
		String modules = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_MODULES));
		if (modules == null)
			return null;
		return modules.split(";");
	}

	protected static String[] listModulesInPath(String path) throws CoreException {
		String[] pathIncrements = path.split("/");
		IProject project = getProjectFromPath(path);
		if (project == null)
			return null;
		int i = 1;
		if (path.startsWith("/"))
			++i;
		String[] moduleNames = getModulesInProject(project);
		if (moduleNames == null)
			return null;
		if (moduleNames.length <= 1)
			return moduleNames;
		String[] appFolder = getAppFolderPath(project);
		if (appFolder == null)
			return null;
		for (int j = 0; j < appFolder.length; ++j) {
			if (i == pathIncrements.length)
				return null;
			if (!pathIncrements[i].equals(appFolder[j]))
				return null;
			++i;
		}
		HashMap<String, String[]> modulePaths = new HashMap<String, String[]>();
		for (String moduleName : moduleNames) {
			modulePaths.put(moduleName, MagentoModule.getModulePath(project, moduleName));
		}
		for (int j = 0; i < pathIncrements.length; ++i, ++j) {
			for (String module : modulePaths.keySet()) {
				String[] modulePath = modulePaths.get(module);
				if (j < modulePath.length) {
					if (!modulePath[j].equals(pathIncrements[i])) {
						modulePaths.remove(module);
					}
				}
			}
		}
		return modulePaths.keySet().toArray(new String[0]);
	}

	public static String[] getAppFolderPath(IProject project) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID;
		String path = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_APPPATH));
		if (path == null)
			return null;
		return path.split("/");
	}

	public static void setAppFolderPath(IProject project, String[] path) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID;
		StringBuilder sb = new StringBuilder();
		for (String pathIncrement : path) {
			if (sb.length() != 0) {
				sb.append('/');
			}
			sb.append(pathIncrement);
		}
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_APPPATH), sb.toString());
	}

	public static MagentoModule getModuleFromPath(String path) throws CoreException {
		String[] modules = listModulesInPath(path);
		if (modules == null)
			return null;
		if (modules.length != 1)
			return null;
		IProject project = getProjectFromPath(path);
		if (project == null)
			return null;
		return MagentoModule.getMagentoModule(project, modules[0]);
	}

	protected static IProject getProjectFromPath(String path) {
		String[] pathIncrements = path.split("/");
		String projectName = "";
		if (pathIncrements.length == 0)
			return null;
		if (pathIncrements[0].equals("")) {
			if (pathIncrements.length == 1) {
				return null;
			}
			projectName = pathIncrements[1];
		} else {
			projectName = pathIncrements[0];
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		return project;
	}

	public static void registerModule(IProject project, MagentoModule module) throws CoreException {
		String[] modules = getModulesInProject(project);
		LinkedList<String> modulesList = null;
		if (modules == null) {
			modulesList = new LinkedList<String>();
		} else {
			modulesList = new LinkedList<String>();
			for (String moduleName : modules) {
				modulesList.addLast(moduleName);
			}
		}
		modulesList.addLast(module.getModuleName());
		StringBuilder sb = new StringBuilder();
		for (String moduleName : modulesList) {
			if (sb.length() != 0) {
				sb.append(';');
			}
			sb.append(moduleName);
		}
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_MODULES), sb.toString());
	}
}
