package pl.mamooth.eclipse.magento.wizards.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Field {
	private String columnName;
	private String fieldName;
	private boolean primary;
	private String type;
	private String magentoType;
	private Map<String, String> attributes = new HashMap<String, String>();

	public String getAttribute(String attribue) {
		return attributes.get(attribue);
	}

	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public String[] getAttributes() {
		Set<String> keys = attributes.keySet();
		return keys.toArray(new String[] {});
	}

	public void clearAttrubutes() {
		attributes.clear();
	}

	public String getMagentoType() {
		return magentoType;
	}

	public void setMagentoType(String magentoType) {
		this.magentoType = magentoType;
	}

	public String getUnderscoreName() {
		return columnName;
	}

	public void setUnderscoreName(String columnName) {
		this.columnName = columnName;
	}

	public String getCamelcaseName() {
		return fieldName;
	}

	public void setCamelcaseName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
