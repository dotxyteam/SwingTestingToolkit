package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;

import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class CallMainMethodAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected String className = "";

	public String getClassName() {
		return className;
	}

	public void setClassName(String mainClassName) {
		this.className = mainClassName;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent event) {
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
	public Component findComponent() {
		return null;
	}

	@Override
	public String getComponentInformation() {
		return "";
	}

	@Override
	public String toString() {
		return "Call main method of <" + className + ">";
	}

}
