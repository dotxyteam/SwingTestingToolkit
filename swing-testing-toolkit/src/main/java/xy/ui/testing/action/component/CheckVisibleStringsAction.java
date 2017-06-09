package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

@SuppressWarnings("unused")
public class CheckVisibleStringsAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected List<String> visibleStrings = new ArrayList<String>();
	protected boolean completenessChecked = false;
	protected boolean orderChecked = false;

	public List<String> getVisibleStrings() {
		return visibleStrings;
	}

	public void setVisibleStrings(List<String> visibleStrings) {
		this.visibleStrings = visibleStrings;
	}

	public void loadVisibleStringsFromText(String s) {
		visibleStrings = TestingUtils.parseVisibleStrings(s);
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
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		visibleStrings.addAll(TestingUtils.extractComponentTreeDisplayedStrings(c, testEditor.getTester()));
		return true;
	}

	@Override
	public Component findComponent(Tester tester) {
		Component c = super.findComponent(tester);
		List<String> currentVisibleStrings = TestingUtils.extractComponentTreeDisplayedStrings(c, tester);
		try {
			if (completenessChecked && orderChecked) {
				if (!visibleStrings.equals(currentVisibleStrings)) {
					throw new TestFailure("The visible string(s) have changed");
				}
			} else if (completenessChecked && !orderChecked) {
				{
					List<String> currentVisibleStrings2 = new ArrayList<String>(currentVisibleStrings);
					currentVisibleStrings2.removeAll(visibleStrings);
					if (currentVisibleStrings.size() > 0) {
						throw new TestFailure("The following visible string(s) were not declared: "
								+ TestingUtils.formatVisibleStrings(new ArrayList<String>(currentVisibleStrings2)));
					}
				}
				{
					List<String> visibleStrings2 = new ArrayList<String>(visibleStrings);
					visibleStrings2.removeAll(currentVisibleStrings);
					if (visibleStrings2.size() > 0) {
						throw new TestFailure("The following declared string(s) are not visible: "
								+ TestingUtils.formatVisibleStrings(new ArrayList<String>(visibleStrings2)));
					}
				}
			} else if (!completenessChecked && orderChecked) {
				List<String> currentVisibleStrings2 = new ArrayList<String>(currentVisibleStrings);
				currentVisibleStrings2.retainAll(visibleStrings);
				if (!visibleStrings.equals(currentVisibleStrings2)) {
					throw new TestFailure("The visible strings order or occurences have changed");
				}
			} else if (!completenessChecked && !orderChecked) {
				SortedSet<String> visibleStringSortedSet = new TreeSet<String>(visibleStrings);
				visibleStringSortedSet.removeAll(currentVisibleStrings);
				if (visibleStringSortedSet.size() > 0) {
					throw new TestFailure("The following declared visible string(s) were not found: "
							+ TestingUtils.formatVisibleStrings(new ArrayList<String>(visibleStringSortedSet)));
				}
			}
			return c;
		} catch (Exception e) {
			throw new TestFailure(
					"Visible strings checking failed: " + e.toString()
							+ ".\nThese are the original and the current visible strings:\n"
							+ TestingUtils.formatVisibleStrings(visibleStrings) + "\n"
							+ TestingUtils.formatVisibleStrings(currentVisibleStrings), e);
		}
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
	public String toString() {
		return "Check " + getValueDescription() + " displayed on " + getComponentInformation();
	}

}
