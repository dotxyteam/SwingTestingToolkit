package xy.ui.testing;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public class LaunchUIAutomationTests {

	@Before
	public void before() {
		TestingUtils.closeAllTestableWindows();
	}

	@Test
	public void testMenus() throws IOException {
		Tester.assertSuccessfulReplay(LaunchUIAutomationTests.class
				.getResourceAsStream("testMenus.stt"));
	}

}
