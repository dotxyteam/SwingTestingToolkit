package xy.ui.testing.finder;

import java.awt.Component;
import org.apache.commons.lang3.StringEscapeUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Component finder that finds the components by comparing their names.
 * 
 * @author olitank
 *
 */
public class NameBasedComponentFinder extends MatchingComponentFinder {
	private static final long serialVersionUID = 1L;

	protected String componentName = "";
	protected ClassBasedComponentFinder classMatcher = new ClassBasedComponentFinder() {
		private static final long serialVersionUID = 1L;
		{
			this.setComponentClassName(null);
		}
	};

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentClassName() {
		return classMatcher.getComponentClassName();
	}

	public void setComponentClassName(String componentClassName) {
		classMatcher.setComponentClassName(componentClassName);
	}

	@Override
	protected boolean initializeSpecificValues(Component c, TestEditor testEditor) {
		if (c.getName() != null) {
			componentName = c.getName();
			return true;
		}
		return false;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c, Tester tester) {
		if (!componentName.equals(c.getName())) {
			return false;
		}
		if (classMatcher.getComponentClassName() != null) {
			if (!classMatcher.matchesInContainingWindow(c, tester)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void validate() throws ValidationError {
		super.validate();
		if ((componentName == null) || (componentName.length() == 0)) {
			throw new ValidationError("The name of the component to find has not been defined");
		}
		if (classMatcher.getComponentClassName() != null) {
			classMatcher.validate();
		}
	}

	@Override
	public String toString() {
		String result = "";
		if (componentName == null) {
			result += "<unspecified name>";
		} else {
			result += "\"" + StringEscapeUtils.escapeJava(componentName) + "\"";
		}
		result = MiscUtils.formatOccurrence(result, occurrencesToSkip);
		return result;
	}

}
