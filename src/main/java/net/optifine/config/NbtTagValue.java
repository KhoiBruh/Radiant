package net.optifine.config;

import net.minecraft.nbt.*;
import net.minecraft.src.Config;
import net.optifine.util.StrUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

public class NbtTagValue {
	private static final int TYPE_TEXT = 0;
	private static final int TYPE_PATTERN = 1;
	private static final int TYPE_IPATTERN = 2;
	private static final int TYPE_REGEX = 3;
	private static final int TYPE_IREGEX = 4;
	private static final String PREFIX_PATTERN = "pattern:";
	private static final String PREFIX_IPATTERN = "ipattern:";
	private static final String PREFIX_REGEX = "regex:";
	private static final String PREFIX_IREGEX = "iregex:";
	private static final int FORMAT_DEFAULT = 0;
	private static final int FORMAT_HEX_COLOR = 1;
	private static final String PREFIX_HEX_COLOR = "#";
	private static final Pattern PATTERN_HEX_COLOR = Pattern.compile("^#[0-9a-f]{6}+$");
	private final String[] parents;
	private final String name;
	private final int type;
	private final String value;
	private boolean negative = false;
	private int valueFormat = 0;

	public NbtTagValue(String tag, String value) {
		String[] astring = Config.tokenize(tag, ".");
		this.parents = Arrays.copyOfRange(astring, 0, astring.length - 1);
		this.name = astring[astring.length - 1];

		if (value.startsWith("!")) {
			this.negative = true;
			value = value.substring(1);
		}

		if (value.startsWith("pattern:")) {
			this.type = 1;
			value = value.substring("pattern:".length());
		} else if (value.startsWith("ipattern:")) {
			this.type = 2;
			value = value.substring("ipattern:".length()).toLowerCase();
		} else if (value.startsWith("regex:")) {
			this.type = 3;
			value = value.substring("regex:".length());
		} else if (value.startsWith("iregex:")) {
			this.type = 4;
			value = value.substring("iregex:".length()).toLowerCase();
		} else {
			this.type = 0;
		}

		value = StringEscapeUtils.unescapeJava(value);

		if (this.type == 0 && PATTERN_HEX_COLOR.matcher(value).matches()) {
			this.valueFormat = 1;
		}

		this.value = value;
	}

	private static NBTBase getChildTag(NBTBase tagBase, String tag) {
		if (tagBase instanceof NBTTagCompound nbttagcompound) {
			return nbttagcompound.getTag(tag);
		} else if (tagBase instanceof NBTTagList nbttaglist) {

			if (tag.equals("count")) {
				return new NBTTagInt(nbttaglist.tagCount());
			} else {
				int i = Config.parseInt(tag, -1);
				return i >= 0 && i < nbttaglist.tagCount() ? nbttaglist.get(i) : null;
			}
		} else {
			return null;
		}
	}

	private static String getNbtString(NBTBase nbtBase, int format) {
		return switch (nbtBase) {
			case null -> null;
			case NBTTagString nbttagstring -> nbttagstring.getString();
			case NBTTagInt nbttagint ->
					format == 1 ? "#" + StrUtils.fillLeft(Integer.toHexString(nbttagint.getInt()), 6, '0') : Integer.toString(nbttagint.getInt());
			case NBTTagByte nbttagbyte -> Byte.toString(nbttagbyte.getByte());
			case NBTTagShort nbttagshort -> Short.toString(nbttagshort.getShort());
			case NBTTagLong nbttaglong -> Long.toString(nbttaglong.getLong());
			case NBTTagFloat nbttagfloat -> Float.toString(nbttagfloat.getFloat());
			case NBTTagDouble nbttagdouble -> Double.toString(nbttagdouble.getDouble());
			default -> nbtBase.toString();
		};
	}

	public boolean matches(NBTTagCompound nbt) {
		return this.negative != this.matchesCompound(nbt);
	}

	public boolean matchesCompound(NBTTagCompound nbt) {
		if (nbt == null) {
			return false;
		} else {
			NBTBase nbtbase = nbt;

			for (String s : this.parents) {
				nbtbase = getChildTag(nbtbase, s);

				if (nbtbase == null) {
					return false;
				}
			}

			if (this.name.equals("*")) {
				return this.matchesAnyChild(nbtbase);
			} else {
				nbtbase = getChildTag(nbtbase, this.name);

				if (nbtbase == null) {
					return false;
				} else return this.matchesBase(nbtbase);
			}
		}
	}

	private boolean matchesAnyChild(NBTBase tagBase) {
		if (tagBase instanceof NBTTagCompound nbttagcompound) {

			for (String s : nbttagcompound.getKeySet()) {
				NBTBase nbtbase = nbttagcompound.getTag(s);

				if (this.matchesBase(nbtbase)) {
					return true;
				}
			}
		}

		if (tagBase instanceof NBTTagList nbttaglist) {
			int i = nbttaglist.tagCount();

			for (int j = 0; j < i; ++j) {
				NBTBase nbtbase1 = nbttaglist.get(j);

				if (this.matchesBase(nbtbase1)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean matchesBase(NBTBase nbtBase) {
		if (nbtBase == null) {
			return false;
		} else {
			String s = getNbtString(nbtBase, this.valueFormat);
			return this.matchesValue(s);
		}
	}

	public boolean matchesValue(String nbtValue) {
		if (nbtValue == null) {
			return false;
		} else {
			return switch (this.type) {
				case 0 -> nbtValue.equals(this.value);
				case 1 -> this.matchesPattern(nbtValue, this.value);
				case 2 -> this.matchesPattern(nbtValue.toLowerCase(), this.value);
				case 3 -> this.matchesRegex(nbtValue, this.value);
				case 4 -> this.matchesRegex(nbtValue.toLowerCase(), this.value);
				default -> throw new IllegalArgumentException("Unknown NbtTagValue type: " + this.type);
			};
		}
	}

	private boolean matchesPattern(String str, String pattern) {
		return StrUtils.equalsMask(str, pattern, '*', '?');
	}

	private boolean matchesRegex(String str, String regex) {
		return str.matches(regex);
	}

	public String toString() {
		StringBuilder stringbuffer = new StringBuilder();

		for (int i = 0; i < this.parents.length; ++i) {
			String s = this.parents[i];

			if (i > 0) {
				stringbuffer.append(".");
			}

			stringbuffer.append(s);
		}

		if (!stringbuffer.isEmpty()) {
			stringbuffer.append(".");
		}

		stringbuffer.append(this.name);
		stringbuffer.append(" = ");
		stringbuffer.append(this.value);
		return stringbuffer.toString();
	}
}
