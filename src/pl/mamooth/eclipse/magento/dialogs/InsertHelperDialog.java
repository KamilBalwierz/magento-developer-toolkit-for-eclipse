package pl.mamooth.eclipse.magento.dialogs;

import java.util.HashMap;

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
import pl.mamooth.eclipse.magento.handlers.InsertHelperHandler;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.UIHelper;

public class InsertHelperDialog extends Dialog {
	private static final Color colorWarn = SWTResourceManager.getColor(SWT.COLOR_DARK_YELLOW);
	private static final Color colorErr = SWTResourceManager.getColor(SWT.COLOR_RED);
	private static final Image imageWarn = ResourceManager.getPluginImage("org.eclipse.ui.ide", "/icons/full/elcl16/showwarn_tsk.gif");
	private static final Image imageErr = ResourceManager.getPluginImage("org.eclipse.ui.ide", "/icons/full/elcl16/showerr_tsk.gif");

	private Text textVariable;
	private Text textClass;
	private Label lblImage;
	private Label lblInfo;
	private Button buttonOk;
	private InsertHelperHandler action;
	private Combo comboGroup;
	private Combo comboModel;

	private MagentoModule module;

	private String[] modelGroups;
	private String[] models;
	private HashMap<String, String> classes;
	private String baseClass;
	private AutoCompleteField groupAutocomplete;
	private AutoCompleteField modelAutocomplete;

	class UpdateGroupCombo extends UIJob {
		private Combo combo;
		private MagentoModule module;

		public UpdateGroupCombo(String name, MagentoModule module, Combo combo) {
			super(name);
			this.module = module;
			this.combo = combo;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				modelGroups = module.getHelperGroups();
				combo.setItems(modelGroups);
				groupAutocomplete.setProposals(modelGroups);
				return Status.OK_STATUS;
			} catch (Exception e) {
				e.printStackTrace();
				return Status.CANCEL_STATUS;
			}
		}
	}

	class UpdateModelCombo extends UIJob {
		private Combo combo;
		private MagentoModule module;
		private String group;

		public UpdateModelCombo(String name, MagentoModule module, Combo combo, String group) {
			super(name);
			this.module = module;
			this.combo = combo;
			this.group = group;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				String classBase = module.getClassBaseForHelperGroup(group);
				String[] classList = module.getClassesStartingWith(classBase);
				classes = new HashMap<String, String>();
				models = new String[classList.length];
				int i = 0;
				for (String className : classList) {
					String model = StringHelper.toName(className, classBase);
					classes.put(model, className);
					models[i] = model;
					++i;
				}
				combo.setItems(models);
				modelAutocomplete.setProposals(models);
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
	public InsertHelperDialog(Shell parentShell, InsertHelperHandler insertHelperHandler, MagentoModule module) throws XPathExpressionException, ParserConfigurationException, CoreException {
		super(parentShell);
		this.action = insertHelperHandler;
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
		lblGroup.setText(I18n.get("group"));

		comboGroup = new Combo(container, SWT.NONE);
		groupAutocomplete = new AutoCompleteField(comboGroup, new ComboContentAdapter(), new String[0]);
		comboGroup.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
				updateComboGroup();
			}
		});
		comboGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblModel = new Label(container, SWT.NONE);
		lblModel.setText(I18n.get("helper"));

		comboModel = new Combo(container, SWT.NONE);
		modelAutocomplete = new AutoCompleteField(comboModel, new ComboContentAdapter(), new String[0]);
		comboModel.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
				updateComboModel();
			}
		});
		comboModel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblClass = new Label(container, SWT.NONE);
		lblClass.setText(I18n.get("class"));

		textClass = new Text(container, SWT.BORDER);
		textClass.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		textClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblVariable = new Label(container, SWT.NONE);
		lblVariable.setText(I18n.get("variable"));

		textVariable = new Text(container, SWT.BORDER);
		textVariable.setText("helper");
		textVariable.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
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

		new UpdateGroupCombo(I18n.get("pupulate_groups"), module, comboGroup).schedule();

		return container;
	}

	protected void updateComboModel() {
		textVariable.setText(StringHelper.toCamelCase(comboModel.getText()));
		updateClass();
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

	protected void dialogChanged() {
		ready();
		if (textVariable.getText().startsWith("$")) {
			action.setVariable(textVariable.getText());
		} else {
			action.setVariable("$" + textVariable.getText());
		}
		if (comboGroup.getText().length() == 0) {
			setError(I18n.get("empty_group"));
			return;
		}
		action.setGroup(comboGroup.getText());
		action.setModel(comboModel.getText());
		if (textClass.getText().length() == 0) {
			setError(I18n.get("empty_class"));
			return;
		}
		if (!UIHelper.comboSelected(comboModel)) {
			setWarining(I18n.get("custom_model"));
		}
		if (UIHelper.comboSelected(comboGroup)) {
			setWarining(I18n.get("custom_group"));
		}
		if (textVariable.getText().length() == 0) {
			setError(I18n.get("empty_variable"));
			return;
		}
		action.setClassName(textClass.getText());
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

	protected void updateComboGroup() {
		try {
			baseClass = module.getClassBaseForHelperGroup(comboGroup.getText());
			if (baseClass.length() == 0) {
				String base = comboGroup.getText();
				if (base.indexOf('_') < 0) {
					base = "mage_" + base;
				}
				baseClass = StringHelper.toCamelCase(base, true, true) + '_' + FolderHelper.HELPER_FOLDER;
			}
			updateClass();
			if (UIHelper.comboSelected(comboGroup)) {
				new UpdateModelCombo(I18n.get("pupulate_models"), module, comboModel, comboGroup.getText()).schedule();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void updateClass() {
		if (baseClass != null && baseClass.length() > 0) {
			String model = comboModel.getText();
			if (model.length() == 0) {
				model = "data";
			}
			textClass.setText(StringHelper.toCamelCase(baseClass + '_' + model, true, true));
		}
	}
}
