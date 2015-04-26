package pl.mamooth.eclipse.magento.helpers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ResourceHelper {
	public static IFolder getFolder(IProject project, String[] path, IProgressMonitor monitor) throws CoreException {
		if (project == null || path == null)
			return null;
		if (path.length == 0)
			return null;
		IFolder folder = project.getFolder(path[0]);
		if (!folder.exists()) {
			folder.create(true, true, monitor);
		}
		for (int i = 1; i < path.length; ++i) {
			folder = folder.getFolder(path[i]);
			if (!folder.exists()) {
				folder.create(true, true, monitor);
			}
		}
		return folder;
	}

	public static IFile getFile(IProject project, String[] path, String name, IProgressMonitor monitor) throws CoreException {
		IFolder folder = getFolder(project, path, monitor);
		return folder.getFile(name);
	}

	public static IFolder getFolder(IProject project, String path, IProgressMonitor monitor) throws CoreException {
		return getFolder(project, path.split("/"), monitor);
	}

	public static IResource getResource(ISelection sel) {
		if (!(sel instanceof IStructuredSelection))
			return null;
		IStructuredSelection ss = (IStructuredSelection) sel;
		Object element = ss.getFirstElement();
		if (element instanceof IResource)
			return (IResource) element;
		if (!(element instanceof IAdaptable))
			return null;
		IAdaptable adaptable = (IAdaptable) element;
		Object adapter = adaptable.getAdapter(IResource.class);
		return (IResource) adapter;
	}

	public static IResource getResource(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		if (!(input instanceof IFileEditorInput))
			return null;
		return ((IFileEditorInput) input).getFile();
	}

	public static IResource getResource() {
		return getResource(getEditor());
	}

	public static IEditorPart getEditor() {
		IWorkbench iworkbench = PlatformUI.getWorkbench();
		if (iworkbench == null)
			return null;
		IWorkbenchWindow iworkbenchwindow = iworkbench.getActiveWorkbenchWindow();
		if (iworkbenchwindow == null)
			return null;
		IWorkbenchPage iworkbenchpage = iworkbenchwindow.getActivePage();
		if (iworkbenchpage == null)
			return null;
		IEditorPart ieditorpart = iworkbenchpage.getActiveEditor();
		return ieditorpart;
	}

	public static boolean folderExists(IProject project, String[] filePath) {
		if (project == null || filePath == null)
			return false;
		if (filePath.length == 0)
			return false;
		IFolder folder = project.getFolder(filePath[0]);
		if (!folder.exists()) {
			return false;
		}
		for (int i = 1; i < filePath.length; ++i) {
			folder = folder.getFolder(filePath[i]);
			if (!folder.exists()) {
				return false;
			}
		}
		return true;
	}

	public static boolean fileExists(IProject project, String[] filePath, String fileName) {
		if (project == null || filePath == null)
			return false;
		if (filePath.length == 0)
			return false;
		IFolder folder = project.getFolder(filePath[0]);
		if (!folder.exists()) {
			return false;
		}
		for (int i = 1; i < filePath.length; ++i) {
			folder = folder.getFolder(filePath[i]);
			if (!folder.exists()) {
				return false;
			}
		}
		IFile file = folder.getFile(fileName);
		return file.exists();
	}
}
