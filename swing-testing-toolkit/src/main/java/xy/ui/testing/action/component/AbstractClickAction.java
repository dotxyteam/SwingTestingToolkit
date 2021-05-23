package xy.ui.testing.action.component;

import java.awt.event.MouseEvent;
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
	public void validate() throws ValidationError {
		if (button == null) {
			throw new ValidationError("Missing button identifier");
		}
		super.validate();
	};

	@Override
	public String toString() {
		return (doubleClick ? "Double-click" : "Click") + " on " + getComponentInformation();
	}

	public enum ButtonId {
		LEFT_BUTTON, MIDDLE_BUTTON, RIGHT_BUTTON
	}

}
