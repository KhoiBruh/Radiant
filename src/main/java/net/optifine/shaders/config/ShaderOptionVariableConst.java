package net.optifine.shaders.config;

import net.optifine.util.StrUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderOptionVariableConst extends ShaderOptionVariable {
	private static final Pattern PATTERN_CONST = Pattern.compile("^\\s*const\\s*(float|int)\\s*([A-Za-z0-9_]+)\\s*=\\s*(-?[0-9\\.]+f?F?)\\s*;\\s*(//.*)?$");
	private final String type;

	public ShaderOptionVariableConst(String name, String type, String description, String value, String[] values, String path) {
		super(name, description, value, values, path);
		this.type = type;
	}

	public static ShaderOption parseOption(String line, String path) {
		Matcher matcher = PATTERN_CONST.matcher(line);

		if (!matcher.matches()) {
			return null;
		} else {
			String s = matcher.group(1);
			String s1 = matcher.group(2);
			String s2 = matcher.group(3);
			String s3 = matcher.group(4);
			String s4 = StrUtils.getSegment(s3, "[", "]");

			if (s4 != null && !s4.isEmpty()) {
				s3 = s3.replace(s4, "").trim();
			}

			String[] astring = parseValues(s2, s4);

			if (s1 != null && !s1.isEmpty()) {
				path = StrUtils.removePrefix(path, "/shaders/");
				return new ShaderOptionVariableConst(s1, s, s3, s2, astring, path);
			} else {
				return null;
			}
		}
	}

	public String getSourceLine() {
		return "const " + this.type + " " + this.getName() + " = " + this.getValue() + "; // Shader option " + this.getValue();
	}

	public boolean matchesLine(String line) {
		Matcher matcher = PATTERN_CONST.matcher(line);

		if (!matcher.matches()) {
			return false;
		} else {
			String s = matcher.group(2);
			return s.matches(this.getName());
		}
	}
}
