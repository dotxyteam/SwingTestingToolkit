package xy.ui.testing.action.window;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;

import xy.ui.testing.Tester;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.finder.MatchingComponentFinder;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestingUtils;

/**
 * Base class of test actions that target a window.
 * 
 * @author olitank
 *
 */
public abstract class TargetWindowTestAction extends TestAction {

	protected static final long serialVersionUID = 1L;

	protected abstract boolean initializeSpecificProperties(Window w, TestEditor testEditor);

	protected MatchingComponentFinder windowFinder = new MatchingComponentFinder() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean matchesInContainingWindow(Component c, Tester tester) {
			return c instanceof Window;
		}

		@Override
		protected boolean initializeSpecificValues(Component c, TestEditor testEditor) {
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
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor) {
		Window window = TestingUtils.getWindowAncestorOrSelf(c);
		if (!windowFinder.initializeFrom(window, testEditor)) {
			return false;
		}
		initializeSpecificProperties(window, testEditor);
		return true;
	}

	@Override
	public Window findComponent(Tester tester) {
		Window window = (Window) windowFinder.find(tester);
		return window;
	}

	@Override
	public String getComponentInformation() {
		return MiscUtils.formatOccurrence("<window>", getWindowIndex());
	}
}
