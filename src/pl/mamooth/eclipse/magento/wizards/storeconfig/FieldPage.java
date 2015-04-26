package pl.mamooth.eclipse.magento.wizards.storeconfig;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.I18n;

public class FieldPage extends WizardPage {

	private NewStoreConfigWizard parent;
	private Text labelText;
	private Text commentText;
	private Text nameText;
	private Text frontEndModelText;
	private Text backendModelText;
	private Button defaultCheckbox;
	private Button websiteCheckbox;
	private Button shopCheckbox;
	private Spinner sortOrderSpinner;
	private Combo frontEndTypeCombo;
	protected String[] aviableBackends;
	protected String[] aviableSources;
	protected String[] aviableFrontends;
	private String[] names;
	private boolean pageReady = false;
	private Button browseFrontendButton;
	private Text sourceModelText;

	public FieldPage(NewStoreConfigWizard newStoreConfigWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newStoreConfigWizard;
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		setControl(container);

		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(I18n.get("name"));

		nameText = new Text(container, SWT.BORDER);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameChanged();
			}
		});
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label labelLabel = new Label(container, SWT.NONE);
		labelLabel.setText(I18n.get("label"));

		labelText = new Text(container, SWT.BORDER);
		labelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				labelChanged();
			}
		});
		labelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label commentLabel = new Label(container, SWT.NONE);
		commentLabel.setText(I18n.get("comment"));

		commentText = new Text(container, SWT.BORDER);
		commentText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				commentChanged();
			}
		});
		commentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label frontendTypeLabel = new Label(container, SWT.NONE);
		frontendTypeLabel.setText(I18n.get("frontend_type"));

		String[] frontendTypes = new String[] { "checkbox", "date", "file", "imagefile", "link", "note", "radio", "reset", "textarea", "button", "collection", "editor", "gallery", "image", "multiline", "obscure", "radios", "select", "text", "checkboxes", "column", "fieldset", "hidden", "label", "multiselect", "password", "submit", "time" };

		frontEndTypeCombo = new Combo(container, SWT.NONE);
		frontEndTypeCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				frontendTypeChanged();
			}
		});
		frontEndTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		frontEndTypeCombo.setItems(frontendTypes);
		new AutoCompleteField(frontEndTypeCombo, new ComboContentAdapter(), frontendTypes);
		new Label(container, SWT.NONE);

		Label frontendModelLabel = new Label(container, SWT.NONE);
		frontendModelLabel.setText(I18n.get("frontend_model"));

		frontEndModelText = new Text(container, SWT.BORDER);
		frontEndModelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				frontendModelChanged();
			}
		});
		frontEndModelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		browseFrontendButton = new Button(container, SWT.NONE);
		browseFrontendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseFrondendModel();
			}
		});
		browseFrontendButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		browseFrontendButton.setText(I18n.get("browse_frontend"));

		Label sourceModelLabel = new Label(container, SWT.NONE);
		sourceModelLabel.setText(I18n.get("source_model"));

		sourceModelText = new Text(container, SWT.BORDER);
		sourceModelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				sourceModelChanged();
			}
		});
		sourceModelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button browseSourceButton = new Button(container, SWT.NONE);
		browseSourceButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseSourceModel();
			}
		});
		browseSourceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		browseSourceButton.setText(I18n.get("browse_source"));

		Label backendModelLabel = new Label(container, SWT.NONE);
		backendModelLabel.setText(I18n.get("backend_model"));

		backendModelText = new Text(container, SWT.BORDER);
		backendModelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				backendModelChanged();
			}
		});
		backendModelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button browseBackendButton = new Button(container, SWT.NONE);
		browseBackendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseBackendModel();
			}
		});
		browseBackendButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		browseBackendButton.setText(I18n.get("browse_backend"));

		Label visibleLabel = new Label(container, SWT.NONE);
		visibleLabel.setText(I18n.get("visible"));

		SashForm sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		defaultCheckbox = new Button(sashForm, SWT.CHECK);
		defaultCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				defaultChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				defaultChanged();
			}
		});
		defaultCheckbox.setText(I18n.get("default"));

		websiteCheckbox = new Button(sashForm, SWT.CHECK);
		websiteCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				websiteChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				websiteChanged();
			}
		});
		websiteCheckbox.setText(I18n.get("website"));

		shopCheckbox = new Button(sashForm, SWT.CHECK);
		shopCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				shopChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				shopChanged();
			}
		});
		shopCheckbox.setText(I18n.get("shop"));
		sashForm.setWeights(new int[] { 1, 1, 1 });
		new Label(container, SWT.NONE);

		Label sortOrderLabel = new Label(container, SWT.NONE);
		sortOrderLabel.setText(I18n.get("sort_order"));

		sortOrderSpinner = new Spinner(container, SWT.BORDER);
		sortOrderSpinner.setMaximum(10000);
		sortOrderSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				sortorderChanged();
			}
		});
		new Label(container, SWT.NONE);
		pageReady = true;
		dialogChanged();
	}

	protected void browseSourceModel() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setElements(aviableSources);
		dialog.setTitle(I18n.get("title"));
		dialog.setAllowDuplicates(false);
		dialog.setIgnoreCase(true);
		dialog.setMessage(I18n.get("message"));
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				sourceModelText.setText(result[0].toString());
			}
		}
	}

	protected void sourceModelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					sourceModelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldSourceModel(sourceModelText.getText());
		dialogChanged();
	}

	protected void sortorderChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					sortorderChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldSortOrder(sortOrderSpinner.getSelection());
	}

	protected void shopChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					shopChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldShowStore(shopCheckbox.getSelection());
	}

	protected void websiteChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					websiteChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldShowWebsite(websiteCheckbox.getSelection());
	}

	protected void defaultChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					defaultChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldShowDefault(defaultCheckbox.getSelection());
	}

	protected void browseBackendModel() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setElements(aviableBackends);
		dialog.setTitle(I18n.get("title"));
		dialog.setAllowDuplicates(false);
		dialog.setIgnoreCase(true);
		dialog.setMessage(I18n.get("message"));
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				backendModelText.setText(result[0].toString());
			}
		}
	}

	protected void backendModelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					backendModelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldBackednModel(backendModelText.getText());
		dialogChanged();
	}

	protected void browseFrondendModel() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setElements(aviableFrontends);
		dialog.setTitle(I18n.get("title"));
		dialog.setAllowDuplicates(false);
		dialog.setIgnoreCase(true);
		dialog.setMessage(I18n.get("message"));
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				frontEndModelText.setText(result[0].toString());
			}
		}
	}

	protected void frontendModelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					frontendModelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (frontEndModelText.getText().length() == 0) {
			frontEndTypeCombo.setEnabled(true);
		} else {
			frontEndTypeCombo.setEnabled(false);
		}

		getConfig().setFieldFrontendModel(frontEndModelText.getText());
		dialogChanged();
	}

	protected void frontendTypeChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					frontendTypeChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (frontEndTypeCombo.getText().length() == 0) {
			frontEndModelText.setEnabled(true);
			browseFrontendButton.setEnabled(true);
		} else {
			frontEndModelText.setEnabled(false);
			browseFrontendButton.setEnabled(false);
		}
		getConfig().setFieldFrontendType(frontEndTypeCombo.getText());
		dialogChanged();
	}

	protected void commentChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					commentChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFiledComment(commentText.getText());
	}

	protected void labelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					labelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFieldLabel(labelText.getText());
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

		getConfig().setFieldName(nameText.getText());
		dialogChanged();
	}

	public void initialize(final MagentoModule module) {
		new Job(I18n.get("load_source_models")) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					aviableSources = module.getSourceModels();
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
		new Job(I18n.get("load_backend_models")) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					aviableBackends = module.getBackendModels();
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
		new Job(I18n.get("load_frontend_models")) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					aviableFrontends = module.getFrontedModels();
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
			}
		}.schedule();
	}

	private void dialogChanged() {
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

		if (names != null) {
			for (String name : names) {
				if (name.equals(nameText.getText())) {
					updateStatus(I18n.get("already_exists"));
					return;
				}
			}
		}
		if (!Pattern.matches("^[a-zA-Z0-9_]+$", nameText.getText())) {
			updateStatus(I18n.get("wrong_name"));
			return;
		}
		if (labelText.getText().length() == 0) {
			updateStatus(I18n.get("empty_label"));
			return;
		}
		if (frontEndModelText.getText().length() != 0 && !Pattern.matches("^[a-zA-Z0-9_]+/[a-zA-Z0-9_]+$", frontEndModelText.getText())) {
			updateStatus(I18n.get("wrong_frontend_model"));
			return;
		}
		if (backendModelText.getText().length() != 0 && !Pattern.matches("^[a-zA-Z0-9_]+/[a-zA-Z0-9_]+$", backendModelText.getText())) {
			updateStatus(I18n.get("wrong_backend_model"));
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public void initialize(String group) {
		names = getConfig().getModule().getConfigFieldNames(getConfig().getSectionName(), group);
	}
}