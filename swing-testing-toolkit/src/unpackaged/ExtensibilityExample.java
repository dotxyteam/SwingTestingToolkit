import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.ui.testing.Tester;
import xy.ui.testing.TesterUI;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class ExtensibilityExample {

	public static void main(String[] args) throws Exception {

		/*
		 * If you want a specific test action/assertion that is not available in
		 * the framework, you can extend it. It is very very very ... easy.
		 * 
		 * Suppose you have a custom component that you need to manipulate
		 * during the automatic replay. You first need to create your action
		 * class by extending the 'TestAction' class. Most of the time it will
		 * be enough to sub-class 'TargetComponentTestAction'. See the
		 * 'TargetComponentTestAction' sub-class below for more information.
		 * 
		 * Once your action class is done, you must register it with the
		 * TesterUI instance that you will use.
		 */
		Tester tester = new Tester();
		TesterUI testerUI = new TesterUI(tester) {

			@Override
			public Class<?>[] getTestActionClasses() {
				List<Class<?>> result = new ArrayList<Class<?>>(Arrays.asList(super.getTestActionClasses()));
				result.add(CustomComponentAssertion.class);
				return result.toArray(new Class<?>[result.size()]);
			}

		};
		testerUI.open();

	}

	/*
	 * This is the custom test action class. Usually you would use it either to
	 * send events, change some properties of some target component (action) or
	 * check that some stored property values do not change during the replay
	 * (assertion).
	 */
	public static class CustomComponentAssertion extends TargetComponentTestAction {
		private static final long serialVersionUID = 1L;

		/*
		 * Here are your action settings/properties. IMPORTANT: you must provide
		 * getters and setters for these properties in order to be able to edit
		 * them in the TesterUI.
		 */
		private boolean property1;
		private int property2;

		public boolean isProperty1() {
			return property1;
		}

		public void setProperty1(boolean property1) {
			this.property1 = property1;
		}

		public int getProperty2() {
			return property2;
		}

		public void setProperty2(int property2) {
			this.property2 = property2;
		}

		@Override
		protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
			/*
			 * Here you can initialize your action from the state of the
			 * component it is targeted to.
			 * 
			 * Note that the return value is used to indicate that the action
			 * class can handle (return true) the component passed as argument.
			 */
			if (c instanceof CustomComponent) {
				property1 = ((CustomComponent) c).property1;
				property2 = ((CustomComponent) c).property2;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void execute(Component c, Tester tester) {
			/*
			 * Here you actually execute the action/assertion.
			 */
			if (((CustomComponent) c).property1 != property1) {
				throw new TestFailure("property1 is not ok");
			}
			if (((CustomComponent) c).property2 != property2) {
				throw new TestFailure("property2 is not ok");
			}
		}

		@Override
		public void validate() throws ValidationError {
			/*
			 * Here you can optionally verify your action properties.
			 */
			if (property2 < 0) {
				throw new TestFailure("property2 is not ok");
			}
		}

		@Override
		public String getValueDescription() {
			/*
			 * Here you should provide informations about your action current
			 * property values.
			 */
			return "property1=" + property1 + "; property2=" + property2;
		}
	}

	/*
	 * the custom component class.
	 */
	public static class CustomComponent extends Component {
		private static final long serialVersionUID = 1L;

		public boolean property1;
		public int property2;
	}

}
