package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

public class TestExtensibility {

	@Test
	public void test() throws Exception {
		TestingUtils.assertSuccessfulReplay(new File(System.getProperty("swing-testing-toolkit.project.directory", "./")
				+ "test-specifications/testExtensibility.stt"));
		System.out.println("debug");
	}

	public static void main(String[] args) throws Exception {

		/*
		 * If you want a specific test action/assertion that is not available in the
		 * framework, you can easily extend it.
		 * 
		 * Suppose you have a custom component that you need to manipulate during the
		 * automatic replay. You first need to create your action class by extending the
		 * 'TestAction' class. Most of the time it will be enough to sub-class
		 * 'TargetComponentTestAction'. See the 'TargetComponentTestAction' sub-class
		 * below for more information.
		 * 
		 * Once your action class is created, you must register it with the TestEditor
		 * instance that you will use.
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
		testEditor.setDecorationsBackgroundColor(new Color(68, 61, 205));
		testEditor.setDecorationsForegroundColor(new Color(216, 214, 245));
		testEditor.open();

	}

	/*
	 * This is the custom test action class. Usually you would use it either to send
	 * events, change or check several properties of the target component during the
	 * replay.
	 */
	public static class CustomComponentAssertion extends TargetComponentTestAction {
		private static final long serialVersionUID = 1L;

		/*
		 * Here are your action settings/properties. IMPORTANT: you must provide getters
		 * and setters for these properties in order to be able to edit them in the
		 * TestEditor.
		 */
		private boolean propertytoCheckExpectpedValue;
		private int propertytoChangeNewValue;

		public boolean isPropertytoCheckExpectpedValue() {
			return propertytoCheckExpectpedValue;
		}

		public void setPropertytoCheckExpectpedValue(boolean propertytoCheckExpectpedValue) {
			this.propertytoCheckExpectpedValue = propertytoCheckExpectpedValue;
		}

		public int getPropertytoChangeNewValue() {
			return propertytoChangeNewValue;
		}

		public void setPropertytoChangeNewValue(int propertytoChangeNewValue) {
			this.propertytoChangeNewValue = propertytoChangeNewValue;
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
				propertytoCheckExpectpedValue = ((CustomComponent) c).propertytoCheck;
				propertytoChangeNewValue = ((CustomComponent) c).propertytoChange + 1;
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
			if (((CustomComponent) c).propertytoCheck != propertytoCheckExpectpedValue) {
				throw new TestFailure("propertytoCheck is not ok");
			}

			/*
			 * Note that the current thread is the UI thread. Then it is safe here to
			 * execute instructions that updates the UI. No need to use
			 * SwingUtilities.invoke* methods.
			 */
			((CustomComponent) c).propertytoChange = propertytoChangeNewValue;
		}

		@Override
		public void validate() throws ValidationError {
			/*
			 * Here you can optionally verify your action properties. The thrown exceptions
			 * will be displayed in the test editor.
			 */
			if (propertytoChangeNewValue < 0) {
				throw new TestFailure("propertytoChange is not ok");
			}
		}

		@Override
		public String getValueDescription() {
			/*
			 * Here you should provide informations about your action current property
			 * values.
			 */
			return "propertytoCheck=" + propertytoCheckExpectpedValue + "; propertytoChange="
					+ propertytoChangeNewValue;
		}
	}

	/*
	 * the custom component class.
	 */
	public static class CustomComponent extends Component {
		private static final long serialVersionUID = 1L;

		public boolean propertytoCheck;
		public int propertytoChange;
	}

}
