package pl.mamooth.eclipse.magento.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class MagentoPerspective implements IPerspectiveFactory {

	private IPageLayout factory;

	public MagentoPerspective() {
		super();
	}

	@Override
	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
		addNewWizardShortcuts();
		addPerspectiveShortcuts();
	}

	private void addViews() {
		String editorArea = factory.getEditorArea();

		IFolderLayout topLeft = factory.createFolder("top_left", IPageLayout.LEFT, 0.22f, editorArea);
		topLeft.addView("org.eclipse.php.ui.explorer");
		topLeft.addView("org.eclipse.dltk.ui.TypeHierarchy");

		IFolderLayout bottom = factory.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IPageLayout.ID_TASK_LIST);
		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		IFolderLayout outlineFolder = factory.createFolder("top_right", IPageLayout.RIGHT, 0.75f, editorArea);
		outlineFolder.addView(IPageLayout.ID_OUTLINE);
		outlineFolder.addPlaceholder("org.eclipse.php.ui.projectOutline");
		outlineFolder.addPlaceholder("pl.mamooth.eclipse.magento.outline.ModuleConfig");
		outlineFolder.addPlaceholder("org.eclipse.php.ui.functions");
	}

	private void addPerspectiveShortcuts() {
		factory.addPerspectiveShortcut("pl.mamooth.eclipse.magento.perspectives.MagentoPerspective");
		factory.addPerspectiveShortcut("org.eclipse.php.perspective");
		factory.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective");
	}

	private void addNewWizardShortcuts() {
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.module");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.model");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.block");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.controller");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.helper");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.storeconfig");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.event");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.cron");
		factory.addNewWizardShortcut("pl.mamooth.eclipse.magento.wizards.translation");
	}
}
