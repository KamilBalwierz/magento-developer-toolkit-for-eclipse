package pl.mamooth.eclipse.magento.wizards.module;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.core.includepath.IncludePath;
import org.eclipse.php.internal.core.includepath.IncludePathManager;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Document;

import pl.mamooth.eclipse.magento.MagentoEclipsePlugin;
import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.TemplateParser;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;
import pl.mamooth.eclipse.magento.worker.MagentoWorker;

@SuppressWarnings("restriction")
public class NewModuleWizard extends Wizard implements INewWizard {
	private NamePage namePage;
	private ComponentsPage componentsPage;
	private ControllersPage controllersPage;
	private Configuration configuration;
	private IWorkbench workbench;

	public NewModuleWizard() {
		super();
		setNeedsProgressMonitor(true);
		configuration = new Configuration();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void notifyShortName(String shortName) {
		componentsPage.notifyShortName(shortName);
		controllersPage.notifyShortName(shortName);
	}

	@Override
	public void addPages() {
		namePage = new NamePage(this);
		addPage(namePage);
		componentsPage = new ComponentsPage(this);
		addPage(componentsPage);
		controllersPage = new ControllersPage(this);
		addPage(controllersPage);
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

	private void doFinish(IProgressMonitor monitor) throws CoreException {
		try {
			monitor.beginTask(I18n.get("creating", configuration.getProjectName()), 7);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IProject project = root.getProject(configuration.getProjectName());
			project.create(monitor);
			project.open(monitor);
			monitor.worked(1);

			MagentoModule module = new MagentoModule(project, configuration);
			ModuleHelper.setAppFolderPath(project, new String[] { FolderHelper.SOURCE_FOLDER, FolderHelper.APP_FOLDER });

			monitor.worked(1);

			monitor.setTaskName(I18n.get("folder", configuration.getProjectName()));

			createCacheDirectory(monitor, project);
			markProjectAsPHPProject(project);

			IFile cacheXmlFile = module.getCacheConfigXml();
			Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, monitor);

			monitor.worked(1);

			IFile modulesXmlFile = module.getModuleXml();
			Document modulesXmlDocument = XMLHelper.open(modulesXmlFile, monitor);
			writeBasicConfiguration(modulesXmlDocument);
			writeBasicConfiguration(cacheXmlDocument);
			XMLHelper.save(modulesXmlDocument, modulesXmlFile, monitor);

			monitor.setTaskName(I18n.get("creating", configuration.getProjectName()));
			monitor.worked(1);

			createDirectoryTree(monitor, module);

			monitor.worked(1);

			monitor.setTaskName(I18n.get("config", configuration.getProjectName()));

			IFile configXmlFile = module.getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);

			XMLHelper.getElement(configXmlDocument, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.MODULES_NODE, configuration.getProjectName(), XMLHelper.VERSION_NODE }, configuration.getVersion());
			XMLHelper.getElement(cacheXmlDocument, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.MODULES_NODE, configuration.getProjectName(), XMLHelper.VERSION_NODE }, configuration.getVersion());

			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheXmlDocument, cacheXmlFile, monitor);

			writeModuleConfiguration(module);

			monitor.worked(1);

			if (configuration.isDefaultHelper()) {

				monitor.setTaskName(I18n.get("helper", configuration.getProjectName()));

				TemplateParser parser = new TemplateParser("phpClass");
				parser.addVariable("className", configuration.getProjectName() + "_" + FolderHelper.HELPER_FOLDER + "_Data");
				parser.addVariable("extendsClassName", "Mage_Core_Helper_Data");
				parser.addVariable("extends", "extends");
				String templateParsed = parser.parse();

				IFile defaultHelperFile = ResourceHelper.getFile(project, StringHelper.append(module.getSourceFolderPath(), FolderHelper.HELPER_FOLDER), "Data.php", monitor);

				InputStream is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
				defaultHelperFile.create(is, true, monitor);
			}

			monitor.worked(1);

			openMagentoPerspective();

		} catch (Exception e) {
			e.printStackTrace();
			throwCoreException(e.getMessage());
		}
	}

	private void openMagentoPerspective() throws WorkbenchException {
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		final IWorkbenchWindow window = workbench.getWorkbenchWindowCount() > 0 ? windows[0] : null;
		boolean changePerspective = true;
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IPerspectiveDescriptor perspective = page.getPerspective();
				if (perspective.getId().equals(MagentoEclipsePlugin.PERSPECTIVE_ID)) {
					changePerspective = false;
				}
			}
		}
		if (changePerspective) {
			final String jobName = I18n.get("job");
			final String title = I18n.get("title");
			final String question = I18n.get("question");
			new UIJob(jobName) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						boolean proceedWithChange = MessageDialog.openQuestion(getShell(), title, question);
						if (proceedWithChange) {
							workbench.showPerspective(MagentoEclipsePlugin.PERSPECTIVE_ID, window);
						}
					} catch (WorkbenchException e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private void createCacheDirectory(IProgressMonitor monitor, IProject project) throws CoreException, FileNotFoundException {
		IFolder cacheFolder = project.getFolder(FolderHelper.PLUGIN_CACHE_FOLDER);
		cacheFolder.create(true, true, monitor);
		cacheFolder.setHidden(true);

		for (String file : ComboSource.baseVersionFiles) {
			FileInputStream fis = new FileInputStream(configuration.getBase() + "/" + file);
			IFile localFile = cacheFolder.getFile(file);
			localFile.create(fis, true, monitor);
		}
	}

	private void writeModuleConfiguration(MagentoModule module) throws UnsupportedEncodingException, CoreException, ParserConfigurationException, TransformerException {
		if (configuration.isModelGroup()) {
			MagentoWorker.createModelGroup(module, configuration.getModelGroupName());
			if (configuration.isResourceModelGroup()) {
				MagentoWorker.createResourceModelGroup(module, configuration.getResourceModelGroupName());
				if (configuration.isSetup()) {
					MagentoWorker.createSetupResource(module, configuration.getSetupName());
				}
			}
		}

		if (configuration.isBlockGroup()) {
			MagentoWorker.createBlockGroup(module, configuration.getBlockGroupName());
		}

		if (configuration.isHelperGroup()) {
			MagentoWorker.createHelperGroup(module, configuration.getBlockGroupName());
		}

		if (configuration.isAdminControllers()) {
			MagentoWorker.createAdminhtmlAdminRouter(module, configuration.getAdminControllersFrontName());
		}

		if (configuration.isDefaultControllers()) {
			MagentoWorker.createFrontendStandardRouter(module, configuration.getDefaultControllersFrontName());
		}
	}

	private void createDirectoryTree(IProgressMonitor monitor, MagentoModule module) throws CoreException {
		ResourceHelper.getFolder(module.getProject(), StringHelper.append(module.getSourceFolderPath(), FolderHelper.ETC_FOLDER), monitor);
		if (configuration.isModelGroup()) {
			ResourceHelper.getFolder(module.getProject(), StringHelper.append(module.getSourceFolderPath(), FolderHelper.MODEL_FOLDER), monitor);
			if (configuration.isResourceModelGroup()) {
				ResourceHelper.getFolder(module.getProject(), StringHelper.concat(module.getSourceFolderPath(), new String[] { FolderHelper.MODEL_FOLDER, FolderHelper.RESOUCE_FOLDER }), monitor);
				if (configuration.isSetup()) {
					ResourceHelper.getFolder(module.getProject(), StringHelper.concat(module.getSourceFolderPath(), new String[] { FolderHelper.SETUP_FOLDER, configuration.getSetupName() }), monitor);
				}
			}
		}
		if (configuration.isDefaultControllers() || configuration.isAdminControllers()) {
			ResourceHelper.getFolder(module.getProject(), StringHelper.append(module.getSourceFolderPath(), FolderHelper.CONTROLLERS_FOLDER), monitor);
			if (configuration.isAdminControllers()) {
				ResourceHelper.getFolder(module.getProject(), StringHelper.concat(module.getSourceFolderPath(), new String[] { FolderHelper.CONTROLLERS_FOLDER, FolderHelper.ADMIN_CONTROLLERS_SUBFOLDER }), monitor);
			}
		}
		if (configuration.isBlockGroup()) {
			ResourceHelper.getFolder(module.getProject(), StringHelper.append(module.getSourceFolderPath(), FolderHelper.BLOCK_FOLDER), monitor);
		}
		if (configuration.isHelperGroup()) {
			ResourceHelper.getFolder(module.getProject(), StringHelper.append(module.getSourceFolderPath(), FolderHelper.HELPER_FOLDER), monitor);
		}
	}

	private void writeBasicConfiguration(Document modulesFile) {
		XMLHelper.getElement(modulesFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.MODULES_NODE, configuration.getProjectName(), XMLHelper.ACTIVE_NODE }, Boolean.toString(configuration.isActive()));
		XMLHelper.getElement(modulesFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.MODULES_NODE, configuration.getProjectName(), XMLHelper.CODEPOOL_NODE }, configuration.getCodePool());
	}

	private void markProjectAsPHPProject(IProject project) throws CoreException, BackingStoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 2];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		String ID = "org.eclipse.php.core.PHPNature";
		newNatures[natures.length] = ID;
		newNatures[natures.length + 1] = "pl.mamooth.eclipse.magento.Nature";
		description.setNatureIds(newNatures);
		project.setDescription(description, null);

		IncludePathManager manager = IncludePathManager.getInstance();
		IncludePath[] includePath = manager.getIncludePaths(project);
		int count = includePath.length;
		System.arraycopy(includePath, 0, includePath = new IncludePath[count + 1], 0, count);
		includePath[count] = new IncludePath(ResourceHelper.getFolder(project, FolderHelper.SOURCE_FOLDER, null), project);
		manager.setIncludePath(project, includePath);
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "pl.mamooth.eclipse.magento", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}
}