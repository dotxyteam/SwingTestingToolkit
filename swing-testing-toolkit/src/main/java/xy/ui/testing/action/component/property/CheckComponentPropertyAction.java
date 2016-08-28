package xy.ui.testing.action.component.property;

import java.awt.Component;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.info.annotation.Validating;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public class CheckComponentPropertyAction extends ComponentPropertyAction {
	private static final long serialVersionUID = 1L;

	protected String propertyValueExpected;

	public String getPropertyValueExpected() {
		return propertyValueExpected;
	}

	public void setPropertyValueExpected(String propertyValueExpected) {
		this.propertyValueExpected = propertyValueExpected;
	}

	@Override
	public void execute(final Component c, Tester tester) {
		IFieldInfo field = getPropertyFieldInfo();
		Object currentFieldValue = field.getValue(c);
		Object expectedFieldValue = propertyValueToFieldValue(propertyValueExpected);
		if (!ReflectionUIUtils.equalsOrBothNull(currentFieldValue, expectedFieldValue)) {
			throw new TestFailure("Component property checking failed: Unexpected property value: '" + currentFieldValue
					+ "'. Expected: '" + expectedFieldValue + "'", "Component", TestingUtils.saveImage(c));
		}
	}

	@Override
	public String getValueDescription() {
		String propertyNameText = (propertyName == null) ? "<unspecified-property>" : propertyName;
		String propertyValueText;
		if (propertyValueExpected == null) {
			propertyValueText = "<null>";
		} else {
			IFieldInfo propertyFieldInfo = getPropertyFieldInfo();
			if ((propertyFieldInfo != null) && String.class.getName().equals(propertyFieldInfo.getType().getName())) {
				propertyValueText = "\"" + StringEscapeUtils.escapeJava(propertyValueExpected) + "\"";
			} else {
				propertyValueText = propertyValueExpected;
			}
		}
		return propertyNameText + " = " + propertyValueText;		
	}

	@Override
	public String toString() {
		return "Check that " + getValueDescription() + " for the " + getComponentInformation();
	}

	@Override
	@Validating
	public void validate() throws ValidationError {
		super.validate();
	}

}
