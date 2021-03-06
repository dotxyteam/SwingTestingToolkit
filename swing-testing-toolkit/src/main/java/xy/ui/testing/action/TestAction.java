package xy.ui.testing.action;

import java.awt.AWTEvent;
import java.awt.Component;
import java.io.Serializable;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * The base class of test actions.
 * 
 * @author olitank
 *
 */
public abstract class TestAction implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Is called to adjust the configuration of the current test action according to
	 * the nature/state of the given component. If the component is compatible then
	 * the test action should be able to find its reference and use it after the
	 * call of this method. It is intended to be used when "recording" test actions.
	 * Note that there may be some test actions that do not use the provided
	 * component to initialize themselves.
	 * 
	 * @param c                         The component to initialize from.
	 * @param introspectionRequestEvent The AWT event that was triggered when
	 *                                  requesting the initialization of the current
	 *                                  test action.
	 * @param testEditor                A reference to the test editor that was used
	 *                                  to invoke this method.
	 * @return Whether the initialization was successful or not. 'true' will usually
	 *         be returned if the test action is compatible with the component.
	 */
	public abstract boolean initializeFrom(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor);

	/**
	 * Searches for the component on which this test action should be executed. Note
	 * that this method is called from the UI thread.
	 * 
	 * @param tester The test specification and execution object.
	 * @return The reference to the component on which this test action should be
	 *         executed or null if the action is not intended to be executed on a
	 *         component.
	 * @throws TestFailure If the component was searched for but could not be found.
	 *                     Note that throwing this exception may cause the test
	 *                     action to be retried. Throwing another type of exception
	 *                     will interrupt the replay.
	 */
	public abstract Component findComponent(Tester tester) throws TestFailure;

	/**
	 * Executes the current action on the given component. Note that this method is
	 * called from the UI thread.
	 * 
	 * @param c      The component reference that should have been retrieved by
	 *               calling {@link #findComponent(Tester)}.
	 * @param tester The test specification and execution object.
	 * @throws TestFailure If the action was not executed successfully. Note that
	 *                     throwing this exception may cause the test action to be
	 *                     retried. Throwing another type of exception will
	 *                     interrupt the replay.
	 */
	public abstract void execute(Component c, Tester tester) throws TestFailure;

	/**
	 * ALlows to check the configuration of the current test action.
	 * 
	 * @throws ValidationError If the current test action configuration is not
	 *                         valid.
	 */
	public abstract void validate() throws ValidationError;

	/**
	 * @return A text summarizing the configuration of the this test action.
	 */
	public abstract String getValueDescription();

	/**
	 * @return A text describing the component targeted by this test action.
	 */
	public abstract String getComponentInformation();

	protected boolean disabled = false;

	/**
	 * @return Whether the current test action is disabled (will be skipped during
	 *         test execution) or not.
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Updates whether the current test action is disabled (will be skipped during
	 * test execution) or not.
	 * 
	 * @param disabled The new flag.
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * This method is called before {@link #findComponent(Tester)} and
	 * {@link #execute(Component, Tester)} to eventually setup the action. Note that
	 * this method is not called from the UI thread.
	 * 
	 * @param tester The test specification and execution object.
	 * @throws TestFailure If the action was not prepared successfully. Note that
	 *                     throwing this exception may cause the test action to be
	 *                     retried. Throwing another type of exception will
	 *                     interrupt the replay.
	 */
	public void prepare(Tester tester) throws TestFailure {
	}

}
