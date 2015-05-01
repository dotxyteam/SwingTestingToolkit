package xy.ui.testing.action.component.property;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.IBooleanTypeInfo;
import xy.reflect.ui.info.type.ITextualTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.TesterUI;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.TestingError;

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
			if (field.getType() instanceof ITextualTypeInfo) {
				result.add(field.getCaption());
			} else if (field.getType() instanceof IBooleanTypeInfo) {
				result.add(field.getCaption());
			}
		}
		Collections.sort(result);
		return result;
	}

	protected ITypeInfo getComponentTypeInfo() {
		if (componentClassName == null) {
			return null;
		}
		if (componentClassName.trim().length() == 0) {
			return null;
		}
		try {
			return TesterUI.INSTANCE.getTypeInfo(new JavaTypeInfoSource(Class
					.forName(componentClassName)));
		} catch (ClassNotFoundException e) {
			throw new TestingError(e);
		}
	}

	protected Object filedValueToPropertyValue(Object fieldValue) {
		if(fieldValue == null){
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo();
		if (field == null) {
			return null;
		}
		if (field.getType() instanceof ITextualTypeInfo) {
			return ((ITextualTypeInfo) field.getType()).toText(fieldValue);
		} else if (field.getType() instanceof IBooleanTypeInfo) {
			return ((IBooleanTypeInfo) field.getType()).toBoolean(fieldValue)
					.toString();
		} else {
			throw new AssertionError();
		}
	}

	protected Object propertyValueToFieldValue(String propertyValue) {
		if(propertyValue == null){
			return null;
		}
		IFieldInfo field = getPropertyFieldInfo();
		if (field == null) {
			return null;
		}
		if (field.getType() instanceof ITextualTypeInfo) {
			return ((ITextualTypeInfo) field.getType()).fromText(propertyValue);
		} else if (field.getType() instanceof IBooleanTypeInfo) {
			return ((IBooleanTypeInfo) field.getType()).fromBoolean(Boolean
					.valueOf(propertyValue));
		} else {
			throw new AssertionError();
		}
	}

	protected IFieldInfo getPropertyFieldInfo() {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return null;
		}
		return ReflectionUIUtils.findInfoByCaption(componentType.getFields(),
				propertyName);
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

}