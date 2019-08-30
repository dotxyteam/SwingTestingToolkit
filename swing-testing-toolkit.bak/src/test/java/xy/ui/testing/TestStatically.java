package xy.ui.testing;

import java.io.IOException;

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

	@Test
	public void testComponentFinderOnlyOneEmptyContructor() throws IOException {
		Tester tester = new Tester();
		TestEditor testEditor = new TestEditor(tester);
		for (Class<?> cls : testEditor.getComponentFinderClasses()) {
			Assert.assertTrue(cls.getConstructors().length == 1);
			Assert.assertTrue(cls.getConstructors()[0].getParameterTypes().length == 0);
		}
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
