package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public class CallMainMethodAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected String className = "";
	protected String[] arguments = new String[0];
	protected Integer checkThrownExceptionAfterSeconds = 2;

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

	public Integer getCheckThrownExceptionAfterSeconds() {
		return checkThrownExceptionAfterSeconds;
	}

	public void setCheckThrownExceptionAfterSeconds(Integer checkThrownExceptionAfterSeconds) {
		this.checkThrownExceptionAfterSeconds = checkThrownExceptionAfterSeconds;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterEditor testerEditor) {
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		final TestFailure[] error = new TestFailure[1];
		new Thread(CallMainMethodAction.class.getName()) {
			@Override
			public void run() {
				try {
					TestingUtils.launchClassMainMethod(className, arguments);
				} catch (Throwable e) {
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
		StringBuilder result = new StringBuilder("<" + className + ">");
		if (arguments.length > 0) {
			result.append(" ");
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) {
					result.append(" ");
				}
				result.append("\"" + StringEscapeUtils.escapeJava(arguments[i]) + "\"");
			}
		}
		return result.toString();
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
		return "Call main method of <" + className + ">";
	}

	@Override
	public void validate() throws ValidationError {
		if (checkThrownExceptionAfterSeconds != null) {
			if (checkThrownExceptionAfterSeconds < 0) {
				throw new ValidationError("'Check Thrown Exception After Seconds': Negative number forbidden");
			}
		}

		if ((className == null) || (className.length() == 0)) {
			throw new ValidationError("Missing class name");
		}

		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ValidationError("Invalid class name: '" + className + "': Class not found");
		}

	}

}
