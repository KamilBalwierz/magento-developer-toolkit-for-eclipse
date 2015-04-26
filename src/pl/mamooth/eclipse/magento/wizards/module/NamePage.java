package pl.mamooth.eclipse.magento.wizards.module;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.UIHelper;

public class NamePage extends WizardPage {
	private Text textVendor;
	private Text textName;
	private ISelection selection;
	private Text textShortname;
	private GridData gd_textVendor;
	private GridData gd_textName;
	private Combo comboCodepool;
	private Spinner spinnerMajor;
	private Spinner spinnerMinor;
	private Spinner spinnerChange;
	private Button chckActive;
	private String lastVendor = "";
	private String lastName = "";
	private NewModuleWizard parent;
	private Combo comboBase;
	private boolean pageReady = false;

	public NamePage(NewModuleWizard parent) {
		super("namePage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		this.parent = parent;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		Label lblName;
		Label lblVendor = new Label(container, SWT.NULL);
		lblVendor.setText(I18n.get("vendor"));

		textVendor = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd_textVendor = new GridData(GridData.FILL_HORIZONTAL);
		textVendor.setLayoutData(gd_textVendor);
		textVendor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		lblName = new Label(container, SWT.NULL);
		lblName.setText(I18n.get("name"));

		textName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd_textName = new GridData(GridData.FILL_HORIZONTAL);
		textName.setLayoutData(gd_textName);
		textName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);

		Label lblShortname = new Label(container, SWT.NONE);
		lblShortname.setText(I18n.get("shortname"));

		textShortname = new Text(container, SWT.BORDER);
		textShortname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textShortname.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Label lblCodepool = new Label(container, SWT.NONE);
		lblCodepool.setText(I18n.get("codepool"));

		comboCodepool = new Combo(container, SWT.READ_ONLY);
		comboCodepool.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		comboCodepool.setItems(ComboSource.getCodePools());
		comboCodepool.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboCodepool.select(0);

		Label lblVersion = new Label(container, SWT.NONE);
		lblVersion.setText(I18n.get("version"));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));

		spinnerMajor = new Spinner(composite, SWT.BORDER);
		spinnerMajor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		spinnerMajor.setMaximum(10000);

		Label label = new Label(composite, SWT.NONE);
		label.setText(".");

		spinnerMinor = new Spinner(composite, SWT.BORDER);
		spinnerMinor.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		spinnerMinor.setMaximum(10000);
		spinnerMinor.setSelection(1);

		Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText(".");

		spinnerChange = new Spinner(composite, SWT.BORDER);
		spinnerChange.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		spinnerChange.setMaximum(10000);
		new Label(container, SWT.NONE);

		chckActive = new Button(container, SWT.CHECK);
		chckActive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		chckActive.setSelection(true);
		chckActive.setText(I18n.get("active"));

		Label lblBase = new Label(container, SWT.NONE);
		lblBase.setText(I18n.get("base"));

		comboBase = new Combo(container, SWT.READ_ONLY);
		comboBase.setItems(ComboSource.getBaseVersions());
		comboBase.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		comboBase.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboBase.select(0);
		pageReady = true;
	}

	private void initialize() {
		if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
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
				textVendor.setText(container.getFullPath().toString());
			}
		}
	}

	private void updateShortname() {
		String vendor = textVendor.getText().toLowerCase();
		String name = textName.getText().toLowerCase();
		StringBuilder shortNameBuilder = new StringBuilder();
		shortNameBuilder.append(vendor);
		shortNameBuilder.append('_');
		shortNameBuilder.append(name);
		textShortname.setText(shortNameBuilder.toString());
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

		setErrorMessage(null);
		setMessage(null);
		Configuration configuration = parent.getConfiguration();
		if (textVendor.getText().length() == 0) {
			updateStatus(I18n.get("emptyVendor"));
			return;
		} else if (!Pattern.matches("^[a-zA-Z0-9]+$", textVendor.getText())) {
			updateStatus(I18n.get("wrongVendor"));
			return;
		}
		configuration.setVendor(textVendor.getText());
		if (textName.getText().length() == 0) {
			updateStatus(I18n.get("emptyName"));
			return;
		} else if (!Pattern.matches("^[a-zA-Z0-9]+$", textName.getText())) {
			updateStatus(I18n.get("wrongName"));
			return;
		}
		configuration.setName(textName.getText());
		if (!lastName.equals(textName.getText())) {
			lastName = textName.getText();
			updateShortname();
		}
		if (!lastVendor.endsWith(textVendor.getText())) {
			lastVendor = textVendor.getText();
			updateShortname();
		}
		if (!Pattern.matches("^[a-zA-Z0-9_]+$", textShortname.getText())) {
			updateStatus(I18n.get("wrongShortname"));
			return;
		}
		configuration.setShortName(textShortname.getText());
		String projectName = textVendor.getText() + "_" + textName.getText();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			if (project.getName().equals(projectName)) {
				updateStatus(I18n.get("projectExists", projectName));
				return;
			}
		}
		configuration.setProjectName(projectName);
		if (textShortname.getText().length() == 0) {
			updateStatus(I18n.get("emptyShortname"));
			return;
		}
		if (comboCodepool.getText().length() == 0) {
			updateStatus(I18n.get("emptyCodepool"));
			return;
		}

		configuration.setCodePool(comboCodepool.getText());
		if (!UIHelper.comboSelected(comboCodepool, false)) {
			setMessage(I18n.get("customCodepool", comboCodepool.getText()), WARNING);
			if (!Pattern.matches("^[a-zA-Z0-9_]+$", comboCodepool.getText())) {
				updateStatus(I18n.get("wrongCodepool"));
				return;
			}
		}
		configuration.setVersion(spinnerMajor.getText() + "." + spinnerMinor.getText() + "." + spinnerChange.getText());
		configuration.setActive(chckActive.getSelection());
		if (!UIHelper.comboSelected(comboBase, false)) {
			updateStatus(I18n.get("emptyBase"));
			return;
		}
		configuration.setBase(ComboSource.getBaseVersionPath(UIHelper.getComboIndex(comboBase, false)));
		parent.notifyShortName(textShortname.getText());
		updateStatus(null);
	}

	private void updateStatus(String message) {
		if (message != null)
			setErrorMessage(message);
		setPageComplete(message == null);
	}
}