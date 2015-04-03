import xy.ui.testing.TesterUI;
import xy.ui.testing.action.SendKeysAction;


public class TestSpecialKeyInteraction {

	public static void main(String[] args) {
		TesterUI.INSTANCE.openObjectFrame(new SendKeysAction.SpecialKey(), null, null);
	}

}
