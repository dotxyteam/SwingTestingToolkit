package xy.ui.testing.action.window;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

import xy.reflect.ui.info.annotation.Validating;
import xy.ui.testing.util.ValidationError;

public class CloseWindowAction extends TargetWindowTestAction {

	private static final long serialVersionUID = 1L;

	
	@Override
	protected boolean initializeSpecificProperties(Window w) {
		return true;
	}


	@Override
	public void execute(Component c) {
		Window window = (Window) c;
		WindowEvent closeEvent = new WindowEvent(window,
				WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue()
				.postEvent(closeEvent);
	}

	@Override
	public String getValueDescription() {
		return "";
	}


	public static boolean matchIntrospectionRequestEvent(AWTEvent event) {
		if (event instanceof WindowEvent) {
			WindowEvent windowEvent = (WindowEvent) event;
			if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Validating
	public void validate() throws ValidationError {
	}
}
