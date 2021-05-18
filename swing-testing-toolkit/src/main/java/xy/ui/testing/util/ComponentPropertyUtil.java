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

	public IFieldInfo getPropertyFieldInfo() {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return null;
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
		if ("ToString".equals(propertyName)) {
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
		return ReflectionUIUtils.findInfoByCaption(componentType.getFields(), propertyName);
	}

	public List<String> getPropertyNameOptions() {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>();
		for (IFieldInfo field : componentType.getFields()) {
			if (isSupportedPropertyField(field)) {
				result.add(field.getCaption());
			}
		}
		result.add("ToString");
		result.add("Class");
		Collections.sort(result);
		return result;
	}

	public boolean isSupportedPropertyField(IFieldInfo field) {
		Class<?> javaType = getFieldJavaType(field);
		if (xy.reflect.ui.util.ReflectionUtils.isPrimitiveClassOrWrapperOrString(javaType)) {
			return true;
		}
		if (Color.class.equals(javaType)) {
			return true;
		}
		return false;
	}

	public Class<?> getFieldJavaType(IFieldInfo field) {
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

	public String fieldValueToPropertyValue(Object fieldValue) {
		if (fieldValue == null) {
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo();
		if (field == null) {
			return null;
		}
		if (xy.reflect.ui.util.ReflectionUtils.isPrimitiveClassOrWrapperOrString(fieldValue.getClass())) {
			return fieldValue.toString();
		} else if (Color.class.isAssignableFrom(fieldValue.getClass())) {
			return MiscUtils.colorToString((Color) fieldValue);
		} else {
			throw new AssertionError();
		}
	}

	public Object propertyValueToFieldValue(String propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo();
		if (field == null) {
			return null;
		}
		Class<?> javaType = getFieldJavaType(field);
		if (xy.reflect.ui.util.ReflectionUtils.isPrimitiveClassOrWrapperOrString(javaType)) {
			if (javaType.equals(String.class)) {
				return propertyValue;
			} else {
				return xy.reflect.ui.util.ReflectionUtils.primitiveFromString(propertyValue, javaType);
			}
		} else if (Color.class.equals(javaType)) {
			return MiscUtils.stringToColor(propertyValue);
		} else {
			throw new AssertionError();
		}
	}

	public boolean initializeSpecificProperties(Component c, AWTEvent event) {
		componentClassName = c.getClass().getName();
		List<String> propertyNameOptions = getPropertyNameOptions();
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