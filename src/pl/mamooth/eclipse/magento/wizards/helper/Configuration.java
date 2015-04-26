package pl.mamooth.eclipse.magento.wizards.helper;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {
	private String fileName;
	private String[] filePath;
	private String className;
	private String extendsClass;
	private boolean rewrite;
	private String rewriteGroup;
	private String rewriteModel;
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

	public String getRewriteModel() {
		return rewriteModel;
	}

	public void setRewriteModel(String rewriteModel) {
		this.rewriteModel = rewriteModel;
	}

}
