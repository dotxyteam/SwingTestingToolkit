package xy.ui.testing.action.component.property;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.ComponentPropertyUtil;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public class CheckComponentPropertyAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected String propertyValueExpected;
	protected ComponentPropertyUtil propertyUtil = new ComponentPropertyUtil();

	public String getPropertyValueExpected() {
		return propertyValueExpected;
	}

	public void setPropertyValueExpected(String propertyValueExpected) {
		this.propertyValueExpected = propertyValueExpected;
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
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent, TesterEditor testerEditor) {
		return propertyUtil.initializeSpecificProperties(c, introspectionRequestEvent);
	}

	@Override
	public void execute(final Component c, Tester tester) {
		IFieldInfo field = propertyUtil.getPropertyFieldInfo();
		Object currentFieldValue = field.getValue(c);
		Object expectedFieldValue = propertyUtil.propertyValueToFieldValue(propertyValueExpected);
		if (!ReflectionUIUtils.equalsOrBothNull(currentFieldValue, expectedFieldValue)) {
			throw new TestFailure(
					"Component property checking failed: Unexpected property value: '" + currentFieldValue
							+ "'. Expected: '" + expectedFieldValue + "'",
					"Component image", TestingUtils.saveTestableComponentImage(tester, c));
		}
	}

	@Override
	public String getValueDescription() {
		String propertyNameText = (getPropertyName() == null) ? "<unspecified-property>" : getPropertyName();
		String propertyValueText = (propertyValueExpected == null) ? "<null>" : propertyValueExpected;
		IFieldInfo propertyFieldInfo = propertyUtil.getPropertyFieldInfo();
		if ((propertyFieldInfo == null) || String.class.getName().equals(propertyFieldInfo.getType().getName())) {
			propertyValueText = "\"" + StringEscapeUtils.escapeJava(propertyValueExpected) + "\"";
		}
		return propertyNameText + " = " + propertyValueText;
	}

	@Override
	public String toString() {
		return "Check " + getValueDescription() + " for " + getComponentInformation();
	}

	@Override
	public void validate() throws ValidationError {
		propertyUtil.validate();
		super.validate();
	}

}
