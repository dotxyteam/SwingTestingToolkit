package xy.ui.testing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.io.File;

import javax.swing.SwingUtilities;

import org.junit.Test;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;

public class TestTesterEditor {

	@Test
	public void test() throws Exception {
		TestingUtils.assertSuccessfulReplay(new File(System.getProperty("swing-testing-toolkit.project.directory", "./")
				+ "test-specifications/testTesterEditor.stt"));
	}

	public boolean booleanData;
	public int intData;
	public String stringData;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if ((args.length == 1) && args[0].equals("TesterEditor frame")) {
					Tester tester = new Tester() {

						@Override
						public boolean isTestable(Component c) {
							if (!super.isTestable(c)) {
								return false;
							}
							for (Window w : Window.getWindows()) {
								if (w.isDisplayable()) {
									if (w instanceof TestEditor) {
										Tester otherTester = ((TestEditor) w).getTester();
										if (otherTester != this) {
											if (!otherTester.isTestable(c)) {
												return false;
											}
										}
									}
								}
							}
							return true;
						}

					};
					TestEditor testEditor = new TestEditor(tester);
					testEditor.setDecorationsBackgroundColor(new Color(68, 61, 205));
					testEditor.setDecorationsForegroundColor(new Color(216, 214, 245));
					testEditor.open();
				} else if ((args.length == 1) && args[0].equals("TestTesterEditor dialog")) {
					new SwingRenderer(ReflectionUIUtils.STANDARD_REFLECTION).openObjectDialog(null,
							new TestTesterEditor(), null, null, false, false);
				}
			}
		});
	}

}
