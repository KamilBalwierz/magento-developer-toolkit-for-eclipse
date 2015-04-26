package pl.mamooth.eclipse.magento.helpers;

import java.util.Arrays;

import org.eclipse.swt.widgets.Combo;

public class UIHelper {
	public static boolean comboSelected(Combo combo) {
		return comboSelected(combo, true);
	}

	public static boolean comboSelected(Combo combo, boolean sorted) {
		if (sorted) {
			return Arrays.binarySearch(combo.getItems(), combo.getText()) >= 0;
		} else {
			for (String element : combo.getItems())
				if (element.equals(combo.getText()))
					return true;
		}
		return false;
	}

	public static int getComboIndex(Combo combo) {
		return getComboIndex(combo, true);
	}

	public static int getComboIndex(Combo combo, boolean sorted) {
		if (sorted) {
			return Arrays.binarySearch(combo.getItems(), combo.getText());
		} else {
			for (int i = 0; i < combo.getItems().length; ++i) {
				if (combo.getItem(i).equals(combo.getText()))
					return i;
			}
		}
		return -1;
	}
}
