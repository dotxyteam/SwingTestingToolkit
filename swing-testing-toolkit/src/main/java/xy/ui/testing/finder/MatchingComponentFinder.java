package xy.ui.testing.finder;

import java.awt.Component;
import java.awt.Window;
import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.IComponentTreeVisitor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public abstract class MatchingComponentFinder extends ComponentFinder {
	private static final long serialVersionUID = 1L;

	protected int windowIndex;
	protected int occurrencesToSkip;

	protected abstract boolean matchesInContainingWindow(Component c, Tester tester);

	protected abstract boolean initializeSpecificValues(Component c, TestEditor testEditor);

	public int getWindowIndex() {
		return windowIndex;
	}

	public void setWindowIndex(int windowIndex) {
		this.windowIndex = windowIndex;
	}

	public int getOccurrencesToSkip() {
		return occurrencesToSkip;
	}

	public void setOccurrencesToSkip(int occurrencesToSkip) {
		this.occurrencesToSkip = occurrencesToSkip;
	}

	@Override
	public Component find(Tester tester) {
		int windowCount = 0;
		for (Window window : Window.getWindows()) {
			boolean testable = true;
			if (!tester.isTestable(window)) {
				testable = false;
			}
			if (!testable) {
				continue;
			}
			if (windowCount == windowIndex) {
				return find(window, tester);
			}
			windowCount++;
		}
		throw new TestFailure(
				"Component not found: Containing window index is out of bounds: " + windowIndex + ": Only "
						+ windowCount + " window(s) found",
				"Found window(s) image", TestingUtils.saveAllTestableWindowImages(tester));
	}

	protected Component find(Window containingWindow, final Tester tester) {
		final Component[] result = new Component[1];
		TestingUtils.visitComponentTree(tester, containingWindow, new IComponentTreeVisitor() {
			int occurrences = 0;

			@Override
			public boolean visit(Component c) {
				if (tester.isVisible(c)) {
					if (matchesInContainingWindow(c, tester)) {
						if (occurrences == occurrencesToSkip) {
							result[0] = c;
							return false;
						} else {
							occurrences++;
						}
					}
				}
				return true;
			}

		}, true);
		return result[0];
	}

	@Override
	public boolean initializeFrom(final Component c, TestEditor testEditor) {
		if (!initializeSpecificValues(c, testEditor)) {
			return false;
		}
		Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
		if (!initializeWindowIndex(componentWindow, testEditor)) {
			return false;
		}
		if (!initializeOccurrencesToSkip(componentWindow, c, testEditor)) {
			return false;
		}
		return true;
	}

	protected boolean initializeOccurrencesToSkip(Window componentWindow, final Component c,
			final TestEditor testEditor) {
		occurrencesToSkip = 0;
		final boolean[] ok = new boolean[] { false };
		TestingUtils.visitComponentTree(testEditor.getTester(), componentWindow, new IComponentTreeVisitor() {
			@Override
			public boolean visit(Component otherComponent) {
				if (otherComponent == c) {
					ok[0] = true;
					return false;
				}
				if (testEditor.getTester().isVisible(c)) {
					if (matchesInContainingWindow(otherComponent, testEditor.getTester())) {
						occurrencesToSkip++;
					}
				}
				return true;
			}
		}, true);
		return ok[0];

	}

	protected boolean initializeWindowIndex(Window componentWindow, TestEditor testEditor) {
		windowIndex = 0;
		for (Window window : Window.getWindows()) {
			if (!testEditor.getTester().isTestable(window)) {
				continue;
			}
			if (window == componentWindow) {
				return true;
			}
			windowIndex++;
		}
		return false;
	}

	@Override
	public void validate() throws ValidationError {
		if (occurrencesToSkip < 0) {
			throw new ValidationError("The number of occurences to skip is invalid. Must be >= 0");
		}
		if (windowIndex < 0) {
			throw new ValidationError("The window index is invalid. Must be >= 0");
		}
	}

	protected static String appendOccurrenceNumber(String s, int occurrencesToSkip) {
		if (occurrencesToSkip == 0) {
			return s;
		}
		return s + " (" + (occurrencesToSkip + 1) + ")";
	}

}
