package xy.ui.testing.action;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;

import xy.ui.testing.finder.WindowFinder;

public class CloseWindowAction extends TestAction {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean initializeFrom(Component c) {
		if(!(c instanceof Window)){
			return false;
		}
		Window window = (Window) c;
		WindowFinder windowFinder = new WindowFinder();
		if(!windowFinder.initializeFrom(window)){
			return false;
		}
		setComponentFinder(windowFinder);
		return true;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c) {
		throw new AssertionError();
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
