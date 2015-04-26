package pl.mamooth.eclipse.magento.helpers;

import java.util.LinkedList;

public class StringHelper {

	public static String join(String[] path, char c) {
		if (path.length == 0)
			return "";
		StringBuilder sb = new StringBuilder(path[0]);
		int i = 1;
		while (i < path.length) {
			sb.append(c);
			sb.append(path[i++]);
		}
		return sb.toString();
	}

	public static String[] subarray(String[] path, int start, int length) {
		String[] result = new String[length];
		System.arraycopy(path, start, result, 0, length);
		return result;
	}

	public static String[] concat(String[] first, String[] second) {
		String[] result = new String[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static String[] append(String[] first, String second) {
		String[] result = new String[first.length + 1];
		System.arraycopy(first, 0, result, 0, first.length);
		result[first.length] = second;
		return result;
	}

	public static String extract(String source, int index) {
		return extract(source, index, null, false);
	}

	public static String extract(String source, int index, String after) {
		return extract(source, index, after, false);
	}

	public static String extract(String source, int index, String after, boolean removeQuotes) {
		LinkedList<String> sections = new LinkedList<String>();
		LinkedList<Integer> starts = new LinkedList<Integer>();
		StringBuilder builder = new StringBuilder();
		boolean inSection = false;
		boolean isSingle = false;
		for (int i = 0; i < source.length(); ++i) {
			if (inSection) {
				if (source.charAt(i) == '\\' && i + 1 < source.length()) {
					if (source.charAt(i + 1) == '\'') {
						if (!removeQuotes)
							builder.append('\'');
						++i;
					} else if (source.charAt(i + 1) == '"') {
						if (!removeQuotes) {
							builder.append('\\');
							builder.append('"');
						}
						++i;
					} else {
						builder.append(source.charAt(i));
					}
				} else if (source.charAt(i) == '\'') {
					if (isSingle) {
						if (!removeQuotes)
							builder.append('"');
						inSection = false;
						sections.addLast(builder.toString());
						builder = new StringBuilder();
					} else {
						builder.append(source.charAt(i));
					}
				} else if (source.charAt(i) == '"') {
					if (isSingle) {
						if (!removeQuotes)
							builder.append('\\');
						builder.append(source.charAt(i));
					} else {
						if (!removeQuotes)
							builder.append('"');
						inSection = false;
						sections.addLast(builder.toString());
						builder = new StringBuilder();
					}
				} else {
					builder.append(source.charAt(i));
				}
			} else {
				if (source.charAt(i) == '\'') {
					if (!removeQuotes)
						builder.append('"');
					isSingle = true;
					inSection = true;
					starts.addLast(i);
				} else if (source.charAt(i) == '"') {
					if (!removeQuotes)
						builder.append('"');
					isSingle = false;
					inSection = true;
					starts.addLast(i);
				}
			}
		}
		if (after != null) {
			int found = source.indexOf(after);
			boolean done = false;
			while (!done && found != -1) {
				int endLast = -1;
				int nextStart = -1;
				for (int i = 0; i < starts.size(); ++i) {
					nextStart = starts.get(i);
					if (endLast < found && found < nextStart) {
						if (source.charAt(found + after.length()) == '(') {
							for (int j = i; j != 0; --j) {
								sections.removeFirst();
								starts.removeFirst();
								done = true;
							}
							break;
						}
					}
					endLast = starts.get(i) + sections.get(i).length();
				}
				found = source.indexOf(after, found + after.length());
			}
		}
		if (index < sections.size()) {
			return sections.get(index);
		}
		return "";
	}

	public static String escape(String text) {
		StringBuilder builder = new StringBuilder();
		builder.append('"');
		for (int i = 0; i < text.length(); ++i) {
			if (text.charAt(i) == '"') {
				builder.append('\\');
			}
			builder.append(text.charAt(i));
		}
		builder.append('"');
		return builder.toString();
	}

	public static String toCamelCase(String text) {
		return toCamelCase(text, false, false);
	}

	public static String toCamelCase(String text, boolean capitalizeFirst, boolean insertUnderscores) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < text.length(); ++i) {
			if (i == 0 && capitalizeFirst) {
				builder.append(Character.toUpperCase(text.charAt(i)));
			} else if (text.charAt(i) == '_') {
				if (insertUnderscores) {
					builder.append(Character.toUpperCase(text.charAt(i)));
				}
				if (i + 1 < text.length()) {
					builder.append(Character.toUpperCase(text.charAt(++i)));
				}
			} else {
				builder.append(text.charAt(i));
			}
		}
		return builder.toString();
	}

	public static String toName(String className, String baseClassName) {
		StringBuilder builder = new StringBuilder();
		int j = 0;
		for (int i = 0; i < baseClassName.length(); ++i, ++j) {
			if (baseClassName.charAt(i) != className.charAt(j))
				return "";
		}
		if (!baseClassName.equals(""))
			++j; // trailing underscore after base class name
		for (int i = 0; j < className.length(); ++j, ++i) {
			if (i == 0) {
				builder.append(Character.toLowerCase(className.charAt(j)));
			} else if (className.charAt(j) == '_') {
				builder.append(Character.toLowerCase(className.charAt(j)));
				if (j + 1 < className.length()) {
					builder.append(Character.toLowerCase(className.charAt(++j)));
				}
			} else {
				builder.append(className.charAt(j));
			}
		}
		return builder.toString();
	}
}