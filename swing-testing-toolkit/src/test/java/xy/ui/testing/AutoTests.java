package xy.ui.testing;

import java.awt.Color;
import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;

public class AutoTests {

	public boolean booleanData;
	public int intData;
	public String stringData;

	public static void main(String[] args) {
		if ((args.length == 1) && args[0].equals("TestEditor frame")) {
			Tester tester = new Tester();
			TestEditor testEditor = new TestEditor(tester);
			testEditor.setDecorationsBackgroundColor(new Color(68, 61, 205));
			testEditor.setDecorationsForegroundColor(new Color(216, 214, 245));
			testEditor.open();
		} else if ((args.length == 1) && args[0].equals("TestTestEditor dialog")) {
			new SwingRenderer(new ReflectionUI()).openObjectDialog(null, new AutoTests(), null, null, false,
					false);
		}
	}

	@BeforeClass
	public static void beforeAllTests() {
		TestingUtils.purgeAllReportsDirectory();
	}

	
	@Test
	public void testMenus() throws Exception {
		Tester tester = new Tester();
		TestingUtils.assertSuccessfulReplay(tester, AutoTests.class.getResourceAsStream("testMenus.stt"));
	}

	@Test
	public void testTestEditor() throws Exception {
		Tester tester = new Tester();
		TestingUtils.assertSuccessfulReplay(tester, AutoTests.class.getResourceAsStream("testTestEditor.stt"));
	}
}
