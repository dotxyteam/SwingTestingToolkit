package xy.ui.testing.finder;

import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.ValidationError;

/**
 * The base class of component finders.
 * 
 * @author olitank
 *
 */
public abstract class ComponentFinder implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * @param tester The test specification and execution object.
	 * @return The reference of the found component.
	 */
	public abstract Component find(Tester tester);

	/**
	 * Is called to adjust the configuration of the current component finder
	 * according to the nature/state of the given component. If the component is
	 * compatible then the component finder should be able to find its reference
	 * after the call of this method. It is intended to be used when "recording"
	 * test actions.
	 * 
	 * @param c          The component to initialize from.
	 * @param testEditor A reference to the test editor that was used to invoke this
	 *                   method.
	 * @return Whether the initialization was successful or not. 'true' will usually
	 *         be returned if the component finder is compatible with the given
	 *         component.
	 */
	public abstract boolean initializeFrom(final Component c, TestEditor testEditor);

	/**
	 * @return The index of the window in which the component is searched for.
	 */
	public abstract int getWindowIndex();

	/**
	 * Allows to check the configuration of the current component finder.
	 * 
	 * @throws ValidationError If the current component finder configuration is not
	 *                         valid.
	 */
	public abstract void validate() throws ValidationError;

}
