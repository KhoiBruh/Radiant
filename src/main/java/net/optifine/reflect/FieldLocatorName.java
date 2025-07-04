package net.optifine.reflect;

import net.optifine.Log;

import java.lang.reflect.Field;

public class FieldLocatorName implements IFieldLocator {
	private final ReflectorClass reflectorClass;
	private final String targetFieldName;

	public FieldLocatorName(ReflectorClass reflectorClass, String targetFieldName) {
		this.reflectorClass = reflectorClass;
		this.targetFieldName = targetFieldName;
	}

	public Field field() {
		Class<?> oclass = this.reflectorClass.getTargetClass();

		if (oclass == null) {
			return null;
		} else {
			try {
				Field field = this.getDeclaredField(oclass, this.targetFieldName);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException exception) {
				Log.info("(Reflector) Field not present: " + oclass.getName() + "." + this.targetFieldName);
				return null;
			} catch (Throwable throwable) {
				throwable.printStackTrace();
				return null;
			}
		}
	}

	private Field getDeclaredField(Class<?> cls, String name) throws NoSuchFieldException {
		Field[] afield = cls.getDeclaredFields();

		for (Field field : afield) {
			if (field.getName().equals(name)) {
				return field;
			}
		}

		if (cls == Object.class) {
			throw new NoSuchFieldException(name);
		} else {
			return this.getDeclaredField(cls.getSuperclass(), name);
		}
	}
}
