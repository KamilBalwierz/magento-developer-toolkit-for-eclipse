package pl.mamooth.eclipse.magento.wizards.controller;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.helpers.I18n;

public class ActionsPage extends WizardPage {
	private Table tableActions;
	private NewControllerWizard parent;
	private Button postDispatchCheckbox;
	private Button preDispatchCheckbox;
	private boolean pageReady = false;

	public ActionsPage(NewControllerWizard newControllerWizard) {
		super("actionsPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newControllerWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		setControl(container);

		Label actions_label = new Label(container, SWT.NONE);
		actions_label.setText(I18n.get("actions"));

		tableActions = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		tableActions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableActions.setHeaderVisible(true);
		tableActions.setLinesVisible(true);

		TableColumn nameColumn = new TableColumn(tableActions, SWT.NONE);
		nameColumn.setWidth(415);
		nameColumn.setText(I18n.get("column_name"));

		TableColumn tblclmnDeletecolumn = new TableColumn(tableActions, SWT.NONE);
		tblclmnDeletecolumn.setWidth(60);
		tblclmnDeletecolumn.setText(I18n.get("delete_column"));

		Button addButton = new Button(container, SWT.NONE);
		addButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				add();
			}
		});
		addButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		addButton.setText(I18n.get("add"));
		new Label(container, SWT.NONE);

		postDispatchCheckbox = new Button(container, SWT.CHECK);
		postDispatchCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				postDispatchChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				postDispatchChanged();
			}
		});
		postDispatchCheckbox.setText(I18n.get("post_dispatch"));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		preDispatchCheckbox = new Button(container, SWT.CHECK);
		preDispatchCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				preDispatchChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				preDispatchChanged();
			}
		});
		preDispatchCheckbox.setText(I18n.get("pre_dispatch"));
		new Label(container, SWT.NONE);
		pageReady = true;
		dialogChanged();
	}

	protected Configuration getConfig() {
		return parent.getConfigration();
	}

	protected void preDispatchChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					preDispatchChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGeneratePreDispatch(preDispatchCheckbox.getSelection());
	}

	protected void postDispatchChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					postDispatchChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGeneratePostDispatch(postDispatchCheckbox.getSelection());
	}

	protected void add() {
		new ActionTableEntry(this, tableActions);
	}

	public void dialogChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					dialogChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		TableItem[] items = tableActions.getItems();
		String[] actions = new String[items.length];
		for (int i = 0; i < items.length; ++i) {
			String action = items[i].getText();
			if (!action.endsWith("Action")) {
				action = action + "Action";
			}
			actions[i] = action;
			if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*$", action)) {
				updateStatus(I18n.get("wrong_action"));
				return;
			}
		}
		getConfig().setActions(actions);
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}