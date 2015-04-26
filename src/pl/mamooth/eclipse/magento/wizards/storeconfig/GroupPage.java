package pl.mamooth.eclipse.magento.wizards.storeconfig;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.I18n;

public class GroupPage extends WizardPage {

	private NewStoreConfigWizard parent;
	private Text labelText;
	private Combo nameCombo;
	private Button websiteCheckbox;
	private Button defaultCheckbox;
	private Button shopCheckbox;
	private AutoCompleteField nameAutoComplete;
	private Spinner sortOrderSpinner;
	private boolean pageReady = false;

	public GroupPage(NewStoreConfigWizard newStoreConfigWizard) {
		super("wizardPage");
		setTitle(I18n.get("title"));
		setDescription(I18n.get("description"));
		parent = newStoreConfigWizard;
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		setControl(container);

		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(I18n.get("name"));

		nameCombo = new Combo(container, SWT.NONE);
		nameAutoComplete = new AutoCompleteField(nameCombo, new ComboContentAdapter(), new String[] {});
		nameCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				nameChanged();
			}
		});
		nameCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label labelLabel = new Label(container, SWT.NONE);
		labelLabel.setText(I18n.get("label"));

		labelText = new Text(container, SWT.BORDER);
		labelText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				labelChanged();
			}
		});
		labelText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label visibleLabel = new Label(container, SWT.NONE);
		visibleLabel.setText(I18n.get("visible"));

		SashForm sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		defaultCheckbox = new Button(sashForm, SWT.CHECK);
		defaultCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				defaultChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				defaultChanged();
			}
		});
		defaultCheckbox.setText(I18n.get("default"));

		websiteCheckbox = new Button(sashForm, SWT.CHECK);
		websiteCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				websiteChanged();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				websiteChanged();
			}
		});
		websiteCheckbox.setText(I18n.get("website"));

		shopCheckbox = new Button(sashForm, SWT.CHECK);
		shopCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				shopChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				shopChanged();
			}
		});
		shopCheckbox.setText(I18n.get("shop"));
		sashForm.setWeights(new int[] { 1, 1, 1 });

		Label sortOrderLabel = new Label(container, SWT.NONE);
		sortOrderLabel.setText(I18n.get("sort_order"));

		sortOrderSpinner = new Spinner(container, SWT.BORDER);
		sortOrderSpinner.setMaximum(10000);
		sortOrderSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				sortOrderChanged();
			}
		});
		pageReady = true;
		dialogChanged();
	}

	public Configuration getConfig() {
		return parent.getConfiguration();
	}

	protected void sortOrderChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					sortOrderChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGroupSortOrder(sortOrderSpinner.getSelection());
	}

	protected void shopChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					shopChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGroupShowStore(shopCheckbox.getSelection());
	}

	protected void websiteChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					websiteChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGroupShowWebsite(websiteCheckbox.getSelection());
	}

	protected void defaultChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					defaultChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGroupShowDefault(defaultCheckbox.getSelection());
	}

	protected void labelChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					labelChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		getConfig().setGroupLabel(labelText.getText());
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

		getConfig().setGroupName(nameCombo.getText());
		parent.getFiledPage().initialize(nameCombo.getText());
		GroupData group = getConfig().getModule().getConfigGroup(getConfig().getSectionName(), nameCombo.getText());
		if (group != null) {
			labelText.setText(group.getLabel());
			sortOrderSpinner.setSelection(group.getSortOrder());
			defaultCheckbox.setSelection(group.isVisibleDefault());
			websiteCheckbox.setSelection(group.isVisibleWebsite());
			shopCheckbox.setSelection(group.isVisibleStore());
		} else {
			labelText.setText("");
			sortOrderSpinner.setSelection(0);
			defaultCheckbox.setSelection(false);
			websiteCheckbox.setSelection(false);
			shopCheckbox.setSelection(false);
		}
		dialogChanged();
	}

	public void initialize(final MagentoModule module, final String section) {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					initialize(module, section);
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		String[] names = module.getConfigGroupNames(section);
		nameCombo.setItems(names);
		nameAutoComplete.setProposals(names);
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

		shopChanged();
		websiteChanged();
		defaultChanged();

		if (!Pattern.matches("^[a-zA-Z0-9_]+$", nameCombo.getText())) {
			updateStatus(I18n.get("wrong_name"));
			return;
		}
		if (labelText.getText().length() == 0) {
			updateStatus(I18n.get("empty_label"));
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}
}