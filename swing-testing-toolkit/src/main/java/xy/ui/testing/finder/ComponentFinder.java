package xy.ui.testing.finder;

import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.util.ValidationError;

public abstract class ComponentFinder implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract Component find();

	public abstract boolean initializeFrom(final Component c);
	
	public abstract int getWindowIndex();

	public abstract void validate() throws ValidationError;
	
}
