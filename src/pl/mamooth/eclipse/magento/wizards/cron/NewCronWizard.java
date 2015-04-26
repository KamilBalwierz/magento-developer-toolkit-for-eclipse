package pl.mamooth.eclipse.magento.wizards.cron;

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
public class NewCronWizard extends Wizard implements INewWizard {
	private NamePage namePage;
	private ISelection selection;
	private Configuration configuration;

	public NamePage getNamePage() {
		return namePage;
	}

	public ISelection getSelection() {
		return selection;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public NewCronWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		configuration = new Configuration();
		namePage = new NamePage(this);
		addPage(namePage);

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
		TemplateParser parser = null;

		if (config.getModule().getModelGroupName() == null) {
			MagentoWorker.createModelGroup(config.getModule(), null);
		}

		parser = new TemplateParser("phpMethod");
		parser.addVariable("visibility", "public");
		parser.addVariable("name", config.getFunctionName());
		String methodContents = parser.parse();

		final IFile cronFile = ResourceHelper.getFile(config.getModule().getProject(), config.getFilePath(), config.getFileName(), monitor);

		if (cronFile.exists()) {
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
			SearchPattern pattern = SearchPattern.createPattern(config.getClassName(), IDLTKSearchConstants.TYPE, IDLTKSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH, PHPLanguageToolkit.getDefault());
			IDLTKSearchScope scope = SearchEngine.createSearchScope(DLTKCore.create(cronFile));
			SearchEngine engine = new SearchEngine();
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
			int offset = 0;
			InputStream is = cronFile.getContents();
			String fileContents = new java.util.Scanner(is, cronFile.getCharset()).useDelimiter("\\A").next();
			if (matches.size() == 1) {
				SearchMatch match = matches.get(0);
				if (cronFile.equals(match.getResource())) {
					offset = fileContents.indexOf('{', match.getOffset() + match.getLength()) + 2;
				}
			}
			StringBuilder builder = new StringBuilder(fileContents);
			builder.insert(offset, methodContents);
			is = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
			cronFile.setContents(is, 0, monitor);
		} else {
			parser = new TemplateParser("phpClass");
			parser.addVariable("className", config.getClassName());
			parser.addVariable("methods", methodContents);
			String fileContents = parser.parse();
			InputStream is = new ByteArrayInputStream(fileContents.getBytes("UTF-8"));
			cronFile.create(is, true, monitor);
		}
		monitor.worked(1);

		// insert crontask into config
		IFile configXmlFile = config.getModule().getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
		IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
		Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
		writeCronTask(configXmlDocument, config);
		writeCronTask(cacheConfigXmlDocument, config);
		XMLHelper.save(configXmlDocument, configXmlFile, monitor);
		XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);

		monitor.worked(1);

		// open files
		monitor.setTaskName(I18n.get("opening", config.getClassName()));
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, cronFile, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private void writeCronTask(Document document, Configuration config) {
		XMLHelper.getElement(document, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.CRONTAB_NODE, XMLHelper.JOBS_NODE, config.getTaskName(), XMLHelper.SHEDULE_NODE, XMLHelper.CRONEXPR_NODE }, config.getCronExpr());
		XMLHelper.getElement(document, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.CRONTAB_NODE, XMLHelper.JOBS_NODE, config.getTaskName(), XMLHelper.RUN_NODE, "model" }, config.getModelGroup() + "/" + config.getModelName() + "::" + config.getFunctionName());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}