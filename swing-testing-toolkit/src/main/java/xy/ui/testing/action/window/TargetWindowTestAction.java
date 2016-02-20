package xy.ui.testing.action.window;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;

import xy.ui.testing.Tester;
import xy.ui.testing.TesterUI;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.MatchingComponentFinder;
import xy.ui.testing.util.TestingUtils;

public abstract class TargetWindowTestAction extends TestAction {

	protected static final long serialVersionUID = 1L;

	protected abstract boolean initializeSpecificProperties(Window w);

	protected MatchingComponentFinder windowFinder = new MatchingComponentFinder() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean matchesInContainingWindow(Component c, Tester tester) {
			return c instanceof Window;
		}

		@Override
		protected boolean initializeSpecificValues(Component c, TesterUI testerUI) {
			return true;
		}
	};

	public int getWindowIndex() {
		return windowFinder.getWindowIndex();
	}

	public void setWindowIndex(int index) {
		windowFinder.setWindowIndex(index);
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterUI testerUI) {
		Window window = TestingUtils.getWindowAncestorOrSelf(c);
		if (!windowFinder.initializeFrom(window, testerUI)) {
			return false;
		}
		initializeSpecificProperties(window);
		return true;
	}

	@Override
	public Window findComponent(Tester tester) {
		Window window = (Window) windowFinder.find(tester);
		return window;
	}

	@Override
	public String getComponentInformation() {
		return "Window n°" + (getWindowIndex() + 1);
	}
}
