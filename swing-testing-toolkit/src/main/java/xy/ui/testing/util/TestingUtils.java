package xy.ui.testing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TesterEditor;

public class TestingUtils {

	private static Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();

	public static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	public static final Image TESTER_IMAGE = loadImageResource("Tester.png");
	public static final ImageIcon TESTER_ICON = new ImageIcon(
			TESTER_IMAGE.getScaledInstance(16, 16, Image.SCALE_SMOOTH));

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

	public static boolean isTesterEditorComponent(TesterEditor testerEditor, Component c) {
		if (testerEditor != null) {
			Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
			if (testerEditor.getAllwindows().contains(componentWindow)) {
				return true;
			}
			while (componentWindow.getOwner() != null) {
				if (isTesterEditorComponent(testerEditor, componentWindow.getOwner())) {
					return true;
				}
				componentWindow = componentWindow.getOwner();
			}
		}
		return false;
	}

	public static boolean isDIrectOrIndirectOwner(Window ownerWindow, Window window) {
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
			if (tester.isTestableWindow(w)) {
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

	public static BufferedImage joinImages(List<BufferedImage> images) {
		int width = 0;
		int height = 0;
		for (BufferedImage image : images) {
			width += image.getWidth();
			height = Math.max(height, image.getHeight());
		}
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		int x = 0;
		for (BufferedImage image : images) {
			g.drawImage(image, null, x, 0);
			x += image.getWidth();
		}
		g.dispose();
		return result;
	}

	public static File saveTesterImage(Tester tester, BufferedImage image) {
		File dir = tester.getSavedImagesDirectory();
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new AssertionError("Failed to create the directory: '" + dir.getAbsolutePath() + "'");
			}
		}
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

	public static void purgeSavedImagesDirectory(Tester tester) {
		File dir = tester.getSavedImagesDirectory();
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new AssertionError("Failed to delete the directory: '" + dir.getAbsolutePath() + "': " + e);
		}
	}

	public static TesterEditor[] getTesterEditors(Tester tester) {
		List<TesterEditor> result = getKeysFromValue(TesterEditor.TESTER_BY_EDITOR, tester);
		return result.toArray(new TesterEditor[result.size()]);
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

	public static void assertSuccessfulReplay(Tester tester, File replayFile) throws IOException {
		assertSuccessfulReplay(tester, new FileInputStream(replayFile));
	}

	public static void assertSuccessfulReplay(Tester tester, InputStream replayStream) throws IOException {
		tester.loadFromStream(replayStream);
		closeAllTestableWindows(tester);
		tester.replayAll();
	}

	public static boolean visitComponentTree(Tester tester, Component treeRoot, IComponentTreeVisitor visitor) {
		if (!visitor.visit(treeRoot)) {
			return false;
		}
		if (treeRoot instanceof Container) {
			List<Component> components = tester.getChildrenComponents((Container) treeRoot);
			for (Component childComponent : components) {
				if (!visitComponentTree(tester, childComponent, visitor)) {
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
		});
		return result;
	}
}
