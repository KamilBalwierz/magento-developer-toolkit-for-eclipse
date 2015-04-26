package pl.mamooth.eclipse.magento.wizards.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.SearchMatch;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.SearchRequestor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
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

@SuppressWarnings("restriction")
public class NewModelWizard extends Wizard implements INewWizard {
	private NamePage namePage;
	private ModelPage modelPage;
	private ISelection selection;
	private Configuration configuration;

	public NamePage getNamePage() {
		return namePage;
	}

	public ModelPage getModelPage() {
		return modelPage;
	}

	public ISelection getSelection() {
		return selection;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public NewModelWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		configuration = new Configuration();
		namePage = new NamePage(this);
		addPage(namePage);
		modelPage = new ModelPage(this);
		addPage(modelPage);
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
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), I18n.get("error"), realException.getMessage());
			return false;
		}
		return true;
	}

	protected void doFinish(Configuration config, IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException, ParserConfigurationException, TransformerException {
		monitor.beginTask(I18n.get("creating", config.getClassName()), 6);
		// create block class file
		TemplateParser parser = null;
		String templateParsed = "";
		String methods = "";

		if (config.getModule().getModelGroupName() == null) {
			MagentoWorker.createModelGroup(config.getModule(), null);
		}

		if (config.isResourceModel()) {
			if (config.getModule().getResourceModelGroupName() == null) {
				MagentoWorker.createResourceModelGroup(config.getModule(), null);
			}

			parser = new TemplateParser("phpMethod");
			parser.addVariable("visibility", "public");
			parser.addVariable("name", "_construct");
			String init = "$this->_init(\"" + config.getModelGroupName() + "/" + config.getModelName() + "\");\n\tparent::_construct();";
			parser.addVariable("contents", init);
			methods = parser.parse();
		}

		StringBuilder phpDoc = new StringBuilder();
		if (config.isCreateFieldsComments()) {
			for (Field field : config.getFileds()) {
				parser = new TemplateParser("phpDocMethod");
				parser.addVariable("returnType", config.getClassName());
				parser.addVariable("name", "set" + field.getCamelcaseName());
				parser.addVariable("params", "$" + field.getUnderscoreName());
				phpDoc.append(parser.parse());
				parser = new TemplateParser("phpDocMethod");
				parser.addVariable("returnType", field.getType());
				parser.addVariable("name", "get" + field.getCamelcaseName());
				phpDoc.append(parser.parse());
			}
		}

		StringBuilder fields = new StringBuilder();
		if (config.getEventObjectName() != null && !config.getEventObjectName().equals("")) {
			parser = new TemplateParser("phpField");
			parser.addVariable("visibility", "protected");
			parser.addVariable("assign", "=");
			parser.addVariable("name", "$_eventObject");
			parser.addVariable("value", config.getEventObjectName());
			fields.append(parser.parse());
		}
		if (config.getEventPrefix() != null && !config.getEventPrefix().equals("")) {
			parser = new TemplateParser("phpField");
			parser.addVariable("visibility", "protected");
			parser.addVariable("assign", "=");
			parser.addVariable("name", "$_eventPrefix");
			parser.addVariable("value", config.getEventPrefix());
			fields.append(parser.parse());
		}

		parser = new TemplateParser("phpClass");
		parser.addVariable("methods", methods);
		parser.addVariable("phpDoc", phpDoc.toString());
		parser.addVariable("fields", fields.toString());
		parser.addVariable("className", config.getClassName());
		parser.addVariable("extendsClassName", config.getExtendsClass());
		parser.addVariable("extends", "extends");

		templateParsed = parser.parse();

		final IFile modelFile = ResourceHelper.getFile(config.getModule().getProject(), config.getFilePath(), config.getFileName(), monitor);

		InputStream is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
		modelFile.create(is, true, monitor);
		monitor.worked(1);

		if (config.getVersionTarget() != null && config.getVersionTarget().length() != 0) {
			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			updateModuleVersion(configXmlDocument, config);
			updateModuleVersion(cacheConfigXmlDocument, config);
			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);
		}

		if (config.getTableName() != null && config.getTableName().length() != 0) {
			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			writeEntityEntry(configXmlDocument, config);
			writeEntityEntry(cacheConfigXmlDocument, config);
			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);
		}

		// insert rewrite config
		if (config.isRewrite()) {
			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			writeModelRewrite(configXmlDocument, config);
			writeModelRewrite(cacheConfigXmlDocument, config);
			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);
		}
		monitor.worked(1);

		// create resource model file
		final IFile resourceModelFile = config.isResourceModel() ? ResourceHelper.getFile(config.getModule().getProject(), config.getResourceFilePath(), config.getResourceFileName(), monitor) : null;
		if (config.isResourceModel()) {
			parser = new TemplateParser("phpMethod");
			parser.addVariable("visibility", "public");
			parser.addVariable("name", "_construct");
			String init = "$this->_init(\"" + config.getModelGroupName() + "/" + config.getModelName() + "\", \"" + config.getTableIdField() + "\");";
			parser.addVariable("contents", init);
			methods = parser.parse();

			parser = new TemplateParser("phpClass");
			parser.addVariable("methods", methods);
			parser.addVariable("className", config.getResourceClassName());
			parser.addVariable("extendsClassName", config.getResourceExtendsClass());
			parser.addVariable("extends", "extends");
			templateParsed = parser.parse();
			is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
			resourceModelFile.create(is, true, monitor);
		}
		monitor.worked(1);

		// create collection file
		final IFile collectionFile = config.isCollection() ? ResourceHelper.getFile(config.getModule().getProject(), config.getCollectionFilePath(), config.getCollectionFileName(), monitor) : null;
		if (config.isCollection()) {
			parser = new TemplateParser("phpMethod");
			parser.addVariable("visibility", "public");
			parser.addVariable("name", "_construct");
			String init = "$this->_init(\"" + config.getModelGroupName() + "/" + config.getModelName() + "\");";
			parser.addVariable("contents", init);
			methods = parser.parse();

			parser = new TemplateParser("phpClass");
			parser.addVariable("methods", methods);
			parser.addVariable("className", config.getCollectionClassName());
			parser.addVariable("extendsClassName", config.getCollectionExtendsClass());
			parser.addVariable("extends", "extends");
			templateParsed = parser.parse();
			is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
			collectionFile.create(is, true, monitor);
		}
		monitor.worked(1);

		if (config.isCreateInstallScript()) {
			if (config.getModule().getSetupName() == null) {
				MagentoWorker.createSetupResource(config.getModule(), null);
			}
		}

		// create setup script file
		final IFile setupScriptFile = config.isCreateInstallScript() ? ResourceHelper.getFile(config.getModule().getProject(), config.getSetupScriptFilePath(), config.getSetupScriptFileName(), monitor) : null;
		if (config.isCreateInstallScript()) {

			// create table
			StringBuilder createTable = new StringBuilder("$table = $installer->getConnection()\n");
			createTable.append("\t->newTable($installer->getTable('" + config.getModelGroupName() + "/" + config.getModelName() + "'))\n");
			for (Field field : config.getFileds()) {
				createTable.append("\t->addColumn('" + field.getUnderscoreName() + "', " + field.getMagentoType() + ", null, array(\n");
				for (String attribute : field.getAttributes()) {
					createTable.append("\t\t'" + attribute + "' => " + field.getAttribute(attribute) + ",\n");
				}
				createTable.append("\t\t), '" + I18n.get("field_comment", field.getCamelcaseName()) + "')\n");
			}
			createTable.append("\t->setComment('" + I18n.get("table_comment", config.getModelName()) + "');\n");
			createTable.append("$installer->getConnection()->createTable($table);\n");

			if (setupScriptFile.exists()) {
				final ArrayList<SearchMatch> matches = new ArrayList<SearchMatch>();
				SearchRequestor requestor = new SearchRequestor() {
					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						try {
							matches.add(match);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				SearchPattern pattern = SearchPattern.createPattern("startSetup", IDLTKSearchConstants.METHOD, IDLTKSearchConstants.ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH, PHPLanguageToolkit.getDefault());
				IDLTKSearchScope scope = SearchEngine.createSearchScope(DLTKCore.create(setupScriptFile));
				SearchEngine engine = new SearchEngine();
				engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
				int offset = 0;
				is = setupScriptFile.getContents();
				String fileContents = new java.util.Scanner(is, setupScriptFile.getCharset()).useDelimiter("\\A").next();
				if (matches.size() == 1) {
					SearchMatch match = matches.get(0);
					if (setupScriptFile.equals(match.getResource())) {
						offset = match.getOffset() + match.getLength() + 2;
					}
				}
				if (offset == 0) {
					offset = fileContents.indexOf("startSetup();") + "startSetup();".length() + 1;
				}
				StringBuilder builder = new StringBuilder(fileContents);
				builder.insert(offset, createTable.toString());
				is = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
				setupScriptFile.setContents(is, 0, monitor);
			} else {
				parser = new TemplateParser("magentoSetup");
				parser.addVariable("contents", createTable.toString());
				templateParsed = parser.parse();
				is = new ByteArrayInputStream(templateParsed.getBytes("UTF-8"));
				setupScriptFile.create(is, true, monitor);
			}
		}
		monitor.worked(1);

		// open files
		monitor.setTaskName(I18n.get("opening", config.getClassName()));
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, modelFile, true);
				} catch (PartInitException e) {
				}
			}
		});
		if (config.isResourceModel()) {
			monitor.setTaskName(I18n.get("opening", config.getResourceClassName()));
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, resourceModelFile, true);
					} catch (PartInitException e) {
					}
				}
			});
		}
		if (config.isCollection()) {
			monitor.setTaskName(I18n.get("opening", config.getCollectionClassName()));
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, collectionFile, true);
					} catch (PartInitException e) {
					}
				}
			});
		}
		if (config.isCreateInstallScript()) {
			monitor.setTaskName(I18n.get("opening", config.getSetupScriptFileName()));
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, setupScriptFile, true);
					} catch (PartInitException e) {
					}
				}
			});
		}
		monitor.worked(1);
	}

	private void writeEntityEntry(Document document, Configuration config) {
		XMLHelper.getElement(document, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.MODEL_NODE, config.getModule().getResourceModelGroupName(), XMLHelper.ENTITIES_NODE, config.getModelName(), XMLHelper.TABLE_NODE }, config.getTableName());
	}

	private void updateModuleVersion(Document document, Configuration config) {
		XMLHelper.getElement(document, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.MODULES_NODE, config.getModule().getModuleName(), XMLHelper.VERSION_NODE }, config.getVersionTarget());
	}

	private void writeModelRewrite(Document configXmlDocument, Configuration config) {
		String[] path = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.MODEL_NODE, config.getRewriteGroup(), XMLHelper.REWRITE_NODE, config.getRewriteName() };
		XMLHelper.getElement(configXmlDocument, path, config.getClassName());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}