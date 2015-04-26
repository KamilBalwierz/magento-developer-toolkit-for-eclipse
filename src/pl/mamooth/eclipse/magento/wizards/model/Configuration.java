package pl.mamooth.eclipse.magento.wizards.model;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {
	private String fileName;
	private String[] filePath;
	private String className;
	private String extendsClass;
	private boolean rewrite;
	private String rewriteGroup;
	private String rewriteName;

	private String eventObjectName;
	private String eventPrefix;
	private String tableName;
	private String tableIdField;
	private String modelGroupName;
	private String modelName;

	private boolean resourceModel;
	private String resourceFileName;
	private String[] resourceFilePath;
	private String resourceClassName;
	private String resourceExtendsClass;

	private boolean collection;
	private String collectionFileName;
	private String[] collectionFilePath;
	private String collectionClassName;
	private String collectionExtendsClass;

	private boolean createInstallScript;
	private boolean createFieldsComments;
	private String versionTarget;
	private String[] setupScriptFilePath;
	private String setupScriptFileName;

	private MagentoModule module;

	private Field[] fileds;

	public String getTableIdField() {
		return tableIdField;
	}

	public void setTableIdField(String tableIdField) {
		this.tableIdField = tableIdField;
	}

	public String[] getSetupScriptFilePath() {
		return setupScriptFilePath;
	}

	public void setSetupScriptFilePath(String[] setupScriptFilePath) {
		this.setupScriptFilePath = setupScriptFilePath;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getSetupScriptFileName() {
		return setupScriptFileName;
	}

	public void setSetupScriptFileName(String setupScriptFileName) {
		this.setupScriptFileName = setupScriptFileName;
	}

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

	public boolean isRewrite() {
		return rewrite;
	}

	public void setRewrite(boolean rewrite) {
		this.rewrite = rewrite;
	}

	public String getRewriteGroup() {
		return rewriteGroup;
	}

	public void setRewriteGroup(String rewriteGroup) {
		this.rewriteGroup = rewriteGroup;
	}

	public String getRewriteName() {
		return rewriteName;
	}

	public void setRewriteName(String rewriteName) {
		this.rewriteName = rewriteName;
	}

	public boolean isResourceModel() {
		return resourceModel;
	}

	public void setResourceModel(boolean resourceModel) {
		this.resourceModel = resourceModel;
	}

	public String getResourceFileName() {
		return resourceFileName;
	}

	public void setResourceFileName(String resourceFileName) {
		this.resourceFileName = resourceFileName;
	}

	public String[] getResourceFilePath() {
		return resourceFilePath;
	}

	public void setResourceFilePath(String[] resourceFilePath) {
		this.resourceFilePath = resourceFilePath;
	}

	public String getResourceClassName() {
		return resourceClassName;
	}

	public void setResourceClassName(String resourceClassName) {
		this.resourceClassName = resourceClassName;
	}

	public String getResourceExtendsClass() {
		return resourceExtendsClass;
	}

	public void setResourceExtendsClass(String resourceExtendsClass) {
		this.resourceExtendsClass = resourceExtendsClass;
	}

	public boolean isCollection() {
		return collection;
	}

	public void setCollection(boolean collection) {
		this.collection = collection;
	}

	public String getCollectionFileName() {
		return collectionFileName;
	}

	public void setCollectionFileName(String collectionFileName) {
		this.collectionFileName = collectionFileName;
	}

	public String[] getCollectionFilePath() {
		return collectionFilePath;
	}

	public void setCollectionFilePath(String[] collectionFilePath) {
		this.collectionFilePath = collectionFilePath;
	}

	public String getCollectionClassName() {
		return collectionClassName;
	}

	public void setCollectionClassName(String collectionClassName) {
		this.collectionClassName = collectionClassName;
	}

	public String getCollectionExtendsClass() {
		return collectionExtendsClass;
	}

	public void setCollectionExtendsClass(String collectionExtendsClass) {
		this.collectionExtendsClass = collectionExtendsClass;
	}

	public boolean isCreateInstallScript() {
		return createInstallScript;
	}

	public void setCreateInstallScript(boolean createInstallScript) {
		this.createInstallScript = createInstallScript;
	}

	public boolean isCreateFieldsComments() {
		return createFieldsComments;
	}

	public void setCreateFieldsComments(boolean createFieldsComments) {
		this.createFieldsComments = createFieldsComments;
	}

	public String getVersionTarget() {
		return versionTarget;
	}

	public void setVersionTarget(String versionTarget) {
		this.versionTarget = versionTarget;
	}

	public Field[] getFileds() {
		return fileds;
	}

	public void setFileds(Field[] fileds) {
		this.fileds = fileds;
	}

	public String getEventObjectName() {
		return eventObjectName;
	}

	public void setEventObjectName(String eventObjectName) {
		this.eventObjectName = eventObjectName;
	}

	public String getEventPrefix() {
		return eventPrefix;
	}

	public void setEventPrefix(String eventPrefix) {
		this.eventPrefix = eventPrefix;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getModelGroupName() {
		if (modelGroupName == null)
			return module.getShortName();
		return modelGroupName;
	}

	public void setModelGroupName(String modelGroupName) {
		this.modelGroupName = modelGroupName;
	}

}
