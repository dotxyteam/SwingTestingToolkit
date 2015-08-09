package xy.ui.testing.finder;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import xy.reflect.ui.info.annotation.ValueOptionsForField;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.util.TesterError;

public class PropertyBasedComponentFinder extends ClassBasedComponentFinder {
	private static final long serialVersionUID = 1L;

	protected List<PropertyCriteria> propertyCriterias = new ArrayList<PropertyBasedComponentFinder.PropertyCriteria>();

	public PropertyBasedComponentFinder(String... propertynames) {
		for (int i = 0; i < propertynames.length; i++) {
			String propertyName = propertynames[i];
			PropertyCriteria propertyCriteria = createPropertyCriteria();
			propertyCriteria.setPropertyName(propertyName);
			addPropertyCriteria(i, propertyCriteria);
		}
	}

	public PropertyBasedComponentFinder() {
		this(new String[0]);
	}
	
	

	public List<PropertyCriteria> getPropertyCriteriaList() {
		return propertyCriterias;
	}

	public void setPropertyCriteriaList(List<PropertyCriteria> propertyCriterias) {
		this.propertyCriterias = propertyCriterias;
	}

	public PropertyCriteria createPropertyCriteria() {
		return new PropertyCriteria();
	}

	public PropertyCriteria getPropertyCriteria(int index) {
		return propertyCriterias.get(index);
	}

	public void addPropertyCriteria(int index, PropertyCriteria c) {
		if (findPropertyCriteria(c.getPropertyName()) != null) {
			throw new TesterError(
					"Cannot have duplicate property criterias: '"
							+ c.getPropertyName() + "'");
		}
		propertyCriterias.add(index, c);
	}

	public void removePropertyCriteria(int index) {
		propertyCriterias.remove(index);
	}

	public int getPropertyCriteriaCount() {
		return propertyCriterias.size();
	}

	public void setPropertyCriteria(String propertyName,
			String propertyValueExpected) {
		PropertyCriteria criteria = findPropertyCriteria(propertyName);
		if (criteria == null) {
			criteria = createPropertyCriteria();
			criteria.setPropertyName(propertyName);
			addPropertyCriteria(getPropertyCriteriaCount(), criteria);
		}
		criteria.setPropertyValueExpected(propertyValueExpected);
	}
	


	public String getPropertyCriteria(String propertyName) {
		PropertyCriteria criteria = findPropertyCriteria(propertyName);
		if (criteria == null) {
			return null;
		}
		return criteria.getPropertyValueExpected();
	}

	public PropertyCriteria findPropertyCriteria(String propertyName) {
		for (PropertyCriteria criteria : propertyCriterias) {
			if (propertyName.equals(criteria.getPropertyName())) {
				return criteria;
			}
		}
		return null;
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		if (!super.initializeSpecificCriterias(c)) {
			return false;
		}
		for (PropertyCriteria propertyCriteria : propertyCriterias) {
			if (!propertyCriteria.initialize(c)) {
				return false;
			}
		}
		return true;
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

	public class PropertyCriteria implements Serializable {

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
			final boolean[] ok = new boolean[] { false };
			new CheckComponentPropertyAction() {
				private static final long serialVersionUID = 1L;
				{
					setComponentClassName(PropertyBasedComponentFinder.this.componentClassName);
					setPropertyName(PropertyCriteria.this.propertyName);
					IFieldInfo field = super.getPropertyFieldInfo();
					if (field != null) {
						Object fieldValue = field.getValue(c);
						PropertyCriteria.this.propertyValueExpected = filedValueToPropertyValue(fieldValue);
						ok[0] = true;
					}
				}
			};
			return ok[0];
		}

		public boolean matches(Component c) {
			try {
				getSubCheckPropertyAction().execute(c);
				return true;
			} catch (TesterError e) {
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
