/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package pl.mamooth.eclipse.magento.importWizards.module;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;

public class ImportWizardPage extends WizardPage {

	protected FileFieldEditor editor;
	private Text directoryText;
	private Text nameText;
	private Button browseButton;
	private boolean pageReady = false;
	private ModuleImportWizard parent;
	private Button copyToWorkspaceCheck;
	private ArrayList<File> apps;
	private Table modulesTable;
	private TableColumn moduleNameColumn;

	public ImportWizardPage(String pageName, ModuleImportWizard parent) {
		super(pageName);
		this.parent = parent;
		setPageComplete(false);
		setTitle(I18n.get("title")); // NON-NLS-1
		setDescription(I18n.get("description")); // NON-NLS-1
		apps = new ArrayList<File>();
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		layout.verticalSpacing = 9;
		setControl(container);

		Label directoryLabel = new Label(container, SWT.NONE);
		directoryLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		directoryLabel.setText(I18n.get("directory"));

		directoryText = new Text(container, SWT.BORDER);
		directoryText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				directoryNameChanged();
			}
		});
		directoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		browseButton = new Button(container, SWT.NONE);
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				handleBrowseButton();
			}
		});
		browseButton.setText(I18n.get("browse"));

		Label nameLabel = new Label(container, SWT.NONE);
		nameLabel.setText(I18n.get("project_name"));

		nameText = new Text(container, SWT.BORDER);
		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				projectNameChanged();
			}
		});
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		modulesTable = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		modulesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		modulesTable.setHeaderVisible(true);
		modulesTable.setLinesVisible(true);

		moduleNameColumn = new TableColumn(modulesTable, SWT.NONE);
		moduleNameColumn.setWidth(100);
		moduleNameColumn.setText(I18n.get("module_name"));
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		copyToWorkspaceCheck = new Button(container, SWT.CHECK);
		copyToWorkspaceCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				copyFlagChanged();
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				copyFlagChanged();
			}
		});
		copyToWorkspaceCheck.setSelection(true);
		copyToWorkspaceCheck.setText(I18n.get("copy_to_workspace"));
		new Label(container, SWT.NONE);

		pageReady = true;
	}

	protected void copyFlagChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					copyFlagChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}
		parent.getConfiguration().setCopySource(copyToWorkspaceCheck.getSelection());
	}

	protected void projectNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					projectNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}
		parent.getConfiguration().setProjectName(nameText.getText());
	}

	protected void handleBrowseButton() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					handleBrowseButton();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
		String path = dialog.open();
		if (path != null) {
			directoryText.setText(path);
		}
	}

	protected void directoryNameChanged() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					directoryNameChanged();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		parent.getConfiguration().setPath(directoryText.getText());

		setPageComplete(false);
		modulesTable.clearAll();

		final File testFile = new File(directoryText.getText());
		if (!testFile.isDirectory()) {
			parent.getConfiguration().clearModules();
			refreshModuleList();
			return;
		}

		setMessage(I18n.get("processing"), WizardPage.INFORMATION);

		new Job(I18n.get("job")) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				parsePath(testFile);
				return Status.OK_STATUS;
			}

		}.schedule();

	}

	public void parsePath(File directory) {
		parent.getConfiguration().clearModules();
		apps.clear();
		lookForAppFolder(directory);
		if (apps.size() == 0) {
			new UIJob(I18n.get("ejjoj")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					setMessage(I18n.get("no_app_folders"));
					return Status.OK_STATUS;
				}
			}.schedule();
		} else if (apps.size() > 1) {
			new UIJob(I18n.get("ejjoj")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					setErrorMessage(I18n.get("multiple_app_folders"));
					return Status.OK_STATUS;
				}
			}.schedule();
		} else {
			File app = apps.get(0);
			StringBuilder path = new StringBuilder(app.getAbsolutePath());
			if (path.charAt(path.length() - 1) != '/') {
				path.append('/');
			}
			path.append(FolderHelper.ETC_FOLDER);
			path.append('/');
			path.append(FolderHelper.MODULES_FOLDER);
			System.out.println(path.toString());
			File modules = new File(path.toString());

			if (modules.isDirectory()) {
				File[] moduleList = modules.listFiles();
				if (moduleList != null) {
					for (File module : moduleList) {
						ImportedModule[] imported = ImportedModule.parseXmlFile(module, app);
						for (ImportedModule impModule : imported) {
							parent.getConfiguration().addModule(impModule);
						}
					}
				}
			}
			new UIJob(I18n.get("ejjoj")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					setErrorMessage(null);
					setMessage(null);
					return Status.OK_STATUS;
				}
			}.schedule();
		}

		new UIJob(I18n.get("refresh")) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				refreshModuleList();
				return Status.OK_STATUS;
			}
		}.schedule();

	}

	protected void foundAppDirectory(File app) {
		// TODO: test if it is valid app directory

		// TODO: app directory must have etc and code children
		// TODO: code firectory must have at least one child that have at least
		// one child that have at least one child that have etc child that have
		// at least one child ;)
		System.out.println("APP Path " + app.getAbsolutePath());
		apps.add(app);
	}

	protected void lookForAppFolder(File directory) {
		if (directory == null) {
			return;
		}
		if (directory.isDirectory()) {
			if (directory.getName().equals("app")) {
				foundAppDirectory(directory);
			}
			File[] fileList = directory.listFiles();
			if (fileList == null) {
				return;
			}
			for (File content : fileList) {
				lookForAppFolder(content);
			}
		}
	}

	public void refreshModuleList() {
		if (!pageReady) {
			new UIJob(I18n.get("job")) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					refreshModuleList();
					return Status.OK_STATUS;
				}
			}.schedule();
			return;
		}

		modulesTable.removeAll();

		for (ImportedModule module : parent.getConfiguration().getModules()) {
			TableItem item = new TableItem(modulesTable, NONE);
			item.setText(module.toString());
			System.out.println(module.toString());

			/*
			 * TODO: populate table with module names
			 */
		}

		if (parent.getConfiguration().getModules().length > 0) {
			setPageComplete(true);
		}
	}
}
