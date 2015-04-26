package pl.mamooth.eclipse.magento.helpers;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n extends SecurityManager {
	public static final String PREFIX = "resources.i18n";

	private static I18n _instance = new I18n();

	private I18n() {
	}

	public static String get(String key, Object... args) {

		Locale loc = Locale.getDefault();
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		@SuppressWarnings("rawtypes")
		Class[] stack = _instance.getClassContext();
		if (stack[1].getClassLoader() != null) {
			cl = stack[1].getClassLoader();
		}

		StackTraceElement elem[] = Thread.currentThread().getStackTrace();
		String message;
		try {
			String bname = PREFIX + "." + elem[2].getClassName();
			message = ResourceBundle.getBundle(bname, loc, cl).getString(elem[2].getMethodName() + "." + key);
		} catch (Exception e) {
			// message = elem[2].getClassName() + "::" + elem[2].getMethodName()
			// + "." + key;
			message = key;
		}

		return new MessageFormat(message).format(args);
	}
}
