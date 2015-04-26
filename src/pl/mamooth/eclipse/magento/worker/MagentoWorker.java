package pl.mamooth.eclipse.magento.worker;

import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.helpers.FolderHelper;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;

public class MagentoWorker {

	protected static String[] globalPath = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.GLOBAL_NODE };

	public static void createModelGroup(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = module.getShortName();
		}
		String baseClass = module.getVendor() + '_' + module.getName() + '_' + FolderHelper.MODEL_FOLDER;

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeModelGroup(module, name, configXmlDocument, baseClass);
		_writeModelGroup(module, name, cacheXmlDocument, baseClass);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistModelGroupName(name);
	}

	protected static void _writeModelGroup(MagentoModule module, String name, Document configFile, String baseClass) {
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.MODEL_NODE, name, XMLHelper.CLASS_NODE }), baseClass);
	}

	public static void createResourceModelGroup(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = module.getShortName() + "_resource";
		}
		String baseClass = module.getVendor() + '_' + module.getName() + '_' + FolderHelper.MODEL_FOLDER + '_' + FolderHelper.RESOUCE_FOLDER;

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeResourceModelGroup(module, name, configXmlDocument, baseClass);
		_writeResourceModelGroup(module, name, cacheXmlDocument, baseClass);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistResourceModelGroupName(name);
	}

	protected static void _writeResourceModelGroup(MagentoModule module, String name, Document configFile, String baseClass) {
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.MODEL_NODE, name, XMLHelper.CLASS_NODE }), baseClass);
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.MODEL_NODE, module.getModelGroupName(), XMLHelper.RESOURCE_MODEL_NODE }), name);
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.RESOUCE_NODE, module.getModelGroupName() + "_read", XMLHelper.CONNECTION_NODE, XMLHelper.USE_NODE }), "core_read");
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.RESOUCE_NODE, module.getModelGroupName() + "_write", XMLHelper.CONNECTION_NODE, XMLHelper.USE_NODE }), "core_write");
	}

	public static void createSetupResource(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = module.getShortName() + "_setup";
		}

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeSetupResource(module, name, configXmlDocument);
		_writeSetupResource(module, name, cacheXmlDocument);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistSetupResourceName(name);
	}

	protected static void _writeSetupResource(MagentoModule module, String name, Document configFile) {
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.RESOUCE_NODE, name, XMLHelper.CONNECTION_NODE, XMLHelper.USE_NODE }), "core_setup");
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.RESOUCE_NODE, name, XMLHelper.SETUP_NODE, XMLHelper.MODULE_NODE }), module.getVendor() + '_' + module.getName());
	}

	public static void createHelperGroup(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = module.getShortName() + '_' + FolderHelper.HELPER_FOLDER;
		}

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeHelperGroup(module, name, configXmlDocument);
		_writeHelperGroup(module, name, cacheXmlDocument);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistHelperGroupName(name);
	}

	protected static void _writeHelperGroup(MagentoModule module, String name, Document configFile) {
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.HELPER_NODE, name, XMLHelper.CLASS_NODE }), module.getVendor() + '_' + module.getName() + '_' + FolderHelper.HELPER_FOLDER);

	}

	public static void createBlockGroup(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = module.getShortName() + '_' + FolderHelper.BLOCK_FOLDER;
		}

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeBlockGroup(module, name, configXmlDocument);
		_writeBlockGroup(module, name, cacheXmlDocument);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistBlockGroupName(name);
	}

	protected static void _writeBlockGroup(MagentoModule module, String name, Document configFile) {
		XMLHelper.getElement(configFile, StringHelper.concat(globalPath, new String[] { XMLHelper.BLOCKS_NODE, name, XMLHelper.CLASS_NODE }), module.getVendor() + '_' + module.getName() + '_' + FolderHelper.BLOCK_FOLDER);
	}

	public static void createFrontendStandardRouter(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = module.getShortName();
		}

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeFrontendStandardRouter(module, name, configXmlDocument);
		_writeFrontendStandardRouter(module, name, cacheXmlDocument);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistFrontendControllerFrontname(name);
	}

	protected static void _writeFrontendStandardRouter(MagentoModule module, String name, Document configFile) {
		XMLHelper.getElement(configFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.FRONTEND_NODE, XMLHelper.ROUTER_NODE, module.getShortName(), XMLHelper.USE_NODE }, "standard");
		XMLHelper.getElement(configFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.FRONTEND_NODE, XMLHelper.ROUTER_NODE, module.getShortName(), XMLHelper.ARG_NODE, XMLHelper.MODULE_NODE }, module.getVendor() + '_' + module.getName());
		XMLHelper.getElement(configFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.FRONTEND_NODE, XMLHelper.ROUTER_NODE, module.getShortName(), XMLHelper.ARG_NODE, XMLHelper.FRONTNAME_NODE }, name);

	}

	public static void createAdminhtmlAdminRouter(MagentoModule module, String name) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		if (name == null) {
			name = "admin_" + module.getShortName();
		}

		IFile configXmlFile = module.getConfigXml();
		Document configXmlDocument = XMLHelper.open(configXmlFile, null);
		IFile cacheXmlFile = module.getCacheConfigXml();
		Document cacheXmlDocument = XMLHelper.open(cacheXmlFile, null);
		_writeAdminhtmlAdminRouter(module, name, configXmlDocument);
		_writeAdminhtmlAdminRouter(module, name, cacheXmlDocument);
		XMLHelper.save(configXmlDocument, configXmlFile, null);
		XMLHelper.save(cacheXmlDocument, cacheXmlFile, null);

		module.persistAdminhtmlControllerFrontname(name);
	}

	protected static void _writeAdminhtmlAdminRouter(MagentoModule module, String name, Document configFile) {
		String adminShortName = module.getShortName() + "_admin";
		XMLHelper.getElement(configFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.ADMIN_NODE, XMLHelper.ROUTER_NODE, adminShortName, XMLHelper.USE_NODE }, "admin");
		XMLHelper.getElement(configFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.ADMIN_NODE, XMLHelper.ROUTER_NODE, adminShortName, XMLHelper.ARG_NODE, XMLHelper.MODULE_NODE }, module.getVendor() + '_' + module.getName() + '_' + FolderHelper.ADMIN_CONTROLLERS_SUBFOLDER);
		XMLHelper.getElement(configFile, new String[] { XMLHelper.CONFIG_NODE, XMLHelper.ADMIN_NODE, XMLHelper.ROUTER_NODE, adminShortName, XMLHelper.ARG_NODE, XMLHelper.FRONTNAME_NODE }, name);
	}

}
