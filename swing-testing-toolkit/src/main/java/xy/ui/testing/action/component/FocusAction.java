package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;

/**
 * Base class of mouse-click test actions.
 * 
 * @author olitank
 *
 */
public class FocusAction extends TargetComponentTestAction {
	private static final long serialVersionUID = 1L;

	protected EventType eventType = EventType.FOCUS_GAIN;

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
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
				apply(c);
			}
		});
	}

	protected void apply(Component c) {
		for (FocusListener l : c.getFocusListeners()) {
			if (eventType == EventType.FOCUS_GAIN) {
				l.focusGained(createEvent(c));
			} else if (eventType == EventType.FOCUS_LOSS) {
				l.focusLost(createEvent(c));
			} else {
				throw new AssertionError();
			}
		}
	}

	protected FocusEvent createEvent(Component c) {
		int id;
		if (eventType == EventType.FOCUS_GAIN) {
			id = FocusEvent.FOCUS_GAINED;
		} else if (eventType == EventType.FOCUS_LOSS) {
			id = FocusEvent.FOCUS_LOST;
		} else {
			throw new AssertionError();
		}
		return new FocusEvent(c, id);
	}

	@Override
	public String getValueDescription() {
		return eventType.name().replace("_", " ");
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (eventType == EventType.FOCUS_GAIN) {
			result.append("Focus gain");
		} else if (eventType == EventType.FOCUS_LOSS) {
			result.append("Focus loss");
		} else {
			throw new AssertionError();
		}
		result.append(" on " + getComponentInformation());
		return result.toString();
	}

	public enum EventType {
		FOCUS_GAIN, FOCUS_LOSS
	}

}
