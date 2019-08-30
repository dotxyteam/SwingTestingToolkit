import java.io.File;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;

public class TestAssertions {

	public static void main(String[] args) throws Exception {
		TestingUtils.assertSuccessfulReplay(new TestEditor(new Tester()), new File("efgegrgeg"));
	}

}
