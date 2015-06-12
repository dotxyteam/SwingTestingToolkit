package xy.ui.testing.finder;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.util.TestingError;

public class PropertyBasedComponentFinder extends ClassBasedComponentFinder {
	private static final long serialVersionUID = 1L;

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
	protected boolean matchesInContainingWindow(Component c) {
		if (!super.matchesInContainingWindow(c)) {
			return false;
		}
		for (PropertyCriteria propertyCriteria : propertyCriterias) {
			if (!propertyCriteria.matches(c)) {
				return false;
			}
		}
		return true;
	}


	@Override
	public String toString() {
		String criteriasDescription;
		if (propertyCriterias.size() == 0) {
			criteriasDescription = "";
		} else {
			List<String> criteriaStrings = new ArrayList<String>();
			for (PropertyCriteria criteria : propertyCriterias) {
				criteriaStrings.add(criteria.toString());
			}
			criteriasDescription = ", having the following properties:\n"
					+ StringUtils.join(criteriaStrings, "\n");
		}
		return super.toString() + criteriasDescription;
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
			result.setComponentClassName(getComponentClassName());
			result.setPropertyName(propertyName);
			result.setPropertyValueExpected(propertyValueExpected);
			return result;
		}

		@ValueOptionsForField("propertyName")
		public List<String> getPropertyNameOptions() {
			return getSubCheckPropertyAction().getPropertyNameOptions();
		}

		@Override
		public String toString() {
			return getSubCheckPropertyAction().getValueDescription();
		}

	}

}
