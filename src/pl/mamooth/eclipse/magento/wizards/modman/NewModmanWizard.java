package pl.mamooth.eclipse.magento.wizards.modman;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

import pl.mamooth.eclipse.magento.helpers.I18n;

public class NewModmanWizard extends Wizard implements INewWizard {
	private ISelection selection;
	private Configuration configuration;
	private NamePage modmanPage;

	public NewModmanWizard() {
		super();
		setNeedsProgressMonitor(true);
		configuration = new Configuration();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void addPages() {
		configuration = new Configuration();
		modmanPage = new NamePage(this);
		addPage(modmanPage);
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

	protected void doFinish(final Configuration config, IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException, ParserConfigurationException, TransformerException, InterruptedException {
		monitor.beginTask(I18n.get("creating"), 3);

		UIJob job = new UIJob(I18n.get("job")) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				config.setChecked(modmanPage.getTree().getCheckedElements());
				config.setGrayed(modmanPage.getTree().getGrayedElements());
				config.setRoot((IFolder) modmanPage.getTree().getInput());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		job.join();

		monitor.worked(1);

		final IFile modmanFile = config.getRoot().getFile("modman");

		int length = config.getRoot().getFullPath().toString().length() + 1;

		StringBuilder contents = new StringBuilder();
		for (Object element : config.getChecked()) {
			IResource resource = (IResource) element;
			if (element.equals(modmanFile)) {
				continue;
			}
			if (config.getGrayed().contains(element)) {
				continue;
			}
			String path = resource.getFullPath().toString().substring(length);
			contents.append(path);
			contents.append("\t");
			contents.append(path);
			contents.append("\n");

		}

		InputStream is = new ByteArrayInputStream(contents.toString().getBytes("UTF-8"));
		if (modmanFile.exists()) {
			modmanFile.setContents(is, 0, monitor);
		} else {
			modmanFile.create(is, true, monitor);
		}
		monitor.worked(1);

		// open files
		monitor.setTaskName(I18n.get("opening"));
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, modmanFile, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public ISelection getSelection() {
		return selection;
	}
}