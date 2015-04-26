package pl.mamooth.eclipse.magento.wizards.modman;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;

import pl.mamooth.eclipse.magento.MagentoModule;

public class Configuration {

	private MagentoModule _module;
	private List<Object> _checked;
	private List<Object> _grayed;
	private IFolder _root;

	public void setRoot(IFolder root) {
		_root = root;
	}

	public IFolder getRoot() {
		return _root;
	}

	public MagentoModule getModule() {
		return _module;
	}

	public void setModule(MagentoModule module) {
		_module = module;
	}

	public List<Object> getChecked() {
		return _checked;
	}

	public void setChecked(Object[] checked) {
		_checked = Arrays.asList(checked);
	}

	public List<Object> getGrayed() {
		return _grayed;
	}

	public void setGrayed(Object[] grayed) {
		_grayed = Arrays.asList(grayed);
	}

}
