package xy.ui.testing.action.component.property;

import java.awt.Component;

import xy.reflect.ui.info.field.IFieldInfo;

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
		if(field.isReadOnly()){
			return false;
		}
		return super.isSupportedPropertyField(field);
	}

	@Override
	public void execute(final Component c) {
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
		return "Set \"" + getValueDescription() + "\" for the "
				+ getComponentFinder();
	}

}