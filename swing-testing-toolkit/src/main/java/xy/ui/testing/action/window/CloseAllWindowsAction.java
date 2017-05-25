package xy.ui.testing.action.window;

import java.awt.AWTEvent;
import java.awt.Component;
import javax.swing.SwingUtilities;

import xy.ui.testing.Tester;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public class CloseAllWindowsAction extends TestAction {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Component c, final Tester tester) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TestingUtils.closeAllTestableWindows(tester);
			}
		});
	}

	@Override
	public String getValueDescription() {
		return "";
	}

	@Override
	public boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterEditor testerEditor) {
		return true;
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
	public void validate() throws ValidationError {
	}

	@Override
	public String toString() {
		return "Close All Windows";
	}

}
