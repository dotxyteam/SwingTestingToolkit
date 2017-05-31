package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class WaitAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected int secondsToWait = 30;

	public int getSecondsToWait() {
		return secondsToWait;
	}

	public void setSecondsToWait(int secondsToWait) {
		this.secondsToWait = secondsToWait;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor) {
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		try {
			Thread.sleep(secondsToWait * 1000);
		} catch (InterruptedException e) {
			throw new TestFailure(e);
		}
	}

	@Override
	public String getValueDescription() {
		return secondsToWait + " seconds";
	}

	@Override
	public Component findComponent(Tester tester) {
		return null;
	}

	@Override
	public String getComponentInformation() {
		return "";
	}

	@Override
	public String toString() {
		return "Wait " + getValueDescription();
	}

	@Override
	public void validate() throws ValidationError {
		if (secondsToWait <= 0) {
			throw new ValidationError("The number of seconds to wait must be > 0");
		}
	}

}
