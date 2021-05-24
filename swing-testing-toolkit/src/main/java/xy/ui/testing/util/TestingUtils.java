package xy.ui.testing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import xy.ui.testing.TestReport;
import xy.ui.testing.TestReport.TestReportStepStatus;
import xy.ui.testing.Tester;
import xy.ui.testing.action.SystemExitCallInterceptionAction;
import xy.ui.testing.editor.TestEditor;

/**
 * Testing utilities.
 * 
 * @author olitank
 *
 */
public class TestingUtils {

	/**
	 * An image representing a {@link Tester}.
	 */
	public static final Image TESTER_IMAGE = MiscUtils.loadImageResource("Tester.png");

	/**
	 * An icon representing a {@link Tester}.
	 */
	public static final ImageIcon TESTER_ICON = new ImageIcon(
			TESTER_IMAGE.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
	/**
	 * Whether the test editor and the status window should open by default when
	 * executing the assert*(...) methods of this class. Depends on this system
	 * property: -Dxy.ui.testing.assertion.editorHidden=true (false by default).
	 */
	public static final boolean TEST_EDITOR_HIDDEN_DURING_ASSERTIONS = System
			.getProperty("xy.ui.testing.assertion.editorHidden", "false").equals("true");

	private static final int FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS = Integer
			.valueOf(System.getProperty("xy.ui.testing.assertion.fixtureRequestTimeout", "60"));

	/**
	 * @param c The component to search from.
	 * @return The window containing the given component or the component itself if
	 *         it is a window.
	 */
	public static Window getWindowAncestorOrSelf(Component c) {
		if (c instanceof Window) {
			return (Window) c;
		}
		return SwingUtilities.getWindowAncestor(c);
	}

	/**
	 * @param testEditor A reference to the test editor.
	 * @param c          The analyzed component.
	 * @return Whether the given component is part of or is a owned by the specified
	 *         test editor. It would typically mean that the component is not
	 *         testable.
	 */
	public static boolean isTestEditorComponent(TestEditor testEditor, Component c) {
		if (testEditor != null) {
			Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
			if (testEditor.getAllWindows().contains(componentWindow)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Closes all the windows except the test editor windows related to the given
	 * tester instance.
	 * 
	 * @param tester The tester instance to consider.
	 */
	public static void closeAllTestableWindows(Tester tester) {
		List<Window> openWindows = getAllTestableWindows(tester);
		MiscUtils.sortWindowsByOwnershipDepth(openWindows);
		Collections.reverse(openWindows);
		for (Window w : openWindows) {
			if (!w.isVisible()) {
				continue;
			}
			tester.logInfo("Closing " + w);
			w.dispose();
		}
	}

	/**
	 * @param tester The tester instance to consider.
	 * @return All the windows except the test editor windows related to the given
	 *         tester.
	 */
	public static List<Window> getAllTestableWindows(Tester tester) {
		List<Window> result = new ArrayList<Window>();
		for (Window w : Window.getWindows()) {
			if (tester.isTestable(w)) {
				result.add(w);
			}
		}
		return result;
	}

	/**
	 * @param menuItem The analyzed menu item.
	 * @return The list of ancestor menu items of the given menu item.
	 */
	public static List<JMenuItem> getAncestorMenuItems(JMenuItem menuItem) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();
		while (true) {
			Container menuItemParent = menuItem.getParent();
			boolean isSubMenuOrContextMenuIItem = menuItemParent instanceof JPopupMenu;
			if (isSubMenuOrContextMenuIItem) {
				JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
				Component invoker = popupMenu.getInvoker();
				if (!(invoker instanceof JMenuItem)) {
					break;
				}
				menuItem = (JMenuItem) invoker;
				result.add(menuItem);
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * Saves to the specified directory, a single image file containing screenshots
	 * of all testable windows.
	 * 
	 * @param tester    The tester instance to consider.
	 * @param directory The directory that will contain the image file.
	 * @return The image file.
	 */
	public static File saveAllTestableWindowsScreenshot(Tester tester, File directory) {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (Window w : getAllTestableWindows(tester)) {
			if (!w.isVisible()) {
				continue;
			}
			BufferedImage windowImage = getScreenShot(w);
			images.add(windowImage);
		}
		if (images.size() == 0) {
			return null;
		}
		return MiscUtils.saveTimestampedImageFile(directory, joinImages(images));
	}

	/**
	 * @param images                     The list of images to join.
	 * @param horizontallyElseVertically Whether the images should be aligned
	 *                                   horizontally or vertically.
	 * @return A new image containing all the given images.
	 */
	public static BufferedImage joinImages(List<BufferedImage> images, boolean horizontallyElseVertically) {
		int width = 0;
		int height = 0;
		for (BufferedImage image : images) {
			if (horizontallyElseVertically) {
				width += image.getWidth();
				height = Math.max(height, image.getHeight());
			} else {
				width = Math.max(width, image.getWidth());
				height += image.getHeight();
			}
		}
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		int x = 0;
		int y = 0;
		for (BufferedImage image : images) {
			if (horizontallyElseVertically) {
				g.drawImage(image, null, x, 0);
				x += image.getWidth();
			} else {
				g.drawImage(image, null, 0, y);
				y += image.getHeight();
			}
		}
		g.dispose();
		return result;
	}

	/**
	 * @param images The list of images to join.
	 * @return A new image containing all the given images.
	 */
	public static BufferedImage joinImages(List<BufferedImage> images) {
		return joinImages(images, true);
	}

	/**
	 * @param component The component to capture.
	 * @return A screenshot of the given component.
	 */
	public static BufferedImage getScreenShot(Component component) {
		if ((component.getWidth() == 0) || (component.getHeight() == 0)) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		} else {
			BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			component.paint(image.getGraphics());
			return image;
		}

	}

	/**
	 * Deletes the directory where all reports are stored.
	 */
	public static void purgeAllReportsDirectory() {
		File dir = TestReport.getAllTestReportsDirectory();
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new AssertionError("Failed to delete the directory: '" + dir.getAbsolutePath() + "': " + e);
		}
	}

	/**
	 * @param tester The tester instance to consider.
	 * @return A list of all test editors bound to the given tester.
	 */
	public static TestEditor[] getTestEditors(Tester tester) {
		List<TestEditor> result = MiscUtils.getKeysFromValue(TestEditor.TESTER_BY_EDITOR, tester);
		return result.toArray(new TestEditor[result.size()]);
	}

	/**
	 * Sends a closing event to the given window.
	 * 
	 * @param w The window to close.
	 */
	public static void sendWindowClosingEvent(Window w) {
		WindowEvent closeEvent = new WindowEvent(w, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeEvent);
	}

	/**
	 * Asserts that the given test specification file gets load and executed
	 * successfully. Note that the test editor and the status window may open during
	 * the test execution according to the value of
	 * {@link #TEST_EDITOR_HIDDEN_DURING_ASSERTIONS}.
	 * 
	 * @param specificationFile The test specification file.
	 * @throws Exception If the specification file cannot be loaded or the test
	 *                   execution fails.
	 */
	public static void assertSuccessfulReplay(File specificationFile) throws Exception {
		Tester tester = new Tester();
		assertSuccessfulReplay(tester, specificationFile);
	}

	/**
	 * Asserts that the given test specification file gets load and executed
	 * successfully with the specified tester instance. Note that the test editor
	 * and the status window may open during the test execution according to the
	 * value of {@link #TEST_EDITOR_HIDDEN_DURING_ASSERTIONS}.
	 * 
	 * @param tester            The tester instance that will be used.
	 * @param specificationFile The test specification file.
	 * @throws Exception If the specification file cannot be loaded or the test
	 *                   execution fails.
	 */
	public static void assertSuccessfulReplay(final Tester tester, File specificationFile) throws Exception {
		try {
			if (TEST_EDITOR_HIDDEN_DURING_ASSERTIONS) {
				assertSuccessfulReplayWithoutTestEditor(tester, specificationFile);
			} else {
				final TestEditor[] testEditor = new TestEditor[1];
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						testEditor[0] = new TestEditor(tester);
					}
				});
				assertSuccessfulReplayWithTestEditor(testEditor[0], specificationFile);
			}
		} finally {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						tester.logInfo("Closing all testable windows");
						closeAllTestableWindows(tester);
					}
				});
			} catch (Throwable ignore) {
			}
			if (SystemExitCallInterceptionAction.isInterceptionEnabled()) {
				tester.logInfo("Disabling System.exit() call interception");
				SystemExitCallInterceptionAction.disableInterception();
			}
		}
	}

	/**
	 * Asserts that the given test specification file gets load and executed
	 * successfully with the specified tester instance. The test editor and the
	 * status window will not open during the test execution.
	 * 
	 * @param tester            The tester instance that will be used.
	 * @param specificationFile The test specification file.
	 * @throws Exception If the specification file cannot be loaded or the test
	 *                   execution fails.
	 */
	private static void assertSuccessfulReplayWithoutTestEditor(Tester tester, File specificationFile)
			throws Exception {
		tester.loadFromFile(specificationFile);
		TestReport report = tester.replayAll();
		if (report.getFinalStatus() != TestReportStepStatus.SUCCESSFUL) {
			throw generateTestFailure(tester, report);
		}
	}

	/**
	 * Asserts that the given test specification file gets load and executed
	 * successfully with the specified test editor. The given test editor and its
	 * status window will open during the test execution.
	 * 
	 * @param testEditor        The test editor instance that will be used.
	 * @param specificationFile The test specification file.
	 * @throws Exception If the specification file cannot be loaded or the test
	 *                   execution fails.
	 */
	private static void assertSuccessfulReplayWithTestEditor(final TestEditor testEditor, File specificationFile)
			throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				testEditor.setLastTesterFile(specificationFile);
			}
		});
		final Tester tester = testEditor.getTester();
		if (!specificationFile.exists()) {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					testEditor.open();
					testEditor.toFront();
				}
			});
			Toolkit.getDefaultToolkit().beep();
			if (MiscUtils.askWithTimeout(testEditor.getSwingRenderer(), testEditor,
					"Test specification not found." + "\nThis test editor window will be automatically closed in "
							+ FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS + " seconds.",
					testEditor.getSwingRenderer().getObjectTitle(tester), "Close Now", "Do Not Close",
					FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS, true)) {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						testEditor.dispose();
					}
				});
			} else {
				waitUntilClosed(testEditor);
			}
			throw new TestFailure("Test specification not found.");
		} else {
			tester.loadFromFile(specificationFile);
			final boolean[] activated = new boolean[] { false };
			testEditor.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent ev) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
					testEditor.getReplayWindowSwitch().setActionsToReplay(Arrays.asList(tester.getTestActions()));
					testEditor.getReplayWindowSwitch().activate();
					activated[0] = true;
					testEditor.removeWindowListener(this);
				}
			});
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					testEditor.open();
					testEditor.refresh();
				}
			});
			while (!activated[0] || testEditor.getReplayWindowSwitch().isActive()) {
				Thread.sleep(1000);
			}
			TestReport report = testEditor.getTestReport();
			if (report.getFinalStatus() == TestReportStepStatus.SUCCESSFUL) {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						testEditor.dispose();
					}
				});
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						testEditor.toFront();
					}
				});
				Toolkit.getDefaultToolkit().beep();
				if (MiscUtils.askWithTimeout(testEditor.getSwingRenderer(), testEditor,
						"This test editor window will be automatically closed in "
								+ FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS + " seconds.",
						testEditor.getSwingRenderer().getObjectTitle(tester), "Close Now", "Do Not Close",
						FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS, true)) {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							testEditor.dispose();
						}
					});
				} else {
					waitUntilClosed(testEditor);
				}
				throw generateTestFailure(tester, report);
			}
		}
	}

	/**
	 * Blocks the caller until the given test editor gets closed.
	 * 
	 * @param testEditor The test editor to consider.
	 */
	public static void waitUntilClosed(TestEditor testEditor) {
		while (testEditor.isDisplayable()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 
	 * @param tester The tester instance to consider.
	 * @param report The associated test report.
	 * @return An exception describing the failure of the given tester detailed by
	 *         the given test report.
	 */
	public static Exception generateTestFailure(Tester tester, TestReport report) {
		return new TestFailure("The replay was not successful." + "\nMore information can be found in this report:"
				+ "\n" + report.getMainFile().getAbsolutePath() + "\nLast logs:\n" + report.getLastLogs());
	}

	/**
	 * Allows to submit all the elements of the specified component tree to the
	 * given visitor.
	 * 
	 * @param tester                   The tester instance used to navigate in the
	 *                                 component tree.
	 * @param treeRoot                 The component tree root.
	 * @param visitor                  The visitor instance.
	 * @param skipNonVisibleComponents Whether non-visible components (according to
	 *                                 {@link Tester#isVisible(Component)}) should
	 *                                 be skipped or not.
	 * @return Whether the navigation was interrupted or not.
	 */
	public static boolean visitComponentTree(Tester tester, Component treeRoot, IComponentTreeVisitor visitor,
			boolean skipNonVisibleComponents) {
		if (skipNonVisibleComponents) {
			if (!tester.isVisible(treeRoot)) {
				return true;
			}
		}
		if (!visitor.visit(treeRoot)) {
			return false;
		}
		if (treeRoot instanceof Container) {
			List<Component> components = tester.getChildrenComponents((Container) treeRoot);
			for (Component childComponent : components) {
				if (!visitComponentTree(tester, childComponent, visitor, skipNonVisibleComponents)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Retrieves all strings displayed on the given component or any of its
	 * descendant.
	 * 
	 * @param c      The inspected component.
	 * @param tester The tester instance to use.
	 * @return
	 */
	public static List<String> extractComponentTreeDisplayedStrings(Component c, final Tester tester) {
		final List<String> result = new ArrayList<String>();
		TestingUtils.visitComponentTree(tester, c, new IComponentTreeVisitor() {
			@Override
			public boolean visit(Component c) {
				result.addAll(tester.extractDisplayedStrings(c));
				return true;
			}
		}, true);
		return result;
	}

}
