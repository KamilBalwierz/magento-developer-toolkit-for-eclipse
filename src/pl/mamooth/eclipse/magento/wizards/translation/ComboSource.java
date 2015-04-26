package pl.mamooth.eclipse.magento.wizards.translation;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;

public class ComboSource {

	protected static String[] labels = null;
	protected static String[] codes = null;
	protected static LinkedList<String> namesList = null;

	public static String[] getLanguagesLabels() {
		if (labels == null || codes == null)
			prepare();
		return labels;
	}

	public static int getHint(String text) {
		int result = Collections.binarySearch(namesList, text.toUpperCase());
		if (result < 0)
			result = -result - 1;
		return result;
	}

	private static void prepare() {
		Locale[] list = Locale.getAvailableLocales();
		LinkedList<Locale> toSort = new LinkedList<Locale>();
		for (Locale locale : list) {
			if (locale.getLanguage().length() == 0 || locale.getCountry().length() == 0 || locale.getDisplayName().length() == 0)
				continue;
			toSort.push(locale);
		}
		Collections.sort(toSort, new Comparator<Locale>() {
			@Override
			public int compare(Locale arg0, Locale arg1) {
				String first = arg0.getDisplayName();
				String second = arg1.getDisplayName();
				return second.compareTo(first);
			}
		});
		namesList = new LinkedList<String>();
		LinkedList<String> codesList = new LinkedList<String>();
		for (Locale locale : toSort) {
			namesList.push(locale.getDisplayName());
			codesList.push(locale.getLanguage() + "_" + locale.getCountry());
		}
		labels = namesList.toArray(new String[0]);
		codes = codesList.toArray(new String[0]);
	}

	public static String getLanguageCode(int i) {
		return codes[i];
	}
}
