package pl.mamooth.eclipse.magento.wizards.module;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

public class ComponentsPage extends WizardPage {

	private Text textModelGroup;
	private Text textResourceModelGroup;
	private Text textBlockGroup;
	private Text textHelperGroup;
	private Button btnCreateModelGroup;
	private Button btnCreateResourceModel;
	private Button btnCreateBlockGroup;
	private Button btnCreateHelperGroup;
	private Button btnCreateDefaltHelper;
	private Button btnCreateSetupScript;
	private Text textSetupScript;
	private NewModuleWizard parent;
	private boolean pageReady = false;

	public ComponentsPage(NewModuleWizard newModuleWizard) {
		super("componentsPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newModuleWizard;
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

		textBlockGroup.setText(shortName);
		textHelperGroup.setText(shortName);
		textModelGroup.setText(shortName);
		textResourceModelGroup.setText(shortName + "_resource");
		textSetupScript.setText(shortName + "_setup");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		layout.verticalSpacing = 9;
		dialogChanged();
		setControl(container);

		btnCreateModelGroup = new Button(container, SWT.CHECK);
		btnCreateModelGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnCreateModelGroup.setSelection(true);
		btnCreateModelGroup.setText(I18n.get("model"));

		textModelGroup = new Text(container, SWT.BORDER);
		textModelGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textModelGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnCreateResourceModel = new Button(container, SWT.CHECK);
		btnCreateResourceModel.setSelection(true);
		btnCreateResourceModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnCreateResourceModel.setText(I18n.get("resource_model"));

		textResourceModelGroup = new Text(container, SWT.BORDER);
		textResourceModelGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textResourceModelGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnCreateSetupScript = new Button(container, SWT.CHECK);
		btnCreateSetupScript.setSelection(true);
		btnCreateSetupScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnCreateSetupScript.setText(I18n.get("setup"));

		textSetupScript = new Text(container, SWT.BORDER);
		textSetupScript.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textSetupScript.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnCreateBlockGroup = new Button(container, SWT.CHECK);
		btnCreateBlockGroup.setSelection(true);
		btnCreateBlockGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnCreateBlockGroup.setText(I18n.get("block"));

		textBlockGroup = new Text(container, SWT.BORDER);
		textBlockGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textBlockGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnCreateHelperGroup = new Button(container, SWT.CHECK);
		btnCreateHelperGroup.setSelection(true);
		btnCreateHelperGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		btnCreateHelperGroup.setText(I18n.get("helper"));

		textHelperGroup = new Text(container, SWT.BORDER);
		textHelperGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textHelperGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);

		btnCreateDefaltHelper = new Button(container, SWT.CHECK);
		btnCreateDefaltHelper.setSelection(true);
		btnCreateDefaltHelper.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				dialogChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		pageReady = true;
		btnCreateDefaltHelper.setText(I18n.get("default_helper"));
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
		if (btnCreateModelGroup.getSelection()) {
			textModelGroup.setEnabled(true);
			textResourceModelGroup.setEnabled(true);
			textSetupScript.setEnabled(true);
			btnCreateResourceModel.setEnabled(true);
			btnCreateSetupScript.setEnabled(true);
			if (textModelGroup.getText().length() == 0) {
				updateStatus(I18n.get("model"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textModelGroup.getText())) {
				updateStatus(I18n.get("wrongModel"));
				return;
			}
		} else {
			textModelGroup.setEnabled(false);
			textResourceModelGroup.setEnabled(false);
			textSetupScript.setEnabled(false);
			btnCreateResourceModel.setSelection(false);
			btnCreateResourceModel.setEnabled(false);
			btnCreateSetupScript.setSelection(false);
			btnCreateSetupScript.setEnabled(false);
		}
		if (btnCreateResourceModel.getSelection()) {
			textResourceModelGroup.setEnabled(true);
			textSetupScript.setEnabled(true);
			btnCreateSetupScript.setEnabled(true);
			if (textResourceModelGroup.getText().length() == 0) {
				updateStatus(I18n.get("resource_model"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textResourceModelGroup.getText())) {
				updateStatus(I18n.get("wrongResourceModel"));
				return;
			} else if (textResourceModelGroup.getText().equals(textModelGroup.getText())) {
				updateStatus(I18n.get("sameModelAsResourceModel"));
				return;
			}
		} else {
			textResourceModelGroup.setEnabled(false);
			textSetupScript.setEnabled(false);
			btnCreateSetupScript.setSelection(false);
			btnCreateSetupScript.setEnabled(false);
		}
		if (btnCreateSetupScript.getSelection()) {
			textSetupScript.setEnabled(true);
			if (textSetupScript.getText().length() == 0) {
				updateStatus(I18n.get("setup"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textSetupScript.getText())) {
				updateStatus(I18n.get("wrongSetup"));
				return;
			}
		} else {
			textSetupScript.setEnabled(false);
		}
		if (btnCreateBlockGroup.getSelection()) {
			textBlockGroup.setEnabled(true);
			if (textBlockGroup.getText().length() == 0) {
				updateStatus(I18n.get("block"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textBlockGroup.getText())) {
				updateStatus(I18n.get("wrongBlock"));
				return;
			}
		} else {
			textBlockGroup.setEnabled(false);
		}
		if (btnCreateHelperGroup.getSelection()) {
			textHelperGroup.setEnabled(true);
			btnCreateDefaltHelper.setEnabled(true);
			if (textHelperGroup.getText().length() == 0) {
				updateStatus(I18n.get("helper"));
				return;
			} else if (!Pattern.matches("^[a-zA-Z0-9_]+$", textHelperGroup.getText())) {
				updateStatus(I18n.get("wrongHelper"));
				return;
			}
		} else {
			textHelperGroup.setEnabled(false);
			btnCreateDefaltHelper.setEnabled(false);
			btnCreateDefaltHelper.setSelection(false);
		}
		configuration.setModelGroup(btnCreateModelGroup.getSelection());
		configuration.setModelGroupName(textModelGroup.getText());
		configuration.setResourceModelGroup(btnCreateResourceModel.getSelection());
		configuration.setResourceModelGroupName(textResourceModelGroup.getText());
		configuration.setSetup(btnCreateSetupScript.getSelection());
		configuration.setSetupName(textSetupScript.getText());
		configuration.setBlockGroup(btnCreateBlockGroup.getSelection());
		configuration.setBlockGroupName(textBlockGroup.getText());
		configuration.setHelperGroup(btnCreateHelperGroup.getSelection());
		configuration.setHelperGroupName(textHelperGroup.getText());
		configuration.setDefaultHelper(btnCreateDefaltHelper.getSelection());
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}