package xy.ui.testing.finder;

import java.awt.Component;

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
		return "Component n°" + (occurrencesToSkip + 1) + " of type '"
				+ className + "' in the window n°" + (windowIndex+1);
	}

}
