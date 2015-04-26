package pl.mamooth.eclipse.magento;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MagentoEclipsePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "pl.mamooth.eclipse.magento"; //$NON-NLS-1$
	public static final String PERSPECTIVE_ID = "pl.mamooth.eclipse.magento.perspectives.MagentoPerspective";

	// The shared instance
	private static MagentoEclipsePlugin plugin;

	protected BundleContext context;

	public BundleContext getContext() {
		return context;
	}

	/**
	 * The constructor
	 */
	public MagentoEclipsePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		// MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
		// "Licence", "Licence is invalid you fuck");
		// throw new Exception("License is invalid you fuck");
		super.start(context);
		this.context = context;
		plugin = this;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static MagentoEclipsePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
