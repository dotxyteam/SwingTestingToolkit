package xy.ui.testing.action;

import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.TesterUI;
import xy.ui.testing.finder.ComponentFinder;

public abstract class TestAction implements Serializable{

	private static final long serialVersionUID = 1L;
	
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

}
