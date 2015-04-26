package pl.mamooth.eclipse.magento.wizards.helper;

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

public class NamePage extends WizardPage {
	private Text nameText;
	private Text extendsText;
	private NewHelperWizard parent;
	private Button rewriteCheckbox;
	private Text containerText;
	private Button contaierBrowseButton;
	private Label containerLabel;
	private Label classNameLabel;
	private MagentoModule module;
	private String path = "";
	private String[] aviableClasses;
	private boolean pageReady = false;

	NamePage(NewHelperWizard parent) {
		super("wizardHelperPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		this.parent = parent;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		containerLabel = new Label(container, SWT.NONE);
		containerLabel.setText(I18n.get("container"));

		containerText = new Text(container, SWT.BORDER);
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
		containerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				containerChanged();
			}
		});
		containerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		contaierBrowseButton = new Button(container, SWT.NONE);
		contaierBrowseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseContainer();
			}
		});
		contaierBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		contaierBrowseButton.setText(I18n.get("browse_container"));

		Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(I18n.get("name"));

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameChanged();
			}
		});

		new Label(container, SWT.NULL);
		setControl(container);

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

		Button containerExtendsButton = new Button(container, SWT.NONE);
		containerExtendsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseExtends();
			}
		});
		containerExtendsButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		containerExtendsButton.setText(I18n.get("browse_extends"));
		new Label(container, SWT.NONE);

		rewriteCheckbox = new Button(container, SWT.CHECK);
		rewriteCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				rewriteChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				rewriteChanged();
			}
		});
		rewriteCheckbox.setText(I18n.get("rewrite"));
		new Label(container, SWT.NONE);
		pageReady = true;
		initalize();
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

		getConfig().setRewrite(rewriteCheckbox.getSelection());
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

		getConfig().setExtendsClass(extendsText.getText());
		try {
			String id = module.getMagentoClassIdentifier(extendsText.getText());
			String[] identify = id.split("/");
			if (identify.length == 2) {
				getConfig().setRewriteGroup(identify[0]);
				getConfig().setRewriteModel(identify[1]);
			} else {
				getConfig().setRewriteGroup("");
				getConfig().setRewriteModel("");
			}
		} catch (Exception e) {
			getConfig().setRewriteGroup("");
			getConfig().setRewriteModel("");
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
			String className = module.getHelperClassBase() + '_' + name;
			classNameLabel.setText(className);
			String[] elements = name.split("_");
			getConfig().setClassName(className);
			getConfig().setFileName(elements[elements.length - 1] + FolderHelper.PHP_EXTENSION);
			String[] path;
			try {
				path = module.getHelperPath();
			} catch (CoreException e) {
				path = new String[] {};
			}
			if (elements.length > 1) {
				path = StringHelper.concat(path, Arrays.copyOfRange(elements, 0, elements.length - 1));
			}
			getConfig().setFilePath(path);
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
		extendsText.setText("Mage_Core_Helper_Data");
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
				name = module.getHelperNameFromPath(path);
			} catch (CoreException e) {
				name = null;
			}
			if (name != null) {
				nameText.setText(name);
			}
			nameChanged();
			dialogChanged();
			getConfig().setModule(module);
			new Job(I18n.get("load_extends_classes")) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						aviableClasses = module.getHelperClasses();
						return Status.OK_STATUS;
					} catch (Exception e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
				}
			}.schedule();
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
		if (nameText.getText().endsWith("_")) {
			updateStatus(I18n.get("name_cannot_end_with_underscore"), true);
			return;
		}
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)*$", nameText.getText())) {
			updateStatus(I18n.get("illegal_name"), true);
			return;
		}
		if (ResourceHelper.fileExists(module.getProject(), getConfig().getFilePath(), getConfig().getFileName())) {
			updateStatus(I18n.get("file_exists"), true);
			return;
		}

		if (getConfig().getRewriteGroup() == null || getConfig().getRewriteModel() == null || getConfig().getRewriteGroup().equals("") || getConfig().getRewriteModel().equals("")) {
			try {
				String id = module.getMagentoClassIdentifier(extendsText.getText());
				String[] identify = id.split("/");
				if (identify.length == 2) {
					getConfig().setRewriteGroup(identify[0]);
					getConfig().setRewriteModel(identify[1]);
				} else {
					getConfig().setRewriteGroup("");
					getConfig().setRewriteModel("");
				}
			} catch (Exception e) {
				getConfig().setRewriteGroup("");
				getConfig().setRewriteModel("");
			}
		}
		if ((getConfig().getRewriteGroup().equals("") || getConfig().getRewriteModel().equals("")) && rewriteCheckbox.getSelection()) {
			rewriteCheckbox.setSelection(false);
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