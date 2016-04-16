package xy.ui.testing.action.component.property;

import java.awt.Component;

import xy.reflect.ui.info.annotation.Validating;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.Tester;
import xy.ui.testing.util.ValidationError;

public class ChangeComponentPropertyAction extends ComponentPropertyAction {
	private static final long serialVersionUID = 1L;

	protected String newPropertyValue;

	public String getNewPropertyValue() {
		return newPropertyValue;
	}

	public void setNewPropertyValue(String newPropertyValue) {
		this.newPropertyValue = newPropertyValue;
	}

	@Override
	protected boolean isSupportedPropertyField(IFieldInfo field) {
		if (field.isGetOnly()) {
			return false;
		}
		return super.isSupportedPropertyField(field);
	}

	@Override
	public void execute(final Component c, Tester tester) {
		IFieldInfo field = getPropertyFieldInfo();
		Object newFieldValue = propertyValueToFieldValue(newPropertyValue);
		field.setValue(c, newFieldValue);
	}

	@Override
	public String getValueDescription() {
		return propertyName + " = " + newPropertyValue;
	}

	@Override
	public String toString() {
		return "Set \"" + getValueDescription() + "\" for the " + getComponentFinder();
	}

	@Override
	@Validating
	public void validate() throws ValidationError {
		super.validate();
	}

}
