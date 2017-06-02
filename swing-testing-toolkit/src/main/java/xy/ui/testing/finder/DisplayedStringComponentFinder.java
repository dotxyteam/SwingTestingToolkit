package xy.ui.testing.finder;

import java.awt.Component;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public class DisplayedStringComponentFinder extends MatchingComponentFinder {
	private static final long serialVersionUID = 1L;

	protected String visibleString = "";
	protected boolean visibleStringInTree = false;

	public String getVisibleString() {
		return visibleString;
	}

	public void setVisibleString(String visibleString) {
		this.visibleString = visibleString;
	}

	public boolean isVisibleStringInTree() {
		return visibleStringInTree;
	}

	public void setVisibleStringInTree(boolean visibleStringInTree) {
		this.visibleStringInTree = visibleStringInTree;
	}

	@Override
	protected boolean initializeSpecificValues(Component c, TestEditor testEditor) {
		List<String> visibleStrings = testEditor.getTester().extractDisplayedStrings(c);
		if (visibleStrings.size() > 0) {
			visibleString = visibleStrings.get(0);
			return true;
		}
		visibleStrings = TestingUtils.extractComponentTreeDisplayedStrings(c, testEditor.getTester());
		if (visibleStrings.size() > 0) {
			visibleString = visibleStrings.get(0);
			visibleStringInTree = true;
			return true;
		}
		return false;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c, Tester tester) {
		List<String> visibleStrings;
		if (visibleStringInTree) {
			visibleStrings = TestingUtils.extractComponentTreeDisplayedStrings(c, tester);
		} else {
			visibleStrings = tester.extractDisplayedStrings(c);

		}
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
		result = MatchingComponentFinder.appendOccurrenceNumber(result, occurrencesToSkip);
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
