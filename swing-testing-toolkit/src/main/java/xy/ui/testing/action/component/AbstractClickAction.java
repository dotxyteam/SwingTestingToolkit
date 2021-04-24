package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Base class of mouse-click test actions.
 * 
 * @author olitank
 *
 */
public abstract class AbstractClickAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected ButtonId button = ButtonId.LEFT_BUTTON;
	protected boolean doubleClick = false;
	protected boolean poupTrigger = false;
	protected int eventsIntervalMilliseconds = 100;

	public int getEventsIntervalMilliseconds() {
		return eventsIntervalMilliseconds;
	}

	public void setEventsIntervalMilliseconds(int eventsIntervalMilliseconds) {
		this.eventsIntervalMilliseconds = eventsIntervalMilliseconds;
	}

	public ButtonId getButton() {
		return button;
	}

	public void setButton(ButtonId button) {
		this.button = button;
	}

	public boolean isDoubleClick() {
		return doubleClick;
	}

	public void setDoubleClick(boolean doubleClick) {
		this.doubleClick = doubleClick;
	}

	public boolean isPoupTrigger() {
		return poupTrigger;
	}

	public void setPoupTrigger(boolean poupTrigger) {
		this.poupTrigger = poupTrigger;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		return true;
	}

	@Override
	public void execute(final Component c, Tester tester) {
		MiscUtils.ensureStartedInUIThread(new Runnable() {
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
		MouseEvent mouseEvent;

		mouseEvent = createPressedEvent(c);
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
	}

	protected MouseEvent createReleaseEvent(Component c) {
		return new MouseEvent(c, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, c.getWidth() / 2,
				c.getHeight() / 2, 1, false, getButtonMask());
	}

	protected MouseEvent createPressedEvent(Component c) {
		return new MouseEvent(c, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, c.getWidth() / 2,
				c.getHeight() / 2, 1, poupTrigger, getButtonMask());
	}

	protected int getButtonMask() {
		if (button == ButtonId.LEFT_BUTTON) {
			return MouseEvent.BUTTON1;
		} else if (button == ButtonId.MIDDLE_BUTTON) {
			return MouseEvent.BUTTON2;
		} else if (button == ButtonId.RIGHT_BUTTON) {
			return MouseEvent.BUTTON3;
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public String getValueDescription() {
		return button.name().replace("_", " ") + (doubleClick ? " x 2" : "");
	}

	@Override
	public String toString() {
		return (doubleClick ? "Double-click" : "Click") + " on " + getComponentInformation();
	}

	public enum ButtonId {
		LEFT_BUTTON, MIDDLE_BUTTON, RIGHT_BUTTON
	}

	@Override
	public void validate() throws ValidationError {
		if (button == null) {
			throw new ValidationError("Missing button identifier");
		}
		super.validate();
	};

}
