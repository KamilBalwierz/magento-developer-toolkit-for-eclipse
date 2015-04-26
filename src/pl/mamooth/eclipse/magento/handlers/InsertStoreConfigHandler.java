package pl.mamooth.eclipse.magento.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import pl.mamooth.eclipse.magento.MagentoModule;
import pl.mamooth.eclipse.magento.dialogs.InsertStoreConfigDialog;
import pl.mamooth.eclipse.magento.helpers.I18n;
import pl.mamooth.eclipse.magento.helpers.ModuleHelper;
import pl.mamooth.eclipse.magento.helpers.ResourceHelper;
import pl.mamooth.eclipse.magento.helpers.TemplateParser;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class InsertStoreConfigHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public InsertStoreConfigHandler() {
	}

	protected String variable;
	protected String group;
	protected String section;
	protected String fields;

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getField() {
		return fields;
	}

	public void setField(String field) {
		this.fields = field;
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IEditorPart editorPart = ResourceHelper.getEditor();
		if (!(editorPart instanceof AbstractTextEditor)) {
			MessageBox msg = new MessageBox(window.getShell(), SWT.ICON_ERROR);
			msg.setMessage(I18n.get("editor_error"));
			msg.open();
			return null;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocumentProvider dp = editor.getDocumentProvider();
		IDocument doc = dp.getDocument(editor.getEditorInput());
		if (!(editor.getSelectionProvider().getSelection() instanceof ITextSelection)) {
			MessageBox msg = new MessageBox(window.getShell(), SWT.ICON_ERROR);
			msg.setMessage(I18n.get("selection_error"));
			msg.open();
			return null;
		}
		IResource resource = ResourceHelper.getResource(editorPart);
		IPath path = resource.getProjectRelativePath();
		IProject project = resource.getProject();
		String resourcePath = project.getName() + IPath.SEPARATOR + path.toPortableString();
		MagentoModule module = null;
		try {
			module = ModuleHelper.getModuleFromPath(resourcePath);
		} catch (CoreException e) {
			throw new ExecutionException(e.getLocalizedMessage());
		}
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		Dialog dialog;
		try {
			dialog = new InsertStoreConfigDialog(window.getShell(), this, module);
		} catch (Exception e) {
			throw new ExecutionException(e.getLocalizedMessage());
		}
		if (dialog.open() == Window.OK) {
			try {
				TemplateParser parser = new TemplateParser("getStoreConfig");
				parser.addVariable("variable", getVariable());
				StringBuilder sb = new StringBuilder();
				if (getSection().length() > 0) {
					sb.append(getSection());
					if (getGroup().length() > 0) {
						sb.append('/');
						sb.append(getGroup());
						if (getField().length() > 0) {
							sb.append('/');
							sb.append(getField());
						}
					}
				}
				parser.addVariable("configString", sb.toString());
				String templateParsed = parser.parse();
				doc.replace(selection.getOffset(), 0, templateParsed);
			} catch (BadLocationException e) {
				throw new ExecutionException(e.getLocalizedMessage());
			}
		}

		return null;
	}
}
