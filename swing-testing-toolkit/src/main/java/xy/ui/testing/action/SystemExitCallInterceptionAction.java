package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.security.Permission;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
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
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor) {
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		if (oppposite) {
			disableInterception();
		} else {
			enableInterception();
		}
	}

	public static void enableInterception() {
		System.setSecurityManager(new NoExitSecurityManager(System.getSecurityManager()));
	}

	public static void disableInterception() {
		System.setSecurityManager(((NoExitSecurityManager) System.getSecurityManager()).getInitialSecurityManager());
	}

	public static boolean isInterceptionEnabled() {
		return System.getSecurityManager() instanceof NoExitSecurityManager;
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
			return "Intercept System.exit() calls";
		} else {
			return "Do not intercept System.exit() calls";
		}
	}

	@Override
	public void validate() throws ValidationError {
	}

	public static class NoExitSecurityManager extends SecurityManager {
		protected SecurityManager initialSecurityManager;

		public NoExitSecurityManager(SecurityManager initialSecurityManager) {
			this.initialSecurityManager = initialSecurityManager;
		}

		public SecurityManager getInitialSecurityManager() {
			return initialSecurityManager;
		}

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
	}

}
