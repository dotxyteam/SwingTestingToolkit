package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.security.Permission;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.ValidationError;

public class SystemExitCallInterceptionAction extends TestAction {

	private static final long serialVersionUID = 1L;

	protected boolean oppposite = false;

	public boolean isOppposite() {
		return oppposite;
	}

	public void setOppposite(boolean oppposite) {
		this.oppposite = oppposite;
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterEditor testerEditor) {
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		if (oppposite) {
			System.setSecurityManager(null);
		} else {
			System.setSecurityManager(new SecurityManager() {
				@Override
				public void checkExit(int status) {
					throw new SecurityException("System.exit() call intercepted");
				}

				@Override
				public void checkPermission(Permission perm) {
					// allow anything.
				}

				@Override
				public void checkPermission(Permission perm, Object context) {
					// allow anything.
				}
			});
		}
	}

	@Override
	public String getValueDescription() {
		return "";
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
		if (oppposite) {
			return "Do not intercept System.exit() calls";
		} else {
			return "Intercept System.exit() calls";
		}
	}

	@Override
	public void validate() throws ValidationError {
	}

}
