package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that just checks the number of open windows.
 * 
 * @author olitank
 *
 */
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
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor) {
		count = countWindows(testEditor.getTester());
		return true;
	}

	@Override
	public void execute(Component c, Tester tester) {
	}

	protected int countWindows(Tester tester) {
		int n = 0;
		for (Window window : Window.getWindows()) {
			if (tester.isTestable(window) && tester.isVisible(window)) {
				n++;
			}
		}
		return n;
	}

	@Override
	public String getValueDescription() {
		return Integer.toString(count);
	}

	@Override
	public Component findComponent(Tester tester) {
		int n = countWindows(tester);
		if (count != n) {
			throw new TestFailure(
					"The number of currently open windows (" + n + ") does not match the declared number: " + count);
		}
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

	@Override
	public void validate() throws ValidationError {
		if (count < 0) {
			throw new ValidationError("Negative count forbidden");
		}
	}

}
