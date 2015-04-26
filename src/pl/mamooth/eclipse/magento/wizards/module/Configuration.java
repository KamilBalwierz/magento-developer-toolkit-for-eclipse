package pl.mamooth.eclipse.magento.wizards.module;

import java.io.Serializable;

public class Configuration implements Serializable {

	private static final long serialVersionUID = 8872479784406609177L;

	private String vendor;
	private String name;
	private String projectName;
	private String shortName;
	private String codePool;
	private String version;
	private String base;
	private boolean active;
	private boolean modelGroup;
	private String modelGroupName;
	private boolean resourceModelGroup;
	private String resourceModelGroupName;
	private boolean setup;
	private String setupName;
	private boolean blockGroup;
	private String blockGroupName;
	private boolean helperGroup;
	private String helperGroupName;
	private boolean defaultHelper;
	private boolean defaultControllers;
	private String defaultControllersFrontName;
	private boolean adminControllers;
	private String adminControllersFrontName;

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getCodePool() {
		return codePool;
	}

	public void setCodePool(String codePool) {
		this.codePool = codePool;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isModelGroup() {
		return modelGroup;
	}

	public void setModelGroup(boolean modelGroup) {
		this.modelGroup = modelGroup;
	}

	public String getModelGroupName() {
		return modelGroupName;
	}

	public void setModelGroupName(String modelGroupName) {
		this.modelGroupName = modelGroupName;
	}

	public boolean isResourceModelGroup() {
		return resourceModelGroup;
	}

	public void setResourceModelGroup(boolean resourceModelGroup) {
		this.resourceModelGroup = resourceModelGroup;
	}

	public String getResourceModelGroupName() {
		return resourceModelGroupName;
	}

	public void setResourceModelGroupName(String resourceModelGroupName) {
		this.resourceModelGroupName = resourceModelGroupName;
	}

	public boolean isSetup() {
		return setup;
	}

	public void setSetup(boolean setup) {
		this.setup = setup;
	}

	public String getSetupName() {
		return setupName;
	}

	public void setSetupName(String setupName) {
		this.setupName = setupName;
	}

	public boolean isBlockGroup() {
		return blockGroup;
	}

	public void setBlockGroup(boolean blockGroup) {
		this.blockGroup = blockGroup;
	}

	public String getBlockGroupName() {
		return blockGroupName;
	}

	public void setBlockGroupName(String blockGroupName) {
		this.blockGroupName = blockGroupName;
	}

	public boolean isHelperGroup() {
		return helperGroup;
	}

	public void setHelperGroup(boolean helperGroup) {
		this.helperGroup = helperGroup;
	}

	public String getHelperGroupName() {
		return helperGroupName;
	}

	public void setHelperGroupName(String helperGroupName) {
		this.helperGroupName = helperGroupName;
	}

	public boolean isDefaultHelper() {
		return defaultHelper;
	}

	public void setDefaultHelper(boolean defaultHelper) {
		this.defaultHelper = defaultHelper;
	}

	public boolean isDefaultControllers() {
		return defaultControllers;
	}

	public void setDefaultControllers(boolean defaultControllers) {
		this.defaultControllers = defaultControllers;
	}

	public String getDefaultControllersFrontName() {
		return defaultControllersFrontName;
	}

	public void setDefaultControllersFrontName(String defaultControllersFrontName) {
		this.defaultControllersFrontName = defaultControllersFrontName;
	}

	public boolean isAdminControllers() {
		return adminControllers;
	}

	public void setAdminControllers(boolean adminControllers) {
		this.adminControllers = adminControllers;
	}

	public String getAdminControllersFrontName() {
		return adminControllersFrontName;
	}

	public void setAdminControllersFrontName(String adminControllersFrontName) {
		this.adminControllersFrontName = adminControllersFrontName;
	}

	protected String getBase() {
		return base;
	}

	protected void setBase(String base) {
		this.base = base;
	}
}
