package pl.mamooth.eclipse.magento.wizards.storeconfig;

public class SectionData {
	protected String name;
	protected String label;
	protected int sortOrder;
	protected boolean visibleStore;
	protected boolean visibleWebsite;
	protected boolean visibleDefault;
	protected String tab;
	protected String aclLabel;
	protected int aclSortOrder;

	public String getAclLabel() {
		return aclLabel;
	}

	public void setAclLabel(String aclLabel) {
		this.aclLabel = aclLabel;
	}

	public int getAclSortOrder() {
		return aclSortOrder;
	}

	public void setAclSortOrder(int aclSortOrder) {
		this.aclSortOrder = aclSortOrder;
	}

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public boolean isVisibleStore() {
		return visibleStore;
	}

	public void setVisibleStore(boolean visibleStore) {
		this.visibleStore = visibleStore;
	}

	public boolean isVisibleWebsite() {
		return visibleWebsite;
	}

	public void setVisibleWebsite(boolean visibleWebsite) {
		this.visibleWebsite = visibleWebsite;
	}

	public boolean isVisibleDefault() {
		return visibleDefault;
	}

	public void setVisibleDefault(boolean visibleDefault) {
		this.visibleDefault = visibleDefault;
	}
}
