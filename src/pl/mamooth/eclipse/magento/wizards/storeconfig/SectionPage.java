package pl.mamooth.eclipse.magento.wizards.storeconfig;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.UIHelper;

public class SectionPage extends WizardPage {
	private Text moduleText;
	private Text labelText;
	private NewStoreConfigWizard parent;
	private Text tabLabelText;
	private Text aclLabelText;
	private Button shopCheckbox;
	private Button webisteCheckbox;
	private Button defaultCheckbox;
	private Spinner sortOrderTabSpinner;
	private Spinner sortOrderAclSpinner;
	private Spinner sortOrderSectionSpinner;
	private Combo nameCombo;
	private Combo tabNameCombo;
	private AutoCompleteField nameAutoComplete;
	private AutoCompleteField tabNameAutoComplete;
	private String path;
	private MagentoModule module;
	private boolean pageReady = false;

	public SectionPage(NewStoreConfigWizard newStoreConfigWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newStoreConfigWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite labelLabel = new Composite(parent, SWT.NULL);
		GridLayout gl_labelLabel = new GridLayout();
		labelLabel.setLayout(gl_labelLabel);
		gl_labelLabel.numColumns = 3;
		gl_labelLabel.verticalSpacing = 9;
		Label nameLabel;
		Label moduleLabel = new Label(labelLabel, SWT.NULL);
		moduleLabel.setText(I18n.get("module"));

		moduleText = new Text(labelLabel, SWT.BORDER | SWT.SINGLE);
		moduleText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		moduleText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				parsePath(moduleText.getText(), false);
			}
		});
		moduleText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (path != null)
					moduleText.setText(path);
			}

			@Override
			public void focusLost(FocusEvent e) {
				parsePath(moduleText.getText(), true);
			}
		});

		Button borwseModuleButton = new Button(labelLabel, SWT.NONE);
		borwseModuleButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseContainer();
			}
		});
		borwseModuleButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		borwseModuleButton.setText(I18n.get("browse_module"));
		nameLabel = new Label(labelLabel, SWT.NULL);
		nameLabel.setText(I18n.get("name"));
		setControl(labelLabel);

		nameCombo = new Combo(labelLabel, SWT.NONE);
		nameAutoComplete = new AutoCompleteField(nameCombo, new ComboContentAdapter(), new String[] {});
		nameCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameChanged();
			}
		});
		nameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(labelLabel, SWT.NONE);

		Label lblClassName = new Label(labelLabel, SWT.NONE);
		lblClassName.setText(I18n.get("label"));

		labelText = new Text(labelLabel, SWT.BORDER);
		labelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				labelChanged();
			}
		});
		labelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(labelLabel, SWT.NONE);

		Label visibleLablel = new Label(labelLabel, SWT.NONE);
		visibleLablel.setText(I18n.get("visible"));

		SashForm sashForm = new SashForm(labelLabel, SWT.NONE);
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

		webisteCheckbox = new Button(sashForm, SWT.CHECK);
		webisteCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				websiteChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				websiteChanged();
			}
		});
		webisteCheckbox.setText(I18n.get("website"));

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
		new Label(labelLabel, SWT.NONE);

		Label aclLabelLabel = new Label(labelLabel, SWT.NONE);
		aclLabelLabel.setText(I18n.get("acl_label"));

		aclLabelText = new Text(labelLabel, SWT.BORDER);
		aclLabelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				aclLabelChanged();
			}
		});
		aclLabelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(labelLabel, SWT.NONE);

		Label tabNameLabel = new Label(labelLabel, SWT.NONE);
		tabNameLabel.setText(I18n.get("tab_name"));

		tabNameCombo = new Combo(labelLabel, SWT.NONE);
		tabNameAutoComplete = new AutoCompleteField(tabNameCombo, new ComboContentAdapter(), new String[] {});
		tabNameCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tabNameChanged();
			}
		});
		tabNameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(labelLabel, SWT.NONE);

		Label tabLabelLabel = new Label(labelLabel, SWT.NONE);
		tabLabelLabel.setText(I18n.get("tab_label"));

		tabLabelText = new Text(labelLabel, SWT.BORDER);
		tabLabelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tabLabelChanged();
			}
		});
		tabLabelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(labelLabel, SWT.NONE);

		Label sortOrderLabel = new Label(labelLabel, SWT.NONE);
		sortOrderLabel.setText(I18n.get("sortorder"));

		SashForm sashForm_1 = new SashForm(labelLabel, SWT.NONE);
		sashForm_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Label sortOrderSectionLabel = new Label(sashForm_1, SWT.NONE);
		sortOrderSectionLabel.setAlignment(SWT.RIGHT);
		sortOrderSectionLabel.setText(I18n.get("so_section"));

		sortOrderSectionSpinner = new Spinner(sashForm_1, SWT.BORDER);
		sortOrderSectionSpinner.setMaximum(10000);
		sortOrderSectionSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				sectionSortorderChanged();
			}
		});

		Label sortOrderAclLabel = new Label(sashForm_1, SWT.NONE);
		sortOrderAclLabel.setAlignment(SWT.RIGHT);
		sortOrderAclLabel.setText(I18n.get("so_acl"));

		sortOrderAclSpinner = new Spinner(sashForm_1, SWT.BORDER);
		sortOrderAclSpinner.setMaximum(10000);
		sortOrderAclSpinner.setEnabled(false);
		sortOrderAclSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				aclSortorderChanged();
			}
		});

		Label sortOrderTabLabel = new Label(sashForm_1, SWT.NONE);
		sortOrderTabLabel.setAlignment(SWT.RIGHT);
		sortOrderTabLabel.setText(I18n.get("so_tab"));

		sortOrderTabSpinner = new Spinner(sashForm_1, SWT.BORDER);
		sortOrderTabSpinner.setMaximum(10000);
		sortOrderTabSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				tabSortorderChanged();
			}
		});
		sashForm_1.setWeights(new int[] { 1, 1, 1, 1, 1, 1 });
		new Label(labelLabel, SWT.NONE);
		pageReady = true;
		initalize();
	}

	protected void tabSortorderChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					tabSortorderChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setTabSortOrder(sortOrderTabSpinner.getSelection());
	}

	protected void aclSortorderChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					aclSortorderChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setAclSortOrder(sortOrderAclSpinner.getSelection());
	}

	protected void sectionSortorderChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					sectionSortorderChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setSectionSortOrder(sortOrderSectionSpinner.getSelection());
	}

	protected void tabLabelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					tabLabelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setTabLabel(tabLabelText.getText());
		dialogChanged();
	}

	protected void tabNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					tabNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setTabName(tabNameCombo.getText());
		if (UIHelper.comboSelected(tabNameCombo, false)) {
			tabLabelText.setEnabled(false);
			sortOrderTabSpinner.setEnabled(false);
			getConfig().setTabExists(true);
			TabData tab = module.getConfigTab(tabNameCombo.getText());
			if (tab != null) {
				tabLabelText.setText(tab.getLabel());
				sortOrderTabSpinner.setSelection(tab.getSortOrder());
			} else {
				tabLabelText.setText("");
				sortOrderTabSpinner.setSelection(0);
			}
		} else {
			tabLabelText.setEnabled(true);
			sortOrderTabSpinner.setEnabled(true);
			getConfig().setTabExists(false);
		}
		dialogChanged();
	}

	protected void aclLabelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					aclLabelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setAclTitle(aclLabelText.getText());
		if (aclLabelText.getText().length() == 0) {
			sortOrderAclSpinner.setEnabled(false);
		} else {
			sortOrderAclSpinner.setEnabled(true);
		}
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

		getConfig().setSectionShowStore(shopCheckbox.getSelection());
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

		getConfig().setSectionShowWebsite(webisteCheckbox.getSelection());
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

		getConfig().setSectionShowDefault(defaultCheckbox.getSelection());
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

		getConfig().setSectionLabel(labelText.getText());
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

		getConfig().setSectionName(nameCombo.getText());
		getConfig().setAclName(nameCombo.getText());
		parent.getGroupPage().initialize(module, nameCombo.getText());
		SectionData section = module.getConfigSection(nameCombo.getText());
		if (section != null) {
			labelText.setText(section.getLabel());
			sortOrderSectionSpinner.setSelection(section.getSortOrder());
			sortOrderAclSpinner.setSelection(section.getAclSortOrder());
			defaultCheckbox.setSelection(section.isVisibleDefault());
			webisteCheckbox.setSelection(section.isVisibleWebsite());
			shopCheckbox.setSelection(section.isVisibleStore());
			aclLabelText.setText(section.getAclLabel());
			tabNameCombo.setText(section.getTab());
		} else {
			labelText.setText("");
			sortOrderSectionSpinner.setSelection(0);
			sortOrderAclSpinner.setSelection(0);
			aclLabelText.setText("");
			tabNameCombo.setText("");
			defaultCheckbox.setSelection(false);
			webisteCheckbox.setSelection(false);
			shopCheckbox.setSelection(false);
		}
		dialogChanged();
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
	}

	protected void browseContainer() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					browseContainer();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, I18n.get("select_module"));
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				parsePath(((Path) result[0]).toString(), true);
			}
		}
	}

	protected void initalize() {
		if (getSelection() != null) {
			IResource resource = ResourceHelper.getResource(getSelection());
			if (resource != null) {
				IProject proj = resource.getProject();
				IPath path = resource.getProjectRelativePath();
				if (proj != null) {
					String resourcePath = IPath.SEPARATOR + proj.getName() + IPath.SEPARATOR + path.toPortableString();
					parsePath(resourcePath, true);
				}
			}
		}
	}

	protected void parsePath(String path, boolean modifySelection) {
		if (path.startsWith("/"))
			this.path = path;
		try {
			module = ModuleHelper.getModuleFromPath(path);
		} catch (CoreException e) {
			module = null;
		}
		if (module != null) {
			if (modifySelection)
				moduleText.setText(module.getModuleName());
			dialogChanged();
			getConfig().setModule(module);
			getConfig().setHelper(module.getHelperGroupName());
			parent.getFiledPage().initialize(module);
			initilize(module);
		} else {
			try {
				if (ModuleHelper.moduleCountAviableInPath(path) > 1) {
					updateStatus(I18n.get("many_modules_in_path"), true);
				} else {
					updateStatus(I18n.get("no_modules_in_path"), true);
				}
			} catch (CoreException e) {
				updateStatus(I18n.get("no_modules_in_path"), true);
			}
		}
	}

	private void initilize(MagentoModule module) {
		String[] sections = module.getConfigSections();
		String[] tabs = module.getConfigTabs();
		nameCombo.setItems(sections);
		nameAutoComplete.setProposals(sections);
		tabNameCombo.setItems(tabs);
		tabNameAutoComplete.setProposals(tabs);
	}

	protected void containerChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					containerChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		parsePath(moduleText.getText(), false);
		dialogChanged();
	}

	protected void dialogChanged() {
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

		shopChanged();
		websiteChanged();
		defaultChanged();
		aclSortorderChanged();
		tabSortorderChanged();

		if (module == null)
			return;
		if (!Pattern.matches("^[a-zA-Z0-9_]+$", nameCombo.getText())) {
			updateStatus(I18n.get("wrong_name"), true);
			return;
		}
		if (!Pattern.matches("^[a-zA-Z0-9_]+$", tabNameCombo.getText())) {
			updateStatus(I18n.get("wrong_tab_name"), true);
			return;
		}
		if (labelText.getText().length() == 0) {
			updateStatus(I18n.get("empty_label"), true);
			return;
		}
		if (tabLabelText.getText().length() == 0) {
			updateStatus(I18n.get("empty_tab_label"), true);
			return;
		}
		updateStatus(null, false);
	}

	protected ISelection getSelection() {
		return parent.getSelection();
	}

	private void updateStatus(String message, boolean error) {
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

}