package xy.ui.testing;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;

public class TestUtils {

	TestEditor testEditor;

	@Test
	public void testClosingTestableWindows() throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testEditor = new TestEditor(new Tester());
				testEditor.open();
				testEditor.getComponentInspectionWindowSwitch().activate(true);
			}
		});
		final List<Window> disposeOrder = new ArrayList<Window>();

		final JFrame rootFrame = new JFrame() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				super.dispose();
				disposeOrder.add(this);
			}

			@Override
			public String toString() {
				return "rootFrame";
			}

		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				rootFrame.setVisible(true);
			}
		});

		final JDialog dialog = new JDialog(rootFrame) {
			private static final long serialVersionUID = 1L;
			{
				setModal(true);
			}

			@Override
			public void dispose() {
				super.dispose();
				disposeOrder.add(this);
			}

			@Override
			public String toString() {
				return "dialog";
			}

		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				dialog.setVisible(true);
			}
		});

		final JDialog subDialog = new JDialog(dialog) {
			private static final long serialVersionUID = 1L;
			{
				setModal(true);
			}

			@Override
			public void dispose() {
				super.dispose();
				disposeOrder.add(this);
			}

			@Override
			public String toString() {
				return "subDialog";
			}

		};
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				subDialog.setVisible(true);
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testEditor.getComponentInspectionWindowSwitch().activate(false);
			}
		});
		Thread.sleep(5000);
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				TestingUtils.closeAllTestableWindows(testEditor.getTester());
				testEditor.dispose();
			}
		});
		TestingUtils.waitUntilClosed(testEditor);
		Assert.assertEquals(
				new ArrayList<Window>(
						Arrays.<Window>asList(subDialog, subDialog, dialog, subDialog, dialog, rootFrame)),
				disposeOrder);
	}
}
