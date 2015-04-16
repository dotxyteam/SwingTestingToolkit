package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.type.ITextualTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.JavaTypeInfoSource;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.TesterUI;
import xy.ui.testing.util.TestingError;

public class CheckComponentPropertyAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected String propertyName;
	protected String propertyValueExpected;

	private String componentClassName;

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyyName) {
		this.propertyName = propertyyName;
	}

	public String getPropertyValueExpected() {
		return propertyValueExpected;
	}

	public void setPropertyValueExpected(String propertyValueExpected) {
		this.propertyValueExpected = propertyValueExpected;
	}

	public String getComponentClassName() {
		return componentClassName;
	}

	public void setComponentClassName(String componentClassName) {
		this.componentClassName = componentClassName;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		componentClassName = c.getClass().getName();
		if (getPropertyNameEnumeration().size() == 0) {
			return false;
		}
		return true;
	}

	public List<String> getPropertyNameEnumeration() {
		ITypeInfo componentType = getComponentTypeInfo();
		if (componentType == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>();
		for (IFieldInfo field : componentType.getFields()) {
			if (field.getType() instanceof ITextualTypeInfo) {
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
		try {
			return TesterUI.INSTANCE.getTypeInfo(new JavaTypeInfoSource(Class
					.forName(componentClassName)));
		} catch (ClassNotFoundException e) {
			throw new TestingError(e);
		}
	}

	@Override
	public void execute(final Component c) {
		ITypeInfo componentType = getComponentTypeInfo();
		IFieldInfo field = ReflectionUIUtils.findInfoByCaption(
				componentType.getFields(), propertyName);
		ITextualTypeInfo fieldType = (ITextualTypeInfo) field.getType();
		Object expectedFieldValue = fieldType.fromText(propertyValueExpected);
		Object currentFieldValue = field.getValue(c);
		if (!ReflectionUIUtils.equalsOrBothNull(currentFieldValue,
				expectedFieldValue)) {
			throw new TestingError(
					"Component property checking failed: Unexpected property value: "
							+ currentFieldValue + ". Expected: "
							+ propertyValueExpected);
		}
	}

	@Override
	public String getValueDescription() {
		return propertyName + " = " + propertyValueExpected;
	}

	@Override
	public String toString() {
		return "Check that \"" + getValueDescription() + "\" for the "
				+ getComponentFinder();
	}

}
