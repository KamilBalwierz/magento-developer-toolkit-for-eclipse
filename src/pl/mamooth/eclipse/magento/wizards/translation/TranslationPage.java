package pl.mamooth.eclipse.magento.wizards.translation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.UIHelper;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (csv).
 */

public class TranslationPage extends WizardPage {
	private Text containerText;

	private ISelection selection;
	private Combo comboLanguage;
	private Button btnFrontend;
	private Button btnAdminhtml;
	private Label labelLanguageCode;
	private MagentoModule module;
	private boolean pageReady = false;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public TranslationPage(ISelection selection) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(I18n.get("project"));

		String project = "";
		if (selection != null) {
			IResource resource = ResourceHelper.getResource(selection);
			if (resource != null) {
				IProject proj = resource.getProject();
				IPath path = resource.getProjectRelativePath();
				if (proj != null) {
					String resourcePath = proj.getName() + IPath.SEPARATOR + path.toPortableString();
					try {
						module = ModuleHelper.getModuleFromPath(resourcePath);
					} catch (CoreException e1) {
						module = null;
					}
					if (module == null) {
						project = resourcePath;
					} else {
						project = module.getModuleName();
					}
				}
			}
		}

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		containerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		containerText.setText(project);
		containerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText(I18n.get("browse"));
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);

		Label lblLanguage = new Label(container, SWT.NONE);
		lblLanguage.setText(I18n.get("language"));

		comboLanguage = new Combo(container, SWT.NONE);
		new AutoCompleteField(comboLanguage, new ComboContentAdapter(), ComboSource.getLanguagesLabels());
		comboLanguage.setItems(ComboSource.getLanguagesLabels());
		comboLanguage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		comboLanguage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		labelLanguageCode = new Label(container, SWT.NONE);
		labelLanguageCode.setAlignment(SWT.CENTER);
		labelLanguageCode.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		new Label(container, SWT.NONE);

		btnFrontend = new Button(container, SWT.CHECK);
		btnFrontend.setSelection(true);
		btnFrontend.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnFrontend.setText(I18n.get("frontend"));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		btnAdminhtml = new Button(container, SWT.CHECK);
		btnAdminhtml.setSelection(true);
		btnAdminhtml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnAdminhtml.setText(I18n.get("adminhtml"));
		new Label(container, SWT.NONE);
		pageReady = true;
		dialogChanged();
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					initialize();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		if (selection.isEmpty() == false && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, I18n.get("select_module"));
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

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

		if (!UIHelper.comboSelected(comboLanguage)) {
			updateStatus(I18n.get("select_language"));
			return;
		}
		labelLanguageCode.setText(getLanguage());

		if (module != null && !getContainerName().equals(module.getModuleName())) {
			module = null;
		}
		if (module == null) {
			try {
				int count = ModuleHelper.moduleCountAviableInPath(getContainerName());
				if (count == 0) {
					updateStatus(I18n.get("select_project"));
					return;
				} else if (count == 1) {
					module = ModuleHelper.getModuleFromPath(getContainerName());
				} else {
					updateStatus(I18n.get("select_project_multiple"));
					return;
				}
			} catch (CoreException e) {
				module = null;
			}
		}
		if (module != null && !getContainerName().equals(module.getModuleName())) {
			containerText.setText(module.getModuleName());
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public MagentoModule getModule() {
		return module;
	}

	public String getLanguage() {
		return ComboSource.getLanguageCode(UIHelper.getComboIndex(comboLanguage));
	}

	public boolean createFrontend() {
		return btnFrontend.getSelection();
	}

	public boolean createAdminhtml() {
		return btnAdminhtml.getSelection();
	}
}