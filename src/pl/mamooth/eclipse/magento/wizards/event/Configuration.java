package pl.mamooth.eclipse.magento.wizards.event;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {
	private String fileName;
	private String[] filePath;
	private String modelClass;
	private String eventName;
	private String observerName;
	private String modelGroup;
	private String modelName;
	private String functionName;
	private String type;
	private MagentoModule module;

	public MagentoModule getModule() {
		return module;
	}

	public void setModule(MagentoModule module) {
		this.module = module;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String[] getFilePath() {
		return filePath;
	}

	public void setFilePath(String[] filePath) {
		this.filePath = filePath;
	}

	public String getModelClass() {
		return modelClass;
	}

	public void setModelClass(String modelClass) {
		this.modelClass = modelClass;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getObserverName() {
		return observerName;
	}

	public void setObserverName(String observerName) {
		this.observerName = observerName;
	}

	public String getModelGroup() {
		return modelGroup;
	}

	public void setModelGroup(String modelGroup) {
		this.modelGroup = modelGroup;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
