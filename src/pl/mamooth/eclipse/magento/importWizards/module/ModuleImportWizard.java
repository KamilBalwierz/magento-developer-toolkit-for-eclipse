package pl.mamooth.eclipse.magento.importWizards.module;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import pl.mamooth.eclipse.magento.helpers.I18n;

public class ModuleImportWizard extends Wizard implements IImportWizard {

	private ImportWizardPage mainPage;
	private ImportConfiguration configuration;

	public ModuleImportWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		try {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						doFinish(monitor);
					} catch (CoreException e) {
						e.printStackTrace();
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), I18n.get("error"), realException.getMessage());
			return false;
		}
		return true;
	}

	public void doFinish(IProgressMonitor monitor) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(configuration.getProjectName());
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(configuration.getProjectName());

		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 2];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = "org.eclipse.php.core.PHPNature";
		newNatures[natures.length + 1] = "pl.mamooth.eclipse.magento.Nature";
		description.setNatureIds(newNatures);

		// TODO test if need to copy code

		description.setLocation(new Path(configuration.getPath()));
		description.setComment("Imported Magento Module Project");

		// TODO create magentomoduleobjects and assign them to project

		project.create(description, monitor);
		project.open(monitor);
		monitor.worked(1);

		/*
		 * IncludePathManager manager = IncludePathManager.getInstance();
		 * IncludePath[] includePath = manager.getIncludePaths(project); int
		 * count = includePath.length; System.arraycopy(includePath, 0,
		 * includePath = new IncludePath[count + 1], 0, count);
		 * includePath[count] = new
		 * IncludePath(ResourceHelper.getFolder(project,
		 * FolderHelper.SOURCE_FOLDER, null), project);
		 * manager.setIncludePath(project, includePath);
		 */
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(I18n.get("title")); // NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new ImportWizardPage(I18n.get("page_title"), this); // NON-NLS-1
		configuration = new ImportConfiguration();
	}

	public ImportConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

}
