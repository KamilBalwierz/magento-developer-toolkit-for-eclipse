package pl.mamooth.eclipse.magento.wizards.model;

import java.util.regex.Pattern;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.StringHelper;

public class TableFieldEntry {
	private Text nameText;
	private Combo typeCombo;
	private Button idCheckbox;
	private Button deleteButton;
	private TableItem item;
	private TableEditor nameEditor;
	private TableEditor typeEditor;
	private TableEditor idEditor;
	private TableEditor deleteEditor;
	private ModelPage page;
	private Field field;

	public static final String[] TYPES = { "boolean", "smallint", "integer", "bigint", "float", "numeric", "decimal", "date", "timestamp", "datetime", "text", "blob", "varbinary" };

	public static final String[] MAGENTO_TYPES = { "Varien_Db_Ddl_Table::TYPE_BOOLEAN", "Varien_Db_Ddl_Table::TYPE_SMALLINT", "Varien_Db_Ddl_Table::TYPE_INTEGER", "Varien_Db_Ddl_Table::TYPE_BIGINT", "Varien_Db_Ddl_Table::TYPE_FLOAT", "Varien_Db_Ddl_Table::TYPE_NUMERIC", "Varien_Db_Ddl_Table::TYPE_DECIMAL", "Varien_Db_Ddl_Table::TYPE_DATE", "Varien_Db_Ddl_Table::TYPE_TIMESTAMP", "Varien_Db_Ddl_Table::TYPE_DATETIME", "Varien_Db_Ddl_Table::TYPE_TEXT", "Varien_Db_Ddl_Table::TYPE_BLOB", "Varien_Db_Ddl_Table::TYPE_VARBINARY" };

	public TableFieldEntry(final ModelPage page, Table table) {
		this.page = page;
		field = new Field();

		item = new TableItem(table, SWT.NONE);

		nameEditor = new TableEditor(table);
		nameEditor.grabHorizontal = true;
		nameText = new Text(table, SWT.NONE);
		nameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				entryChanged();
			}

		});
		nameEditor.setEditor(nameText, item, 0);
		nameText.setFocus();

		typeEditor = new TableEditor(table);
		typeEditor.grabHorizontal = true;
		typeCombo = new Combo(table, SWT.READ_ONLY);
		typeCombo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		typeCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				entryChanged();
			}

		});
		typeCombo.setItems(TYPES);
		new AutoCompleteField(typeCombo, new ComboContentAdapter(), TYPES);
		typeEditor.setEditor(typeCombo, item, 1);

		idEditor = new TableEditor(table);
		idEditor.grabHorizontal = true;
		idEditor.horizontalAlignment = SWT.CENTER;
		idCheckbox = new Button(table, SWT.RADIO);
		idCheckbox.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		idCheckbox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				entryChanged();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				entryChanged();
			}
		});
		idEditor.setEditor(idCheckbox, item, 2);

		deleteEditor = new TableEditor(table);
		deleteEditor.grabHorizontal = true;
		deleteEditor.horizontalAlignment = SWT.CENTER;
		deleteButton = new Button(table, SWT.PUSH);
		deleteButton.setText(I18n.get("remove"));
		deleteButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				removeLine();
				page.dialogChanged();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});
		deleteEditor.setEditor(deleteButton, item, 3);
		deleteEditor.setItem(item);
	}

	protected void entryChanged() {
		String name = nameText.getText();
		field.clearAttrubutes();
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)*$", nameText.getText())) {
			page.updateStatus(I18n.get("illegal_name"));
			return;
		}
		field.setCamelcaseName(StringHelper.toCamelCase(name, true, false));
		field.setMagentoType(null);
		for (int i = 0; i < TYPES.length; ++i) {
			if (TYPES[i].equals(typeCombo.getText())) {
				field.setMagentoType(MAGENTO_TYPES[i]);
				break;
			}
		}
		if (field.getMagentoType() == null) {
			page.updateStatus(I18n.get("illegal_type"));
			return;
		}
		field.setPrimary(idCheckbox.getSelection());
		if (idCheckbox.getSelection()) {
			field.addAttribute("identity", "true");
			field.addAttribute("unsigned", "true");
			field.addAttribute("nullable", "false");
			field.addAttribute("primary", "true");
		}
		field.setType(typeCombo.getText());
		field.setUnderscoreName(name);
		item.setData(field);
		page.dialogChanged();
	}

	protected void removeLine() {
		nameText.dispose();
		typeCombo.dispose();
		idCheckbox.dispose();
		deleteButton.dispose();
		nameEditor.dispose();
		typeEditor.dispose();
		idEditor.dispose();
		deleteEditor.dispose();
		item.dispose();
	}
}
