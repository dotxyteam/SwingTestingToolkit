package xy.ui.testing.finder;

import java.awt.Component;
import java.io.Serializable;

public abstract class ComponentFinder implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract Component find();

	public abstract boolean initializeFrom(final Component c);
	
	public abstract int getWindowIndex();

}
