package pl.mamooth.eclipse.magento.wizards.cron;

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
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;

public class NamePage extends WizardPage {
	private Text containerText;
	private Text functionNameText;
	private NewCronWizard parent;
	private Text nameText;
	private Text taskNameText;
	private Text cronExprText;
	private Label classNameLabel;
	private MagentoModule module;
	private String path;
	private boolean pageReady = false;

	public NamePage(NewCronWizard newCronWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newCronWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label nameLabel;
		Label contarinerLabel = new Label(container, SWT.NULL);
		contarinerLabel.setText(I18n.get("container"));

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

		Button containerBrowseButton = new Button(container, SWT.NONE);
		containerBrowseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				browseContainer();
			}
		});
		containerBrowseButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		containerBrowseButton.setText(I18n.get("browse"));
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

		Label functionNameLabel = new Label(container, SWT.NONE);
		functionNameLabel.setText(I18n.get("function_name"));

		functionNameText = new Text(container, SWT.BORDER);
		functionNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				functionNameChanged();
			}
		});
		functionNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label taskNameLabel = new Label(container, SWT.NONE);
		taskNameLabel.setText(I18n.get("task_name"));

		taskNameText = new Text(container, SWT.BORDER);
		taskNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				taskNameChanged();
			}
		});
		taskNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		Label cronExprLabel = new Label(container, SWT.NONE);
		cronExprLabel.setText(I18n.get("cron_expr"));

		cronExprText = new Text(container, SWT.BORDER);
		cronExprText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				cronExprChanged();
			}
		});
		cronExprText.setText("* * * * * *");
		cronExprText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		pageReady = true;
		initalize();
	}

	protected void cronExprChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					cronExprChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setCronExpr(cronExprText.getText());
		dialogChanged();
	}

	protected void taskNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					taskNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setTaskName(taskNameText.getText());
		dialogChanged();
	}

	protected void functionNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					functionNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setFunctionName(functionNameText.getText());
		if (module != null) {
			StringBuilder name = new StringBuilder(module.getShortName()).append('_');
			String tmp = functionNameText.getText();
			for (int i = 0; i < tmp.length(); ++i) {
				if (Character.isUpperCase(tmp.charAt(i))) {
					name.append('_');
					name.append(Character.toLowerCase(tmp.charAt(i)));
				} else {
					name.append(tmp.charAt(i));
				}
			}
			taskNameText.setText(name.toString());
		}
		dialogChanged();
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
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
			getConfig().setClassName(className);
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
				name = "cron";
			}
			if (name.endsWith("_")) {
				name = name + "cron";
			}
			nameText.setText(name);
			nameChanged();
			dialogChanged();
			getConfig().setModule(module);
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
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_]*$", functionNameText.getText())) {
			updateStatus(I18n.get("illegal_function_name"), true);
			return;
		}
		if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9_]*$", taskNameText.getText())) {
			updateStatus(I18n.get("illegal_task_name"), true);
			return;
		}
		if (!Pattern.matches("(((([0-9]|[0-5][0-9]),)*([0-9]|[0-5][0-9]))|(([0-9]|[0-5][0-9])(/|-)([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-5][0-9]),)*([0-9]|[0-5][0-9]))|(([0-9]|[0-5][0-9])(/|-)([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-1][0-9]|[2][0-3]),)*([0-9]|[0-1][0-9]|[2][0-3]))|(([0-9]|[0-1][0-9]|[2][0-3])(/|-)([0-9]|[0-1][0-9]|[2][0-3]))|([\\?])|([\\*]))[\\s](((([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]),)*([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(/|-)([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(L)|(LW)|([1-9]W)|([1-3][0-9]W)|([\\?])|([\\*]))[\\s](((([1-9]|0[1-9]|1[0-2]),)*([1-9]|0[1-9]|1[0-2]))|(([1-9]|0[1-9]|1[0-2])(/|-)([1-9]|0[1-9]|1[0-2]))|(((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC),)*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-|/)(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|([\\?])|([\\*]))[\\s]((([1-7],)*([1-7]))|([1-7](/|-)([1-7]))|(((MON|TUE|WED|THU|FRI|SAT|SUN),)*(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|((MON|TUE|WED|THU|FRI|SAT|SUN)(-|/)(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|(([1-7]|(MON|TUE|WED|THU|FRI|SAT|SUN))?(L|LW)?)|([1-7]#([1-7])?)|([\\?])|([\\*]))(([\\s]19[7-9][0-9])|([\\s]20[0-9]{2}))?", cronExprText.getText())) {
			updateStatus(I18n.get("cronexpr_incorrent"), true);
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