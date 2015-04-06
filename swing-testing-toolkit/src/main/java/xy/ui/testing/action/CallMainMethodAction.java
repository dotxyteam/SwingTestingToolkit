package xy.ui.testing.action;

import java.awt.Component;

import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class CallMainMethodAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected String className = "";

	public String getMainClassName() {
		return className;
	}

	public void setMainClassName(String mainClassName) {
		this.className = mainClassName;
	}

	@Override
	public void setComponentFinder(ComponentFinder componentFinder) {
		throw new TestingError(
				"Cannot set the component finder for this type of action");
	}

	@Override
	public boolean initializeFrom(Component c) {
		return false;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c) {
		return false;
	}

	@Override
	public void execute(Component c) {
		new Thread(CallMainMethodAction.class.getName()) {
			@Override
			public void run() {
				try {
					TestingUtils.launchClassMainMethod(className);
				} catch (Exception e) {
					throw new TestingError("Failed to run the main method of '"
							+ className + "': " + e.toString(), e);
				}
			}
		}.start();
	}

	@Override
	public String getValueDescription() {
		return className;
	}

	@Override
	public String toString() {
		return "Call main method of <" + className + ">";
	}
}
