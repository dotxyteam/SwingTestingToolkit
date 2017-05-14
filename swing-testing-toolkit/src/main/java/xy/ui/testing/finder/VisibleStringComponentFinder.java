package xy.ui.testing.finder;

import java.awt.Component;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.ValidationError;

public class VisibleStringComponentFinder extends MatchingComponentFinder {
	private static final long serialVersionUID = 1L;

	protected String visibleString = "";

	public String getVisibleString() {
		return visibleString;
	}

	public void setVisibleString(String visibleString) {
		this.visibleString = visibleString;
	}

	@Override
	protected boolean initializeSpecificValues(Component c, TesterEditor testerEditor) {
		List<String> visibleStrings = testerEditor.getTester().extractVisibleStrings(c);
		if (visibleStrings.size() == 0) {
			return false;
		}
		visibleString = visibleStrings.get(0);
		return true;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c, Tester tester) {
		List<String> visibleStrings = tester.extractVisibleStrings(c);
		for (String s : visibleStrings) {
			if (visibleString.equals(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String result = "";
		if (visibleString == null) {
			result += "<unspecified string>";
		} else {
			result += "\"" + StringEscapeUtils.escapeJava(visibleString) + "\"";
		}
		result += " n°" + (occurrencesToSkip + 1);
		return result;
	}

	@Override
	public void validate() throws ValidationError {
		super.validate();
		if (visibleString == null) {
			throw new ValidationError("The visible string to find has not been defined");
		}
	}

}
