package xy.ui.testing.finder;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.annotation.Validating;
import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.Tester;
import xy.ui.testing.TesterUI;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class PropertyBasedComponentFinder extends ClassBasedComponentFinder {
	private static final long serialVersionUID = 1L;

	protected List<PropertyValue> propertyValues = new ArrayList<PropertyBasedComponentFinder.PropertyValue>();

	public void setPropertyNames(String... propertynames) {
		for (int i = 0; i < propertynames.length; i++) {
			String propertyName = propertynames[i];
			PropertyValue propertyValue = createPropertyValue();
			propertyValue.setPropertyName(propertyName);
			addPropertyValue(i, propertyValue);
		}
	}

	public List<PropertyValue> getPropertyValueList() {
		return propertyValues;
	}

	public void setPropertyValueList(List<PropertyValue> propertyValues) {
		this.propertyValues = propertyValues;
	}

	public PropertyValue createPropertyValue() {
		return new PropertyValue();
	}

	public PropertyValue getPropertyValue(int index) {
		return propertyValues.get(index);
	}

	public void addPropertyValue(int index, PropertyValue c) {
		if (findPropertyValue(c.getPropertyName()) != null) {
			throw new AssertionError("Cannot have duplicate property values: '" + c.getPropertyName() + "'");
		}
		propertyValues.add(index, c);
	}

	public void removePropertyValue(int index) {
		propertyValues.remove(index);
	}

	public int getPropertyValueCount() {
		return propertyValues.size();
	}

	public void setPropertyValue(String propertyName, String propertyValueExpected) {
		PropertyValue value = findPropertyValue(propertyName);
		if (value == null) {
			value = createPropertyValue();
			value.setPropertyName(propertyName);
			addPropertyValue(getPropertyValueCount(), value);
		}
		value.setPropertyValueExpected(propertyValueExpected);
	}

	public String getPropertyValue(String propertyName) {
		PropertyValue value = findPropertyValue(propertyName);
		if (value == null) {
			return null;
		}
		return value.getPropertyValueExpected();
	}

	public PropertyValue findPropertyValue(String propertyName) {
		for (PropertyValue value : propertyValues) {
			if (propertyName.equals(value.getPropertyName())) {
				return value;
			}
		}
		return null;
	}

	@Override
	protected boolean initializeSpecificValues(Component c, TesterUI testerUI) {
		if (!super.initializeSpecificValues(c, testerUI)) {
			return false;
		}
		ComponentPropertyUtil propertyUtil = createPropertyUtil();
		if (propertyValues.size() == 0) {
			for (String propertyName : propertyUtil.getPropertyNameOptions()) {
				PropertyValue propertyValue = new PropertyValue();
				propertyValue.setPropertyName(propertyName);
				propertyValue.initialize(c);
				propertyValues.add(propertyValue);
			}
		} else {
			for (PropertyValue propertyValue : propertyValues) {
				if (!propertyValue.initialize(c)) {
					return false;
				}
			}			
		}
		return true;
	}

	protected ComponentPropertyUtil createPropertyUtil() {
		ComponentPropertyUtil result = new ComponentPropertyUtil();
		result.setComponentClassName(getComponentClassName());
		return result;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c, Tester tester) {
		if (!super.matchesInContainingWindow(c, tester)) {
			return false;
		}
		for (PropertyValue propertyValue : propertyValues) {
			if (!propertyValue.matches(c, tester)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String valuesDescription;
		if (propertyValues.size() == 0) {
			valuesDescription = "";
		} else {
			List<String> valueStrings = new ArrayList<String>();
			for (PropertyValue value : propertyValues) {
				valueStrings.add(value.toString());
			}
			valuesDescription = ", having:";
			for (String s : valueStrings) {
				valuesDescription += "\n- " + s;
			}
		}
		return super.toString() + valuesDescription;
	}

	@Override
	@Validating
	public void validate() throws ValidationError {
		super.validate();
		if (propertyValues.size() == 0) {
			throw new ValidationError("Missing property values");
		}
	}

	public class PropertyValue implements Serializable {

		private static final long serialVersionUID = 1L;

		protected String propertyName;
		protected String propertyValueExpected;

		public String getPropertyName() {
			return propertyName;
		}

		public void setPropertyName(String propertyName) {
			this.propertyName = propertyName;
		}

		public String getPropertyValueExpected() {
			return propertyValueExpected;
		}

		public void setPropertyValueExpected(String propertyValueExpected) {
			this.propertyValueExpected = propertyValueExpected;
		}

		public boolean initialize(final Component c) {
			ComponentPropertyUtil propertyUtil = getPropertyUtil();
			IFieldInfo field = propertyUtil.getPropertyFieldInfo();
			if (field != null) {
				Object fieldValue = field.getValue(c);
				propertyValueExpected = propertyUtil.fieldValueToPropertyValue(fieldValue);
				return true;
			} else {
				return false;
			}
		}

		public boolean matches(Component c, Tester tester) {
			try {
				getPropertyUtil().execute(c, tester);
				return true;
			} catch (TestFailure e) {
				return false;
			}
		}

		protected ComponentPropertyUtil getPropertyUtil() {
			ComponentPropertyUtil result = createPropertyUtil();
			result.setPropertyName(propertyName);
			result.setPropertyValueExpected(propertyValueExpected);
			return result;
		}

		@ValueOptionsForField("propertyName")
		public List<String> getPropertyNameOptions() {
			return createPropertyUtil().getPropertyNameOptions();
		}

		@Override
		public String toString() {
			return getPropertyUtil().getValueDescription();
		}

		@Validating
		public void validate() throws ValidationError {
			if (propertyName == null) {
				throw new ValidationError("Missing property name");
			}
		}

	}

	protected class ComponentPropertyUtil extends CheckComponentPropertyAction {

		private static final long serialVersionUID = 1L;

		@Override
		public String fieldValueToPropertyValue(Object fieldValue) {
			return super.fieldValueToPropertyValue(fieldValue);
		}

		@Override
		public Object propertyValueToFieldValue(String propertyValue) {
			return super.propertyValueToFieldValue(propertyValue);
		}

		@Override
		public IFieldInfo getPropertyFieldInfo() {
			return super.getPropertyFieldInfo();
		}

	}

}
