package pl.mamooth.eclipse.magento.wizards.helper;

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
import org.w3c.dom.Document;

import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.TemplateParser;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;
import pl.mamooth.eclipse.magento.worker.MagentoWorker;

public class NewHelperWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private Configuration configuration;

	public NewHelperWizard() {
		super();
		setNeedsProgressMonitor(true);
		configuration = new Configuration();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void addPages() {
		configuration = new Configuration();
		NamePage blockPage = new NamePage(this);
		addPage(blockPage);
	}

	@Override
	public boolean performFinish() {
		final Configuration config = configuration;
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(config, monitor);
				} catch (Exception e) {
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

		if (config.getModule().getHelperGroupName() == null) {
			MagentoWorker.createHelperGroup(config.getModule(), null);
		}

		// create block class file
		TemplateParser parser = null;
		String templateParsed = "";
		parser = new TemplateParser("phpClass");
		// add template name variable
		parser.addVariable("className", config.getClassName());
		if (!config.getExtendsClass().equals("")) {
			parser.addVariable("extendsClassName", config.getExtendsClass());
			parser.addVariable("extends", "extends");
		}

		templateParsed = parser.parse();

		final IFile blockFile = ResourceHelper.getFile(config.getModule().getProject(), config.getFilePath(), config.getFileName(), monitor);

		InputStream is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
		blockFile.create(is, true, monitor);
		monitor.worked(1);

		// insert rewrite config
		if (config.isRewrite()) {
			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			writeHelperRewrite(configXmlDocument, config);
			writeHelperRewrite(cacheConfigXmlDocument, config);
			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);
		}
		monitor.worked(1);

		// open files
		monitor.setTaskName(I18n.get("opening", config.getClassName()));
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, blockFile, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private void writeHelperRewrite(Document configXmlDocument, Configuration config) {
		String[] path = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.HELPER_NODE, config.getRewriteGroup(), XMLHelper.REWRITE_NODE, config.getRewriteModel() };
		XMLHelper.getElement(configXmlDocument, path, config.getClassName());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public ISelection getSelection() {
		return selection;
	}
}