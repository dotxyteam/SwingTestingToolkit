package xy.ui.testing.action.window;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

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
}
