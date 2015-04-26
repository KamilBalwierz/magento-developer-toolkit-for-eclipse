package pl.mamooth.eclipse.magento.helpers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLHelper {

	public static final String CONFIG_NODE = "config";
	public static final String MODULES_NODE = "modules";
	public static final String VERSION_NODE = "version";
	public static final String CODEPOOL_NODE = "codePool";
	public static final String GLOBAL_NODE = "global";
	public static final String MODEL_NODE = "models";
	public static final String RESOUCE_NODE = "resources";
	public static final String BLOCKS_NODE = "blocks";
	public static final String HELPER_NODE = "helpers";
	public static final String SETUP_NODE = "setup";
	public static final String CONNECTION_NODE = "connection";
	public static final String CLASS_NODE = "class";
	public static final String ADMIN_NODE = "admin";
	public static final String ADMINHTML_NODE = "adminhtml";
	public static final String TRANSLATE_NODE = "translate";
	public static final String DEFAULT_NODE = "default";
	public static final String FILES_NODE = "files";
	public static final String FRONTEND_NODE = "frontend";
	public static final String ROUTER_NODE = "routers";
	public static final String ARG_NODE = "args";
	public static final String MODULE_NODE = "module";
	public static final String FRONTNAME_NODE = "frontName";
	public static final String ACTIVE_NODE = "active";
	public static final String USE_NODE = "use";
	public static final String RESOURCE_MODEL_NODE = "resourceModel";
	public static final String REWRITE_NODE = "rewrite";
	public static final String CRONTAB_NODE = "crontab";
	public static final String JOBS_NODE = "jobs";
	public static final String SHEDULE_NODE = "schedule";
	public static final String CRONEXPR_NODE = "cron_expr";
	public static final String RUN_NODE = "run";
	public static final String EVENTS_NODE = "events";
	public static final String OBSERVERS_NODE = "observers";
	public static final String METHOD_NODE = "method";
	public static final String TYPE_NODE = "type";
	public static final String SYSTEM_NODE = "system";
	public static final String TABS_NODE = "tabs";
	public static final String LABEL_NODE = "label";
	public static final String SORTORDER_NODE = "sort_order";
	public static final String SECTIONS_NODE = "sections";
	public static final String TAB_NODE = "tab";
	public static final String FRONTENDTYPE_NODE = "frontend_type";
	public static final String SHOWDEFAULT_NODE = "show_in_default";
	public static final String SHOWWEBSITE_NODE = "show_in_website";
	public static final String SHOWSTORE_NODE = "show_in_store";
	public static final String GROUPS_NODE = "groups";
	public static final String FIELDS_NODE = "fields";
	public static final String COMMENT_NODE = "comment";
	public static final String FRONTENDMODEL_NODE = "frontend_model";
	public static final String BACKENDMODEL_NODE = "backend_model";
	public static final String ENTITIES_NODE = "entities";
	public static final String TABLE_NODE = "table";
	public static final String LAYOUT_NODE = "layout";
	public static final String REFERENCES_NODE = "reference";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String BLOCK_NODE = "block";
	public static final String ALIAS_ATTRIBUTE = "as";
	public static final String TYPE_ATTRIBUTE = "type";
	public static final String TEMPLATE_ATTRIBUTE = "template";
	public static final String CHILDREN_NODE = "children";
	public static final String RESOURCES_NODE = "resources";
	public static final String ACL_NODE = "acl";
	public static final String UPDATES_NODE = "updates";
	public static final String FILE_NODE = "file";
	public static final String TITLE_NODE = "title";
	public static final String SOURCEMODEL_NODE = "source_model";

	public static Document open(IFile file, IProgressMonitor monitor) throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		if (!file.exists()) {
			return dBuilder.newDocument();
		}
		try {
			return dBuilder.parse(file.getContents());
		} catch (Exception e) {
			return dBuilder.newDocument();
		}
	}

	public static void writeAttribute(Document document, String[] path, String name, String value) {
		Element element = getElement(document, path, null);
		element.setAttribute(name, value);
	}

	public static Element getElement(Document document, String[] path, String value, String translate, String module) {
		Node node = document;
		NodeList list;
		for (int i = 0; i < path.length; ++i) {
			list = node.getChildNodes();
			boolean found = false;
			for (int j = 0; j < list.getLength(); ++j) {
				Node testNode = list.item(j);
				if (testNode.getNodeName().equals(path[i])) {
					found = true;
					node = testNode;
					break;
				}
			}
			if (found == false) {
				Element element = document.createElement(path[i]);
				node.appendChild(element);
				node = element;
			}
		}
		if (value != null) {
			Text text = document.createTextNode(value);
			list = node.getChildNodes();
			boolean found = false;
			for (int j = 0; j < list.getLength(); ++j) {
				Node testNode = list.item(j);
				if (testNode instanceof Text) {
					node.replaceChild(text, testNode);
					found = true;
					break;
				}
			}
			if (!found)
				node.appendChild(text);
		}
		if (node instanceof Element) {
			Element element = (Element) node;
			if (translate != null && element.getAttribute("translate").equals("")) {
				element.setAttribute("translate", translate);
			}
			if (module != null && element.getAttribute("module").equals("")) {
				element.setAttribute("module", module);
			}
		}
		return (Element) node;
	}

	public static Element getElement(Document document, String[] path, String value) {
		return getElement(document, path, value, null, null);
	}

	public static Element getElement(Document document, String path, String value, String translate, String module) {
		String[] pathArray = path.split("/");
		return getElement(document, pathArray, value, translate, module);
	}

	public static Element getElement(Document document, String path, String value) {
		return getElement(document, path, value, null, null);
	}

	public static void save(Document document, IFile file, IProgressMonitor monitor) throws TransformerException, CoreException, UnsupportedEncodingException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		DOMSource ds = new DOMSource(document);
		trans.transform(ds, sr);
		String xmlString = sw.toString();

		InputStream is = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));

		if (file.exists()) {
			file.setContents(is, 0, monitor);
		} else {
			file.create(is, true, monitor);
		}
	}

	public static void writeAttribute(Document document, String path, String name, String value) {
		writeAttribute(document, path.split("/"), name, value);
	}
}
