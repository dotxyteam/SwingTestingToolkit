package xy.ui.testing.action;

import java.awt.Component;
import java.io.Serializable;

public abstract class TestAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract boolean initializeFrom(Component c);
	public abstract Component findComponent();
	public abstract void execute(Component c);

	public abstract String getValueDescription();
	public abstract String getComponentDescription();



}
