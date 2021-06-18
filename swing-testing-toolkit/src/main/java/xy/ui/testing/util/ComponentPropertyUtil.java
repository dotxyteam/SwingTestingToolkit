package xy.ui.testing.util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.Tester;

/**
 * This class allows to infer component properties from the class name. Note
 * that a property is typically a field (with a getter and a setter) that is
 * supported (not supported: image fields, icon fields, ...) and converted to
 * string.
 * 
 * @author olitank
 *
 */
public class ComponentPropertyUtil {

	protected static ReflectionUI introspector = ReflectionUIUtils.STANDARD_REFLECTION;

	protected String propertyName;
	protected String componentClassName;

	public ComponentPropertyUtil() {
		super();
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getComponentClassName() {
		return componentClassName;
	}

	public void setComponentClassName(String componentClassName) {
		this.componentClassName = componentClassName;
	}

	public IFieldInfo getPropertyFieldInfo(final Tester tester) {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return null;
		}
		if ("To String".equals(propertyName)) {
			return new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
				@Override
				public Object getValue(Object object) {
					return object.toString();
				}

				@Override
				public ITypeInfo getType() {
					return introspector.getTypeInfo(new JavaTypeInfoSource(introspector, String.class, null));
				}
			};
		}
		if ("Class".equals(propertyName)) {
			return new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
				@Override
				public Object getValue(Object object) {
					return object.getClass().getName();
				}

				@Override
				public ITypeInfo getType() {
					return introspector.getTypeInfo(new JavaTypeInfoSource(introspector, String.class, null));
				}
			};
		}
		if ("Displayed Strings".equals(propertyName)) {
			return new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {
				@Override
				public Object getValue(Object object) {
					List<String> strings = TestingUtils.extractComponentTreeDisplayedStrings((Component) object,
							tester);
					if (strings.size() == 0) {
						return "";
					}
					return MiscUtils.formatStringList(strings);
				}

				@Override
				public ITypeInfo getType() {
					return introspector.getTypeInfo(new JavaTypeInfoSource(introspector, String.class, null));
				}
			};
		}
		return ReflectionUIUtils.findInfoByCaption(componentType.getFields(), propertyName);
	}

	public Class<?> getJavaType() {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return null;
		}
		if ("To String".equals(propertyName)) {
			return String.class;
		}
		if ("Class".equals(propertyName)) {
			return String.class;
		}
		if ("Displayed Strings".equals(propertyName)) {
			return String.class;
		}
		IFieldInfo field = ReflectionUIUtils.findInfoByCaption(componentType.getFields(), propertyName);
		if (field == null) {
			return null;
		}
		return getFieldJavaType(field);
	}

	public List<String> getPropertyNameOptions(boolean modifiable) {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>();
		for (IFieldInfo field : componentType.getFields()) {
			if (isComponentFieldSupported(field, modifiable)) {
				result.add(field.getCaption());
			}
		}
		if (!modifiable) {
			result.add("To String");
			result.add("Class");
			result.add("Displayed Strings");
		}
		Collections.sort(result);
		return result;
	}

	protected boolean isComponentFieldSupported(IFieldInfo field, boolean modifiable) {
		if (modifiable && field.isGetOnly()) {
			return false;
		}
		Class<?> javaType = getFieldJavaType(field);
		if (xy.reflect.ui.util.ClassUtils.isPrimitiveClassOrWrapperOrString(javaType)) {
			return true;
		}
		if (Color.class.equals(javaType)) {
			return true;
		}
		return false;
	}

	protected Class<?> getFieldJavaType(IFieldInfo field) {
		try {
			return ClassUtils.getClass(field.getType().getName());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	public ITypeInfo getComponentTypeInfo() {
		if (componentClassName == null) {
			return null;
		}
		if (componentClassName.length() == 0) {
			return null;
		}
		try {
			return introspector
					.getTypeInfo(new JavaTypeInfoSource(introspector, Class.forName(componentClassName), null));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public String fieldValueToPropertyValue(Tester tester, Object fieldValue) {
		if (fieldValue == null) {
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo(tester);
		if (field == null) {
			return null;
		}
		if (xy.reflect.ui.util.ClassUtils.isPrimitiveClassOrWrapperOrString(fieldValue.getClass())) {
			return fieldValue.toString();
		} else if (Color.class.isAssignableFrom(fieldValue.getClass())) {
			return MiscUtils.colorToString((Color) fieldValue);
		} else {
			throw new AssertionError();
		}
	}

	public Object propertyValueToFieldValue(Tester tester, String propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo(tester);
		if (field == null) {
			return null;
		}
		Class<?> javaType = getFieldJavaType(field);
		if (xy.reflect.ui.util.ClassUtils.isPrimitiveClassOrWrapperOrString(javaType)) {
			if (javaType.equals(String.class)) {
				return propertyValue;
			} else {
				return xy.reflect.ui.util.ReflectionUIUtils.primitiveFromString(propertyValue, javaType);
			}
		} else if (Color.class.equals(javaType)) {
			return MiscUtils.stringToColor(propertyValue);
		} else {
			throw new AssertionError();
		}
	}

	public boolean initializeSpecificProperties(Component c, AWTEvent event) {
		componentClassName = c.getClass().getName();
		List<String> propertyNameOptions = getPropertyNameOptions(true);
		if (propertyNameOptions.size() == 0) {
			return false;
		}
		propertyName = propertyNameOptions.get(0);
		return true;
	}

	public void validate() throws ValidationError {
		if (componentClassName == null) {
			throw new ValidationError("Missing component class name");
		}
		try {
			Class<?> clazz = Class.forName(componentClassName);
			if (!Component.class.isAssignableFrom(clazz)) {
				throw new ValidationError(
						"The component class is not a sub-type of '" + Component.class.getName() + "'");
			}
		} catch (ClassNotFoundException e) {
			throw new ValidationError("Invalid class name: " + componentClassName);
		}
	}

}