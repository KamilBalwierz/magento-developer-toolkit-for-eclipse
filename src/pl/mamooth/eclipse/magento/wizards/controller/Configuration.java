package pl.mamooth.eclipse.magento.wizards.controller;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {
	private String fileName;
	private String className;
	private String extendsClass;
	private String[] filePath;
	private String[] actions;
	private boolean generatePreDispatch;
	private boolean generatePostDispatch;
	private MagentoModule module;

	public String getFileName() {
		return fileName;
	}

	public MagentoModule getModule() {
		return module;
	}

	public void setModule(MagentoModule module) {
		this.module = module;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getExtendsClass() {
		return extendsClass;
	}

	public void setExtendsClass(String extendsClass) {
		this.extendsClass = extendsClass;
	}

	public String[] getFilePath() {
		return filePath;
	}

	public void setFilePath(String[] filePath) {
		this.filePath = filePath;
	}

	public String[] getActions() {
		return actions;
	}

	public void setActions(String[] actions) {
		this.actions = actions;
	}

	public boolean isGeneratePreDispatch() {
		return generatePreDispatch;
	}

	public void setGeneratePreDispatch(boolean generatePreDispatch) {
		this.generatePreDispatch = generatePreDispatch;
	}

	public boolean isGeneratePostDispatch() {
		return generatePostDispatch;
	}

	public void setGeneratePostDispatch(boolean generatePostDispatch) {
		this.generatePostDispatch = generatePostDispatch;
	}

}
