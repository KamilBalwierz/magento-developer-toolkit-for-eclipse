package pl.mamooth.eclipse.magento.importWizards.module;

import java.util.ArrayList;

public class ImportConfiguration {
	protected String projectName;
	protected boolean copySource;
	protected ArrayList<ImportedModule> modules;
	protected String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ImportConfiguration() {
		modules = new ArrayList<ImportedModule>();
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public boolean isCopySource() {
		return copySource;
	}

	public void setCopySource(boolean copySource) {
		this.copySource = copySource;
	}

	public void addModule(ImportedModule module) {
		modules.add(module);
	}

	public ImportedModule[] getModules() {
		return modules.toArray(new ImportedModule[0]);
	}

	public void clearModules() {
		modules.clear();
	}

}
