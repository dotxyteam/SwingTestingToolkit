package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.TesterUI;
import xy.ui.testing.util.ValidationError;

public abstract class TestAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterUI testerUI);

	public abstract Component findComponent();

	public abstract void execute(Component c);

	public abstract void validate() throws ValidationError;

	public abstract String getValueDescription();

	public abstract String getComponentInformation();

}
