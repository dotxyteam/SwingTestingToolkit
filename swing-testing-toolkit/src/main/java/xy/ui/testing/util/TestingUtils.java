package xy.ui.testing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrBuilder;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.ui.testing.TestReport;
import xy.ui.testing.Tester;
import xy.ui.testing.TestReport.TestReportStepStatus;
import xy.ui.testing.action.SystemExitCallInterceptionAction;
import xy.ui.testing.editor.TestEditor;

public class TestingUtils {

	private static Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();

	public static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	public static final Image TESTER_IMAGE = loadImageResource("Tester.png");
	public static final ImageIcon TESTER_ICON = new ImageIcon(
			TESTER_IMAGE.getScaledInstance(16, 16, Image.SCALE_SMOOTH));

	private static final boolean TEST_EDITOR_HIDDEN_IN_ASSERTIONS = System
			.getProperty("xy.ui.testing.assertion.editorHidden", "false").equals("true");
	private static final int FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS = Integer
			.valueOf(System.getProperty("xy.ui.testing.assertion.fixtureRequestTimeout", "60"));

	public static Color shiftColor(Color color, int redOffset, int greenOffset, int blueOffset) {
		int red = (color.getRed() + redOffset) % 256;
		int green = (color.getGreen() + greenOffset) % 256;
		int blue = (color.getBlue() + blueOffset) % 256;
		while (red < 0)
			red += 256;
		while (green < 0)
			green += 256;
		while (blue < 0)
			blue += 256;
		return new Color(red, green, blue, color.getAlpha());
	}

	public static Window getWindowAncestorOrSelf(Component c) {
		if (c instanceof Window) {
			return (Window) c;
		}
		return SwingUtilities.getWindowAncestor(c);
	}

	public static boolean isTestEditorComponent(TestEditor testEditor, Component c) {
		if (testEditor != null) {
			Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
			if (testEditor.getAllWindows().contains(componentWindow)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDirectOrIndirectOwner(Window ownerWindow, Window window) {
		while ((window = window.getOwner()) != null) {
			if (ownerWindow == window) {
				return true;
			}
		}
		return false;
	}

	public static void insertMouseListener(Component c, int position, MouseListener listenerToInsert) {
		MouseListener[] currentListeners = c.getMouseListeners();
		while (c.getMouseListeners().length > 0) {
			c.removeMouseListener(c.getMouseListeners()[0]);
		}
		c.addMouseListener(listenerToInsert);
		for (int i = 0; i < currentListeners.length; i++) {
			MouseListener l = currentListeners[i];
			if (i == position) {
				c.addMouseListener(listenerToInsert);
			}
			c.addMouseListener(l);
		}
		if (position == -1) {
			c.addMouseListener(listenerToInsert);
		}
	}

	public static void launchClassMainMethod(String mainClassName, String[] arguments) throws Throwable {
		try {
			Class.forName(mainClassName).getMethod("main", new Class[] { String[].class }).invoke(null,
					new Object[] { arguments });
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	public static <T> List<T> getReversed(List<T> list) {
		List<T> result = new ArrayList<T>(list);
		Collections.reverse(result);
		return result;
	}

	public static String formatVisibleStrings(List<String> visibleStrings) {
		StrBuilder result = new StrBuilder();
		for (int i = 0; i < visibleStrings.size(); i++) {
			if (i > 0) {
				result.append(", ");
			}
			String s = visibleStrings.get(i);
			s = "\"" + StringEscapeUtils.escapeJava(s) + "\"";
			result.append(s);
		}
		return result.toString();
	}

	public static List<String> parseVisibleStrings(String formattedVisibleStrings) {
		List<String> result = new ArrayList<String>();
		String QUOTED_STRING = "\".*?(?<!\\\\)\"";
		String MULTIPLE_QUOTED_STRINGS = "((" + QUOTED_STRING + "\\s*,\\s*)*" + QUOTED_STRING + ")?";
		if (!formattedVisibleStrings.matches(MULTIPLE_QUOTED_STRINGS)) {
			throw new AssertionError("Invalid string list:\n" + formattedVisibleStrings
					+ "\nExpected string list formatted as: \"string 1\", \"string 2\", ... \"string n\"");
		}
		Pattern p = Pattern.compile(QUOTED_STRING);
		Matcher m = p.matcher(formattedVisibleStrings);
		while (m.find()) {
			String s = m.group();
			s = s.substring(1, s.length() - 1);
			s = StringEscapeUtils.unescapeJava(s);
			result.add(s);
		}
		return result;
	}

	public static void closeAllTestableWindows(Tester tester) {
		for (Window w : getAllTestableWindows(tester)) {
			w.dispose();
		}
	}

	public static List<Window> getAllTestableWindows(Tester tester) {
		List<Window> result = new ArrayList<Window>();
		for (Window w : Window.getWindows()) {
			if (tester.isTestable(w)) {
				result.add(w);
			}
		}
		return result;
	}

	public static List<Component> getAncestors(Component c) {
		List<Component> result = new ArrayList<Component>();
		while ((c = c.getParent()) != null) {
			result.add(c);
		}
		return result;
	}

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

	public static List<JPopupMenu> getPopupMenuAncestors(JPopupMenu popupMenu) {
		List<JPopupMenu> result = new ArrayList<JPopupMenu>();
		while (true) {
			Component invoker = popupMenu.getInvoker();
			if (!(invoker instanceof JMenuItem)) {
				break;
			}
			JMenuItem menuItem = (JMenuItem) invoker;
			if (!(menuItem.getParent() instanceof JPopupMenu)) {
				break;
			}
			popupMenu = (JPopupMenu) menuItem.getParent();
			result.add(popupMenu);
		}
		return result;
	}

	public static boolean belongsToPopupMenu(Component c, JPopupMenu popupMenu) {
		if (c == popupMenu) {
			return true;
		}
		if (c instanceof JMenuItem) {
			if (c.getParent() == popupMenu) {
				return true;
			}
			JMenuItem menuItem = (JMenuItem) c;
			for (JMenuItem ancestorMenuItem : TestingUtils.getAncestorMenuItems(menuItem)) {
				if (ancestorMenuItem.getParent() == popupMenu) {
					return true;
				}
			}
		}
		if (c instanceof JPopupMenu) {
			if (getPopupMenuAncestors((JPopupMenu) c).contains(popupMenu)) {
				return true;
			}
		}
		return false;
	}

	public static File saveAllTestableWindowImages(Tester tester) {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (Window w : getAllTestableWindows(tester)) {
			BufferedImage windowImage = getScreenShot(w);
			images.add(windowImage);
		}
		if (images.size() == 0) {
			return null;
		}
		return saveTesterImage(tester, joinImages(images));
	}

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

	public static BufferedImage joinImages(List<BufferedImage> images) {
		return joinImages(images, true);
	}

	public static File saveTesterImage(Tester tester, BufferedImage image) {
		File dir = tester.requireReportDirectory();
		String fileExtension = "png";
		File outputfile;
		try {
			outputfile = File.createTempFile("image-", "." + fileExtension, dir);
		} catch (IOException e1) {
			throw new AssertionError(
					"Failed to save image file in the directory: '" + dir.getAbsolutePath() + "': " + e1);
		}
		try {
			ImageIO.write(image, fileExtension, outputfile);
		} catch (IOException e) {
			throw new AssertionError("Failed to save the image file: '" + outputfile.getAbsolutePath() + "': " + e);
		}
		return outputfile;
	}

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

	public static File saveTestableWindowImage(Tester tester, int windowIndex) {
		return saveTesterImage(tester, getScreenShot(getAllTestableWindows(tester).get(windowIndex)));
	}

	public static File saveTestableComponentImage(Tester tester, Component c) {
		return saveTesterImage(tester, getScreenShot(c));
	}

	public static void purgeAllReportsDirectory() {
		File dir = Tester.getAllReportsDirectory();
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new AssertionError("Failed to delete the directory: '" + dir.getAbsolutePath() + "': " + e);
		}
	}

	public static TestEditor[] getTestEditors(Tester tester) {
		List<TestEditor> result = getKeysFromValue(TestEditor.TESTER_BY_EDITOR, tester);
		return result.toArray(new TestEditor[result.size()]);
	}

	public static <K, V> List<K> getKeysFromValue(Map<K, V> map, Object value) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (equalsOrBothNull(entry.getValue(), value)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public static boolean equalsOrBothNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	public static Color stringToColor(String s) {
		try {
			String[] rgb = s.split(",");
			return new Color(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
		} catch (Exception e) {
			return Color.decode("0x" + s);
		}
	}

	public static Image loadImage(File file) {
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Image loadImageResource(String imageResourceName) {
		Image result = IMAGE_CACHE.get(imageResourceName);
		if (result == null) {
			if (Tester.class.getResource(imageResourceName) == null) {
				result = NULL_IMAGE;
			} else {
				try {
					result = ImageIO.read(Tester.class.getResourceAsStream(imageResourceName));
				} catch (IOException e) {
					throw new AssertionError(e);
				}
			}
			IMAGE_CACHE.put(imageResourceName, result);
		}
		if (result == NULL_IMAGE) {
			return null;
		}
		return result;
	}

	public static void sendWindowClosingEvent(Window w) {
		WindowEvent closeEvent = new WindowEvent(w, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closeEvent);
	}

	public static void assertSuccessfulReplay(Tester tester, File specificationFile) throws Exception {
		assertSuccessfulReplay(tester, specificationFile.exists() ? new FileInputStream(specificationFile) : null);
	}

	public static void assertSuccessfulReplay(TestEditor testEditor, File specificationFile) throws Exception {
		assertSuccessfulReplay(testEditor, specificationFile.exists() ? new FileInputStream(specificationFile) : null);
	}

	public static void assertSuccessfulReplay(Tester tester, InputStream specificationStream) throws Exception {
		assertSuccessfulReplay(new TestEditor(tester), specificationStream);
	}

	public static void assertSuccessfulReplay(TestEditor testEditor, InputStream specificationStream) throws Exception {
		final Tester tester = testEditor.getTester();
		try {
			if (TEST_EDITOR_HIDDEN_IN_ASSERTIONS) {
				assertSuccessfulReplayWithoutTestEditor(tester, specificationStream);
			} else {
				assertSuccessfulReplayWithTestEditor(testEditor, specificationStream);
			}
		} finally {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						closeAllTestableWindows(tester);
					}
				});
			} catch (Throwable ignore) {
			}
			if (SystemExitCallInterceptionAction.isInterceptionEnabled()) {
				SystemExitCallInterceptionAction.disableInterception();
			}
		}
	}

	public static void assertSuccessfulReplayWithoutTestEditor(Tester tester, InputStream specificationStream)
			throws Exception {
		if (specificationStream == null) {
			throw new TestFailure("Test specification not found.");
		}
		tester.loadFromStream(specificationStream);
		TestReport report = tester.replayAll();
		if (report.getFinalStatus() != TestReportStepStatus.SUCCESSFUL) {
			throw generateTestFailure(tester, report);
		}
	}

	public static void assertSuccessfulReplayWithTestEditor(final TestEditor testEditor,
			InputStream specificationStream) throws Exception {
		final Tester tester = testEditor.getTester();
		if (specificationStream == null) {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					testEditor.open();
					testEditor.toFront();
				}
			});
			if (askWithTimeout(testEditor.getSwingRenderer(), testEditor,
					"Test specification not found." + "\nThis test editor window will be automatically closed in "
							+ FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS + " seconds.",
					testEditor.getSwingRenderer().getObjectTitle(tester), "OK", "Cancel (for fixture)",
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
			tester.loadFromStream(specificationStream);
			final boolean[] started = new boolean[] { false };
			testEditor.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent ev) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new AssertionError(e);
					}
					testEditor.getReplayWindowSwitch().setActionsToReplay(Arrays.asList(tester.getTestActions()));
					testEditor.getReplayWindowSwitch().activate(true);
					started[0] = true;
				}
			});
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					testEditor.open();
					testEditor.refresh();
				}
			});
			while (!started[0] || testEditor.getReplayWindowSwitch().isActive()) {
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
				if (askWithTimeout(testEditor.getSwingRenderer(), testEditor,
						"This test editor window will be automatically closed in "
								+ FAILED_TEST_FIXTURE_REQUEST_TIMEOUT_SECONDS + " seconds.",
						testEditor.getSwingRenderer().getObjectTitle(tester), "OK", "Cancel (for fixture)",
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

	public static void waitUntilClosed(TestEditor testEditor) {
		while (testEditor.isDisplayable()) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static boolean askWithTimeout(final SwingRenderer swingRenderer, final Component activatorComponent,
			final String question, final String title, final String yesCaption, final String noCaption,
			final int timeoutSeconds, boolean defaultAnswer) {
		final Boolean[] ok = new Boolean[] { null };
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ok[0] = swingRenderer.openQuestionDialog(activatorComponent, question, title, yesCaption, noCaption);
			}
		});
		for (int i = 0; i < timeoutSeconds; i++) {
			if (ok[0] != null) {
				return ok[0];
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return defaultAnswer;
	}

	public static Exception generateTestFailure(Tester tester, TestReport report) {
		return new TestFailure("The replay was not successful." + "\nMore informatyion can be found in this report:"
				+ "\n" + tester.getMainReportFile() + "\nLast logs:\n" + report.getLastLogs());
	}

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

	public static List<String> extractComponentTreeDisplayedStrings(Component c, final Tester tester) {
		final List<String> result = new ArrayList<String>();
		TestingUtils.visitComponentTree(tester, c, new IComponentTreeVisitor() {

			@Override
			public boolean visit(Component c) {
				if (tester.isVisible(c)) {
					result.addAll(tester.extractDisplayedStrings(c));
				}
				return true;
			}
		}, true);
		return result;
	}

	public static String getOSAgnosticFilePath(String path) {
		return path.replace(File.separator, "/");
	}

}
