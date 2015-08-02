package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;

import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class CallMainMethodAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected String className = "";
	protected String[] arguments;
	protected Integer checkThrownExceptionAFterSeconds = 2;

	public String getClassName() {
		return className;
	}

	public void setClassName(String mainClassName) {
		this.className = mainClassName;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
	
	

	public Integer getCheckThrownExceptionAFterSeconds() {
		return checkThrownExceptionAFterSeconds;
	}

	public void setCheckThrownExceptionAFterSeconds(
			Integer checkThrownExceptionAFterSeconds) {
		this.checkThrownExceptionAFterSeconds = checkThrownExceptionAFterSeconds;
	}

	@Override
	public boolean initializeFrom(Component c,
			AWTEvent introspectionRequestEvent) {
		return false;
	}

	@Override
	public void execute(Component c) {
		final TestingError[] error = new TestingError[1];
		new Thread(CallMainMethodAction.class.getName()) {
			@Override
			public void run() {
				try {
					TestingUtils.launchClassMainMethod(className);
				} catch (Exception e) {
					error[0] = new TestingError(
							"Failed to run the main method of '" + className
									+ "': " + e.toString(), e);
				}
			}
		}.start();
		if(checkThrownExceptionAFterSeconds != null){
			try {
				Thread.sleep(checkThrownExceptionAFterSeconds*1000);
			} catch (InterruptedException e) {
				throw new TestingError(e);
			}
			if(error[0] != null){
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
