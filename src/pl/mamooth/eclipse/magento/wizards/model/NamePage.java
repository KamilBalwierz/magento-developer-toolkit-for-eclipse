package pl.mamooth.eclipse.magento.wizards.model;

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
	private Text containerText;
	private Text extendsText;
	private NewModelWizard parent;
	private Text nameText;
	private Text eventPrefixText;
	private Text eventObejctText;
	private Label classNameLabel;
	private Button resourceModelCheckbox;
	private Button rewriteCheckbox;
	private Button collectionCheckbox;
	private String[] aviableClasses;
	private MagentoModule module;
	private String path;
	private boolean pageReady = false;

	public NamePage(NewModelWizard newModelWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newModelWizard;
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

		Button browseContainerButton = new Button(container, SWT.NONE);
		browseContainerButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseContainer();
			}
		});
		browseContainerButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		browseContainerButton.setText(I18n.get("container_borwse"));
		nameLabel = new Label(container, SWT.NULL);
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
		extendsText.setText("Mage_Core_Model_Abstract");
		extendsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button browseExtendsButton = new Button(container, SWT.NONE);
		browseExtendsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseExtends();
			}
		});
		browseExtendsButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		browseExtendsButton.setText(I18n.get("browse_extends"));
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

		Label eventPrefixLabel = new Label(container, SWT.NONE);
		eventPrefixLabel.setText(I18n.get("event_prefix"));

		eventPrefixText = new Text(container, SWT.BORDER);
		eventPrefixText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				eventPrefixChanged();
			}
		});
		eventPrefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label eventObjectLabel = new Label(container, SWT.NONE);
		eventObjectLabel.setText(I18n.get("event_object"));

		eventObejctText = new Text(container, SWT.BORDER);
		eventObejctText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				eventObjectChanged();
			}
		});
		eventObejctText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		resourceModelCheckbox = new Button(container, SWT.CHECK);
		resourceModelCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				resourceModelChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				resourceModelChanged();
			}
		});
		resourceModelCheckbox.setText(I18n.get("resource_model"));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		collectionCheckbox = new Button(container, SWT.CHECK);
		collectionCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				colltionChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				colltionChanged();
			}
		});
		collectionCheckbox.setText(I18n.get("collection"));
		collectionCheckbox.setEnabled(false);
		new Label(container, SWT.NONE);
		pageReady = true;
		initalize();
		setControl(container);

	}

	protected void colltionChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					colltionChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setCollection(collectionCheckbox.getSelection());
		dialogChanged();
	}

	protected void resourceModelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					resourceModelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setResourceModel(resourceModelCheckbox.getSelection());
		parent.getModelPage().activeChanged(resourceModelCheckbox.getSelection());
		collectionCheckbox.setEnabled(resourceModelCheckbox.getSelection());
		if (!collectionCheckbox.getEnabled() && collectionCheckbox.getSelection()) {
			collectionCheckbox.setSelection(false);
		}
		dialogChanged();
	}

	protected void eventObjectChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					eventObjectChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (eventObejctText.getText().length() > 0) {
			getConfig().setEventObjectName("'" + eventObejctText.getText() + "'");
		} else {
			getConfig().setEventObjectName("");
		}
		dialogChanged();
	}

	protected void eventPrefixChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					eventPrefixChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (eventPrefixText.getText().length() > 0) {
			getConfig().setEventPrefix("'" + eventPrefixText.getText() + "'");
		} else {
			getConfig().setEventPrefix("");
		}
		dialogChanged();
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
				getConfig().setRewriteName(identify[1]);
			} else {
				getConfig().setRewriteGroup("");
				getConfig().setRewriteName("");
			}
		} catch (Exception e) {
			getConfig().setRewriteGroup("");
			getConfig().setRewriteName("");
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
			String className = module.getModelClassBase() + '_' + name;
			classNameLabel.setText(className);
			String resourceClassName = module.getModelClassBase() + '_' + FolderHelper.RESOUCE_FOLDER + '_' + name;
			String collectionClassName = resourceClassName + "_Collection";
			String[] elements = name.split("_");
			getConfig().setModelGroupName(module.getModelGroupName());
			getConfig().setModelName(nameText.getText());
			getConfig().setClassName(className);
			getConfig().setResourceClassName(resourceClassName);
			getConfig().setResourceExtendsClass("Mage_Core_Model_Resource_Db_Abstract");
			getConfig().setCollectionClassName(collectionClassName);
			getConfig().setCollectionExtendsClass("Mage_Core_Model_Resource_Db_Collection_Abstract");
			getConfig().setFileName(elements[elements.length - 1] + FolderHelper.PHP_EXTENSION);
			getConfig().setResourceFileName(elements[elements.length - 1] + FolderHelper.PHP_EXTENSION);
			getConfig().setCollectionFileName(FolderHelper.COLECTION_FILE);
			String[] path;
			try {
				path = module.getModelPath();
			} catch (CoreException e) {
				path = new String[] {};
			}
			String[] modelPath = path;
			String[] resourcePath = StringHelper.append(path, FolderHelper.RESOUCE_FOLDER);
			String[] collectionPath = StringHelper.append(path, FolderHelper.RESOUCE_FOLDER);
			if (elements.length > 1) {
				modelPath = StringHelper.concat(path, Arrays.copyOfRange(elements, 0, elements.length - 1));
				resourcePath = StringHelper.concat(resourcePath, Arrays.copyOfRange(elements, 0, elements.length - 1));
			}
			if (elements.length > 0) {
				collectionPath = StringHelper.concat(collectionPath, Arrays.copyOfRange(elements, 0, elements.length));
			}
			getConfig().setFilePath(modelPath);
			getConfig().setResourceFilePath(resourcePath);
			getConfig().setCollectionFilePath(collectionPath);
			eventObejctText.setText(nameText.getText());
			eventPrefixText.setText(module.getShortName() + "_" + nameText.getText());
			parent.getModelPage().setTableName(module.getShortName() + "_" + nameText.getText());
		}
		dialogChanged();
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
		extendsText.setText("Mage_Core_Model_Abstract");
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
				name = module.getModelNameFromPath(path);
			} catch (CoreException e) {
				name = null;
			}
			if (name != null) {
				nameText.setText(name);
			}
			nameChanged();
			dialogChanged();
			getConfig().setModule(module);
			parent.getModelPage().initialize(module);
			new Job(I18n.get("load_extends_classes")) {

				@Override
				public IStatus run(IProgressMonitor monitor) {
					try {
						aviableClasses = module.getModelClasses();
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

		if (eventObejctText.getText().length() != 0 && !Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)*$", eventObejctText.getText())) {
			updateStatus(I18n.get("illegal_event_object"), true);
			return;
		}
		if (eventPrefixText.getText().length() != 0 && !Pattern.matches("^[a-zA-Z][a-zA-Z0-9]*(_[a-zA-Z0-9]+)*$", eventPrefixText.getText())) {
			updateStatus(I18n.get("illegal_event_prefix"), true);
			return;
		}
		if (getConfig().getRewriteGroup() == null || getConfig().getRewriteName() == null || getConfig().getRewriteGroup().equals("") || getConfig().getRewriteName().equals("")) {
			try {
				String id = module.getMagentoClassIdentifier(extendsText.getText());
				String[] identify = id.split("/");
				if (identify.length == 2) {
					getConfig().setRewriteGroup(identify[0]);
					getConfig().setRewriteName(identify[1]);
				} else {
					getConfig().setRewriteGroup("");
					getConfig().setRewriteName("");
				}
			} catch (Exception e) {
				getConfig().setRewriteGroup("");
				getConfig().setRewriteName("");
			}
		}
		if ((getConfig().getRewriteGroup().equals("") || getConfig().getRewriteName().equals("")) && rewriteCheckbox.getSelection()) {
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