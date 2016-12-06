package xy.ui.testing.action.component.property;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import xy.reflect.ui.control.swing.PrimitiveValueControl;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.info.type.custom.TextualTypeInfo;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.TesterUI;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.ValidationError;

public abstract class ComponentPropertyAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;
	protected String propertyName;
	protected String componentClassName;

	public ComponentPropertyAction() {
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
		Collections.sort(result);
		return result;
	}

	protected boolean isSupportedPropertyField(IFieldInfo field) {
		Class<?> javaType = getFieldJavaType(field);
		if (TextualTypeInfo.isCompatibleWith(javaType)) {
			return true;
		}
		if (BooleanTypeInfo.isCompatibleWith(javaType)) {
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

	protected ITypeInfo getComponentTypeInfo() {
		if (componentClassName == null) {
			return null;
		}
		if (componentClassName.length() == 0) {
			return null;
		}
		try {
			return TesterUI.DEFAULT.getTypeInfo(new JavaTypeInfoSource(Class.forName(componentClassName)));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	protected String fieldValueToPropertyValue(Object fieldValue) {
		if (fieldValue == null) {
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo();
		if (field == null) {
			return null;
		}
		if (TextualTypeInfo.isCompatibleWith(fieldValue.getClass())) {
			return PrimitiveValueControl.toText(fieldValue);
		} else if (BooleanTypeInfo.isCompatibleWith(fieldValue.getClass())) {
			return fieldValue.toString();
		} else {
			throw new AssertionError();
		}
	}

	protected Object propertyValueToFieldValue(String propertyValue) {
		if (propertyValue == null) {
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo();
		if (field == null) {
			return null;
		}
		Class<?> javaType = getFieldJavaType(field);
		if (TextualTypeInfo.isCompatibleWith(javaType)) {
			return PrimitiveValueControl.fromText(propertyValue, javaType);
		} else if (BooleanTypeInfo.isCompatibleWith(javaType)) {
			return Boolean.valueOf(propertyValue);
		} else {
			throw new AssertionError();
		}
	}

	protected IFieldInfo getPropertyFieldInfo() {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return null;
		}
		return ReflectionUIUtils.findInfoByCaption(componentType.getFields(), propertyName);
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		componentClassName = c.getClass().getName();
		List<String> propertyNameOptions = getPropertyNameOptions();
		if (propertyNameOptions.size() == 0) {
			return false;
		}
		propertyName = propertyNameOptions.get(0);
		return true;
	}

	@Override
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
			throw new ValidationError("Invalid class name: Class not found");
		}

		if (componentFinder == null) {
			throw new ValidationError("Missing component finding information");
		}
	}

}