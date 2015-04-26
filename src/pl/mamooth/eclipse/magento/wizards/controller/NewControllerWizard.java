package pl.mamooth.eclipse.magento.wizards.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.TemplateParser;
import pl.mamooth.eclipse.magento.worker.MagentoWorker;

public class NewControllerWizard extends Wizard implements INewWizard {
	private NamePage namePage;
	private ActionsPage actionsPage;
	private ISelection selection;
	private Configuration configration;

	public NamePage getNamePage() {
		return namePage;
	}

	public ActionsPage getActionsPage() {
		return actionsPage;
	}

	public ISelection getSelection() {
		return selection;
	}

	public Configuration getConfigration() {
		return configration;
	}

	public NewControllerWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		configration = new Configuration();
		namePage = new NamePage(this);
		actionsPage = new ActionsPage(this);
		addPage(namePage);
		addPage(actionsPage);
	}

	@Override
	public boolean performFinish() {
		final Configuration config = configration;
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(config, monitor);
				} catch (Exception e) {
					e.printStackTrace();
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
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

	protected void doFinish(Configuration config, IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException, ParserConfigurationException, TransformerException {
		monitor.beginTask(I18n.get("creating", config.getClassName()), 3);
		TemplateParser parser = null;

		String adminhtmlprefix = config.getModule().getVendor() + '_' + config.getModule().getName() + '_' + FolderHelper.ADMIN_CONTROLLERS_SUBFOLDER;

		if (config.getClassName().startsWith(adminhtmlprefix)) {
			if (config.getModule().getAdminControllersFrontName() == null) {
				MagentoWorker.createAdminhtmlAdminRouter(config.getModule(), null);
			}
		} else {
			if (config.getModule().getDefaultControllersFrontName() == null) {
				MagentoWorker.createFrontendStandardRouter(config.getModule(), null);
			}
		}

		StringBuilder methods = new StringBuilder();

		for (String action : config.getActions()) {
			parser = new TemplateParser("phpMethod");
			parser.addVariable("visibility", "public");
			parser.addVariable("name", action);
			methods.append(parser.parse());
		}

		if (config.isGeneratePreDispatch()) {
			parser = new TemplateParser("phpMethod");
			parser.addVariable("visibility", "public");
			parser.addVariable("name", "preDispatch");
			parser.addVariable("contents", "parent::preDispatch();");
			methods.append(parser.parse());
		}

		if (config.isGeneratePostDispatch()) {
			parser = new TemplateParser("phpMethod");
			parser.addVariable("visibility", "public");
			parser.addVariable("name", "postDispatch");
			parser.addVariable("contents", "parent::postDispatch();");
			methods.append(parser.parse());
		}

		monitor.worked(1);

		parser = new TemplateParser("phpClass");
		parser.addVariable("className", config.getClassName());
		parser.addVariable("extendsClassName", config.getExtendsClass());
		parser.addVariable("extends", "extends");
		parser.addVariable("methods", methods.toString());

		String templateParsed = parser.parse();

		final IFile controllerFile = ResourceHelper.getFile(config.getModule().getProject(), config.getFilePath(), config.getFileName(), monitor);

		InputStream is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
		controllerFile.create(is, true, monitor);
		monitor.worked(1);

		// open files
		monitor.setTaskName(I18n.get("opening", config.getClassName()));
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, controllerFile, true);
				} catch (PartInitException e) {
				}
			}
		});

		monitor.worked(1);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}