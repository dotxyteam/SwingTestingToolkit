package xy.ui.testing.finder;

import java.awt.Component;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import xy.ui.testing.util.TestingUtils;

public class VisibleStringComponentFinder extends ComponentFinder {
	private static final long serialVersionUID = 1L;

	protected String visibleString = "";

	public String getVisibleString() {
		return visibleString;
	}

	public void setVisibleString(String visibleString) {
		this.visibleString = visibleString;
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		visibleString = StringUtils.join(TestingUtils.extractVisibleStrings(c),
				", ");
		return visibleString.length() > 0;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c) {
		return visibleString.equals(StringUtils.join(
				TestingUtils.extractVisibleStrings(c), ", "));
	}

	@Override
	public String toString() {
		return "Component n°" + (occurrencesToSkip + 1)
				+ " displaying the string \""
				+ StringEscapeUtils.escapeJava(visibleString)
				+ "\" in the window n°" + (windowIndex + 1);
	}
}
