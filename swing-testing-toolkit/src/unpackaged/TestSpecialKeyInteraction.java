import xy.ui.testing.Tester;
import xy.ui.testing.action.component.SendKeysAction;
import xy.ui.testing.editor.TesterEditor;


public class TestSpecialKeyInteraction {

	public static void main(String[] args) {
		new TesterEditor(new Tester()).getSwingRenderer().openObjectFrame(new SendKeysAction.SpecialKey(), null, null);
	}

}
