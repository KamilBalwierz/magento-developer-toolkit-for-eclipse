package pl.mamooth.eclipse.magento.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import pl.mamooth.eclipse.magento.MagentoEclipsePlugin;

public class TemplateParser {
	protected String template;
	protected Map<String, String> variables;

	public TemplateParser(String template) {
		this.template = template;
		variables = new HashMap<String, String>();
	}

	public void addVariable(String name, String value) {
		variables.put(name, value);
	}

	public String getVariable(String name) {
		String var = variables.get(name);
		if (var == null)
			return "";
		return var;
	}

	public String parse() {
		InputStream in = getInputStream();
		StringBuilder builder = new StringBuilder();
		StringBuilder variable = new StringBuilder();
		int ch;
		try {
			ch = in.read();
			while (ch != -1) {
				if (ch == '{') {
					ch = in.read();
					if (ch == '@') {
						ch = in.read();
						while (ch != '}') {
							variable.append((char) ch);
							ch = in.read();
						}
						builder.append(getVariable(variable.toString()));
						variable = new StringBuilder();
					} else {
						builder.append('{');
						builder.append((char) ch);
					}
				} else {
					builder.append((char) ch);
				}
				ch = in.read();
			}
		} catch (IOException e) {

		}
		return builder.toString();
	}

	public InputStream getInputStream() {
		String path = MagentoEclipsePlugin.getDefault().getStateLocation().toString() + "/templates/" + template;
		File templateFile = new File(path);
		if (templateFile.exists()) {
			try {
				return new FileInputStream(templateFile);
			} catch (FileNotFoundException e) {
			}
		}

		ClassLoader loader = TemplateParser.class.getClassLoader();
		String resourcePath = "resources/templates/" + template;
		if (loader == null)
			throw new SecurityException(I18n.get("class_loader_error"));
		InputStream in = loader.getResourceAsStream(resourcePath);
		if (in == null)
			throw new IllegalArgumentException(I18n.get("resource_not_found", resourcePath));
		return in;
	}
}
