package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;

import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class CheckNumberOfOpenWindowsAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent) {
		return false;
	}

	@Override
	public void execute(Component c) {
		int n = countWindows();
		if (count != n) {
			throw new TestingError("The number of currently open windows (" + n
					+ ") does not match the declared number: " + count);
		}
	}

	protected int countWindows() {
		int n = 0;
		for (Window window : Window.getWindows()) {
			if (TestingUtils.isTestableWindow(window)) {
				n++;
			}
		}
		return n;
	}

	@Override
	public String getValueDescription() {
		return count + " open windows";
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
		return "Check that " + count + " windows are open";
	}

}
