package xy.ui.testing.action.component.property;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.ComponentPropertyUtil;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that can change any property of a component.
 * 
 * @author olitank
 *
 */
public class ChangeComponentPropertyAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected ComponentPropertyUtil propertyUtil = new ComponentPropertyUtil() {
		@Override
		public boolean isSupportedPropertyField(IFieldInfo field) {
			if (field.isGetOnly()) {
				return false;
			}
			return super.isSupportedPropertyField(field);
		}

	};
	protected String newPropertyValue;

	public String getNewPropertyValue() {
		return newPropertyValue;
	}

	public void setNewPropertyValue(String newPropertyValue) {
		this.newPropertyValue = newPropertyValue;
	}

	public String getPropertyName() {
		return propertyUtil.getPropertyName();
	}

	public void setPropertyName(String propertyName) {
		propertyUtil.setPropertyName(propertyName);
	}

	public String getComponentClassName() {
		return propertyUtil.getComponentClassName();
	}

	public void setComponentClassName(String componentClassName) {
		propertyUtil.setComponentClassName(componentClassName);
	}

	public List<String> getPropertyNameOptions() {
		return propertyUtil.getPropertyNameOptions();
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		return propertyUtil.initializeSpecificProperties(c, introspectionRequestEvent);
	}

	@Override
	public void execute(final Component c, Tester tester) {
		final IFieldInfo field = propertyUtil.getPropertyFieldInfo();
		final Object newFieldValue = propertyUtil.propertyValueToFieldValue(newPropertyValue);
		MiscUtils.expectingToBeInUIThread(new Runnable() {
			@Override
			public void run() {
				field.setValue(c, newFieldValue);
			}
		});
	}

	@Override
	public String getValueDescription() {
		String propertyNameText = (getPropertyName() == null) ? "<unspecified-property>" : getPropertyName();
		String newPropertyValueText = (newPropertyValue == null) ? "<null>" : newPropertyValue;
		IFieldInfo propertyFieldInfo = propertyUtil.getPropertyFieldInfo();
		if ((propertyFieldInfo == null) || String.class.getName().equals(propertyFieldInfo.getType().getName())) {
			newPropertyValueText = "\"" + StringEscapeUtils.escapeJava(newPropertyValueText) + "\"";
		}
		return propertyNameText + " <= " + newPropertyValueText;
	}

	@Override
	public String toString() {
		return "Set " + getValueDescription() + " for " + getComponentInformation();
	}

	@Override
	public void validate() throws ValidationError {
		propertyUtil.validate();
		super.validate();
	}

}
