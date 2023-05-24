package xy.ui.testing;

import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.ui.testing.action.CallMainMethodAction;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.editor.TestEditor;

public class TestStatically {

	@Test
	public void testClassHierarchy() {
		assertNoInheritanceBetweenClasses(TestEditor.BUILT_IN_TEST_ACTION_CLASSES);
		assertNoInheritanceBetweenClasses(TestEditor.BUILT_IN_COMPONENT_FINDRER_CLASSES);
		assertNoInheritanceBetweenClasses(TestEditor.BUILT_IN_KEYBOARD_INTERACTION_CLASSES);
	}

	@Test
	public void testComponentFinderOnlyOneEmptyContructor() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Tester tester = new Tester();
				TestEditor testEditor = new TestEditor(tester);
				for (Class<?> cls : testEditor.getComponentFinderClasses()) {
					Assert.assertTrue(cls.getConstructors().length == 1);
					Assert.assertTrue(cls.getConstructors()[0].getParameterTypes().length == 0);
				}
			}
		});
	}

	@Test
	public void testGarbageCollection() throws Exception {
		if(!System.getProperty("java.version").contains("1.8")) {
			return;
		}		
		@SuppressWarnings("unchecked")
		final WeakReference<Tester>[] testerWeakRef = new WeakReference[1];
		final TestEditor[] testEditor = new TestEditor[1];
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Tester tester = new Tester();
				tester.setTestActions(new TestAction[] { new CallMainMethodAction() });
				testEditor[0] = new TestEditor(tester);
				testEditor[0].setVisible(true);
				testerWeakRef[0] = new WeakReference<Tester>(tester);
			}
		});
		Thread.sleep(1000);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				Form testerForm = SwingRendererUtils.findFirstObjectDisplayedForm(testerWeakRef[0].get(),
						testEditor[0].getSwingRenderer());
				ListControl testActionsControl = (ListControl) testerForm.getFieldControlPlaceHolder("testActions")
						.getFieldControl();
				testActionsControl.setSingleSelection(testActionsControl.getRootListItemPosition(0));
			}
		});
		Thread.sleep(1000);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				testEditor[0].dispose();
			}
		});
		testEditor[0] = null;
		tryToForceGarbageCollection();
		assertGarbageCollected(testerWeakRef[0], 10, 1000);
	}

	private void assertGarbageCollected(WeakReference<?> weakReference, int gcCount, int delayMilliseconds)
			throws InterruptedException {
		for (int i = 0; i < gcCount; i++) {
			if (weakReference.get() == null) {
				return;
			}
			Thread.sleep(delayMilliseconds);
			tryToForceGarbageCollection();
		}
		Assert.fail();
	}

	private void tryToForceGarbageCollection() {
		System.gc();
	}

	public static class TestObject {

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
