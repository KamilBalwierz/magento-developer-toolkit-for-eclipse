package pl.mamooth.eclipse.magento;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.SearchMatch;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.SearchRequestor;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.mamooth.eclipse.magento.helpers.EventHelper;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;
import pl.mamooth.eclipse.magento.wizards.module.Configuration;
import pl.mamooth.eclipse.magento.wizards.storeconfig.GroupData;
import pl.mamooth.eclipse.magento.wizards.storeconfig.SectionData;
import pl.mamooth.eclipse.magento.wizards.storeconfig.TabData;

@SuppressWarnings("restriction")
public class MagentoModule {

	private final class VersionComparator implements Comparator<String> {
		public int[] toArray(String version) {
			int[] result = new int[3];
			String[] tokens = version.split("\\.");
			if (tokens.length != 3)
				return null;
			for (int i = 0; i < 3; ++i) {
				result[i] = Integer.parseInt(tokens[i]);
			}
			return result;
		}

		@Override
		public int compare(String o1, String o2) {
			int[] version1 = toArray(o1);
			int[] version2 = toArray(o2);
			if (version1 == null) {
				return -1;
			}
			if (version2 == null) {
				return 1;
			}
			for (int i = 0; i < 3; ++i) {
				if (version1[i] < version2[i]) {
					return -1;
				}
				if (version1[i] > version2[i]) {
					return 1;
				}
			}
			return 0;
		}
	}

	public static final String PROPERTY_VENDOR = "vendor";
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CODEPOOL = "codepool";
	public static final String PROPERTY_MODEL = "model";
	public static final String PROPERTY_RESOURCE_MODEL = "resourcemodel";
	public static final String PROPERTY_HELPER = "helper";
	public static final String PROPERTY_BLOCK = "block";
	public static final String PROPERTY_SETUP = "setup";
	public static final String PROPERTY_SHORTNAME = "shortname";
	public static final String PROPERTY_DEFAULT_FRONTNAME = "dfrontname";
	public static final String PROPERTY_ADMIN_FRONTNAME = "afrontname";
	public static final String PROPERTY_TEMPLATE = "template";
	public static final String PROPERTY_PACKAGE = "package";

	private String vendor;
	private String name;
	private String shortName;
	private String codePool;
	private String modelGroupName;
	private String resourceModelGroupName;
	private String setupName;
	private String blockGroupName;
	private String helperGroupName;
	private String defaultControllersFrontName;
	private String adminControllersFrontName;

	private String moduleName;
	private IProject project;

	private XPathFactory factory;
	private XPath xpath;

	private String[] modelClassess = null;
	private String[] helperClassess = null;
	private String[] blockClassess = null;
	private String[] configTabs;
	private String[] configSections;
	private String[] frontend_models;
	private String[] backend_models;
	private String[] events;
	private String[] eventPrefixes;

	private static Map<String, MagentoModule> activeModules = new HashMap<String, MagentoModule>();
	private Map<String, String[]> designReferences = new HashMap<String, String[]>();
	private String[] handlers;
	private String[] source_models;

	public static MagentoModule getMagentoModule(IProject project, String moduleName) throws CoreException {
		if (activeModules.containsKey(moduleName)) {
			return activeModules.get(moduleName);
		}
		return new MagentoModule(project, moduleName);
	}

	private void registerModuleAsSingleton() {
		String name = this.getModuleName();
		activeModules.put(name, this);
	}

	public static String[] getModulePath(IProject project, String moduleName) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		String codePool = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_CODEPOOL));
		String vendor = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_VENDOR));
		String name = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_NAME));
		if (codePool != null && vendor != null && name != null) {
			return new String[] { FolderHelper.CODE_FOLDER, codePool, vendor, name };
		}
		return null;
	}

	public MagentoModule(IProject project, Configuration configuration) throws CoreException {
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		this.project = project;
		moduleName = configuration.getVendor() + '_' + configuration.getName();
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		vendor = configuration.getVendor();
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_VENDOR), vendor);
		name = configuration.getName();
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_NAME), name);
		shortName = configuration.getShortName();
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_SHORTNAME), shortName);
		codePool = configuration.getCodePool();
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_CODEPOOL), codePool);
		if (configuration.isModelGroup()) {
			modelGroupName = configuration.getModelGroupName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_MODEL), modelGroupName);
		} else {
			modelGroupName = null;
		}
		if (configuration.isResourceModelGroup()) {
			resourceModelGroupName = configuration.getResourceModelGroupName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_RESOURCE_MODEL), resourceModelGroupName);
		} else {
			resourceModelGroupName = null;
		}
		if (configuration.isSetup()) {
			setupName = configuration.getSetupName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_SETUP), setupName);
		} else {
			setupName = null;
		}
		if (configuration.isBlockGroup()) {
			blockGroupName = configuration.getBlockGroupName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_BLOCK), blockGroupName);
		} else {
			blockGroupName = null;
		}
		if (configuration.isHelperGroup()) {
			helperGroupName = configuration.getHelperGroupName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_HELPER), helperGroupName);
		} else {
			helperGroupName = null;
		}
		if (configuration.isDefaultControllers()) {
			defaultControllersFrontName = configuration.getDefaultControllersFrontName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_DEFAULT_FRONTNAME), defaultControllersFrontName);
		} else {
			defaultControllersFrontName = null;
		}
		if (configuration.isAdminControllers()) {
			adminControllersFrontName = configuration.getAdminControllersFrontName();
			project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_ADMIN_FRONTNAME), adminControllersFrontName);
		} else {
			adminControllersFrontName = null;
		}
		ModuleHelper.registerModule(project, this);
		registerModuleAsSingleton();
	}

	private MagentoModule(IProject project, String moduleName) throws CoreException {
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		this.moduleName = moduleName;
		this.project = project;
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		vendor = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_VENDOR));
		name = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_NAME));
		shortName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_SHORTNAME));
		codePool = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_CODEPOOL));
		modelGroupName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_MODEL));
		resourceModelGroupName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_RESOURCE_MODEL));
		setupName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_SETUP));
		blockGroupName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_BLOCK));
		helperGroupName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_HELPER));
		defaultControllersFrontName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_DEFAULT_FRONTNAME));
		adminControllersFrontName = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_ADMIN_FRONTNAME));
		registerModuleAsSingleton();
	}

	public IProject getProject() {
		return project;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModelGroupName(String modelGroupName) {
		this.modelGroupName = modelGroupName;
	}

	public void setResourceModelGroupName(String resourceModelGroupName) {
		this.resourceModelGroupName = resourceModelGroupName;
	}

	public void setSetupName(String setupName) {
		this.setupName = setupName;
	}

	public void setBlockGroupName(String blockGroupName) {
		this.blockGroupName = blockGroupName;
	}

	public void setHelperGroupName(String helperGroupName) {
		this.helperGroupName = helperGroupName;
	}

	public void setDefaultControllersFrontName(String defaultControllersFrontName) {
		this.defaultControllersFrontName = defaultControllersFrontName;
	}

	public void setAdminControllersFrontName(String adminControllersFrontName) {
		this.adminControllersFrontName = adminControllersFrontName;
	}

	public String getVendor() {
		return vendor;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String getCodePool() {
		return codePool;
	}

	public String getModelGroupName() {
		return modelGroupName;
	}

	public String getResourceModelGroupName() {
		return resourceModelGroupName;
	}

	public String getSetupName() {
		return setupName;
	}

	public String getBlockGroupName() {
		return blockGroupName;
	}

	public String getHelperGroupName() {
		return helperGroupName;
	}

	public String getDefaultControllerRouter() {
		return shortName;
	}

	public String getDefaultControllersFrontName() {
		return defaultControllersFrontName;
	}

	public String getAdminControllersFrontName() {
		return adminControllersFrontName;
	}

	public String getAdminControllerRouter() {
		return shortName;
	}

	public String[] getSourceFolderPath() throws CoreException {
		String[] modulePath = getModulePath(project, moduleName);
		String[] appPath = getAppFolderPath();
		if (modulePath == null)
			return null;
		if (appPath == null)
			return null;
		return StringHelper.concat(appPath, modulePath);
	}

	public String[] getAppFolderPath() throws CoreException {
		return ModuleHelper.getAppFolderPath(project);
	}

	public IFile getConfigXml() throws CoreException {
		String[] sourcePath = getSourceFolderPath();
		if (sourcePath == null)
			return null;
		String[] path = StringHelper.append(sourcePath, FolderHelper.ETC_FOLDER);
		return ResourceHelper.getFile(project, path, FolderHelper.CONFIG_FILE, null);
	}

	public IFile getSystemXml() throws CoreException {
		String[] sourcePath = getSourceFolderPath();
		if (sourcePath == null)
			return null;
		String[] path = StringHelper.append(sourcePath, FolderHelper.ETC_FOLDER);
		return ResourceHelper.getFile(project, path, FolderHelper.SYSTEM_FILE, null);
	}

	public IFile getLayoutXml(String scope, String designPackage, String template, String templateFileName) throws CoreException {
		String[] designPath = getDesignFolderPath();
		if (designPath == null)
			return null;
		String[] path = StringHelper.concat(designPath, new String[] { scope, designPackage, template, FolderHelper.LAYOUT_FOLDER });
		return ResourceHelper.getFile(project, path, templateFileName, null);
	}

	private String[] getDesignFolderPath() throws CoreException {
		String[] appPath = getAppFolderPath();
		if (appPath == null)
			return null;
		return StringHelper.append(appPath, FolderHelper.DESIGN_FOLDER);
	}

	public IFile getCacheConfigXml() throws CoreException {
		return ResourceHelper.getFile(project, new String[] { FolderHelper.PLUGIN_CACHE_FOLDER }, FolderHelper.CONFIG_FILE, null);
	}

	public IFile getCacheSystemXml() throws CoreException {
		return ResourceHelper.getFile(project, new String[] { FolderHelper.PLUGIN_CACHE_FOLDER }, FolderHelper.SYSTEM_FILE, null);
	}

	public IFile getCacheStructXml() throws CoreException {
		return ResourceHelper.getFile(project, new String[] { FolderHelper.PLUGIN_CACHE_FOLDER }, FolderHelper.STRUCT_FILE, null);
	}

	public IFile getCacheLayoutXml(String scope) throws CoreException {
		return ResourceHelper.getFile(project, new String[] { FolderHelper.PLUGIN_CACHE_FOLDER }, FolderHelper.LAYOUT_FILE_PREFIX + scope + FolderHelper.XML_EXTENSION, null);
	}

	public IFile getModuleXml() throws CoreException {
		String[] sourcePath = getAppFolderPath();
		if (sourcePath == null)
			return null;
		String[] path = StringHelper.concat(sourcePath, new String[] { FolderHelper.ETC_FOLDER, FolderHelper.MODULES_FOLDER });
		return ResourceHelper.getFile(project, path, getModuleName() + FolderHelper.XML_EXTENSION, null);
	}

	public IModelElement getDLTKSourceFolder() throws CoreException {
		// String[] path = getSourceFolderPath();
		// if (path == null)
		// return null;
		// IFolder folder = ResourceHelper.getFolder(project, path, null);
		// IModelElement element = DLTKCore.create(folder);
		// return element;
		return DLTKCore.create(getProject());
	}

	public String[] getModelGroups() throws ParserConfigurationException, CoreException, XPathExpressionException {
		ArrayList<String> groups = new ArrayList<String>();
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] xpathSource = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.MODEL_NODE, "*" };
		StringBuilder sb = new StringBuilder();
		for (String xpathIncrement : xpathSource) {
			sb.append('/');
			sb.append(xpathIncrement);
		}
		XPathExpression translatableNodes = xpath.compile(sb.toString());
		NodeList nodes = (NodeList) translatableNodes.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			groups.add(node.getNodeName());
		}
		Collections.sort(groups);
		return groups.toArray(new String[0]);
	}

	public String[] getBlockGroups() throws ParserConfigurationException, CoreException, XPathExpressionException {
		ArrayList<String> groups = new ArrayList<String>();
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] xpathSource = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.BLOCKS_NODE, "*" };
		StringBuilder sb = new StringBuilder();
		for (String xpathIncrement : xpathSource) {
			sb.append('/');
			sb.append(xpathIncrement);
		}
		XPathExpression translatableNodes = xpath.compile(sb.toString());
		NodeList nodes = (NodeList) translatableNodes.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			groups.add(node.getNodeName());
		}
		Collections.sort(groups);
		return groups.toArray(new String[0]);
	}

	public String[] getHelperGroups() throws ParserConfigurationException, CoreException, XPathExpressionException {
		ArrayList<String> groups = new ArrayList<String>();
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] xpathSource = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.HELPER_NODE, "*" };
		StringBuilder sb = new StringBuilder();
		for (String xpathIncrement : xpathSource) {
			sb.append('/');
			sb.append(xpathIncrement);
		}
		XPathExpression translatableNodes = xpath.compile(sb.toString());
		NodeList nodes = (NodeList) translatableNodes.evaluate(document, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			groups.add(node.getNodeName());
		}
		Collections.sort(groups);
		return groups.toArray(new String[0]);
	}

	public String getClassBaseForModelGroup(String modelGroup) throws ParserConfigurationException, CoreException, XPathExpressionException {
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] xpathSource = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.MODEL_NODE, modelGroup, XMLHelper.CLASS_NODE, "text()" };
		StringBuilder sb = new StringBuilder();
		for (String xpathIncrement : xpathSource) {
			sb.append('/');
			sb.append(xpathIncrement);
		}
		XPathExpression translatableNodes = xpath.compile(sb.toString());
		Node node = (Node) translatableNodes.evaluate(document, XPathConstants.NODE);
		if (node == null)
			return "";
		return node.getNodeValue();
	}

	public String getClassBaseForHelperGroup(String modelGroup) throws ParserConfigurationException, CoreException, XPathExpressionException {
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] xpathSource = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.HELPER_NODE, modelGroup, XMLHelper.CLASS_NODE, "text()" };
		StringBuilder sb = new StringBuilder();
		for (String xpathIncrement : xpathSource) {
			sb.append('/');
			sb.append(xpathIncrement);
		}
		XPathExpression translatableNodes = xpath.compile(sb.toString());
		Node node = (Node) translatableNodes.evaluate(document, XPathConstants.NODE);
		if (node == null)
			return "";
		return node.getNodeValue();
	}

	public String[] getClassesStartingWith(String baseClass) throws CoreException, ParserConfigurationException, XPathExpressionException {
		final ArrayList<String> classes = new ArrayList<String>();
		SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				try {
					IFile file = (IFile) match.getResource();
					InputStream is = file.getContents();
					String wholeFile = new java.util.Scanner(is, file.getCharset()).useDelimiter("\\A").next();
					String definition = wholeFile.substring(match.getOffset(), match.getOffset() + match.getLength());
					classes.add(definition);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		SearchPattern pattern = SearchPattern.createPattern(baseClass, IDLTKSearchConstants.TYPE, IDLTKSearchConstants.DECLARATIONS, SearchPattern.R_PREFIX_MATCH, PHPLanguageToolkit.getDefault());
		IDLTKSearchScope scope = SearchEngine.createSearchScope(getDLTKSourceFolder());
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
		Document document = XMLHelper.open(getCacheStructXml(), null);
		XPathExpression classess = xpath.compile("/structure/classes/*[starts-with(name(),'" + baseClass + "')]");
		classes.addAll(getNames(classess, document));
		Collections.sort(classes);
		return classes.toArray(new String[0]);
	}

	protected ArrayList<String> getNames(XPathExpression expression, Node node) throws XPathExpressionException {
		ArrayList<String> result = new ArrayList<String>();
		NodeList nodes = (NodeList) expression.evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i) {
			result.add(nodes.item(i).getNodeName());
		}
		return result;
	}

	public String[] getSystemSections() throws XPathExpressionException, ParserConfigurationException, CoreException {
		Document document = XMLHelper.open(getCacheSystemXml(), null);
		ArrayList<String> list = getNames(xpath.compile("/system/sections/*"), document);
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

	public String[] getSystemGroups(String section) throws ParserConfigurationException, CoreException, XPathExpressionException {
		Document document = XMLHelper.open(getCacheSystemXml(), null);
		ArrayList<String> list = getNames(xpath.compile("/system/sections/" + section + "/groups/*"), document);
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

	public String[] getSystemFields(String section, String group) throws ParserConfigurationException, CoreException, XPathExpressionException {
		Document document = XMLHelper.open(getCacheSystemXml(), null);
		ArrayList<String> list = getNames(xpath.compile("/system/sections/" + section + "/groups/" + group + "/fields/*"), document);
		Collections.sort(list);
		return list.toArray(new String[0]);
	}

	public String getResourceModelGroupName(String modelGroupName) {
		// this should get resource model group for model group specified in
		// argument
		return getResourceModelGroupName();
	}

	public String getMagentoClassIdentifier(String className) throws ParserConfigurationException, CoreException, XPathExpressionException {
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] classNameTokens = className.split("_");
		if (classNameTokens.length < 3) {
			return null;
		}
		StringBuilder classNameBase = new StringBuilder();
		for (int i = 0; i < 3 && i < classNameTokens.length; ++i) {
			if (i != 0) {
				classNameBase.append('_');
			}
			classNameBase.append(classNameTokens[i]);
		}
		String scope = null;
		if (classNameTokens[2].equals(FolderHelper.MODEL_FOLDER)) {
			scope = XMLHelper.MODEL_NODE;
		} else if (classNameTokens[2].equals(FolderHelper.HELPER_FOLDER)) {
			scope = XMLHelper.HELPER_NODE;
		} else if (classNameTokens[2].equals(FolderHelper.BLOCK_FOLDER)) {
			scope = XMLHelper.BLOCKS_NODE;
		} else {
			return null;
		}
		String groupXPathExpr = "/config/global/" + scope + "/*[class='" + classNameBase.toString() + "']";
		XPathExpression groupXPath = xpath.compile(groupXPathExpr);
		Node groupNode = (Node) groupXPath.evaluate(document, XPathConstants.NODE);
		String groupId;
		if (groupNode == null) {
			groupId = classNameTokens[1].toLowerCase();
		} else {
			groupId = groupNode.getNodeName();
		}
		String name = StringHelper.toName(className, classNameBase.toString());
		return groupId + '/' + name;
	}

	public String[] getHelperClasses() throws XPathExpressionException, ParserConfigurationException, CoreException {
		if (helperClassess == null) {
			String[] groups = getHelperGroups();
			List<String> clasess = new LinkedList<String>();
			for (String group : groups) {
				String base = getClassBaseForHelperGroup(group);
				if (!base.equals("")) {
					String[] groupClasses = getClassesStartingWith(base);
					for (String c : groupClasses) {
						clasess.add(c);
					}
				}
			}
			helperClassess = clasess.toArray(new String[] {});
		}
		return helperClassess;
	}

	public String[] getModelClasses() throws XPathExpressionException, ParserConfigurationException, CoreException {
		if (modelClassess == null) {
			String[] groups = getModelGroups();
			List<String> clasess = new LinkedList<String>();
			for (String group : groups) {
				String base = getClassBaseForModelGroup(group);
				if (!base.equals("")) {
					String[] groupClasses = getClassesStartingWith(base);
					for (String c : groupClasses) {
						clasess.add(c);
					}
				}
			}
			modelClassess = clasess.toArray(new String[] {});
		}
		return modelClassess;
	}

	public String[] getBlockClasses() throws XPathExpressionException, ParserConfigurationException, CoreException {
		if (blockClassess == null) {
			String[] groups = getBlockGroups();
			List<String> clasess = new LinkedList<String>();
			for (String group : groups) {
				String base = getClassBaseForBlockGroup(group);
				if (!base.equals("")) {
					String[] groupClasses = getClassesStartingWith(base);
					for (String c : groupClasses) {
						clasess.add(c);
					}
				}
			}
			blockClassess = clasess.toArray(new String[] {});
		}
		return blockClassess;
	}

	private String getClassBaseForBlockGroup(String group) throws ParserConfigurationException, CoreException, XPathExpressionException {
		Document document = XMLHelper.open(getCacheConfigXml(), null);
		String[] xpathSource = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE, XMLHelper.BLOCKS_NODE, group, XMLHelper.CLASS_NODE, "text()" };
		StringBuilder sb = new StringBuilder();
		for (String xpathIncrement : xpathSource) {
			sb.append('/');
			sb.append(xpathIncrement);
		}
		XPathExpression translatableNodes = xpath.compile(sb.toString());
		Node node = (Node) translatableNodes.evaluate(document, XPathConstants.NODE);
		if (node == null)
			return "";
		return node.getNodeValue();
	}

	public String getHelperClassBase() {
		return getVendor() + "_" + getName() + "_" + FolderHelper.HELPER_FOLDER;
	}

	public String[] getHelperPath() throws CoreException {
		return StringHelper.append(getSourceFolderPath(), FolderHelper.HELPER_FOLDER);
	}

	protected String getNameFromPath(String path, String[] compareToPath) {
		String[] base = FolderHelper.getTrailingPath(path, getProject().getName(), compareToPath);
		if (base == null)
			return null;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < base.length; ++i) {
			String token = base[i].toLowerCase();
			if (token.endsWith(FolderHelper.PHP_EXTENSION)) {
				result.append(token.substring(0, token.length() - FolderHelper.PHP_EXTENSION.length()));
			} else {
				result.append(token);
				result.append('_');
			}
		}
		return result.toString();
	}

	public String getHelperNameFromPath(String path) throws CoreException {
		return getNameFromPath(path, getHelperPath());
	}

	public String getBlockClassBase() {
		return getVendor() + "_" + getName() + "_" + FolderHelper.BLOCK_FOLDER;
	}

	public String[] getBlockPath() throws CoreException {
		return StringHelper.append(getSourceFolderPath(), FolderHelper.BLOCK_FOLDER);
	}

	public String getBlockNameFromPath(String path) throws CoreException {
		return getNameFromPath(path, getBlockPath());
	}

	public String getModelClassBase() {
		return getVendor() + "_" + getName() + "_" + FolderHelper.MODEL_FOLDER;
	}

	public String[] getModelPath() throws CoreException {
		return StringHelper.append(getSourceFolderPath(), FolderHelper.MODEL_FOLDER);
	}

	public String getModelNameFromPath(String path) throws CoreException {
		return getNameFromPath(path, getModelPath());
	}

	public String getControllerClassBase() {
		return getVendor() + "_" + getName();
	}

	public String[] getControllerPath() throws CoreException {
		return StringHelper.append(getSourceFolderPath(), FolderHelper.CONTROLLERS_FOLDER);
	}

	public String getControllerNameFromPath(String path) throws CoreException {
		return getNameFromPath(path, getControllerPath());
	}

	public String[] getAviableEvents() {
		if (events == null) {
			events = EventHelper.getAviableEvents(getEventPrefixes(), getControllerHandlers(), getConfigSections());
		}
		return events;
	}

	private String[] getControllerHandlers() {
		if (handlers == null) {
			// TODO get controller handlers
			try {
				HashSet<String> handlersSet = new HashSet<String>();
				Document frontend = XMLHelper.open(getCacheLayoutXml("frontend"), null);
				Document adminhtml = XMLHelper.open(getCacheLayoutXml("adminhtml"), null);
				Document[] layoutfiles = new Document[] { frontend, adminhtml };
				XPathExpression expression = xpath.compile("/layout/*");
				for (Document document : layoutfiles) {
					handlersSet.addAll(getNames(expression, document));
				}
				handlers = handlersSet.toArray(new String[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return handlers;
	}

	private String[] getEventPrefixes() {
		if (eventPrefixes == null) {
			final HashSet<String> prefixes = new HashSet<String>();
			try {
				Document document = XMLHelper.open(getCacheStructXml(), null);
				XPathExpression expr = xpath.compile("//eventPrefix/text()");
				NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
				if (nodes != null) {
					for (int i = 0; i < nodes.getLength(); ++i) {
						Node node = nodes.item(i);
						prefixes.add(node.getNodeValue());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				SearchRequestor requestor = new SearchRequestor() {
					@Override
					public void acceptSearchMatch(SearchMatch match) throws CoreException {
						try {
							IFile file = (IFile) match.getResource();
							InputStream is = file.getContents();
							String wholeFile = new java.util.Scanner(is, file.getCharset()).useDelimiter("\\A").next();
							String definition = wholeFile.substring(match.getOffset(), match.getOffset() + match.getLength());
							String value = StringHelper.extract(definition, 0, "=", true);
							if (value != null && value.length() > 0)
								prefixes.add(value);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				SearchPattern pattern = SearchPattern.createPattern("$_eventPrefix", IDLTKSearchConstants.FIELD, IDLTKSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH, PHPLanguageToolkit.getDefault());
				IDLTKSearchScope scope;
				scope = SearchEngine.createSearchScope(getDLTKSourceFolder());
				SearchEngine engine = new SearchEngine();
				engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			eventPrefixes = prefixes.toArray(new String[] {});
		}
		return eventPrefixes;
	}

	public String getVersion() {
		try {
			Document document = XMLHelper.open(getCacheConfigXml(), null);
			String expr = StringHelper.join(new String[] { "", XMLHelper.CONFIG_NODE, XMLHelper.MODULES_NODE, getModuleName(), XMLHelper.VERSION_NODE, "text()" }, '/');
			XPathExpression xpathExpr = xpath.compile(expr);
			Node node = (Node) xpathExpr.evaluate(document, XPathConstants.NODE);
			if (node != null) {
				return node.getNodeValue();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getPreviousVersion(int major, int minor, int change) {
		try {
			if (getSetupName() == null)
				return null;
			IFolder folder = ResourceHelper.getFolder(getProject(), getSetupScriptPath(), null);
			IResource[] resources = folder.members();
			ArrayList<String> versions = new ArrayList<String>();
			for (IResource resource : resources) {
				if (resource.getType() != IResource.FILE)
					continue;
				IFile file = (IFile) resource;
				String name = file.getName();
				String[] tokens = name.split("-");
				if (tokens != null) {
					for (String token : tokens) {
						token = token.replaceAll("\\.[pP][hH][pP]$", "");
						if (!Pattern.matches("^[0-9]+\\.[0-9]+\\.[0-9]+$", token))
							continue;
						versions.add(token);
					}
				}
			}
			Collections.sort(versions, new VersionComparator());
			for (String version : versions) {
				System.out.println(version);
			}
			String version = major + "." + minor + "." + change;
			int index = Collections.binarySearch(versions, version, new VersionComparator());
			if (index == 0) {
				return null;
			}
			if (index < 0) {
				index += 1;
				index = -index;
				while (index >= versions.size()) {
					--index;
				}
				while (index >= 0 && new VersionComparator().compare(versions.get(index), version) >= 0)
					--index;
				if (index < 0)
					return null;
				return versions.get(index);
			}
			while (index >= 0 && new VersionComparator().compare(versions.get(index), version) == 0) {
				--index;
			}
			if (index < 0)
				return null;
			return versions.get(index);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String[] getSetupScriptPath() throws CoreException {
		if (getSetupName() == null) {
			return StringHelper.concat(getSourceFolderPath(), new String[] { FolderHelper.SETUP_FOLDER, getShortName() + "_setup" });
		}
		return StringHelper.concat(getSourceFolderPath(), new String[] { FolderHelper.SETUP_FOLDER, getSetupName() });
	}

	public String getDesignTemplate(String scope) {
		try {
			String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
			String template = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_TEMPLATE + '#' + scope));
			if (template != null) {
				return template;
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "default";
	}

	public String getDesignPackage(String scope) {
		try {
			String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
			String designPackage = project.getPersistentProperty(new QualifiedName(qualifier, PROPERTY_PACKAGE + '#' + scope));
			if (designPackage != null)
				return designPackage;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return "base";
	}

	public String[] getDesignHandlers() {
		return getControllerHandlers();
	}

	public String[] getDesignRefernces(String scope) throws ParserConfigurationException, CoreException, XPathExpressionException {
		if (designReferences.containsKey(scope)) {
			return designReferences.get(scope);
		}
		Document document = XMLHelper.open(getCacheLayoutXml(scope), null);
		XPathExpression expr = xpath.compile("//block/@name");
		NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		HashSet<String> references = new HashSet<String>();
		for (int i = 0; i < nodes.getLength(); ++i) {
			Node node = nodes.item(i);
			references.add(node.getNodeValue());
		}
		String[] result = references.toArray(new String[] {});
		designReferences.put(scope, result);
		return result;
	}

	public void setDesignTemplate(String scope, String template) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_TEMPLATE + '#' + scope), template);
	}

	public void setDesignPackage(String scope, String designPackage) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_PACKAGE + '#' + scope), designPackage);
	}

	public String[] getConfigFieldNames(String section, String group) {
		try {
			String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.SECTIONS_NODE, section, XMLHelper.GROUPS_NODE, group, XMLHelper.FIELDS_NODE, "*" }, '/');
			XPathExpression expression = xpath.compile(expr);
			Document document = XMLHelper.open(getCacheSystemXml(), null);
			ArrayList<String> names;
			names = getNames(expression, document);
			return names.toArray(new String[] {});
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] {};
		}
	}

	public String[] getConfigGroupNames(String section) {
		try {
			String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.SECTIONS_NODE, section, XMLHelper.GROUPS_NODE, "*" }, '/');
			XPathExpression expression = xpath.compile(expr);
			Document document = XMLHelper.open(getCacheSystemXml(), null);
			ArrayList<String> names;
			names = getNames(expression, document);
			return names.toArray(new String[] {});
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] {};
		}
	}

	public String[] getConfigSections() {
		if (configSections == null) {
			try {
				String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.SECTIONS_NODE, "*" }, '/');
				XPathExpression expression = xpath.compile(expr);
				Document document = XMLHelper.open(getCacheSystemXml(), null);
				ArrayList<String> names;
				names = getNames(expression, document);
				configSections = names.toArray(new String[] {});
			} catch (Exception e) {
				e.printStackTrace();
				return new String[] {};
			}
		}
		return configSections;
	}

	public String[] getConfigTabs() {
		if (configTabs == null) {
			try {
				String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.TABS_NODE, "*" }, '/');
				XPathExpression expression = xpath.compile(expr);
				Document document = XMLHelper.open(getCacheSystemXml(), null);
				ArrayList<String> names;
				names = getNames(expression, document);
				configTabs = names.toArray(new String[] {});
			} catch (Exception e) {
				e.printStackTrace();
				return new String[] {};
			}
		}
		return configTabs;
	}

	public SectionData getConfigSection(String section) {
		try {
			String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.SECTIONS_NODE, section }, '/');
			XPathExpression expression = xpath.compile(expr);
			Node sectionNode = (Node) expression.evaluate(XMLHelper.open(getCacheSystemXml(), null), XPathConstants.NODE);
			if (sectionNode != null) {
				SectionData data = new SectionData();
				data.setName(section);
				expression = xpath.compile("label/text()");
				data.setLabel((String) expression.evaluate(sectionNode, XPathConstants.STRING));
				expression = xpath.compile("sort_order/text()");
				data.setSortOrder(((Double) expression.evaluate(sectionNode, XPathConstants.NUMBER)).intValue());
				expression = xpath.compile("tab/text()");
				data.setTab((String) expression.evaluate(sectionNode, XPathConstants.STRING));
				expression = xpath.compile("show_in_default/text()");
				data.setVisibleDefault((Boolean) expression.evaluate(sectionNode, XPathConstants.BOOLEAN));
				expression = xpath.compile("show_in_store/text()");
				data.setVisibleStore((Boolean) expression.evaluate(sectionNode, XPathConstants.BOOLEAN));
				expression = xpath.compile("show_in_website/text()");
				data.setVisibleWebsite((Boolean) expression.evaluate(sectionNode, XPathConstants.BOOLEAN));
				String[] prefix = new String[] { "", XMLHelper.CONFIG_NODE, XMLHelper.ADMINHTML_NODE, XMLHelper.ACL_NODE, XMLHelper.RESOURCES_NODE, XMLHelper.ADMIN_NODE, XMLHelper.CHILDREN_NODE, XMLHelper.SYSTEM_NODE, XMLHelper.CHILDREN_NODE, XMLHelper.CONFIG_NODE, XMLHelper.CHILDREN_NODE, section };
				Document config = XMLHelper.open(getCacheConfigXml(), null);
				expression = xpath.compile(StringHelper.join(prefix, '/') + "/title/text()");
				data.setAclLabel((String) expression.evaluate(config, XPathConstants.STRING));
				expression = xpath.compile(StringHelper.join(prefix, '/') + "/sort_order/text()");
				data.setAclSortOrder(((Double) expression.evaluate(sectionNode, XPathConstants.NUMBER)).intValue());
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public TabData getConfigTab(String tab) {
		try {
			String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.TABS_NODE, tab }, '/');
			XPathExpression expression = xpath.compile(expr);
			Node tabNode = (Node) expression.evaluate(XMLHelper.open(getCacheSystemXml(), null), XPathConstants.NODE);
			if (tabNode != null) {
				TabData data = new TabData();
				data.setName(tab);
				expression = xpath.compile("label/text()");
				data.setLabel((String) expression.evaluate(tabNode, XPathConstants.STRING));
				expression = xpath.compile("sort_order/text()");
				data.setSortOrder(((Double) expression.evaluate(tabNode, XPathConstants.NUMBER)).intValue());
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public GroupData getConfigGroup(String section, String group) {
		try {
			String expr = StringHelper.join(new String[] { "", XMLHelper.SYSTEM_NODE, XMLHelper.SECTIONS_NODE, section, XMLHelper.GROUPS_NODE, group }, '/');
			XPathExpression expression = xpath.compile(expr);
			Node groupNode = (Node) expression.evaluate(XMLHelper.open(getCacheSystemXml(), null), XPathConstants.NODE);
			if (groupNode != null) {
				GroupData data = new GroupData();
				data.setName(group);
				expression = xpath.compile("label/text()");
				data.setLabel((String) expression.evaluate(groupNode, XPathConstants.STRING));
				expression = xpath.compile("sort_order/text()");
				data.setSortOrder(((Double) expression.evaluate(groupNode, XPathConstants.NUMBER)).intValue());
				expression = xpath.compile("show_in_default/text()");
				data.setVisibleDefault((Boolean) expression.evaluate(groupNode, XPathConstants.BOOLEAN));
				expression = xpath.compile("show_in_store/text()");
				data.setVisibleStore((Boolean) expression.evaluate(groupNode, XPathConstants.BOOLEAN));
				expression = xpath.compile("show_in_website/text()");
				data.setVisibleWebsite((Boolean) expression.evaluate(groupNode, XPathConstants.BOOLEAN));
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String[] getBackendModels() {
		if (backend_models == null) {
			try {
				String expr = "/structure/classes/*[contains(name(),'Model']";
				XPathExpression expression = xpath.compile(expr);
				Document document = XMLHelper.open(getCacheStructXml(), null);
				ArrayList<String> names;
				names = getNames(expression, document);
				backend_models = new String[names.size()];
				for (int i = 0; i < names.size(); ++i) {
					backend_models[i] = getMagentoClassIdentifier(names.get(i));
				}
			} catch (Exception e) {
				return new String[] {};
			}
		}
		return backend_models;
	}

	public String[] getFrontedModels() throws XPathExpressionException, ParserConfigurationException, CoreException {
		if (frontend_models == null) {
			String[] blocks = getBlockClasses();
			frontend_models = new String[blocks.length];
			for (int i = 0; i < blocks.length; ++i) {
				frontend_models[i] = getMagentoClassIdentifier(blocks[i]);
			}
		}
		return frontend_models;
	}

	public void persistModelGroupName(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		modelGroupName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_MODEL), modelGroupName);
	}

	public void persistResourceModelGroupName(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		resourceModelGroupName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_RESOURCE_MODEL), resourceModelGroupName);
	}

	public void persistSetupResourceName(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		setupName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_SETUP), setupName);
	}

	public void persistHelperGroupName(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		helperGroupName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_HELPER), helperGroupName);
	}

	public void persistBlockGroupName(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		blockGroupName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_BLOCK), blockGroupName);
	}

	public void persistFrontendControllerFrontname(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		defaultControllersFrontName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_DEFAULT_FRONTNAME), defaultControllersFrontName);
	}

	public void persistAdminhtmlControllerFrontname(String name) throws CoreException {
		String qualifier = MagentoEclipsePlugin.PLUGIN_ID + "#" + moduleName;
		adminControllersFrontName = name;
		project.setPersistentProperty(new QualifiedName(qualifier, PROPERTY_ADMIN_FRONTNAME), adminControllersFrontName);
	}

	public String[] getSourceModels() {
		if (source_models == null) {
			try {
				String expr = "/structure/classes/*[method/text()='toOptionArray']";
				XPathExpression expression = xpath.compile(expr);
				Document document = XMLHelper.open(getCacheStructXml(), null);
				ArrayList<String> names;
				names = getNames(expression, document);
				source_models = new String[names.size()];
				for (int i = 0; i < names.size(); ++i) {
					source_models[i] = getMagentoClassIdentifier(names.get(i));
				}
			} catch (Exception e) {
				return new String[] {};
			}
		}
		return source_models;
	}
}
