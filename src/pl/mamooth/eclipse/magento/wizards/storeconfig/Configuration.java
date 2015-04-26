package pl.mamooth.eclipse.magento.wizards.storeconfig;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {
	private String tabName;
	private String tabLabel;
	private int tabSortOrder;
	private boolean tabExists;

	private String helper;

	private String sectionName;
	private String sectionLabel;
	private int sectionSortOrder;
	private boolean sectionShowDefault;
	private boolean sectionShowWebsite;
	private boolean sectionShowStore;

	private String aclName;
	private String aclTitle;
	private int aclSortOrder;

	private String groupName;
	private String groupLabel;
	private int groupSortOrder;
	private boolean groupShowDefault;
	private boolean groupShowWebsite;
	private boolean groupShowStore;

	private String fieldName;
	private String fieldLabel;
	private String filedComment;
	private String fieldFrontendModel;
	private String fieldBackednModel;
	private String fieldSourceModel;
	private String fieldFrontendType;
	private int fieldSortOrder;
	private boolean fieldShowDefault;
	private boolean fieldShowWebsite;
	private boolean fieldShowStore;

	private MagentoModule module;

	public boolean isTabExists() {
		return tabExists;
	}

	public void setTabExists(boolean tabExists) {
		this.tabExists = tabExists;
	}

	public MagentoModule getModule() {
		return module;
	}

	public void setModule(MagentoModule module) {
		this.module = module;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getTabLabel() {
		return tabLabel;
	}

	public void setTabLabel(String tabLabel) {
		this.tabLabel = tabLabel;
	}

	public int getTabSortOrder() {
		return tabSortOrder;
	}

	public void setTabSortOrder(int tabSortOrder) {
		this.tabSortOrder = tabSortOrder;
	}

	public String getHelper() {
		return helper;
	}

	public void setHelper(String helper) {
		this.helper = helper;
	}

	public String getSectionName() {
		return sectionName;
	}

	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}

	public String getSectionLabel() {
		return sectionLabel;
	}

	public void setSectionLabel(String sectionLabel) {
		this.sectionLabel = sectionLabel;
	}

	public int getSectionSortOrder() {
		return sectionSortOrder;
	}

	public void setSectionSortOrder(int sectionSortOrder) {
		this.sectionSortOrder = sectionSortOrder;
	}

	public boolean isSectionShowDefault() {
		return sectionShowDefault;
	}

	public void setSectionShowDefault(boolean sectionShowDefault) {
		this.sectionShowDefault = sectionShowDefault;
	}

	public boolean isSectionShowWebsite() {
		return sectionShowWebsite;
	}

	public void setSectionShowWebsite(boolean sectionShowWebsite) {
		this.sectionShowWebsite = sectionShowWebsite;
	}

	public boolean isSectionShowStore() {
		return sectionShowStore;
	}

	public void setSectionShowStore(boolean sectionShowStore) {
		this.sectionShowStore = sectionShowStore;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupLabel() {
		return groupLabel;
	}

	public void setGroupLabel(String groupLabel) {
		this.groupLabel = groupLabel;
	}

	public int getGroupSortOrder() {
		return groupSortOrder;
	}

	public void setGroupSortOrder(int groupSortOrder) {
		this.groupSortOrder = groupSortOrder;
	}

	public boolean isGroupShowDefault() {
		return groupShowDefault;
	}

	public void setGroupShowDefault(boolean groupShowDefault) {
		this.groupShowDefault = groupShowDefault;
	}

	public boolean isGroupShowWebsite() {
		return groupShowWebsite;
	}

	public void setGroupShowWebsite(boolean groupShowWebsite) {
		this.groupShowWebsite = groupShowWebsite;
	}

	public boolean isGroupShowStore() {
		return groupShowStore;
	}

	public int getAclSortOrder() {
		return aclSortOrder;
	}

	public void setAclSortOrder(int aclSortOrder) {
		this.aclSortOrder = aclSortOrder;
	}

	public void setGroupShowStore(boolean groupShowStore) {
		this.groupShowStore = groupShowStore;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldLabel() {
		return fieldLabel;
	}

	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}

	public String getFiledComment() {
		return filedComment;
	}

	public void setFiledComment(String filedComment) {
		this.filedComment = filedComment;
	}

	public String getFieldFrontendModel() {
		return fieldFrontendModel;
	}

	public void setFieldFrontendModel(String fieldFrontendModel) {
		this.fieldFrontendModel = fieldFrontendModel;
	}

	public String getFieldBackednModel() {
		return fieldBackednModel;
	}

	public void setFieldBackednModel(String fieldBackednModel) {
		this.fieldBackednModel = fieldBackednModel;
	}

	public String getFieldFrontendType() {
		return fieldFrontendType;
	}

	public void setFieldFrontendType(String fieldFrontendType) {
		this.fieldFrontendType = fieldFrontendType;
	}

	public int getFieldSortOrder() {
		return fieldSortOrder;
	}

	public void setFieldSortOrder(int fieldSortOrder) {
		this.fieldSortOrder = fieldSortOrder;
	}

	public boolean isFieldShowDefault() {
		return fieldShowDefault;
	}

	public void setFieldShowDefault(boolean fieldShowDefault) {
		this.fieldShowDefault = fieldShowDefault;
	}

	public boolean isFieldShowWebsite() {
		return fieldShowWebsite;
	}

	public void setFieldShowWebsite(boolean fieldShowWebsite) {
		this.fieldShowWebsite = fieldShowWebsite;
	}

	public boolean isFieldShowStore() {
		return fieldShowStore;
	}

	public void setFieldShowStore(boolean fieldShowStore) {
		this.fieldShowStore = fieldShowStore;
	}

	public String getAclName() {
		return aclName;
	}

	public void setAclName(String aclName) {
		this.aclName = aclName;
	}

	public String getAclTitle() {
		return aclTitle;
	}

	public void setAclTitle(String aclTitle) {
		this.aclTitle = aclTitle;
	}

	public String getFieldSourceModel() {
		return fieldSourceModel;
	}

	public void setFieldSourceModel(String fieldSourceModel) {
		this.fieldSourceModel = fieldSourceModel;
	}

}
