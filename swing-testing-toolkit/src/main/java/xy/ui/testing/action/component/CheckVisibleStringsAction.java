package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that checks the strings displayed on a component.
 * 
 * @author olitank
 *
 */
public class CheckVisibleStringsAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected List<String> visibleStrings = new ArrayList<String>();
	protected boolean completenessChecked = false;
	protected boolean orderChecked = false;
	protected boolean negated = false;

	public List<String> getVisibleStrings() {
		return visibleStrings;
	}

	public void setVisibleStrings(List<String> visibleStrings) {
		this.visibleStrings = visibleStrings;
	}

	public void loadVisibleStringsFromText(String s) {
		visibleStrings = MiscUtils.parseStringList(s);
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public boolean isCompletenessChecked() {
		return completenessChecked;
	}

	public void setCompletenessChecked(boolean completenessChecked) {
		this.completenessChecked = completenessChecked;
	}

	public boolean isOrderChecked() {
		return orderChecked;
	}

	public void setOrderChecked(boolean orderChecked) {
		this.orderChecked = orderChecked;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		visibleStrings.addAll(TestingUtils.extractComponentTreeDisplayedStrings(c, testEditor.getTester()));
		return true;
	}

	@Override
	public Component findComponent(Tester tester) {
		Component c = super.findComponent(tester);
		List<String> currentVisibleStrings = TestingUtils.extractComponentTreeDisplayedStrings(c, tester);
		String checkFailureMessage = null;
		if (completenessChecked && orderChecked) {
			if (!visibleStrings.equals(currentVisibleStrings)) {
				checkFailureMessage = "The visible string(s) have changed";
			}
		} else if (completenessChecked && !orderChecked) {
			{
				List<String> currentVisibleStringsMinusVisibleStrings = new ArrayList<String>(currentVisibleStrings);
				currentVisibleStringsMinusVisibleStrings.removeAll(visibleStrings);
				if (currentVisibleStringsMinusVisibleStrings.size() > 0) {
					checkFailureMessage = "The following visible string(s) were not declared: "
							+ MiscUtils.formatStringList(currentVisibleStringsMinusVisibleStrings);
				}
			}
			{
				List<String> visibleStringsMinusCurrentVisibleStrings = new ArrayList<String>(visibleStrings);
				visibleStringsMinusCurrentVisibleStrings.removeAll(currentVisibleStrings);
				if (visibleStringsMinusCurrentVisibleStrings.size() > 0) {
					checkFailureMessage = "The following declared string(s) are not visible: "
							+ MiscUtils.formatStringList(visibleStringsMinusCurrentVisibleStrings);
				}
			}
		} else if (!completenessChecked && orderChecked) {
			List<String> currentVisibleStrings2 = new ArrayList<String>(currentVisibleStrings);
			currentVisibleStrings2.retainAll(visibleStrings);
			if (!visibleStrings.equals(currentVisibleStrings2)) {
				checkFailureMessage = "The visible strings order or occurences have changed: "
						+ MiscUtils.formatStringList(currentVisibleStrings2);
			}
		} else if (!completenessChecked && !orderChecked) {
			SortedSet<String> visibleStringSortedSet = new TreeSet<String>(visibleStrings);
			visibleStringSortedSet.removeAll(currentVisibleStrings);
			if (visibleStringSortedSet.size() > 0) {
				checkFailureMessage = "The following declared visible string(s) were not found: "
						+ MiscUtils.formatStringList(new ArrayList<String>(visibleStringSortedSet));
			}
		}
		if (negated) {
			if (checkFailureMessage == null) {
				checkFailureMessage = "The check was successful but negated";
			} else {
				checkFailureMessage = null;
			}
		}
		if (checkFailureMessage != null) {
			throw new TestFailure("Visible strings checking failed: " + checkFailureMessage
					+ ".\nThese are the expected and actual visible strings:\n"
					+ MiscUtils.formatStringList(visibleStrings) + "\n"
					+ MiscUtils.formatStringList(currentVisibleStrings));
		}
		return c;
	}

	@Override
	public void execute(Component c, Tester tester) {
	}

	@Override
	public void validate() throws ValidationError {
		if (visibleStrings.size() == 0) {
			throw new ValidationError("The visible strings to check have not been defined");
		}
	}

	@Override
	public String getValueDescription() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < visibleStrings.size(); i++) {
			if (i > 0) {
				result.append(", ");
			}
			result.append("\"" + StringEscapeUtils.escapeJava(visibleStrings.get(i)) + "\"");
		}
		return result.toString();
	}

	@Override
	public String toString() {
		return "Verify " + getValueDescription();
	}

}
