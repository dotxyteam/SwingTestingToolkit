package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;

import xy.ui.testing.util.TestingError;

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
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent) {
		return false;
	}

	@Override
	public void execute(Component c) {
		try {
			Thread.sleep(secondsToWait * 1000);
		} catch (InterruptedException e) {
			throw new TestingError(e);
		}
	}

	@Override
	public String getValueDescription() {
		return secondsToWait + " seconds";
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
		return "Wait " + getValueDescription();
	}

}
