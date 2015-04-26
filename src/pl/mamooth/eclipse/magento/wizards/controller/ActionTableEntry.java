package pl.mamooth.eclipse.magento.wizards.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import pl.mamooth.eclipse.magento.helpers.I18n;

class ActionTableEntry {

	private Text actionText;
	private Button deleteButton;
	private TableItem item;
	private TableEditor actionEditor;
	private TableEditor deleteEditor;

	public ActionTableEntry(final ActionsPage page, Table parent) {
		item = new TableItem(parent, SWT.NONE);

		actionEditor = new TableEditor(parent);
		actionEditor.grabHorizontal = true;
		actionText = new Text(parent, SWT.NONE);
		actionText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				item.setText(actionText.getText());
				page.dialogChanged();
			}
		});
		actionText.setFocus();
		actionEditor.setEditor(actionText, item, 0);
		actionEditor.setItem(item);

		deleteEditor = new TableEditor(parent);
		deleteEditor.grabHorizontal = true;
		deleteEditor.horizontalAlignment = SWT.CENTER;
		deleteButton = new Button(parent, SWT.PUSH);
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
		deleteEditor.setEditor(deleteButton, item, 1);
		deleteEditor.setItem(item);
	}

	protected void removeLine() {
		actionEditor.dispose();
		deleteEditor.dispose();
		actionText.dispose();
		deleteButton.dispose();
		item.dispose();
	}
}
