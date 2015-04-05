package xy.ui.testing.finder;

import java.awt.Component;

import xy.ui.testing.util.TestingError;

public class WindowFinder extends ComponentFinder {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean matchesInContainingWindow(Component c) {
		return true;
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		return true;
	}

	@Override
	public void setOccurrencesToSkip(int occurrencesToSkip) {
		throw new TestingError(
				"Cannot change the number of occurrences to skip on this type of component finder");
	}

	@Override
	public String toString() {
		return "Window n°" + (windowIndex + 1);
	}
}
