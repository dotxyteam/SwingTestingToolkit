package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.ValidationError;

public abstract class TestAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TesterEditor testerEditor);

	public abstract Component findComponent(Tester tester);

	public abstract void execute(Component c, Tester tester);

	public abstract void validate() throws ValidationError;

	public abstract String getValueDescription();

	public abstract String getComponentInformation();

}
