package xy.ui.testing;

import java.awt.Color;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.ui.testing.util.TestingUtils;

public class TestTesterUI {

	public boolean booleanData;
	public int intData;
	public String stringData;

	public static void main(String[] args) {
		if ((args.length == 1) && args[0].equals("TesterUI frame")) {
			TesterUI testerUI = new TesterUI(new Tester());
			testerUI.setDecorationsBackgroundColor(new Color(68, 61, 205));
			testerUI.setDecorationsForegroundColor(new Color(216, 214, 245));
			testerUI.open();
		} else if ((args.length == 1) && args[0].equals("TestTesterUI dialog")) {
			new SwingRenderer(new ReflectionUI()).openObjectDialog(null, new TestTesterUI(), null, null, false, false);
		}
	}

	@Test
	public void testMenus() throws IOException {
		TestingUtils.purgeSavedImagesDirectory();
		TestingUtils.closeAllTestableWindows();
		Tester.assertSuccessfulReplay(TestTesterUI.class.getResourceAsStream("testMenus.stt"));
	}

	@Test
	public void testComponentFinderOnlyOneEmptyContructor() throws IOException {
		for (Class<?> cls : TesterUI.DEFAULT.getComponentFinderClasses()) {
			Assert.assertTrue(cls.getConstructors().length == 1);
			Assert.assertTrue(cls.getConstructors()[0].getParameterTypes().length == 0);
		}
	}

	@Test
	public void testTesterUI() throws IOException {
		TestingUtils.purgeSavedImagesDirectory();
		TestingUtils.closeAllTestableWindows();
		Tester.assertSuccessfulReplay(TestTesterUI.class.getResourceAsStream("testTesterUI.stt"));
	}
}
