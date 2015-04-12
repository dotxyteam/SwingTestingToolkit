package xy.ui.testing.finder;

import java.awt.Component;
import java.text.MessageFormat;

public class ClassBasedComponentFinder extends MatchingComponentFinder {
	private static final long serialVersionUID = 1L;
	
	protected String className = "";

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	protected boolean matchesInContainingWindow(Component c) {
		return c.getClass().getName().equals(className);
	}

	@Override
	protected boolean initializeSpecificCriterias(Component c) {
		className = c.getClass().getName();
		return true;
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"<{0}> component ''{1}'' in the window n°{2}",
				className, (occurrencesToSkip + 1), (windowIndex+1));
	}

}
