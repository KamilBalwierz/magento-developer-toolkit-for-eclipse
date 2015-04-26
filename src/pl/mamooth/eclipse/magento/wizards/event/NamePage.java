package pl.mamooth.eclipse.magento.wizards.event;

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
	private Text functionText;
	private NewEventWizard parent;
	private Text nameText;
	private Text eventNameText;
	private Text observerNameText;
	private Label classNameLabel;
	private String[] aviableEvents;
	private MagentoModule module;
	private String path;
	private boolean pageReady = false;

	public NamePage(NewEventWizard newEventWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newEventWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout gl_container = new GridLayout();
		container.setLayout(gl_container);
		gl_container.numColumns = 3;
		gl_container.verticalSpacing = 9;
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
			}
		});
		browseContainerButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		browseContainerButton.setText(I18n.get(I18n.get("browse_container")));
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

		Label eventNameLabel = new Label(container, SWT.NONE);
		eventNameLabel.setText(I18n.get("event_name"));

		eventNameText = new Text(container, SWT.BORDER);
		eventNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				eventNameChanged();
			}
		});
		eventNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button browseEventsButton = new Button(container, SWT.NONE);
		browseEventsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseEvents();
			}
		});
		browseEventsButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		browseEventsButton.setText(I18n.get("browse_events"));

		Label functionNameLabel = new Label(container, SWT.NONE);
		functionNameLabel.setText(I18n.get("function_name"));

		functionText = new Text(container, SWT.BORDER);
		functionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				functionChanged();
			}
		});
		functionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label observerNameLabel = new Label(container, SWT.NONE);
		observerNameLabel.setText(I18n.get("observer_name"));

		observerNameText = new Text(container, SWT.BORDER);
		observerNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				observerNameChanged();
			}
		});
		observerNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		pageReady = true;
		initalize();
	}

	protected void observerNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					observerNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setObserverName(observerNameText.getText());
		dialogChanged();
	}

	protected void eventNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					eventNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setEventName(eventNameText.getText());
		if (module != null) {
			observerNameText.setText(module.getShortName() + '_' + eventNameText.getText());
		}
		functionText.setText(StringHelper.toCamelCase(eventNameText.getText()));
		dialogChanged();
	}

	protected void functionChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					functionChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFunctionName(functionText.getText());
		dialogChanged();
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
	}

	protected void browseEvents() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
		dialog.setElements(aviableEvents);
		dialog.setTitle(I18n.get("title"));
		dialog.setAllowDuplicates(false);
		dialog.setIgnoreCase(true);
		dialog.setMessage(I18n.get("message"));
		dialog.setMultipleSelection(false);
		if (dialog.open() == ElementListSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				eventNameText.setText(result[0].toString());
			}
		}
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
			String[] elements = name.split("_");
			getConfig().setModelClass(className);
			getConfig().setFileName(elements[elements.length - 1] + FolderHelper.PHP_EXTENSION);
			String[] path;
			try {
				path = module.getModelPath();
			} catch (CoreException e) {
				path = new String[] {};
			}
			if (elements.length > 1) {
				path = StringHelper.concat(path, Arrays.copyOfRange(elements, 0, elements.length - 1));
			}
			getConfig().setFilePath(path);
			getConfig().setModelGroup(module.getModelGroupName());
			getConfig().setModelName(nameText.getText());
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
		getConfig().setType("singleton");

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
			if (name == null) {
				name = "observer";
			}
			if (name.endsWith("_")) {
				name = name + "observer";
			}
			nameText.setText(name);
			nameChanged();
			dialogChanged();
			getConfig().setModule(module);
			new Job(I18n.get("load_aviable_events")) {

				@Override
				public IStatus run(IProgressMonitor monitor) {
					try {
						aviableEvents = module.getAviableEvents();
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
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_]*$", eventNameText.getText())) {
			updateStatus(I18n.get("illegal_event_name"), true);
			return;
		}
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_]*$", functionText.getText())) {
			updateStatus(I18n.get("illegal_function_name"), true);
			return;
		}
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_]*$", observerNameText.getText())) {
			updateStatus(I18n.get("illegal_observer_name"), true);
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