package xy.ui.testing.action.window;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import xy.ui.testing.finder.MatchingComponentFinder;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

@SuppressWarnings("unused")
public class CheckWindowVisibleStringsAction extends TargetWindowTestAction {

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
		return Arrays.toString(visibleStrings.toArray());
	}

	@Override
	public String toString() {
		return "Check the visible strings of the " + getComponentInformation();
	}

	@Override
	protected boolean initializeSpecificProperties(Window w) {
		visibleStrings.addAll(TestingUtils.collectVisibleStrings(w));
		return true;
	}

	@Override
	public Window findComponent() {
		Window window = super.findComponent();
		List<String> currentVisibleStrings = TestingUtils
				.collectVisibleStrings(window);
		check(currentVisibleStrings);
		return window;
	}

	protected void check(List<String> currentVisibleStrings) {
		try {
			if (completenessChecked && orderChecked) {
				if (!visibleStrings.equals(currentVisibleStrings)) {
					throw new TestingError(
							"The visible string(s) have changed");
				}
			} else if (completenessChecked && !orderChecked) {
				currentVisibleStrings = new ArrayList<String>(currentVisibleStrings);
				currentVisibleStrings.removeAll(visibleStrings);
				if (currentVisibleStrings.size() > 0) {
					throw new TestingError(
							"The following visible string(s) were not declared: "
									+ TestingUtils
											.formatVisibleStrings(new ArrayList<String>(
													currentVisibleStrings)));
				}
			} else if (!completenessChecked && orderChecked) {
				currentVisibleStrings = new ArrayList<String>(currentVisibleStrings);
				currentVisibleStrings.retainAll(visibleStrings);
				if (!visibleStrings.equals(currentVisibleStrings)) {
					throw new TestingError(
							"The visible strings order have changed");
				}
			} else if (!completenessChecked && !orderChecked) {
				SortedSet<String> visibleStringSortedSet = new TreeSet<String>(
						visibleStrings);
				visibleStringSortedSet.removeAll(currentVisibleStrings);
				if (visibleStringSortedSet.size() > 0) {
					throw new TestingError(
							"The following declared visible string(s) were not found: "
									+ TestingUtils
											.formatVisibleStrings(new ArrayList<String>(
													visibleStringSortedSet)));
				}
			}
		} catch (Exception e) {
			throw new TestingError(
					"Visible strings checking failed: "
							+ e.toString()
							+ ":\nThese are the original and the current visible strings:\n"
							+ TestingUtils.formatVisibleStrings(visibleStrings)
							+ "\n"
							+ TestingUtils
									.formatVisibleStrings(currentVisibleStrings),
					e);
		}
	}

	@Override
	public void execute(Component c) {
	}
}