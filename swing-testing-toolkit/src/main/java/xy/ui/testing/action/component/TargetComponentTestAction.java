package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;

import xy.ui.testing.Tester;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public abstract class TargetComponentTestAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected ComponentFinder componentFinder;

	protected abstract boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor);

	public ComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(ComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor) {
		for (Class<?> componentFinderClass : testEditor.getComponentFinderClasses()) {
			ComponentFinder componentFinderCandidate;
			try {
				componentFinderCandidate = (ComponentFinder) componentFinderClass.newInstance();
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			if (componentFinderCandidate.initializeFrom(c, testEditor)) {
				setComponentFinder(componentFinderCandidate);
				break;
			}
		}
		if (getComponentFinder() == null) {
			return false;
		}
		if (!initializeSpecificProperties(c, introspectionRequestEvent, testEditor)) {
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
				throw new TestFailure("Unable to find " + getComponentFinder().toString(), "Window image",
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
