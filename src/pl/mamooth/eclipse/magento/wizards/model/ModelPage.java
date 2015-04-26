package pl.mamooth.eclipse.magento.wizards.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wb.swt.SWTResourceManager;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;

public class ModelPage extends WizardPage {

	private NewModelWizard parent;
	private Table fieldsTable;
	private TableColumn nameColumn;
	private TableColumn typeColumn;
	private TableColumn idColumn;
	private Text tableText;
	private Button phpdocCheckbox;
	private Button setupScriptCheckbox;
	private Spinner spinnerMajor;
	private Spinner spinnerMinor;
	private Spinner spinnerChange;
	private Label scriptNameLabel;
	private SashForm sashForm;
	private TableColumn deleteColumn;
	private MagentoModule module;
	private String tableName;
	private Button fieldAddButton;
	private boolean pageReady = false;

	public ModelPage(NewModelWizard newModelWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newModelWizard;
	}

	public ISelection getSelection() {
		return parent.getSelection();
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		setControl(container);

		Label tableLabel = new Label(container, SWT.NONE);
		tableLabel.setText(I18n.get("table"));

		tableText = new Text(container, SWT.BORDER);
		tableText.setEnabled(false);
		tableText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tableNameChanged();
			}
		});
		tableText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		Label fieldsLabel = new Label(container, SWT.NULL);
		fieldsLabel.setText(I18n.get("fields"));

		fieldsTable = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		fieldsTable.setEnabled(false);
		fieldsTable.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		fieldsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		fieldsTable.setHeaderVisible(true);
		fieldsTable.setLinesVisible(true);

		nameColumn = new TableColumn(fieldsTable, SWT.NONE);
		nameColumn.setWidth(175);
		nameColumn.setText(I18n.get("name"));

		typeColumn = new TableColumn(fieldsTable, SWT.NONE);
		typeColumn.setWidth(146);
		typeColumn.setText(I18n.get("type"));

		idColumn = new TableColumn(fieldsTable, SWT.NONE);
		idColumn.setWidth(31);
		idColumn.setText(I18n.get("id"));

		deleteColumn = new TableColumn(fieldsTable, SWT.NONE);
		deleteColumn.setWidth(57);
		deleteColumn.setText(I18n.get("delete"));

		fieldAddButton = new Button(container, SWT.PUSH);
		fieldAddButton.setEnabled(false);
		fieldAddButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				add();
			}
		});
		fieldAddButton.setText(I18n.get("add"));
		new Label(container, SWT.NONE);
		sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		setupScriptCheckbox = new Button(sashForm, SWT.CHECK);
		setupScriptCheckbox.setEnabled(false);
		setupScriptCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setupScriptChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				setupScriptChanged();
			}
		});
		setupScriptCheckbox.setText(I18n.get("setup_script"));

		phpdocCheckbox = new Button(sashForm, SWT.CHECK);
		phpdocCheckbox.setEnabled(false);
		phpdocCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				phpdocChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				phpdocChanged();
			}
		});
		phpdocCheckbox.setText(I18n.get("phpdoc_methods"));
		sashForm.setWeights(new int[] { 1, 1 });
		new Label(container, SWT.NONE);

		Label targetVersionLabel = new Label(container, SWT.NONE);
		targetVersionLabel.setText(I18n.get("target_version"));
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));

		spinnerMajor = new Spinner(composite, SWT.BORDER);
		spinnerMajor.setEnabled(false);
		spinnerMajor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				versionChanged();
			}
		});

		Label label1 = new Label(composite, SWT.NONE);
		label1.setText(".");

		spinnerMinor = new Spinner(composite, SWT.BORDER);
		spinnerMinor.setEnabled(false);
		spinnerMinor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				versionChanged();
			}
		});

		Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText(".");

		spinnerChange = new Spinner(composite, SWT.BORDER);
		spinnerChange.setEnabled(false);
		spinnerChange.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				versionChanged();
			}
		});

		scriptNameLabel = new Label(composite, SWT.CENTER);
		scriptNameLabel.setLayoutData(new RowData(251, 18));
		scriptNameLabel.setEnabled(false);
		// scriptNameLabel.setText("update-0.1.0-0.1.12.php");
		new Label(container, SWT.NONE);
		pageReady = true;
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
	}

	public boolean isPageReady() {
		return pageReady;
	}

	protected void tableNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					tableNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setTableName(tableText.getText());
		dialogChanged();
	}

	protected void setupScriptChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					setupScriptChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setCreateInstallScript(setupScriptCheckbox.getSelection());
		spinnerChange.setEnabled(setupScriptCheckbox.getSelection());
		spinnerMajor.setEnabled(setupScriptCheckbox.getSelection());
		spinnerMinor.setEnabled(setupScriptCheckbox.getSelection());
		versionChanged();
	}

	protected void phpdocChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					phpdocChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setCreateFieldsComments(phpdocCheckbox.getSelection());
	}

	protected void versionChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					versionChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (setupScriptCheckbox.getSelection()) {
			String previous = module.getPreviousVersion(spinnerMajor.getSelection(), spinnerMinor.getSelection(), spinnerChange.getSelection());
			String target = spinnerMajor.getText() + "." + spinnerMinor.getText() + "." + spinnerChange.getText();
			try {
				getConfig().setSetupScriptFilePath(module.getSetupScriptPath());
			} catch (CoreException e) {
				setupScriptCheckbox.setSelection(false);
				updateStatus(I18n.get("cannot_read_path"), false);
				return;
			}
			String name;
			if (previous != null) {
				name = "upgrade-" + previous + "-" + target + FolderHelper.PHP_EXTENSION;
			} else {
				name = "install-" + target + FolderHelper.PHP_EXTENSION;
			}
			getConfig().setSetupScriptFileName(name);
			getConfig().setVersionTarget(target);
			scriptNameLabel.setText(name);
		} else {
			scriptNameLabel.setText("");
			getConfig().setSetupScriptFileName(null);
			getConfig().setSetupScriptFilePath(null);
		}
	}

	protected void add() {
		new TableFieldEntry(this, fieldsTable);
		dialogChanged();
	}

	public void activeChanged(final boolean active) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					activeChanged(active);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		tableText.setEnabled(active);
		fieldsTable.setEnabled(active);
		fieldAddButton.setEnabled(active);
		phpdocCheckbox.setEnabled(active);
		setupScriptCheckbox.setEnabled(active);
		if (!active) {
			tableText.setText("");
			phpdocCheckbox.setSelection(false);
			setupScriptCheckbox.setSelection(false);
			scriptNameLabel.setText("");
		} else {
			if (tableName != null)
				tableText.setText(tableName);
		}
	}

	public void initialize(final MagentoModule module) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					initialize(module);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		this.module = module;
		String version = module.getVersion();
		boolean versionChanged = false;
		try {
			if (version != null) {
				String[] increments = version.split("\\.");
				if (increments.length == 3 && spinnerMinor != null && spinnerMajor != null && spinnerChange != null) {
					spinnerMajor.setSelection(Integer.parseInt(increments[0]));
					spinnerMinor.setSelection(Integer.parseInt(increments[1]));
					spinnerChange.setSelection(Integer.parseInt(increments[2]));
					versionChanged = true;
				}
			}
		} catch (Exception e) {
			versionChanged = false;
		}
		if (!versionChanged) {
			updateStatus(I18n.get("cannot_parse_version"), false);
		}
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

		if (tableText.isEnabled()) {
			TableItem[] items = fieldsTable.getItems();
			Field[] fields = new Field[items.length];
			String tableId = null;
			for (int i = 0; i < items.length; ++i) {
				TableItem item = items[i];
				Object itemData = item.getData();
				if (itemData instanceof Field) {
					fields[i] = (Field) itemData;
					if (fields[i].isPrimary()) {
						tableId = fields[i].getUnderscoreName();
					}
				} else {
					updateStatus(I18n.get("incorrect_field"));
					return;
				}
			}
			if (tableId == null) {
				updateStatus(I18n.get("no_table_id"));
				return;
			} else {
				getConfig().setTableIdField(tableId);
			}
			getConfig().setFileds(fields);
		}
		updateStatus(null);
	}

	public void updateStatus(String message) {
		updateStatus(message, true);
	}

	public void updateStatus(String message, boolean error) {
		if (error) {
			setErrorMessage(message);
		} else {
			setMessage(message, WARNING);
		}
		if (message == null) {
			setErrorMessage(null);
			setMessage(getDescription(), NONE);
		}
		if (error) {
			setPageComplete(message == null);
		} else {
			setPageComplete(true);
		}
	}

	public void setTableName(final String string) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					setTableName(string);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		tableName = string;
		if (tableText.getEnabled()) {
			tableText.setText(string);
		}
	}
}