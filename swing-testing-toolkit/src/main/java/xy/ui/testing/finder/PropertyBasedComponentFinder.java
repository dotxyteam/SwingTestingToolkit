package xy.ui.testing.finder;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.util.TestingError;

public class PropertyBasedComponentFinder extends ClassBasedComponentFinder {
	private static final long serialVersionUID = 1L;

	protected CheckComponentPropertyAction checkPropertyAction = new CheckComponentPropertyAction();
	protected List<PropertyCriteria> propertyCriterias = new ArrayList<PropertyBasedComponentFinder.PropertyCriteria>();

	public PropertyCriteria createPropertyCriteria() {
		return new PropertyCriteria();
	}

	public PropertyCriteria getPropertyCriteria(int index) {
		return propertyCriterias.get(index);
	}

	public void addPropertyCriteria(int index, PropertyCriteria c) {
		propertyCriterias.add(index, c);
	}

	public void removePropertyCriteria(int index) {
		propertyCriterias.remove(index);
	}

	public int getPropertyCriteriaCount() {
		return propertyCriterias.size();
	}

	@Override
	public void setComponentClassName(String componentClassName) {
		super.setComponentClassName(componentClassName);
		checkPropertyAction.setComponentClassName(componentClassName);
	}

	@Override
	protected boolean matchesInContainingWindow(Component c) {
		if (!super.matchesInContainingWindow(c)) {
			return false;
		}
		for (PropertyCriteria propertyOption : propertyCriterias) {
			if (!propertyOption.matches(c)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		if (!super.initializeSpecificCriterias(c)) {
			return false;
		}
		if (!checkPropertyAction.initializeFrom(c, null)) {
			return false;
		}
		PropertyCriteria firsPropertyOption = new PropertyCriteria();
		firsPropertyOption.setPropertyName(checkPropertyAction
				.getPropertyName());
		firsPropertyOption.setPropertyValueExpected(checkPropertyAction
				.getPropertyValueExpected());
		propertyCriterias.add(firsPropertyOption);
		return true;

	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"\"{0}\" component n°{1} in the window n°{2}",
				checkPropertyAction.getValueDescription(),
				(occurrencesToSkip + 1), (windowIndex + 1));
	}

	public class PropertyCriteria {

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

		public boolean matches(Component c) {
			try {
				getSubCheckPropertyAction().execute(c);
				return true;
			} catch (TestingError e) {
				return false;
			}
		}

		protected CheckComponentPropertyAction getSubCheckPropertyAction() {
			CheckComponentPropertyAction result = new CheckComponentPropertyAction();
			result.setComponentClassName(checkPropertyAction
					.getComponentClassName());
			result.setPropertyName(propertyName);
			result.setPropertyValueExpected(propertyValueExpected);
			return result;
		}

		@ValueOptionsForField("propertyName")
		public List<String> getPropertyNameOptions() {
			return checkPropertyAction.getPropertyNameOptions();
		}

		@Override
		public String toString() {
			return getSubCheckPropertyAction().getValueDescription();
		}

	}

}
