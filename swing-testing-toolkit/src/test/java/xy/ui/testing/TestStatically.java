package xy.ui.testing;

import org.junit.Assert;
import org.junit.Test;

import xy.ui.testing.editor.TestEditor;

public class TestStatically {

	@Test
	public void testClassHierarchy() {
		assertNoInheritanceBetweenClasses(TestEditor.BUILT_IN_TEST_ACTION_CLASSES);
		assertNoInheritanceBetweenClasses(TestEditor.BUILT_IN_COMPONENT_FINDRER_CLASSES);
		assertNoInheritanceBetweenClasses(TestEditor.BUILT_IN_KEYBOARD_INTERACTION_CLASSES);
	}

	private void assertNoInheritanceBetweenClasses(Class<?>[] classes) {
		for (int i = 0; i < classes.length; i++) {
			Class<?> ci = classes[i];
			for (int j = 0; j < classes.length; j++) {
				Class<?> cj = classes[j];
				if (i == j) {
					continue;
				}
				if (ci.isAssignableFrom(cj)) {
					Assert.fail("<" + cj + "> must not be a subclass of <" + ci + ">");
				}
			}
		}

	}

}
