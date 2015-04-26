package pl.mamooth.eclipse.magento.wizards.block;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.StringHelper;

public class TemplatePage extends WizardPage {

	private Text fileNameText;
	private Button createTemplateCheckbox;
	private Button templateInCodeCheckbox;
	private Button createLayoputEntryCheckbox;
	private Text nameText;
	private Text aliasText;
	private Text handlerText;
	private Combo referencesCombo;
	private Button browseHandlerButton;
	private NewBlockWizard parent;
	private AutoCompleteField referencesAutoComplete;
	protected String[] aviableHandlers;
	private boolean pageReady = false;

	public TemplatePage(NewBlockWizard newBlockWizard) {
		super("wizardTemplatePage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newBlockWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		setControl(container);
		new Label(container, SWT.NONE);

		createTemplateCheckbox = new Button(container, SWT.CHECK);
		createTemplateCheckbox.setEnabled(true);
		createTemplateCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				createTemplateChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				createTemplateChanged();
			}
		});
		createTemplateCheckbox.setText(I18n.get("create_template"));
		new Label(container, SWT.NONE);

		Label fileNameLablel = new Label(container, SWT.NONE);
		fileNameLablel.setText(I18n.get("file_name"));

		fileNameText = new Text(container, SWT.BORDER);
		fileNameText.setEnabled(false);
		fileNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fileNameChanged();
			}
		});
		fileNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		templateInCodeCheckbox = new Button(container, SWT.CHECK);
		templateInCodeCheckbox.setEnabled(false);
		templateInCodeCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				templateInCodeChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				templateInCodeChanged();
			}
		});
		templateInCodeCheckbox.setText(I18n.get("template_in_code"));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		createLayoputEntryCheckbox = new Button(container, SWT.CHECK);
		createLayoputEntryCheckbox.setEnabled(false);
		createLayoputEntryCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				createLayoutEntryChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				createLayoutEntryChanged();
			}
		});
		createLayoputEntryCheckbox.setText(I18n.get("create_layoutxml"));
		new Label(container, SWT.NONE);

		Label handlerLabel = new Label(container, SWT.NONE);
		handlerLabel.setText(I18n.get("handler"));

		handlerText = new Text(container, SWT.BORDER);
		handlerText.setEnabled(false);
		handlerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				handlerChanged();
			}
		});
		handlerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		browseHandlerButton = new Button(container, SWT.NONE);
		browseHandlerButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseHander();
			}
		});
		browseHandlerButton.setText(I18n.get("browse_handler"));

		Label refrencesLabel = new Label(container, SWT.NONE);
		refrencesLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		refrencesLabel.setText(I18n.get("references"));

		referencesCombo = new Combo(container, SWT.NONE);
		referencesCombo.setEnabled(false);
		referencesCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				referencesChanged();
			}
		});
		referencesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		referencesAutoComplete = new AutoCompleteField(referencesCombo, new ComboContentAdapter(), new String[] {});
		new Label(container, SWT.NONE);

		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(I18n.get("name"));

		nameText = new Text(container, SWT.BORDER);
		nameText.setEnabled(false);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameChanged();
			}
		});
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label aliasLabel = new Label(container, SWT.NONE);
		aliasLabel.setText(I18n.get("alias"));

		aliasText = new Text(container, SWT.BORDER);
		aliasText.setEnabled(false);
		aliasText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				aliasChanged();
			}
		});
		aliasText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		pageReady = true;
		dialogChanged();
	}

	public boolean isPageReady() {
		return pageReady;
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
	}

	public void initalize(final MagentoModule module) {
		getConfig().setDesignLayoutFileName(module.getShortName() + FolderHelper.XML_EXTENSION);
		new Job(I18n.get("load_aviable_handlers")) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					aviableHandlers = module.getDesignHandlers();
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
	}

	public void blockNameChanged(final String name) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					blockNameChanged(name);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (getConfig().getModule() != null && fileNameText != null) {
			StringBuilder path = new StringBuilder(getConfig().getModule().getShortName());
			path.append('/');
			path.append(name.replace('_', '/'));
			path.append(".phtml");
			fileNameText.setText(path.toString());
		}
		aliasText.setText(StringHelper.toCamelCase(name));
		nameText.setText(name.replace('_', '.'));
	}

	protected void aliasChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					aliasChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setBlockAlias(aliasText.getText());
		dialogChanged();
	}

	protected void nameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					nameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setBlockName(nameText.getText());
		dialogChanged();
	}

	protected void handlerChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					handlerChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setHandler(handlerText.getText());
		dialogChanged();
	}

	protected void referencesChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					referencesChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setReferences(referencesCombo.getText());
		dialogChanged();
	}

	protected void browseHander() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setElements(aviableHandlers);
		dialog.setTitle(I18n.get("title"));
		dialog.setAllowDuplicates(false);
		dialog.setIgnoreCase(true);
		dialog.setMessage(I18n.get("message"));
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				handlerText.setText(result[0].toString());
			}
		}
	}

	protected void createLayoutEntryChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					createLayoutEntryChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setCreateLayoutXmlEntry(createLayoputEntryCheckbox.getSelection());
		aliasText.setEnabled(createLayoputEntryCheckbox.getSelection());
		nameText.setEnabled(createLayoputEntryCheckbox.getSelection());
		handlerText.setEnabled(createLayoputEntryCheckbox.getSelection());
		referencesCombo.setEnabled(createLayoputEntryCheckbox.getSelection());
		browseHandlerButton.setEnabled(createLayoputEntryCheckbox.getSelection());
		dialogChanged();
	}

	protected void templateInCodeChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					templateInCodeChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setAssignTemplateInCode(templateInCodeCheckbox.getSelection());
		dialogChanged();
	}

	public void fileNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					fileNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setMagentoTemplatePath(fileNameText.getText());
		if (getConfig().getModule() != null) {
			try {
				String[] fileName = fileNameText.getText().split("/");
				if (fileName.length == 0) {
					return;
				}
				String[] path = StringHelper.concat(getConfig().getModule().getAppFolderPath(), new String[] { FolderHelper.DESIGN_FOLDER, getConfig().getDesignScope(), getConfig().getDesignPackage(), getConfig().getDesignTemplate(), FolderHelper.TEMPLATE_FOLDER });
				path = StringHelper.concat(path, Arrays.copyOfRange(fileName, 0, fileName.length - 1));
				getConfig().setTemplateFilePath(path);
				getConfig().setTemplateFileName(fileName[fileName.length - 1]);
			} catch (CoreException e) {
				updateStatus(I18n.get("error_while_path_creation"));
				return;
			}
		}
		dialogChanged();
	}

	protected void createTemplateChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					createTemplateChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setCreateTemplate(createTemplateCheckbox.getSelection());
		if (!createTemplateCheckbox.getSelection()) {
			createLayoputEntryCheckbox.setSelection(false);
		}
		createLayoputEntryCheckbox.setEnabled(createTemplateCheckbox.getSelection());
		templateInCodeCheckbox.setEnabled(createTemplateCheckbox.getSelection());
		fileNameText.setEnabled(createTemplateCheckbox.getSelection());
		createLayoutEntryChanged();
		parent.getNamePage().notifyTemplateCreation(createTemplateCheckbox.getSelection());
		templateInCodeCheckbox.setSelection(createTemplateCheckbox.getSelection());
	}

	private void dialogChanged() {
		if (fileNameText.isEnabled() && !Pattern.matches("^([a-zA-Z0-9_]+/)*[a-zA-Z0-9_]+\\.[pP][hH][tT][mM][lL]$", fileNameText.getText())) {
			updateStatus(I18n.get("invalid_file_name"));
			return;
		}
		if (handlerText.isEnabled() && !Pattern.matches("^([a-zA-Z0-9]+_)*[a-zA-Z0-9]+", handlerText.getText())) {
			updateStatus(I18n.get("invalid_handler"));
			return;
		}
		if (referencesCombo.isEnabled() && referencesCombo.getText().length() == 0) {
			updateStatus(I18n.get("empty_references"));
			return;
		}
		if (nameText.isEnabled() && nameText.getText().length() == 0) {
			updateStatus(I18n.get("empty_names"));
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public void scopeChanged(final String scope, final MagentoModule module) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					scopeChanged(scope, module);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		new UIJob(I18n.get("load_aviable_references")) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					String[] aviableReferences = module.getDesignRefernces(scope);
					referencesCombo.setItems(aviableReferences);
					referencesAutoComplete.setProposals(aviableReferences);
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
	}
}