package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import xy.ui.testing.util.TestingError;

public class ClickAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected ButtonId button = ButtonId.LEFT_BUTTON;
	protected boolean doubleClick = false;

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

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		return true;
	}

	@Override
	public void execute(final Component c) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				click(c);
				if (doubleClick) {
					click(c);
				}
			}
		});
	}

	protected void click(Component c) {
		MouseEvent mouseEvent = createPressedEvent(c);
		for (MouseListener l : c.getMouseListeners()) {
			if (mouseEvent.isConsumed()) {
				break;
			}
			l.mousePressed(mouseEvent);
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			throw new TestingError(e);
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
		return new MouseEvent(c, MouseEvent.MOUSE_RELEASED,
				System.currentTimeMillis(), 0, c.getWidth() / 2,
				c.getHeight() / 2, 1, false, getButtonMask());
	}

	protected MouseEvent createPressedEvent(Component c) {
		return new MouseEvent(c, MouseEvent.MOUSE_PRESSED,
				System.currentTimeMillis(), 0, c.getWidth() / 2,
				c.getHeight() / 2, 1, false, getButtonMask());
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
		return button.name() + " " + (doubleClick ? "Double-" : "")
				+ " Click";
	}

	@Override
	public String toString() {
		return getValueDescription() + " on the " + getComponentFinder();
	}

	public enum ButtonId {
		LEFT_BUTTON, MIDDLE_BUTTON, RIGHT_BUTTON
	};

}