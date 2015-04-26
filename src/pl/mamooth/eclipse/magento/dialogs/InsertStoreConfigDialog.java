package pl.mamooth.eclipse.magento.dialogs;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.handlers.InsertStoreConfigHandler;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.StringHelper;

public class InsertStoreConfigDialog extends Dialog {
	private static final Color colorWarn = SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW);
	private static final Color colorErr = SWTResourceManager.getColor(SWT.COLOR_RED);
	private static final Image imageWarn = ResourceManager.getPluginImage("org.eclipse.ui.ide", "/icons/full/elcl16/showwarn_tsk.gif");
	private static final Image imageErr = ResourceManager.getPluginImage("org.eclipse.ui.ide", "/icons/full/elcl16/showerr_tsk.gif");

	private Text textVariable;
	private Combo comboField;
	private Label lblImage;
	private Label lblInfo;
	private Button buttonOk;
	private InsertStoreConfigHandler action;
	private Combo comboSection;
	private Combo comboGroup;

	private MagentoModule module;

	private String[] sections;
	private String[] groups;
	private String[] fields;
	private AutoCompleteField sectionAutocomplete;
	private AutoCompleteField groupAutocomplete;
	private AutoCompleteField filedAutocomplete;

	class LoadSectionsJob extends UIJob {
		private Combo combo;
		private MagentoModule module;

		public LoadSectionsJob(String name, MagentoModule module, Combo combo) {
			super(name);
			this.module = module;
			this.combo = combo;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				sections = module.getSystemSections();
				combo.setItems(sections);
				sectionAutocomplete.setProposals(sections);
				return Status.OK_STATUS;
			} catch (Exception e) {
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			}
		}
	}

	class LoadGroupsJob extends UIJob {
		private Combo combo;
		private MagentoModule module;
		private String section;

		public LoadGroupsJob(String name, MagentoModule module, Combo combo, String section) {
			super(name);
			this.module = module;
			this.combo = combo;
			this.section = section;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				groups = module.getSystemGroups(section);
				combo.setItems(groups);
				groupAutocomplete.setProposals(groups);
				return Status.OK_STATUS;
			} catch (Exception e) {
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			}
		}
	}

	class LoadFieldsJob extends UIJob {
		private Combo combo;
		private MagentoModule module;
		private String section;
		private String group;

		public LoadFieldsJob(String name, MagentoModule module, Combo combo, String section, String group) {
			super(name);
			this.module = module;
			this.combo = combo;
			this.section = section;
			this.group = group;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				fields = module.getSystemFields(section, group);
				combo.setItems(fields);
				filedAutocomplete.setProposals(fields);
				return Status.OK_STATUS;
			} catch (Exception e) {
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			}
		}
	}

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 * @param module
	 * @throws CoreException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public InsertStoreConfigDialog(Shell parentShell, InsertStoreConfigHandler insertStoreConfigHandler, MagentoModule module) throws XPathExpressionException, ParserConfigurationException, CoreException {
		super(parentShell);
		this.action = insertStoreConfigHandler;
		this.module = module;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gl_container = new GridLayout(2, false);
		container.setLayout(gl_container);

		Label lblGroup = new Label(container, SWT.NONE);
		lblGroup.setText(I18n.get("section"));

		comboSection = new Combo(container, SWT.NONE);
		sectionAutocomplete = new AutoCompleteField(comboSection, new ComboContentAdapter(), new String[0]);
		comboSection.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				sectionChanged();
			}
		});
		comboSection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblModel = new Label(container, SWT.NONE);
		lblModel.setText(I18n.get("group"));

		comboGroup = new Combo(container, SWT.NONE);
		groupAutocomplete = new AutoCompleteField(comboGroup, new ComboContentAdapter(), new String[0]);
		comboGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				groupChanged();
			}
		});
		comboGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblClass = new Label(container, SWT.NONE);
		lblClass.setText(I18n.get("field"));

		comboField = new Combo(container, SWT.BORDER);
		filedAutocomplete = new AutoCompleteField(comboField, new ComboContentAdapter(), new String[0]);
		comboField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fieldChanged();
			}
		});
		comboField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Label lblVariable = new Label(container, SWT.NONE);
		lblVariable.setText(I18n.get("variable"));

		textVariable = new Text(container, SWT.BORDER);
		textVariable.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				variableChanged();
			}
		});
		textVariable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblImage = new Label(container, SWT.NONE);
		lblImage.setImage(imageErr);
		lblImage.setImage(imageWarn);
		lblImage.setVisible(false);
		lblImage.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		lblInfo = new Label(container, SWT.NONE);
		lblInfo.setForeground(colorWarn);
		lblInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		new LoadSectionsJob(I18n.get("job_sections"), module, comboSection).schedule();

		return container;
	}

	protected void fieldChanged() {
		action.setField(comboField.getText());
		textVariable.setText(StringHelper.toCamelCase(comboField.getText()));
	}

	protected void variableChanged() {
		ready();
		if (textVariable.getText().length() == 0) {
			setError(I18n.get("empty_variable"));
			return;
		} else if (textVariable.getText().startsWith("$")) {
			action.setVariable(textVariable.getText());
		} else {
			action.setVariable("$" + textVariable.getText());
		}
	}

	protected void groupChanged() {
		action.setGroup(comboGroup.getText());
		new LoadFieldsJob(I18n.get("job"), module, comboField, comboSection.getText(), comboGroup.getText()).schedule();
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		buttonOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		buttonOk.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 210);
	}

	protected void setWarining(String msg) {
		lblImage.setImage(imageWarn);
		lblImage.setVisible(true);
		lblInfo.setForeground(colorWarn);
		buttonOk.setEnabled(true);
		lblInfo.setText(msg);
		lblInfo.setVisible(true);
	}

	protected void setError(String msg) {
		lblImage.setImage(imageErr);
		lblImage.setVisible(true);
		lblInfo.setForeground(colorErr);
		buttonOk.setEnabled(false);
		lblInfo.setText(msg);
		lblInfo.setVisible(true);
	}

	protected void ready() {
		lblImage.setVisible(false);
		buttonOk.setEnabled(true);
		lblInfo.setVisible(false);
	}

	protected void sectionChanged() {
		action.setSection(comboSection.getText());
		new LoadGroupsJob(I18n.get("job"), module, comboGroup, comboSection.getText()).schedule();
	}

}
