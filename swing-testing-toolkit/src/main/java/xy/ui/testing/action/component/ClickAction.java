package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.LocationOnComponent;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;

/**
 * Test action that sends a mouse-click event on a component.
 * 
 * @author olitank
 *
 */
public class ClickAction extends AbstractClickAction {

	private static final long serialVersionUID = 1L;

	protected LocationOnComponent locationOnComponent;
	protected EventMode eventMode = EventMode.PRESS_AND_RELEASE;

	public EventMode getEventMode() {
		return eventMode;
	}

	public void setEventMode(EventMode eventMode) {
		this.eventMode = eventMode;
	}

	public LocationOnComponent getLocationOnComponent() {
		return locationOnComponent;
	}

	public void setLocationOnComponent(LocationOnComponent locationOnComponent) {
		this.locationOnComponent = locationOnComponent;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		return true;
	}

	@Override
	public void execute(final Component c, Tester tester) {
		MiscUtils.expectingToBeInUIThread(new Runnable() {
			@Override
			public void run() {
				click(c);
				if (doubleClick) {
					try {
						Thread.sleep(eventsIntervalMilliseconds);
					} catch (InterruptedException e) {
						throw new TestFailure(e);
					}
					click(c);
				}
			}
		});
	}

	protected void click(Component c) {
		if (eventMode == EventMode.CLICK) {
			MouseEvent mouseEvent = createClickedEvent(c);
			for (MouseListener l : c.getMouseListeners()) {
				if (mouseEvent.isConsumed()) {
					break;
				}
				l.mouseClicked(mouseEvent);
			}
		} else if (eventMode == EventMode.PRESS_AND_RELEASE) {
			MouseEvent mouseEvent = createPressedEvent(c);
			for (MouseListener l : c.getMouseListeners()) {
				if (mouseEvent.isConsumed()) {
					break;
				}
				l.mousePressed(mouseEvent);
			}
			try {
				Thread.sleep(eventsIntervalMilliseconds);
			} catch (InterruptedException e) {
				throw new TestFailure(e);
			}
			mouseEvent = createReleaseEvent(c);
			for (MouseListener l : c.getMouseListeners()) {
				if (mouseEvent.isConsumed()) {
					break;
				}
				l.mouseReleased(mouseEvent);
			}
		} else {
			throw new AssertionError();
		}
	}

	protected Point getPoint(Component c) {
		Point result = new Point();
		if (locationOnComponent == null) {
			result = new Point(c.getWidth() / 2, c.getHeight() / 2);
		} else {
			result = locationOnComponent.getPoint(c);
		}
		return result;
	}

	protected MouseEvent createClickedEvent(Component c) {
		Point point = getPoint(c);
		return new MouseEvent(c, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, point.x, point.y, 1,
				poupTrigger, getButtonMask());
	}

	protected MouseEvent createReleaseEvent(Component c) {
		Point point = getPoint(c);
		return new MouseEvent(c, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, point.x, point.y, 1, false,
				getButtonMask());
	}

	protected MouseEvent createPressedEvent(Component c) {
		Point point = getPoint(c);
		return new MouseEvent(c, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, point.x, point.y, 1,
				poupTrigger, getButtonMask());
	}

	public enum EventMode {
		CLICK, PRESS_AND_RELEASE

	}

}
