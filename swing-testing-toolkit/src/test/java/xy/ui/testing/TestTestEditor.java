package xy.ui.testing;

import java.awt.Color;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;

public class TestTestEditor {

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
			new SwingRenderer(new ReflectionUI()).openObjectDialog(null, new TestTestEditor(), null, null, false,
					false);
		}
	}

	@BeforeClass
	public static void beforeAllTests() {
		TestingUtils.purgeAllReportsDirectory();
	}

	@Test
	public void testComponentFinderOnlyOneEmptyContructor() throws IOException {
		Tester tester = new Tester();
		TestEditor testEditor = new TestEditor(tester);
		for (Class<?> cls : testEditor.getComponentFinderClasses()) {
			Assert.assertTrue(cls.getConstructors().length == 1);
			Assert.assertTrue(cls.getConstructors()[0].getParameterTypes().length == 0);
		}
	}

	@Test
	public void testMenus() throws IOException {
		Tester tester = new Tester();
		TestingUtils.assertSuccessfulReplay(tester, TestTestEditor.class.getResourceAsStream("testMenus.stt"));
	}

	@Test
	public void testTestEditor() throws IOException {
		Tester tester = new Tester();
		TestingUtils.assertSuccessfulReplay(tester, TestTestEditor.class.getResourceAsStream("testTestEditor.stt"));
	}
}
