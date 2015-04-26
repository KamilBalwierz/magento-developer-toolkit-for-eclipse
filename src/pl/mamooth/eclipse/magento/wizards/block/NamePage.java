package pl.mamooth.eclipse.magento.wizards.block;

import java.util.Arrays;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.UIHelper;

public class NamePage extends WizardPage {
	private Text containerText;
	private Text extendsText;
	private Text nameText;
	private Label classNameLabel;
	private Button rewriteCheck;
	private Combo scopeCombo;
	private Text packageText;
	private Text templateText;
	private NewBlockWizard parent;
	private String[] aviableClasses;
	private MagentoModule module;
	private String path;
	private boolean pageReady = false;

	public NamePage(NewBlockWizard newBlockWizard) {
		super("wizardBlockPage");
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
		Label nameLabel;
		Label containerLabel = new Label(container, SWT.NULL);
		containerLabel.setText(I18n.get("container"));

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		containerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		containerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				containerChanged();
			}
		});
		containerText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (path != null)
					containerText.setText(path);
			}

			@Override
			public void focusLost(FocusEvent e) {
				parsePath(containerText.getText(), true);
			}
		});

		Button groupAddButton = new Button(container, SWT.NONE);
		groupAddButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseContainer();
			}
		});
		groupAddButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		groupAddButton.setText(I18n.get("container_borwse"));
		nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(I18n.get("name"));
		setControl(container);

		nameText = new Text(container, SWT.BORDER);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameChanged();
			}
		});
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label classNameLabelLabel = new Label(container, SWT.NONE);
		classNameLabelLabel.setText(I18n.get("class_name"));

		classNameLabel = new Label(container, SWT.NONE);
		classNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		classNameLabel.setEnabled(false);
		new Label(container, SWT.NONE);

		Label extendsLabel = new Label(container, SWT.NONE);
		extendsLabel.setText(I18n.get("extends"));

		extendsText = new Text(container, SWT.BORDER);
		extendsText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				extendsChanged();
			}
		});
		extendsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button extendsBrowseButton = new Button(container, SWT.NONE);
		extendsBrowseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseExtends();
			}
		});
		extendsBrowseButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		extendsBrowseButton.setText(I18n.get("browse_extends"));
		new Label(container, SWT.NONE);

		rewriteCheck = new Button(container, SWT.CHECK);
		rewriteCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				rewriteChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				rewriteChanged();
			}
		});
		rewriteCheck.setText(I18n.get("rewrite"));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label scopeLabel = new Label(container, SWT.NONE);
		scopeLabel.setText(I18n.get("scope"));

		scopeCombo = new Combo(container, SWT.READ_ONLY);
		scopeCombo.setItems(new String[] { "frontend", "adminhtml" });
		scopeCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				scopeChanged();
			}
		});
		scopeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scopeCombo.select(0);
		new Label(container, SWT.NONE);

		Label packageLabel = new Label(container, SWT.NONE);
		packageLabel.setText(I18n.get("package"));

		packageText = new Text(container, SWT.BORDER);
		packageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		packageText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				packageChanged();
			}
		});
		new Label(container, SWT.NONE);

		Label templateLabel = new Label(container, SWT.NONE);
		templateLabel.setText(I18n.get("template"));

		templateText = new Text(container, SWT.BORDER);
		templateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		templateText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				templateChanged();
			}
		});
		new Label(container, SWT.NONE);
		pageReady = true;
		initalize();

	}

	protected void templateChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					templateChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setDesignTemplate(templateText.getText());
		dialogChanged();
		parent.getTemplatePage().fileNameChanged();
	}

	protected void packageChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					packageChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setDesignPackage(packageText.getText());
		dialogChanged();
		parent.getTemplatePage().fileNameChanged();
	}

	protected void scopeChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					scopeChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setDesignScope(scopeCombo.getText());
		if (module != null) {
			templateText.setText(module.getDesignTemplate(scopeCombo.getText()));
			packageText.setText(module.getDesignPackage(scopeCombo.getText()));
		}
		if (module != null && UIHelper.comboSelected(scopeCombo, false)) {
			parent.getTemplatePage().scopeChanged(scopeCombo.getText(), module);
		}
		dialogChanged();
		parent.getTemplatePage().fileNameChanged();
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
	}

	protected void rewriteChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					rewriteChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setRewrite(rewriteCheck.getSelection());
		dialogChanged();
	}

	protected void browseExtends() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setElements(aviableClasses);
		dialog.setTitle(I18n.get("title"));
		dialog.setAllowDuplicates(false);
		dialog.setIgnoreCase(true);
		dialog.setMessage(I18n.get("message"));
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				extendsText.setText(result[0].toString());
			}
		}
	}

	protected void extendsChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					extendsChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setExtendsClassName(extendsText.getText());
		try {
			String id = module.getMagentoClassIdentifier(extendsText.getText());
			String[] identify = id.split("/");
			if (identify.length == 2) {
				getConfig().setExtendsGroupName(identify[0]);
				getConfig().setExtendsBlockName(identify[1]);
			} else {
				getConfig().setExtendsGroupName("");
				getConfig().setExtendsBlockName("");
			}
		} catch (Exception e) {
			getConfig().setExtendsGroupName("");
			getConfig().setExtendsBlockName("");
		}
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

		if (module != null && containerText.getText().length() > 0 && nameText.getText().length() > 0) {
			String name = StringHelper.toCamelCase(nameText.getText(), true, true);
			String className = module.getBlockClassBase() + '_' + name;
			classNameLabel.setText(className);
			String[] elements = name.split("_");
			getConfig().setClassName(className);
			getConfig().setFileNname(elements[elements.length - 1] + FolderHelper.PHP_EXTENSION);
			String[] path;
			try {
				path = module.getBlockPath();
			} catch (CoreException e) {
				path = new String[] {};
			}
			if (elements.length > 1) {
				path = StringHelper.concat(path, Arrays.copyOfRange(elements, 0, elements.length - 1));
			}
			getConfig().setFilePath(path);
			getConfig().setBlockModel(nameText.getText());
			getConfig().setBlockGroup(module.getBlockGroupName());
			parent.getTemplatePage().blockNameChanged(nameText.getText());
		}
		dialogChanged();
	}

	protected void browseContainer() {
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
		extendsText.setText("Mage_Core_Block_Abstract");
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
				containerText.setText(module.getModuleName());
			String name;
			try {
				name = module.getBlockNameFromPath(path);
			} catch (CoreException e) {
				name = null;
			}
			if (name != null) {
				nameText.setText(name);
			}
			nameChanged();
			dialogChanged();
			getConfig().setModule(module);
			scopeChanged();
			parent.getTemplatePage().initalize(module);
			new Job(I18n.get("loading_extandable_blocks")) {

				@Override
				public IStatus run(IProgressMonitor monitor) {
					try {
						aviableClasses = module.getBlockClasses();
						return Status.OK_STATUS;
					} catch (Exception e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
				}
			}.schedule();
			scopeChanged();
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

		parsePath(containerText.getText(), false);
		dialogChanged();
	}

	public void notifyTemplateCreation(final boolean creating) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					notifyTemplateCreation(creating);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (creating) {
			if (extendsText.getText().equals("Mage_Core_Block_Abstract")) {
				extendsText.setText("Mage_Core_Block_Template");
			}
		} else {
			if (extendsText.getText().equals("Mage_Core_Block_Template")) {
				extendsText.setText("Mage_Core_Block_Abstract");
			}
		}
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

		if (module == null)
			return;
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)*$", nameText.getText())) {
			updateStatus(I18n.get("illegal_name"), true);
			return;
		}
		if (ResourceHelper.fileExists(module.getProject(), getConfig().getFilePath(), getConfig().getFileNname())) {
			updateStatus(I18n.get("file_exists"), true);
			return;
		}

		if (getConfig().getExtendsGroupName() == null || getConfig().getExtendsBlockName() == null || getConfig().getExtendsGroupName().equals("") || getConfig().getExtendsBlockName().equals("")) {
			try {
				String id = module.getMagentoClassIdentifier(extendsText.getText());
				String[] identify = id.split("/");
				if (identify.length == 2) {
					getConfig().setExtendsGroupName(identify[0]);
					getConfig().setExtendsBlockName(identify[1]);
				} else {
					getConfig().setExtendsGroupName("");
					getConfig().setExtendsBlockName("");
				}
			} catch (Exception e) {
				getConfig().setExtendsGroupName("");
				getConfig().setExtendsBlockName("");
			}
		}
		if (templateText.getText().length() == 0) {
			updateStatus(I18n.get("empty_template"), true);
			return;
		}
		if (packageText.getText().length() == 0) {
			updateStatus(I18n.get("empty_package"), true);
			return;
		}
		if ((getConfig().getExtendsGroupName().equals("") || getConfig().getExtendsBlockName().equals("")) && rewriteCheck.getSelection()) {
			rewriteCheck.setSelection(false);
			getConfig().setRewrite(false);
			updateStatus(I18n.get("cannot_rewrite"), false);
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