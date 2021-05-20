package xy.ui.testing.action.component.property;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.MiscUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.ComponentPropertyUtil;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that can check any property of a component.
 * 
 * @author olitank
 *
 */
public class CheckComponentPropertyAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected ComponentPropertyUtil propertyUtil = new ComponentPropertyUtil();
	protected String propertyValueExpected;
	protected boolean regularExpressionExpected = false;

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
		return propertyUtil.getPropertyNameOptions(false);
	}

	public boolean isRegularExpressionExpected() {
		return regularExpressionExpected;
	}

	public void setRegularExpressionExpected(boolean regularExpressionExpected) {
		this.regularExpressionExpected = regularExpressionExpected;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		regularExpressionExpected = false;
		return propertyUtil.initializeSpecificProperties(c, introspectionRequestEvent);
	}

	@Override
	public void execute(final Component c, Tester tester) {
		IFieldInfo field = propertyUtil.getPropertyFieldInfo(tester);
		Object currentFieldValue = field.getValue(c);
		String currentPropertyValue = propertyUtil.fieldValueToPropertyValue(tester, currentFieldValue);
		if (regularExpressionExpected) {
			if (propertyValueExpected != null) {
				if ((currentPropertyValue == null) || !currentPropertyValue.matches(propertyValueExpected)) {
					throw new TestFailure("Component property checking failed: Unexpected property value: "
							+ ((currentPropertyValue == null) ? "<null>" : ("'" + currentPropertyValue + "'"))
							+ ". Expected value matching: '" + propertyValueExpected + "'");
				}
			} else {
				if (currentPropertyValue != null) {
					throw new TestFailure("Component property checking failed: Unexpected property value: '"
							+ currentPropertyValue + "'. Expected <null>");
				}
			}
		} else {
			if (!MiscUtils.equalsOrBothNull(currentPropertyValue, propertyValueExpected)) {
				throw new TestFailure("Component property checking failed: Unexpected property value: "
						+ ((currentPropertyValue == null) ? "<null>" : ("'" + currentPropertyValue + "'"))
						+ ". Expected: "
						+ ((propertyValueExpected == null) ? "<null>" : ("'" + propertyValueExpected + "'")));
			}
		}
	}

	@Override
	public String getValueDescription() {
		String propertyNameText = (getPropertyName() == null) ? "<unspecified-property>" : getPropertyName();
		String propertyValueText;
		if (propertyValueExpected == null) {
			propertyValueText = "<null>";
		} else {
			Class<?> propertyJavaType = propertyUtil.getJavaType();
			if ((propertyJavaType != null) && String.class.equals(propertyJavaType)) {
				propertyValueText = "\"" + StringEscapeUtils.escapeJava(propertyValueExpected) + "\"";
			} else {
				propertyValueText = propertyValueExpected;
			}
		}
		return propertyNameText + (regularExpressionExpected ? " ~ " : " = ") + propertyValueText;
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
