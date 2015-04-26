package pl.mamooth.eclipse.magento.wizards.block;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {
	protected String fileNname;
	protected String className;
	protected String[] filePath;
	protected String extendsClassName;
	protected boolean rewrite;
	protected String extendsGroupName;
	protected String extendsBlockName;
	protected boolean createTemplate;
	protected String templateFileName;
	protected String[] templateFilePath;
	protected boolean assignTemplateInCode;
	protected boolean createLayoutXmlEntry;
	protected String handler;
	protected String references;
	protected MagentoModule module;
	protected String magentoTemplatePath;
	protected String designScope;
	protected String designPackage;
	protected String designTemplate;
	protected String designLayoutFileName;
	protected String blockName;
	protected String blockGroup;
	protected String blockModel;
	protected String blockAlias;

	public String getBlockName() {
		return blockName;
	}

	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}

	public String getBlockGroup() {
		if (blockGroup == null) {
			return module.getShortName();
		}
		return blockGroup;
	}

	public void setBlockGroup(String blockGroup) {
		this.blockGroup = blockGroup;
	}

	public String getBlockModel() {
		return blockModel;
	}

	public void setBlockModel(String blockModel) {
		this.blockModel = blockModel;
	}

	public String getBlockAlias() {
		return blockAlias;
	}

	public void setBlockAlias(String blockAlias) {
		this.blockAlias = blockAlias;
	}

	public String getDesignPackage() {
		return designPackage;
	}

	public void setDesignPackage(String designPackage) {
		this.designPackage = designPackage;
	}

	public String getDesignTemplate() {
		return designTemplate;
	}

	public void setDesignTemplate(String designTemplate) {
		this.designTemplate = designTemplate;
	}

	public String getDesignLayoutFileName() {
		return designLayoutFileName;
	}

	public void setDesignLayoutFileName(String designLayoutFileName) {
		this.designLayoutFileName = designLayoutFileName;
	}

	public String getDesignScope() {
		return designScope;
	}

	public void setDesignScope(String designScope) {
		this.designScope = designScope;
	}

	public String getMagentoTemplatePath() {
		return magentoTemplatePath;
	}

	public void setMagentoTemplatePath(String magentoTemplatePath) {
		this.magentoTemplatePath = magentoTemplatePath;
	}

	public String getFileNname() {
		return fileNname;
	}

	public MagentoModule getModule() {
		return module;
	}

	public void setModule(MagentoModule module) {
		this.module = module;
	}

	public void setFileNname(String fileNname) {
		this.fileNname = fileNname;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String[] getFilePath() {
		return filePath;
	}

	public void setFilePath(String[] filePath) {
		this.filePath = filePath;
	}

	public String getExtendsClassName() {
		return extendsClassName;
	}

	public void setExtendsClassName(String extendsClassName) {
		this.extendsClassName = extendsClassName;
	}

	public boolean isRewrite() {
		return rewrite;
	}

	public void setRewrite(boolean rewrite) {
		this.rewrite = rewrite;
	}

	public String getExtendsGroupName() {
		return extendsGroupName;
	}

	public void setExtendsGroupName(String extendsGroupName) {
		this.extendsGroupName = extendsGroupName;
	}

	public String getExtendsBlockName() {
		return extendsBlockName;
	}

	public void setExtendsBlockName(String extendsBlockName) {
		this.extendsBlockName = extendsBlockName;
	}

	public boolean isCreateTemplate() {
		return createTemplate;
	}

	public void setCreateTemplate(boolean createTemplate) {
		this.createTemplate = createTemplate;
	}

	public String getTemplateFileName() {
		return templateFileName;
	}

	public void setTemplateFileName(String templateFileName) {
		this.templateFileName = templateFileName;
	}

	public String[] getTemplateFilePath() {
		return templateFilePath;
	}

	public void setTemplateFilePath(String[] templateFilePath) {
		this.templateFilePath = templateFilePath;
	}

	public boolean isAssignTemplateInCode() {
		return assignTemplateInCode;
	}

	public void setAssignTemplateInCode(boolean assignTemplateInCode) {
		this.assignTemplateInCode = assignTemplateInCode;
	}

	public boolean isCreateLayoutXmlEntry() {
		return createLayoutXmlEntry;
	}

	public void setCreateLayoutXmlEntry(boolean createLayoutXmlEntry) {
		this.createLayoutXmlEntry = createLayoutXmlEntry;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getReferences() {
		return references;
	}

	public void setReferences(String updates) {
		this.references = updates;
	}

}
