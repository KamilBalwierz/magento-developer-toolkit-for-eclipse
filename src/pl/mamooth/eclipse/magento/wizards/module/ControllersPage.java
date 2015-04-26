package pl.mamooth.eclipse.magento.wizards.module;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.helpers.I18n;

public class ControllersPage extends WizardPage {

	private Text textControllersGroup;
	private Text textAdminControlersGroup;
	private Button btnControllersGroup;
	private Button btnAdminControllersGroup;
	private NewModuleWizard parent;
	private boolean pageReady = false;

	public ControllersPage(NewModuleWizard parent) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		this.parent = parent;
	}

	public void notifyShortName(final String shortName) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					notifyShortName(shortName);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		textControllersGroup.setText(shortName);
		textAdminControlersGroup.setText("admin_" + shortName);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		layout.verticalSpacing = 9;
		dialogChanged();
		setControl(container);

		btnControllersGroup = new Button(container, SWT.CHECK);
		btnControllersGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnControllersGroup.setSelection(true);
		btnControllersGroup.setText(I18n.get("controllers"));
		new Label(container, SWT.NONE);

		Label lblFrontname = new Label(container, SWT.NONE);
		lblFrontname.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFrontname.setText(I18n.get("controllers_frontname"));

		textControllersGroup = new Text(container, SWT.BORDER);
		textControllersGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textControllersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnAdminControllersGroup = new Button(container, SWT.CHECK);
		btnAdminControllersGroup.setSelection(true);
		btnAdminControllersGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnAdminControllersGroup.setText(I18n.get("admin_controllers"));
		new Label(container, SWT.NONE);

		Label lblFrontname_1 = new Label(container, SWT.NONE);
		lblFrontname_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFrontname_1.setText(I18n.get("admin_controllers_frontname"));

		textAdminControlersGroup = new Text(container, SWT.BORDER);
		textAdminControlersGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textAdminControlersGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pageReady = true;
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

		Configuration configuration = parent.getConfiguration();
		if (textAdminControlersGroup == null)
			return;
		if (btnControllersGroup.getSelection()) {
			textControllersGroup.setEnabled(true);
			if (textControllersGroup.getText().length() == 0) {
				updateStatus(I18n.get("controllers"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textControllersGroup.getText())) {
				updateStatus(I18n.get("wrong_controllers"));
				return;
			}
		} else {
			textControllersGroup.setEnabled(false);
		}
		if (btnAdminControllersGroup.getSelection()) {
			textAdminControlersGroup.setEnabled(true);
			if (textAdminControlersGroup.getText().length() == 0) {
				updateStatus(I18n.get("admin_controllers"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textAdminControlersGroup.getText())) {
				updateStatus(I18n.get("wrong_admin_contrllers"));
				return;
			}
		} else {
			textAdminControlersGroup.setEnabled(false);
		}
		configuration.setDefaultControllers(btnControllersGroup.getSelection());
		configuration.setDefaultControllersFrontName(textControllersGroup.getText());
		configuration.setAdminControllers(btnAdminControllersGroup.getSelection());
		configuration.setAdminControllersFrontName(textAdminControlersGroup.getText());
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}