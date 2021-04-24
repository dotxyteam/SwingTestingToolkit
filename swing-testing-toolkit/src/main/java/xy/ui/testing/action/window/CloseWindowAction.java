package xy.ui.testing.action.window;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that closes a window.
 * 
 * @author olitank
 *
 */
public class CloseWindowAction extends TargetWindowTestAction {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean initializeSpecificProperties(Window w, TestEditor testEditor) {
		return true;
	}

	@Override
	public void execute(Component c, Tester tester) {
		Window window = (Window) c;
		final WindowEvent closeEvent = new WindowEvent(window, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeEvent);
	}

	@Override
	public String getValueDescription() {
		return "";
	}

	@Override
	public void validate() throws ValidationError {
	}

	@Override
	public String toString() {
		return "Close " + getComponentInformation();
	}

	public static boolean matchesEvent(AWTEvent event) {
		if (event instanceof WindowEvent) {
			WindowEvent windowEvent = (WindowEvent) event;
			if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
				return true;
			}
		}
		return false;
	}

}
