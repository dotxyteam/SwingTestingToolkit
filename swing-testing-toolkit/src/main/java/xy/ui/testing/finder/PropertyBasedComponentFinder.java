package xy.ui.testing.finder;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class PropertyBasedComponentFinder extends AbstractClassBasedComponentFinder {
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
	protected boolean initializeSpecificValues(Component c, TestEditor testEditor) {
		if (!super.initializeSpecificValues(c, testEditor)) {
			return false;
		}
		LocalComponentPropertyUtil propertyUtil = createPropertyUtil();
		if (propertyValues.size() == 0) {
			for (String propertyName : propertyUtil.getPropertyNameOptions()) {
				PropertyValue propertyValue = new PropertyValue();
				propertyValue.setPropertyName(propertyName);
				try {
					propertyValue.initialize(c);
				} catch (Throwable t) {
					testEditor.logDebug(new Exception("Skipping <" + propertyName + "> property of <"
							+ propertyUtil.getComponentClassName() + "> component: " + t.toString(), t));
					continue;
				}
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

	protected LocalComponentPropertyUtil createPropertyUtil() {
		LocalComponentPropertyUtil result = new LocalComponentPropertyUtil();
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
		String propertyValuesDescription;
		if (propertyValues.size() == 0) {
			propertyValuesDescription = "";
		} else {
			List<String> propertyValueStrings = new ArrayList<String>();
			for (PropertyValue value : propertyValues) {
				propertyValueStrings.add(value.toString());
			}
			propertyValuesDescription = " having ";
			for (String s : propertyValueStrings) {
				propertyValuesDescription += "\n " + s;
			}
		}
		return super.toString() + propertyValuesDescription;
	}

	@Override
	public void validate() throws ValidationError {
		super.validate();
		if (propertyValues.size() == 0) {
			throw new ValidationError("Missing property values");
		}
	}

	public class PropertyValue {

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
			LocalComponentPropertyUtil propertyUtil = getPropertyUtil();
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

		protected LocalComponentPropertyUtil getPropertyUtil() {
			LocalComponentPropertyUtil result = createPropertyUtil();
			result.setPropertyName(propertyName);
			result.setPropertyValueExpected(propertyValueExpected);
			return result;
		}

		public List<String> getPropertyNameOptions() {
			return createPropertyUtil().getPropertyNameOptions();
		}

		@Override
		public String toString() {
			return getPropertyUtil().getValueDescription();
		}

		public void validate() throws ValidationError {
			if (propertyName == null) {
				throw new ValidationError("Missing property name");
			}
		}

	}

	protected class LocalComponentPropertyUtil extends CheckComponentPropertyAction {

		private static final long serialVersionUID = 1L;

		public String fieldValueToPropertyValue(Object fieldValue) {
			return propertyUtil.fieldValueToPropertyValue(fieldValue);
		}

		public Object propertyValueToFieldValue(String propertyValue) {
			return propertyUtil.propertyValueToFieldValue(propertyValue);
		}

		public IFieldInfo getPropertyFieldInfo() {
			return propertyUtil.getPropertyFieldInfo();
		}

	}

}
