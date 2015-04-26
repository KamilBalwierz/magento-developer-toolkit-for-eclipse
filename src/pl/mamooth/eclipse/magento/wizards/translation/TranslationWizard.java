package pl.mamooth.eclipse.magento.wizards.translation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "csv". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

@SuppressWarnings("restriction")
public class TranslationWizard extends Wizard implements INewWizard {
	private TranslationPage page;
	private ISelection selection;

	/**
	 * Constructor for TranslationWizard.
	 */
	public TranslationWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	@Override
	public void addPages() {
		page = new TranslationPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final MagentoModule module = page.getModule();
		final String language = page.getLanguage();
		final boolean frontend = page.createFrontend();
		final boolean adminhtml = page.createAdminhtml();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(module, language, frontend, adminhtml, monitor);
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

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 * 
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */

	private void doFinish(MagentoModule module, String language, boolean frontend, boolean adminhtml, IProgressMonitor monitor) throws CoreException, ParserConfigurationException, TransformerException, IOException, XPathExpressionException {
		monitor.beginTask(I18n.get("create_config"), 5);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, monitor);
		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
		if (adminhtml) {
			XMLHelper.getElement(configXmlDocument, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.ADMINHTML_NODE, XMLHelper.TRANSLATE_NODE, XMLHelper.MODULES_NODE, module.getModuleName(), XMLHelper.FILES_NODE, XMLHelper.DEFAULT_NODE }, module.getModuleName() + FolderHelper.CSV_EXTENSION);
			XMLHelper.getElement(cacheXmlDocument, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.ADMINHTML_NODE, XMLHelper.TRANSLATE_NODE, XMLHelper.MODULES_NODE, module.getModuleName(), XMLHelper.FILES_NODE, XMLHelper.DEFAULT_NODE }, module.getModuleName() + FolderHelper.CSV_EXTENSION);
		}
		if (frontend) {
			XMLHelper.getElement(configXmlDocument, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.FRONTEND_NODE, XMLHelper.TRANSLATE_NODE, XMLHelper.MODULES_NODE, module.getModuleName(), XMLHelper.FILES_NODE, XMLHelper.DEFAULT_NODE }, module.getModuleName() + FolderHelper.CSV_EXTENSION);
			XMLHelper.getElement(cacheXmlDocument, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.FRONTEND_NODE, XMLHelper.TRANSLATE_NODE, XMLHelper.MODULES_NODE, module.getModuleName(), XMLHelper.FILES_NODE, XMLHelper.DEFAULT_NODE }, module.getModuleName() + FolderHelper.CSV_EXTENSION);

		}
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, monitor);
		XMLHelper.save(configXmlDocument, configXmlFile, monitor);
		monitor.worked(1);

		monitor.setTaskName(I18n.get("parsing_existing"));
		final IFile file = ResourceHelper.getFile(module.getProject(), StringHelper.concat(module.getAppFolderPath(), new String[] { FolderHelper.LOCALE_FOLDER, language }), module.getModuleName() + FolderHelper.CSV_EXTENSION, monitor);
		boolean overide = false;
		final HashMap<String, String> translatedWords = new HashMap<String, String>();
		if (file.exists()) {
			overide = true;
			InputStream contents = file.getContents();
			BufferedReader bf = new BufferedReader(new InputStreamReader(contents, file.getCharset()));
			for (String line = bf.readLine(); line != null; line = bf.readLine()) {
				String key = StringHelper.extract(line, 0);
				String value = StringHelper.extract(line, 1);
				translatedWords.put(key, value);
			}
		}
		monitor.worked(1);

		monitor.setTaskName(I18n.get("search_source"));
		SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				try {
					IFile file = (IFile) match.getResource();
					InputStream is = file.getContents();
					String wholeFile = new java.util.Scanner(is, file.getCharset()).useDelimiter("\\A").next();
					String invocation = wholeFile.substring(match.getOffset(), match.getOffset() + match.getLength());
					String text = StringHelper.extract(invocation, 0, "__");
					if (!translatedWords.containsKey(text)) {
						translatedWords.put(text, text);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		SearchPattern pattern = SearchPattern.createPattern("__", IDLTKSearchConstants.METHOD, IDLTKSearchConstants.ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH, PHPLanguageToolkit.getDefault());
		IDLTKSearchScope scope = SearchEngine.createSearchScope(module.getDLTKSourceFolder());
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, monitor);
		monitor.worked(1);

		monitor.setTaskName(I18n.get("search_xml"));
		searchXml(configXmlFile, translatedWords, monitor);
		searchXml(module.getSystemXml(), translatedWords, monitor);
		monitor.worked(1);

		monitor.setTaskName(I18n.get("save"));
		StringBuilder contents = new StringBuilder();
		for (String word : translatedWords.keySet()) {
			if (word.equals(""))
				continue;
			contents.append(word);
			contents.append(',');
			contents.append(translatedWords.get(word));
			contents.append(System.getProperty("line.separator"));
		}
		if (contents.length() == 0) {
			contents.append(System.getProperty("line.separator"));
		}
		InputStream is = new ByteArrayInputStream(contents.toString().getBytes());
		if (overide) {
			file.setContents(is, IFile.FORCE, monitor);
		} else {
			file.create(is, true, monitor);
		}
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
		monitor.worked(1);
	}

	public void searchXml(IFile file, Map<String, String> words, IProgressMonitor monitor) throws ParserConfigurationException, XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		Document document = XMLHelper.open(file, monitor);
		XPathExpression translatableNodes = xpath.compile("//*[@translate]");
		NodeList nodes = (NodeList) translatableNodes.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			XPathExpression translateAttribute = xpath.compile("@translate");
			Node attribute = (Node) translateAttribute.evaluate(node, XPathConstants.NODE);
			String[] childrenNames = attribute.getNodeValue().split(" ");
			for (String child : childrenNames) {
				XPathExpression translateNode = xpath.compile(child + "/text()");
				try {
					Node resultNode = (Node) translateNode.evaluate(node, XPathConstants.NODE);
					String text = StringHelper.escape(resultNode.getNodeValue());
					if (!words.containsKey(text)) {
						words.put(text, text);
					}
				} catch (XPathExpressionException e) {
					// maybe show some warning?
				}
			}
		}
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}