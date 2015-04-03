package xy.ui.testing.action;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SendClickAction extends TestAction {

	public enum ButtonId {
		LEFT_BUTTON, MIDDLE_BUTTON, RIGHT_BUTTON
	};

	protected ButtonId button = ButtonId.LEFT_BUTTON;

	public ButtonId getButton() {
		return button;
	}

	public void setButton(ButtonId button) {
		this.button = button;
	}

	@Override
	public void execute(Component c) {
		int buttonMask;
		if (button == ButtonId.LEFT_BUTTON) {
			buttonMask = MouseEvent.BUTTON1;
		} else if (button == ButtonId.MIDDLE_BUTTON) {
			buttonMask = MouseEvent.BUTTON2;
		} else if (button == ButtonId.RIGHT_BUTTON) {
			buttonMask = MouseEvent.BUTTON3;
		} else {
			throw new AssertionError();
		}
		MouseEvent mouseEvent = new MouseEvent(c, MouseEvent.MOUSE_PRESSED,
				System.currentTimeMillis(), 0, c.getWidth() / 2,
				c.getHeight() / 2, 1, false, buttonMask);
		for (MouseListener l : c.getMouseListeners()) {
			if (mouseEvent.isConsumed()) {
				break;
			}
			l.mousePressed(mouseEvent);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		mouseEvent = new MouseEvent(c, MouseEvent.MOUSE_RELEASED,
				System.currentTimeMillis(), 0, c.getWidth() / 2,
				c.getHeight() / 2, 1, false, buttonMask);
		for (MouseListener l : c.getMouseListeners()) {
			if (mouseEvent.isConsumed()) {
				break;
			}
			l.mouseReleased(mouseEvent);
		}
	}

	@Override
	public String toString() {
		return "Click with the " + button + " on the " + getComponentFinder();
	}

}
