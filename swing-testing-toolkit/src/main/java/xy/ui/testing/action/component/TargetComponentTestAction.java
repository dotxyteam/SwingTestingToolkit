package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;

import xy.ui.testing.TesterUI;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestingError;

public abstract class TargetComponentTestAction extends TestAction{

	private static final long serialVersionUID = 1L;
	
	protected ComponentFinder componentFinder;

	protected abstract boolean initializeSpecificProperties(Component c, AWTEvent event);

	public ComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(ComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent) {
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
		if(!initializeSpecificProperties(c, introspectionRequestEvent)){
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
	public String getComponentInformation() {
		if(getComponentFinder() == null){
			return null;
		}
		return getComponentFinder().toString();
	}

	public static boolean matchIntrospectionRequestEvent(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
				if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
					return true;
				}
			}
		}
		return false;
	}
	
	


}
