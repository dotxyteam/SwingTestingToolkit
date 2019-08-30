import java.util.Arrays;

import xy.ui.testing.util.TestingUtils;

public class TestVisibleStringsParse {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out
				.println(TestingUtils
						.parseVisibleStrings("  \"1\"  ,  \"abc\",  \"\\\"abc\\\"\"  ,  \"a\\na\"  "));
		System.out.println(Arrays.asList("1", "abc", "\"abc\"", "a\na"));
	}
}
