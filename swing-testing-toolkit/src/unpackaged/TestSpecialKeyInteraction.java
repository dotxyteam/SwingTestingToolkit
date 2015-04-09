import xy.ui.testing.TesterUI;
import xy.ui.testing.action.component.SendKeysAction;


public class TestSpecialKeyInteraction {

	public static void main(String[] args) {
		TesterUI.INSTANCE.openObjectFrame(new SendKeysAction.SpecialKey(), null, null);
	}

}
