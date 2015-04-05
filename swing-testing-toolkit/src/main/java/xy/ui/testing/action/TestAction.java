package xy.ui.testing.action;

import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.TesterUI;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestingError;

public abstract class TestAction implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected ComponentFinder componentFinder;

	public abstract void execute(Component c);
	protected abstract boolean initializeSpecificProperties(Component c);

	public ComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(ComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	public boolean initializeFrom(Component c) {
		for (Class<?> componentFinderClass : TesterUI.COMPONENT_FINDER_CLASSESS) {
			ComponentFinder componentFinderCandidate;
			try {
				componentFinderCandidate = (ComponentFinder) componentFinderClass
						.newInstance();
			} catch (Exception e) {
				throw new TestingError(e);
			}
			if (componentFinderCandidate.initializeFrom(c)) {
				setComponentFinder(componentFinderCandidate);
				break;
			}
		}
		if(getComponentFinder() == null){
			return false;
		}
		if(!initializeSpecificProperties(c)){
			return false;
		}		
		return true;
	}


}
