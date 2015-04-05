package xy.ui.testing.finder;

import java.awt.Component;
import java.awt.Window;
import java.io.Serializable;

import xy.ui.testing.util.IComponentTreeVisitor;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public abstract class ComponentFinder implements Serializable{
	private static final long serialVersionUID = 1L;
	
	protected int windowIndex;
	protected int occurrencesToSkip;

	protected abstract boolean matchesInContainingWindow(Component c);

	protected abstract boolean initializeSpecificCriterias(
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
			if(!isValidWindow(window)){
				continue;
			}
			if (windowCount == windowIndex) {
				return find(window);
			}
			windowCount++;
		}
		throw new TestingError(
				"Component not found: Containing window index is out of bounds: "
						+ windowIndex + ": Only " + windowCount
						+ " window(s) found");
	}

	protected boolean isValidWindow(Window window) {
		if(TestingUtils.isTesterUIComponent(window)){
			return false;
		}
		if(!window.isVisible()){
			return false;
		}
		return true;
	}

	protected Component find(Window containingWindow) {
		final Component[] result = new Component[1];
		TestingUtils.visitComponentTree(containingWindow,
				new IComponentTreeVisitor() {
					int occurrences = 0;

					@Override
					public boolean visit(Component c) {
						if (matchesInContainingWindow(c)) {
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
		if (!initializeSpecificCriterias(c)) {
			return false;
		}
		Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
		if(!initializeWindowIndex(componentWindow)){
			return false;
		}
		if(!initializeOccurrencesToSkip(componentWindow, c)){
			return false;
		}
		return true;
	}

	protected boolean initializeOccurrencesToSkip(Window componentWindow, final Component c) {
		occurrencesToSkip = 0;
		final boolean[] ok = new boolean[] { false };		
		TestingUtils.visitComponentTree(componentWindow,
				new IComponentTreeVisitor() {
					@Override
					public boolean visit(Component otherComponent) {
						if (otherComponent == c) {
							ok[0] = true;
							return false;
						}
						if (matchesInContainingWindow(otherComponent)) {
							occurrencesToSkip++;
						}
						return true;
					}
				});
		return ok[0];

	}

	protected boolean initializeWindowIndex(Window componentWindow) {
		windowIndex = 0;
		for (Window window : Window.getWindows()) {
			if(!isValidWindow(window)){
				continue;
			}
			if(window == componentWindow){
				return true;
			}
			windowIndex++;
		}
		return false;
	}

}
