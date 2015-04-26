package pl.mamooth.eclipse.magento.wizards.modman;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;

public class NamePage extends WizardPage {
	private NewModmanWizard parent;
	private Text containerText;
	private Button contaierBrowseButton;
	private Label containerLabel;
	private MagentoModule module;
	private String path = "";
	private boolean pageReady = false;
	private CheckboxTreeViewer tree;

	public CheckboxTreeViewer getTree() {
		return tree;
	}

	NamePage(NewModmanWizard parent) {
		super("wizardHelperPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));

		this.parent = parent;
	}

	@Override
	public void createControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "pl.mamooth.eclipse.magento.modman_wizard");
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

		setControl(container);

		Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(I18n.get("contents"));

		tree = new CheckboxTreeViewer(container, SWT.BORDER | SWT.CHECK);
		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tree.setContentProvider(new WorkbenchContentProvider());
		tree.setLabelProvider(new WorkbenchLabelProvider());
		tree.setAutoExpandLevel(5);

		tree.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				IResource resource = (IResource) event.getElement();
				try {
					grayThemOut(resource, event.getChecked());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				tree.setSubtreeChecked(resource, event.getChecked());
			}
		});
		new Label(container, SWT.NONE);

		pageReady = true;
		initalize();
	}

	protected void grayThemOut(IResource resource, boolean gray) throws CoreException {
		if (resource.getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) resource;
			for (IResource child : folder.members()) {
				tree.setGrayed(child, gray);
				grayThemOut(child, gray);
			}
		}
	}

	protected Configuration getConfig() {
		return parent.getConfiguration();
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

	protected void checkFiles(IResource element) throws CoreException {
		if (element.getType() == IResource.FILE) {
			tree.setChecked(element, true);
		} else if (element.getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) element;
			for (IResource child : folder.members()) {
				checkFiles(child);
			}
		}
	}

	protected void checkDefault() throws CoreException {
		tree.setChecked(module.getModuleXml(), true);
		String[] appPath = module.getAppFolderPath();
		IFolder folder = ResourceHelper.getFolder(module.getProject(), StringHelper.concat(appPath, new String[] { FolderHelper.CODE_FOLDER, module.getCodePool(), module.getVendor(), module.getName() }), null);
		grayThemOut(folder, true);
		tree.setSubtreeChecked(folder, true);
		tree.setChecked(folder, true);
		String scope = "frontend";
		String[] designPath = new String[] { FolderHelper.DESIGN_FOLDER, scope, module.getDesignPackage(scope), module.getDesignTemplate(scope), FolderHelper.TEMPLATE_FOLDER, module.getShortName() };
		designPath = StringHelper.concat(appPath, designPath);
		if (ResourceHelper.folderExists(module.getProject(), designPath)) {
			folder = ResourceHelper.getFolder(module.getProject(), designPath, null);
			grayThemOut(folder, true);
			tree.setSubtreeChecked(folder, true);
			tree.setChecked(folder, true);
		}
		scope = "adminhtml";
		designPath = new String[] { FolderHelper.DESIGN_FOLDER, scope, module.getDesignPackage(scope), module.getDesignTemplate(scope), FolderHelper.TEMPLATE_FOLDER, module.getShortName() };
		designPath = StringHelper.concat(appPath, designPath);
		if (ResourceHelper.folderExists(module.getProject(), designPath)) {
			folder = ResourceHelper.getFolder(module.getProject(), designPath, null);
			grayThemOut(folder, true);
			tree.setSubtreeChecked(folder, true);
			tree.setChecked(folder, true);
		}
		checkFiles((IResource) tree.getInput());
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
			if (modifySelection) {
				containerText.setText(module.getModuleName());
			}
			dialogChanged();
			getConfig().setModule(module);
			try {
				tree.setInput(ResourceHelper.getFolder(module.getProject(), module.getAppFolderPath(), null).getParent());
				checkDefault();
			} catch (CoreException e) {
				e.printStackTrace();
			}
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