package pl.mamooth.eclipse.magento.importWizards.module;

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pl.mamooth.eclipse.magento.helpers.FolderHelper;

public class ImportedModule {
	protected String vendor;
	protected String name;
	protected String codePool;
	protected String version;
	protected String shortname;

	public static ImportedModule[] parseXmlFile(File module, File app) {
		LinkedList<ImportedModule> modules = new LinkedList<ImportedModule>();
		if (module.isFile()) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(module);
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath.compile("/config/modules/*[codePool]");
				NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); ++i) {
					Node node = nodes.item(i);
					String name = node.getNodeName();
					String[] nameNodes = name.split("_");
					if (nameNodes.length != 2) {
						System.out.println("Ommiting module " + name + " due to wrong node name");
						continue;
					}
					ImportedModule newModule = new ImportedModule();
					newModule.setVendor(nameNodes[0]);
					newModule.setName(nameNodes[1]);
					XPathExpression codePoolExpr = xpath.compile("codePool/text()");
					Node codePoolNode = (Node) codePoolExpr.evaluate(node, XPathConstants.NODE);
					String codePoolName = codePoolNode.getNodeValue();
					newModule.setCodePool(codePoolName);

					StringBuilder configFilePath = new StringBuilder(app.getAbsolutePath());
					if (configFilePath.charAt(configFilePath.length() - 1) != FolderHelper.DIRECTORY_SEPATATOR) {
						configFilePath.append(FolderHelper.DIRECTORY_SEPATATOR);
					}

					configFilePath.append(FolderHelper.CODE_FOLDER);
					configFilePath.append(FolderHelper.DIRECTORY_SEPATATOR);
					configFilePath.append(newModule.getCodePool());
					configFilePath.append(FolderHelper.DIRECTORY_SEPATATOR);
					configFilePath.append(newModule.getVendor());
					configFilePath.append(FolderHelper.DIRECTORY_SEPATATOR);
					configFilePath.append(newModule.getName());
					configFilePath.append(FolderHelper.DIRECTORY_SEPATATOR);
					configFilePath.append(FolderHelper.ETC_FOLDER);
					configFilePath.append(FolderHelper.DIRECTORY_SEPATATOR);
					configFilePath.append(FolderHelper.CONFIG_FILE);
					File configFile = new File(configFilePath.toString());
					if (!configFile.isFile() || !configFile.canRead()) {
						System.out.println("Ommiting module " + name + " due to config.xml not found at " + configFilePath.toString());
						continue;
					}
					// TODO get module version and shortcode?
					modules.add(newModule);
				}
			} catch (Exception e) {
				System.out.println("Error while parsing " + module.getAbsolutePath());
				e.printStackTrace();
			}

		}
		return modules.toArray(new ImportedModule[0]);
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodePool() {
		return codePool;
	}

	public void setCodePool(String codePool) {
		this.codePool = codePool;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(vendor);
		sb.append("_");
		sb.append(name);
		sb.append(" (");
		sb.append(codePool);
		sb.append(")");
		return sb.toString();
	}
}
