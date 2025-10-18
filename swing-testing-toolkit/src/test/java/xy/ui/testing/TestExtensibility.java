package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.Test;

import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

/**
 * If you want a specific test action/assertion that is not available in the
 * framework, you can easily extend it.
 * 
 * Suppose you have a custom component that you need to manipulate during the
 * automatic replay. You first need to create your action class by extending the
 * {@link TestAction} class. Most of the time it will be enough to sub-class
 * {@link TargetComponentTestAction}.
 * 
 * Once your action class is created, you must register it with the
 * {@link TestEditor} instance that you will use in order to be able use it
 * while creating your test specification through the editor.
 * 
 * @author olitank
 *
 */
public class TestExtensibility {

	/**
	 * JUnit test of this class.
	 * 
	 * @throws Exception If the test fails.
	 */
	@Test
	public void test() throws Exception {
		TestingUtils.assertSuccessfulReplay(new File(System.getProperty("swing-testing-toolkit.project.directory", "./")
				+ "test-specifications/testExtensibility.stt"));
	}

	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				/*
				 * Create the test editor and register the custom action class.
				 */
				Tester tester = new Tester();
				TestEditor testEditor = new TestEditor(tester) {

					private static final long serialVersionUID = 1L;

					@Override
					public Class<?>[] getTestActionClasses() {
						List<Class<?>> result = new ArrayList<Class<?>>(Arrays.asList(super.getTestActionClasses()));
						result.add(CustomComponentAssertion.class);
						return result.toArray(new Class<?>[result.size()]);
					}

				};

				/*
				 * Customize and open the test editor.
				 */
				testEditor.setDecorationsBackgroundColor(new Color(68, 61, 205));
				testEditor.setDecorationsForegroundColor(new Color(216, 214, 245));
				testEditor.open();
			}
		});
	}

	/*
	 * This is the custom test action class. Usually you would use it either to send
	 * events, change or check several properties of the target component during the
	 * replay.
	 */
	public static class CustomComponentAssertion extends TargetComponentTestAction {
		private static final long serialVersionUID = 1L;

		/*
		 * Here are your action settings/properties. IMPORTANT: you must provide a
		 * default constructor (implicit or not) and getters and setters for these
		 * properties in order to be able to edit them in the TestEditor.
		 */
		private String expectpedValueOfPropertyToCheck;
		private int newValueOfPropertyToChange;

		public String getExpectpedValueOfPropertyToCheck() {
			return expectpedValueOfPropertyToCheck;
		}

		public void setExpectpedValueOfPropertyToCheck(String expectpedValueOfPropertyToCheck) {
			this.expectpedValueOfPropertyToCheck = expectpedValueOfPropertyToCheck;
		}

		public int getNewValueOfPropertyToChange() {
			return newValueOfPropertyToChange;
		}

		public void setNewValueOfPropertyToChange(int newValueOfPropertyToChange) {
			this.newValueOfPropertyToChange = newValueOfPropertyToChange;
		}

		@Override
		protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
				TestEditor testEditor) {
			/*
			 * Here you can initialize your action from the state of the component it is
			 * targeted to.
			 * 
			 * Note that the return value is used to indicate that the action class can
			 * handle (return true) the component passed as argument or not (return false).
			 */
			if (c instanceof CustomComponent) {
				expectpedValueOfPropertyToCheck = ((CustomComponent) c).propertyToCheck;
				newValueOfPropertyToChange = ((CustomComponent) c).propertyToChange + 1;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void execute(final Component c, Tester tester) {
			/*
			 * Here you actually execute the action/assertion.
			 */
			if (!expectpedValueOfPropertyToCheck.equals(((CustomComponent) c).propertyToCheck)) {
				throw new TestFailure("propertyToCheck is not ok");
			}

			/*
			 * Note that the current thread is the UI thread. Then it is safe here to
			 * execute instructions that updates the UI. No need to use
			 * SwingUtilities.invoke* methods.
			 */
			((CustomComponent) c).propertyToChange = newValueOfPropertyToChange;
		}

		@Override
		public void validate() throws ValidationError {
			/*
			 * Here you can optionally verify your action properties. The thrown exceptions
			 * will be displayed in the test editor.
			 */
			if (newValueOfPropertyToChange < 0) {
				throw new TestFailure("propertyToChange is not ok");
			}
			super.validate();
		}

		@Override
		public String getValueDescription() {
			/*
			 * Here you should provide information about your action current property
			 * values.
			 */
			return "propertytoCheck=" + expectpedValueOfPropertyToCheck + "; propertytoChange="
					+ newValueOfPropertyToChange;
		}
	}

	/*
	 * the custom component class.
	 */
	public static class CustomComponent extends Component {
		private static final long serialVersionUID = 1L;

		public String propertyToCheck = "property value";
		public int propertyToChange = 123;
	}

}
