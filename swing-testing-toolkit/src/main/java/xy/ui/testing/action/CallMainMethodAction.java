package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;

import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;

public class CallMainMethodAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected String className = "";
	protected String[] arguments;
	protected Integer checkThrownExceptionAfterSeconds = 2;

	public String getClassName() {
		return className;
	}

	public void setClassName(String mainClassName) throws ClassNotFoundException{
		Class.forName(mainClassName);
		this.className = mainClassName;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	public Integer getCheckThrownExceptionAfterSeconds() {
		return checkThrownExceptionAfterSeconds;
	}

	public void setCheckThrownExceptionAfterSeconds(Integer checkThrownExceptionAfterSeconds) {
		if(checkThrownExceptionAfterSeconds != null){
			if(checkThrownExceptionAfterSeconds < 0){
				throw new NumberFormatException("Negative number forbidden");
			}
		}
		this.checkThrownExceptionAfterSeconds = checkThrownExceptionAfterSeconds;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent) {
		return false;
	}

	@Override
	public void execute(Component c) {
		final TestFailure[] error = new TestFailure[1];
		new Thread(CallMainMethodAction.class.getName()) {
			@Override
			public void run() {
				try {
					TestingUtils.launchClassMainMethod(className);
				} catch (Exception e) {
					error[0] = new TestFailure("Failed to run the main method of '" + className + "': " + e.toString(),
							e);
				}
			}
		}.start();
		if (checkThrownExceptionAfterSeconds != null) {
			try {
				Thread.sleep(checkThrownExceptionAfterSeconds * 1000);
			} catch (InterruptedException e) {
				throw new TestFailure(e);
			}
			if (error[0] != null) {
				throw error[0];
			}
		}
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
