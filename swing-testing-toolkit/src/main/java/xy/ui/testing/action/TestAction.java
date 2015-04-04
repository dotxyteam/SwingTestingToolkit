package xy.ui.testing.action;

import java.awt.Component;

import xy.ui.testing.TesterUI;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestingError;

public abstract class TestAction {

	protected ComponentFinder componentFinder;

	public abstract void execute(Component c);

	public ComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(ComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	public boolean initializeFrom(Component c) {
		for (Class<?> componentFinderClass : TesterUI.COMPONENT_FINDER_CLASSESS) {
			ComponentFinder componentFinder;
			try {
				componentFinder = (ComponentFinder) componentFinderClass
						.newInstance();
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			if (componentFinder.initializeFrom(c)) {
				setComponentFinder(componentFinder);
				return true;
			}
		}
		return false;
	}
	
	public void findComponentAndExecute(){
		Component c = componentFinder.find();
		if (c == null) {
			throw new TestingError("Unable to find "
					+ componentFinder.toString());
		}
		execute(c);
	}

}
