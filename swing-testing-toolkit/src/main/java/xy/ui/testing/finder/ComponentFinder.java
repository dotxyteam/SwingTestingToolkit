package xy.ui.testing.finder;

import java.awt.Component;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.ui.testing.TesterUI;
import xy.ui.testing.util.IComponentTreeVisitor;
import xy.ui.testing.util.TestingUtils;

public abstract class ComponentFinder {

	protected int windowIndex;
	protected int occurrencesToSkip;

	protected abstract boolean matches(Component c);

	protected abstract boolean initializeAllCriteriasExceptOccurrencesToskip(
			Component c);

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

	public Component find() {
		int windowCount = 0;
		for (Window window : Window.getWindows()) {
			for (JPanel form : TesterUI.INSTANCE.getObjectByForm().keySet()) {
				if (window == SwingUtilities.getWindowAncestor(form)) {
					continue;
				}
			}
			if (windowCount == windowIndex) {
				return find(window);
			}
			windowCount++;
		}
		throw new AssertionError(
				"Component not found: Containing window not found: Window index is invalid: "
						+ windowIndex + ": Only " + windowCount
						+ " windows found");
	}

	protected Component find(Window containingWindow) {
		final Component[] result = new Component[1];
		TestingUtils.visitComponentTree(containingWindow,
				new IComponentTreeVisitor() {
					int occurrences = 0;

					@Override
					public boolean visit(Component c) {
						if (matches(c)) {
							if (occurrences == occurrencesToSkip) {
								result[0] = c;
								return false;
							} else {
								occurrences++;
							}
						}
						return true;
					}
				});
		return result[0];
	}

	public boolean initializeFrom(final Component c) {
		if (!initializeAllCriteriasExceptOccurrencesToskip(c)) {
			return false;
		}
		occurrencesToSkip = 0;
		final boolean[] ok = new boolean[] { false };
		Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
		TestingUtils.visitComponentTree(componentWindow,
				new IComponentTreeVisitor() {
					@Override
					public boolean visit(Component otherComponent) {
						if (otherComponent == c) {
							ok[0] = true;
							return false;
						}
						if (matches(otherComponent)) {
							occurrencesToSkip++;
						}
						return true;
					}
				});
		return ok[0];
	}

}
