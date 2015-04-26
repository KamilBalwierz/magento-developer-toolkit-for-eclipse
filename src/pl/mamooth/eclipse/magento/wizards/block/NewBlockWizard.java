package pl.mamooth.eclipse.magento.wizards.block;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.TemplateParser;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;
import pl.mamooth.eclipse.magento.worker.MagentoWorker;

public class NewBlockWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private Configuration configuration;
	private NamePage namePage;
	private TemplatePage templatePage;

	public ISelection getSelection() {
		return selection;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public NamePage getNamePage() {
		return namePage;
	}

	public TemplatePage getTemplatePage() {
		return templatePage;
	}

	public NewBlockWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		configuration = new Configuration();
		namePage = new NamePage(this);
		templatePage = new TemplatePage(this);

		addPage(namePage);
		addPage(templatePage);

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
			e.printStackTrace();
			realException.printStackTrace();
			MessageDialog.openError(getShell(), I18n.get("error"), realException.getMessage());
			return false;
		}
		return true;
	}

	protected void doFinish(Configuration config, IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException, ParserConfigurationException, TransformerException {
		monitor.beginTask(I18n.get("creating", config.getClassName()), 5);
		// create block class file
		TemplateParser parser = null;
		String templateParsed = "";

		if (config.getModule().getBlockGroupName() == null) {
			MagentoWorker.createBlockGroup(config.getModule(), null);
		}

		config.getModule().setDesignTemplate(config.getDesignScope(), config.getDesignTemplate());
		config.getModule().setDesignPackage(config.getDesignScope(), config.getDesignPackage());

		if (config.isAssignTemplateInCode()) {
			parser = new TemplateParser("phpField");
			parser.addVariable("name", "$_template");
			parser.addVariable("visibility", "protected");
			parser.addVariable("assign", "=");
			parser.addVariable("value", "'" + config.getMagentoTemplatePath() + "'");
			templateParsed = parser.parse();
		}

		parser = new TemplateParser("phpClass");
		// add template name variable
		if (config.isAssignTemplateInCode()) {
			parser.addVariable("fields", templateParsed);

		}
		parser.addVariable("className", config.getClassName());
		parser.addVariable("extendsClassName", config.getExtendsClassName());
		parser.addVariable("extends", "extends");

		templateParsed = parser.parse();

		final IFile blockFile = ResourceHelper.getFile(config.getModule().getProject(), config.getFilePath(), config.getFileNname(), monitor);

		InputStream is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
		blockFile.create(is, true, monitor);
		monitor.worked(1);

		// insert rewrite config
		if (config.isRewrite()) {
			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			writeBlockRewrite(configXmlDocument, config);
			writeBlockRewrite(cacheConfigXmlDocument, config);
			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);
		}
		monitor.worked(1);

		// create template file
		final IFile templateFile = config.isCreateTemplate() ? ResourceHelper.getFile(config.getModule().getProject(), config.getTemplateFilePath(), config.getTemplateFileName(), monitor) : null;
		if (config.isCreateTemplate()) {
			parser = new TemplateParser("magentoTemplate");
			parser.addVariable("className", config.getClassName());
			templateParsed = parser.parse();
			is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
			templateFile.create(is, true, monitor);
		}
		monitor.worked(1);

		// create layout.xml entry
		if (config.isCreateLayoutXmlEntry()) {
			IFile layoutXmlFile = config.getModule().getLayoutXml(config.getDesignScope(), config.getDesignPackage(), config.getDesignTemplate(), config.getDesignLayoutFileName());
			Document layoutXmlDocument = XMLHelper.open(layoutXmlFile, monitor);
			IFile layoutConfigXmlFile = config.getModule().getCacheLayoutXml(config.getDesignScope());
			Document layoutConfigXmlDocument = XMLHelper.open(layoutConfigXmlFile, monitor);
			writeLayoutEntry(layoutXmlDocument, config);
			writeLayoutEntry(layoutConfigXmlDocument, config);
			XMLHelper.save(layoutXmlDocument, layoutXmlFile, monitor);
			XMLHelper.save(layoutConfigXmlDocument, layoutConfigXmlFile, monitor);

			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			writeLayoutXmlFileToConfig(configXmlDocument, config);
			writeLayoutXmlFileToConfig(cacheConfigXmlDocument, config);
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
		if (config.isCreateTemplate()) {
			monitor.setTaskName(I18n.get("opening", config.getTemplateFileName()));
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, templateFile, true);
					} catch (PartInitException e) {
					}
				}
			});
		}
		monitor.worked(1);
	}

	private void writeBlockRewrite(Document configXmlDocument, Configuration config) {
		String[] path = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.BLOCKS_NODE, config.getExtendsGroupName(), XMLHelper.REWRITE_NODE, config.getExtendsBlockName() };
		XMLHelper.getElement(configXmlDocument, path, config.getClassName());
	}

	private void writeLayoutEntry(Document document, Configuration config) {
		Element handler = XMLHelper.getElement(document, new String[] { XMLHelper.LAYOUT_NODE, config.getHandler() }, null);
		Element reference = null;
		for (int i = 0; i < handler.getChildNodes().getLength(); ++i) {
			Node possibleReference = handler.getChildNodes().item(i);
			if (possibleReference instanceof Element) {
				Element element = (Element) possibleReference;
				if (element.getNodeName().equals(XMLHelper.REFERENCES_NODE) && element.getAttribute(XMLHelper.NAME_ATTRIBUTE).equals(config.getReferences())) {
					reference = element;
				}
			}
		}
		if (reference == null) {
			reference = document.createElement(XMLHelper.REFERENCES_NODE);
			reference.setAttribute(XMLHelper.NAME_ATTRIBUTE, config.getReferences());
			handler.appendChild(reference);
		}
		Element block = document.createElement(XMLHelper.BLOCK_NODE);
		block.setAttribute(XMLHelper.NAME_ATTRIBUTE, config.getBlockName());
		block.setAttribute(XMLHelper.ALIAS_ATTRIBUTE, config.getBlockAlias());
		block.setAttribute(XMLHelper.TYPE_ATTRIBUTE, config.getBlockGroup() + "/" + config.getBlockModel());
		if (!config.isAssignTemplateInCode() && config.isCreateTemplate()) {
			block.setAttribute(XMLHelper.TEMPLATE_ATTRIBUTE, config.getMagentoTemplatePath());
		}
		reference.appendChild(block);
	}

	private void writeLayoutXmlFileToConfig(Document document, Configuration config) {
		String[] path = new String[] { XMLHelper.CONFIG_NODE, config.getDesignScope(), XMLHelper.LAYOUT_NODE, XMLHelper.UPDATES_NODE, config.getBlockGroup(), XMLHelper.FILE_NODE };
		XMLHelper.getElement(document, path, config.getDesignLayoutFileName());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}