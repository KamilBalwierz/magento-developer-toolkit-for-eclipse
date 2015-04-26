package pl.mamooth.eclipse.magento.wizards.storeconfig;

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
import org.w3c.dom.Document;

import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.StringHelper;
import pl.mamooth.eclipse.magento.helpers.XMLHelper;

public class NewStoreConfigWizard extends Wizard implements INewWizard {
	private static final String TRUE = "1";
	private static final String FALSE = "0";
	private SectionPage sectionPage;
	private GroupPage groupPage;
	private FieldPage fieldPage;
	private ISelection selection;
	private Configuration configuration;

	public SectionPage getSectionPage() {
		return sectionPage;
	}

	public GroupPage getGroupPage() {
		return groupPage;
	}

	public FieldPage getFiledPage() {
		return fieldPage;
	}

	public ISelection getSelection() {
		return selection;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public NewStoreConfigWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		configuration = new Configuration();
		sectionPage = new SectionPage(this);
		addPage(sectionPage);
		groupPage = new GroupPage(this);
		addPage(groupPage);
		fieldPage = new FieldPage(this);
		addPage(fieldPage);

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
			e.printStackTrace();
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), I18n.get("error"), realException.getMessage());
			return false;
		}
		return true;
	}

	protected void doFinish(Configuration config, IProgressMonitor monitor) throws CoreException, ParserConfigurationException, UnsupportedEncodingException, TransformerException {
		monitor.beginTask(I18n.get("creating", config.getFieldLabel()), 2);

		IFile systemXmlFile = config.getModule().getSystemXml();
		Document systemXmlDocument = XMLHelper.open(systemXmlFile, monitor);
		IFile cacheSystemXmlFile = config.getModule().getCacheSystemXml();
		Document cacheSystemXmlDocument = XMLHelper.open(cacheSystemXmlFile, monitor);
		writeConfigField(systemXmlDocument, config);
		writeConfigField(cacheSystemXmlDocument, config);
		XMLHelper.save(systemXmlDocument, systemXmlFile, monitor);
		XMLHelper.save(cacheSystemXmlDocument, cacheSystemXmlFile, monitor);

		if (config.getAclTitle() != null && config.getAclTitle().length() != 0) {
			IFile configXmlFile = config.getModule().getConfigXml();
			Document configXmlDocument = XMLHelper.open(configXmlFile, monitor);
			IFile cacheConfigXmlFile = config.getModule().getCacheConfigXml();
			Document cacheConfigXmlDocument = XMLHelper.open(cacheConfigXmlFile, monitor);
			writeAclNode(configXmlDocument, config);
			writeAclNode(cacheConfigXmlDocument, config);
			XMLHelper.save(configXmlDocument, configXmlFile, monitor);
			XMLHelper.save(cacheConfigXmlDocument, cacheConfigXmlFile, monitor);
		}
	}

	private void writeAclNode(Document document, Configuration config) {
		String[] prefix = new String[] { XMLHelper.CONFIG_NODE, XMLHelper.ADMINHTML_NODE, XMLHelper.ACL_NODE, XMLHelper.RESOURCES_NODE, XMLHelper.ADMIN_NODE, XMLHelper.CHILDREN_NODE, XMLHelper.SYSTEM_NODE, XMLHelper.CHILDREN_NODE, XMLHelper.CONFIG_NODE, XMLHelper.CHILDREN_NODE, config.getAclName() };
		XMLHelper.getElement(document, prefix, null, XMLHelper.TITLE_NODE, config.getHelper());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.TITLE_NODE), config.getAclTitle());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SORTORDER_NODE), Integer.toString(config.getAclSortOrder()));
	}

	private void writeConfigField(Document document, Configuration config) {
		String[] prefix = new String[] { XMLHelper.SYSTEM_NODE, XMLHelper.TABS_NODE, config.getTabName() };
		if (!config.isTabExists()) {
			XMLHelper.getElement(document, prefix, null, XMLHelper.LABEL_NODE, config.getHelper());
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.LABEL_NODE), config.getTabLabel());
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SORTORDER_NODE), Integer.toString(config.getTabSortOrder()));
		}

		prefix = new String[] { XMLHelper.SYSTEM_NODE, XMLHelper.SECTIONS_NODE, config.getSectionName() };
		XMLHelper.getElement(document, prefix, null, XMLHelper.LABEL_NODE, config.getHelper());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.LABEL_NODE), config.getSectionLabel());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.TAB_NODE), config.getTabName());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.FRONTENDTYPE_NODE), "text");
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SORTORDER_NODE), Integer.toString(config.getSectionSortOrder()));
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWDEFAULT_NODE), config.isSectionShowDefault() ? TRUE : FALSE);
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWWEBSITE_NODE), config.isSectionShowWebsite() ? TRUE : FALSE);
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWSTORE_NODE), config.isSectionShowStore() ? TRUE : FALSE);

		prefix = StringHelper.append(prefix, XMLHelper.GROUPS_NODE);
		prefix = StringHelper.append(prefix, config.getGroupName());
		XMLHelper.getElement(document, prefix, null, XMLHelper.LABEL_NODE, config.getHelper());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.LABEL_NODE), config.getGroupLabel());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.FRONTENDTYPE_NODE), "text");
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SORTORDER_NODE), Integer.toString(config.getGroupSortOrder()));
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWDEFAULT_NODE), config.isGroupShowDefault() ? TRUE : FALSE);
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWWEBSITE_NODE), config.isGroupShowWebsite() ? TRUE : FALSE);
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWSTORE_NODE), config.isGroupShowStore() ? TRUE : FALSE);

		prefix = StringHelper.append(prefix, XMLHelper.FIELDS_NODE);
		prefix = StringHelper.append(prefix, config.getFieldName());
		String tanslate = XMLHelper.LABEL_NODE;
		if (config.getFiledComment() != null && !config.getFiledComment().equals("")) {
			tanslate = XMLHelper.LABEL_NODE + " " + XMLHelper.COMMENT_NODE;
		}
		XMLHelper.getElement(document, prefix, null, tanslate, config.getHelper());
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.LABEL_NODE), config.getFieldLabel());
		if (config.getFiledComment() != null && !config.getFiledComment().equals("")) {
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.COMMENT_NODE), config.getFiledComment());
		}
		if (config.getFieldFrontendModel() != null && !config.getFieldFrontendModel().equals("")) {
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.FRONTENDMODEL_NODE), config.getFieldFrontendModel());
		}
		if (config.getFieldFrontendType() != null && !config.getFieldFrontendType().equals("")) {
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.FRONTENDTYPE_NODE), config.getFieldFrontendType());
		}
		if (config.getFieldSourceModel() != null && !config.getFieldSourceModel().equals("")) {
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SOURCEMODEL_NODE), config.getFieldSourceModel());
		}
		if (config.getFieldBackednModel() != null && !config.getFieldBackednModel().equals("")) {
			XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.BACKENDMODEL_NODE), config.getFieldBackednModel());
		}
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SORTORDER_NODE), Integer.toString(config.getFieldSortOrder()));
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWDEFAULT_NODE), config.isFieldShowDefault() ? TRUE : FALSE);
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWWEBSITE_NODE), config.isFieldShowWebsite() ? TRUE : FALSE);
		XMLHelper.getElement(document, StringHelper.append(prefix, XMLHelper.SHOWSTORE_NODE), config.isFieldShowStore() ? TRUE : FALSE);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}