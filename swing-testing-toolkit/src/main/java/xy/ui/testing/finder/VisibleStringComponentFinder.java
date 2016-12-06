package xy.ui.testing.finder;

import java.awt.Component;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.TesterUI;
import xy.ui.testing.util.TestingUtils;
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
	protected boolean initializeSpecificValues(Component c, TesterUI testerUI) {
		visibleString = StringUtils.join(TestingUtils.extractVisibleStrings(c),
				", ");
		return visibleString.length() > 0;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c, Tester tester) {
		return visibleString.equals(StringUtils.join(
				TestingUtils.extractVisibleStrings(c), ", "));
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"component n°{1} labeled with <{0}> in the window n°{2}",
				StringEscapeUtils.escapeJava(visibleString),
				(occurrencesToSkip + 1), (windowIndex + 1));
	}

	@Override
	public void validate() throws ValidationError {
		super.validate();
		if(visibleString == null){
			throw new ValidationError("The visible string to find has not been defined");
		}
	}
	
	
}
