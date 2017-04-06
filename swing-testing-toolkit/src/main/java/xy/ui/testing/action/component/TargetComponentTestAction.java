package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;

import xy.ui.testing.Tester;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public abstract class TargetComponentTestAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected ComponentFinder componentFinder;

	protected abstract boolean initializeSpecificProperties(Component c, AWTEvent event);

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

	public ComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(ComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterEditor testerEditor) {
		for (Class<?> componentFinderClass : testerEditor.getComponentFinderClasses()) {
			ComponentFinder componentFinderCandidate;
			try {
				componentFinderCandidate = (ComponentFinder) componentFinderClass.newInstance();
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			if (componentFinderCandidate.initializeFrom(c, testerEditor)) {
				setComponentFinder(componentFinderCandidate);
				break;
			}
		}
		if (getComponentFinder() == null) {
			return false;
		}
		if (!initializeSpecificProperties(c, introspectionRequestEvent)) {
			return false;
		}
		return true;
	}

	@Override
	public Component findComponent(Tester tester) {
		if (getComponentFinder() == null) {
			return null;
		} else {
			Component c = getComponentFinder().find(tester);
			if (c == null) {
				throw new TestFailure("Unable to find " + getComponentFinder().toString(), "Window",
						TestingUtils.saveTestableWindowImage(tester, getComponentFinder().getWindowIndex()));
			}
			return c;
		}
	}

	@Override
	public String getComponentInformation() {
		if (getComponentFinder() == null) {
			return "<unspecified component>";
		}
		return getComponentFinder().toString();
	}

	public void validate() throws ValidationError {
		if (componentFinder == null) {
			throw new ValidationError("Missing component finding information");
		}
	}

}
