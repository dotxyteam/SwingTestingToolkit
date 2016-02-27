package xy.ui.testing;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import xy.ui.testing.util.TestingUtils;

public class TestTesterUI {

	@Test
	public void testMenus() throws IOException {
		TestingUtils.purgeSavedImagesDirectory();
		TestingUtils.closeAllTestableWindows();
		Tester.assertSuccessfulReplay(TestTesterUI.class
				.getResourceAsStream("testMenus.stt"));
	}

	@Test
	public void testComponentFinderOnlyOneEmptyContructor() throws IOException {
		for(Class<?> cls: TesterUI.DEFAULT.getComponentFinderClasses()){
			Assert.assertTrue(cls.getConstructors().length == 1);
			Assert.assertTrue(cls.getConstructors()[0].getParameterTypes().length == 0);			
		}
	}
	
	
	@Test
	public void testTesterUI() throws IOException {
		TestingUtils.purgeSavedImagesDirectory();
		TestingUtils.closeAllTestableWindows();
		Tester.assertSuccessfulReplay(TestTesterUI.class
				.getResourceAsStream("testTesterUI.stt"));
	}
}
