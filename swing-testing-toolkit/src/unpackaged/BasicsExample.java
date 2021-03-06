import java.io.File;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;

public class BasicsExample {

	
	
	public static void main(String[] args) throws Exception {

		/*
		 * You first need to create a Tester instance.
		 */
		Tester tester = new Tester();

		/*
		 * You can then use the GUI (or not) to create your test cases.
		 */
		TestEditor testEditor = new TestEditor(tester);
		testEditor.open();

		/*
		 * Once you are done creating and saving your test case to a file, you
		 * can reload and replay it programmatically.
		 */
		File testCaseFile = new File("test-case1.stt");
		tester.loadFromFile(testCaseFile);
		tester.replayAll();

		/*
		 * Note that in case of replay failure, the tested window(s) will remain
		 * open. You must ensure that they get closed after any successful or
		 * failed replay session.
		 */
		TestingUtils.closeAllTestableWindows(tester);

		/*
		 * You can also use a utility method to replay the test case file.
		 */
		TestingUtils.assertSuccessfulReplay(tester, testCaseFile);

		/*
		 * In case of replay failure some screenshots of the tested windows are
		 * generated in a sub-directory of the current directory. You may want
		 * purge them every time you re-launch your test session.
		 */
		TestingUtils.purgeAllReportsDirectory();

	}


}
