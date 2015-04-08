package xy.ui.testing.action;

import java.awt.Component;

import xy.ui.testing.TesterUI;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestingError;

public abstract class TargetComponentTestAction extends TestAction{

	private static final long serialVersionUID = 1L;
	
	protected ComponentFinder componentFinder;

	protected abstract boolean initializeSpecificProperties(Component c);

	public ComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(ComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	@Override
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
	
	
	
	@Override
	public Component findComponent() {
		if (getComponentFinder() == null) {
			return null;
		} else {
			Component c = getComponentFinder().find();
			if (c == null) {
				throw new TestingError("Unable to find "
						+ getComponentFinder().toString());
			}
			return c;
		}
	}

	@Override
	public String getComponentDescription() {
		if(getComponentFinder() == null){
			return null;
		}
		return getComponentFinder().toString();
	}
	
	


}
