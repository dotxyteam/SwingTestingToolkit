package xy.ui.testing.action;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import xy.ui.testing.finder.VisibleStringComponentFinder;
import xy.ui.testing.finder.WindowFinder;
import xy.ui.testing.util.IComponentTreeVisitor;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class CheckWindowVisibleStringsAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected List<String> visibleStrings = new ArrayList<String>();

	public List<String> getVisibleStrings() {
		return visibleStrings;
	}

	public void setVisibleStrings(List<String> visibleStrings) {
		this.visibleStrings = visibleStrings;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c) {
		WindowFinder windowFinder = new WindowFinder();
		Window window = TestingUtils.getWindowAncestorOrSelf(c);
		windowFinder.initializeFrom(window);
		setComponentFinder(windowFinder);
		visibleStrings.addAll(collectVisibleStrings(window));
		return true;
	}

	@Override
	public void execute(Component c) {
		Window window = TestingUtils.getWindowAncestorOrSelf(c);
		Collection<String> currentVisibleStrings = collectVisibleStrings(window);
		if (!visibleStrings.equals(currentVisibleStrings)) {
			throw new TestingError(
					"The visible strings have changed: These are the original and the current visible strings:\n"
							+ visibleStrings + "\n" + currentVisibleStrings);
		}
	}

	public static Collection<String> collectVisibleStrings(Window window) {
		final List<String> result = new ArrayList<String>();
		TestingUtils.visitComponentTree(window, new IComponentTreeVisitor() {

			@Override
			public boolean visit(Component c) {
				String s = VisibleStringComponentFinder.extractVisibleString(c);
				if (s != null) {
					result.add(s);
				}
				return true;
			}
		});
		return result;
	}

	@Override
	public String getValueDescription() {
		return  Arrays.toString(visibleStrings.toArray());
	}

	@Override
	public String toString() {
		return "Check the visible strings of the " + getComponentFinder();
	}

}
