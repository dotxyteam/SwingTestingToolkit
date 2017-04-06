package xy.ui.testing;

import java.awt.Color;
import java.io.IOException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.ui.testing.editor.TesterEditor;
import xy.ui.testing.util.TestingUtils;

public class TestTesterEditor {

	public boolean booleanData;
	public int intData;
	public String stringData;

	public static void main(String[] args) {
		if ((args.length == 1) && args[0].equals("TesterEditor frame")) {
			Tester tester = new Tester();
			TesterEditor testerEditor = new TesterEditor(tester);
			testerEditor.setDecorationsBackgroundColor(new Color(68, 61, 205));
			testerEditor.setDecorationsForegroundColor(new Color(216, 214, 245));
			testerEditor.open();
		} else if ((args.length == 1) && args[0].equals("TestTesterEditor dialog")) {
			new SwingRenderer(new ReflectionUI()).openObjectDialog(null, new TestTesterEditor(), null, null, false,
					false);
		}
	}

	@BeforeClass
	public static void beforeAllTests() {
		TestingUtils.purgeSavedImagesDirectory(new Tester());
	}

	@Test
	public void testComponentFinderOnlyOneEmptyContructor() throws IOException {
		Tester tester = new Tester();
		TesterEditor testerEditor = new TesterEditor(tester);
		for (Class<?> cls : testerEditor.getComponentFinderClasses()) {
			Assert.assertTrue(cls.getConstructors().length == 1);
			Assert.assertTrue(cls.getConstructors()[0].getParameterTypes().length == 0);
		}
	}

	@Test
	public void testMenus() throws IOException {
		Tester tester = new Tester();
		TestingUtils.assertSuccessfulReplay(tester, TestTesterEditor.class.getResourceAsStream("testMenus.stt"));
	}

	@Test
	public void testTesterEditor() throws IOException {
		Tester tester = new Tester();
		TestingUtils.assertSuccessfulReplay(tester, TestTesterEditor.class.getResourceAsStream("testTesterEditor.stt"));
	}
}
