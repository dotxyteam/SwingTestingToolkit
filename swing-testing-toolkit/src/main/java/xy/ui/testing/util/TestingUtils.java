package xy.ui.testing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrBuilder;

import xy.ui.testing.Tester;
import xy.ui.testing.TesterUI;

@SuppressWarnings("unused")
public class TestingUtils {

	private static Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();

	public static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	public static final Image TESTER_IMAGE = loadImageResource("Tester.png");
	public static final ImageIcon TESTER_ICON = new ImageIcon(
			TESTER_IMAGE.getScaledInstance(16, 16, Image.SCALE_SMOOTH));

	public static boolean visitComponentTree(Component treeRoot, IComponentTreeVisitor visitor) {
		if (!visitor.visit(treeRoot)) {
			return false;
		}
		if (treeRoot instanceof Container) {
			for (Component childComponent : ((Container) treeRoot).getComponents()) {
				if (!visitComponentTree(childComponent, visitor)) {
					return false;
				}
			}
		}
		return true;
	}

	public static int removeAWTEventListener(AWTEventListener listener) {
		final List<AWTEventListener> listenersToRemove = new ArrayList<AWTEventListener>();
		for (AWTEventListener l : Toolkit.getDefaultToolkit().getAWTEventListeners()) {
			if (l == listener) {
				listenersToRemove.add(l);
			} else if (l instanceof AWTEventListenerProxy) {
				final AWTEventListenerProxy proxyListener = (AWTEventListenerProxy) l;
				if (proxyListener.getListener() == listener) {
					listenersToRemove.add(proxyListener);
				}
			}
		}
		for (AWTEventListener l : listenersToRemove) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(l);
		}
		return listenersToRemove.size();
	}

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

	public static boolean isTesterUIComponent(TesterUI testerUI, Component c) {
		if (testerUI != null) {
			Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
			if (testerUI == TesterUI.BY_WINDOW.get(componentWindow)) {
				return true;
			}
			while (componentWindow.getOwner() != null) {
				if (isTesterUIComponent(testerUI, componentWindow.getOwner())) {
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

	public static void launchClassMainMethod(String mainClassName, String[] arguments) throws Exception {
		Class.forName(mainClassName).getMethod("main", new Class[] { String[].class }).invoke(null,
				new Object[] { arguments });
	}

	public static <T> List<T> getReversed(List<T> list) {
		List<T> result = new ArrayList<T>(list);
		Collections.reverse(result);
		return result;
	}

	public static List<String> extractVisibleStrings(Component c) {
		List<String> result = new ArrayList<String>();
		String s;
		s = extractVisibleStringThroughMethod(c, "getTitle");
		if (s != null) {
			result.add(s);
		}
		s = extractVisibleStringThroughMethod(c, "getText");
		if (s != null) {
			result.add(s);
		}
		if (c instanceof JComponent) {
			Border border = ((JComponent) c).getBorder();
			if (border != null) {
				s = extractVisibleStringFromBorder(border);
				if ((s != null) && (s.trim().length() > 0)) {
					result.add(s);
				}
			}
		}
		if (c instanceof JTable) {
			JTable table = (JTable) c;
			result.addAll(extractVisibleStringsFromTable(table));
		}
		if (c instanceof JTree) {
			JTree tree = (JTree) c;
			result.addAll(extractVisibleStringsFromTree(tree));
		}
		if (c instanceof JList) {
			JList list = (JList) c;
			result.addAll(extractVisibleStringsFromList(list));
		}
		return result;
	}

	public static String extractVisibleStringFromBorder(Border border) {
		if (border instanceof TitledBorder) {
			String s = ((TitledBorder) border).getTitle();
			if ((s != null) && (s.trim().length() > 0)) {
				return s;
			}
		}
		return null;
	}

	public static Collection<String> extractVisibleStringsFromList(JList list) {
		List<String> result = new ArrayList<String>();
		ListModel model = list.getModel();
		ListCellRenderer cellRenderer = list.getCellRenderer();
		for (int i = 0; i < model.getSize(); i++) {
			try {
				Object item = model.getElementAt(i);
				Component cellComponent = cellRenderer.getListCellRendererComponent(list, item, i, false, false);
				result.addAll(extractVisibleStrings(cellComponent));
			} catch (Exception ignore) {
			}
		}
		return result;
	}

	public static List<String> extractVisibleStringsFromTable(JTable table) {
		List<String> result = new ArrayList<String>();
		TableModel model = table.getModel();
		String s;
		for (int i = 0; i < model.getColumnCount(); i++) {
			s = model.getColumnName(i);
			if ((s != null) && (s.trim().length() > 0)) {
				result.add(s);
			}
		}
		for (int iRow = 0; iRow < model.getRowCount(); iRow++) {
			for (int iCol = 0; iCol < model.getColumnCount(); iCol++) {
				try {
					Object cellValue = model.getValueAt(iRow, iCol);
					TableCellRenderer cellRenderer = table.getCellRenderer(iRow, iCol);
					Component cellComponent = cellRenderer.getTableCellRendererComponent(table, cellValue, false, false,
							iRow, iCol);
					List<String> cellVisibleStrings = extractVisibleStrings(cellComponent);
					result.addAll(cellVisibleStrings);
				} catch (Exception ignore) {
				}
			}
		}
		return result;
	}

	public static Collection<? extends String> extractVisibleStringsFromTree(JTree tree) {
		List<String> result = new ArrayList<String>();
		result.addAll(extractVisibleStringsFromTree(0, tree.getModel().getRoot(), tree));
		return result;
	}

	public static List<String> extractVisibleStringsFromTree(int currentRow, Object currentNode, JTree tree) {
		List<String> result = new ArrayList<String>();
		TreeModel model = tree.getModel();
		try {
			String s = tree.convertValueToText(currentNode, false, true, model.isLeaf(currentNode), currentRow, false);
			if ((s != null) && (s.trim().length() > 0)) {
				result.add(s);
			}
		} catch (Exception ignore) {
		}
		for (int i = 0; i < model.getChildCount(currentNode); i++) {
			Object childNode = model.getChild(currentNode, i);
			result.addAll(extractVisibleStringsFromTree(currentRow + 1, childNode, tree));
		}
		return result;
	}

	public static String extractVisibleStringThroughMethod(Component c, String methodName) {
		try {
			Method method = c.getClass().getMethod(methodName);
			String result = (String) method.invoke(c);
			if (result == null) {
				return null;
			}
			if (result.trim().length() == 0) {
				return null;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	public static List<String> collectVisibleStrings(Window window) {
		final List<String> result = new ArrayList<String>();
		visitComponentTree(window, new IComponentTreeVisitor() {

			@Override
			public boolean visit(Component c) {
				result.addAll(extractVisibleStrings(c));
				return true;
			}
		});
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

	public static boolean isTestableWindow(Window window, TesterUI... testerUIs) {
		for (TesterUI testerUI : testerUIs) {
			if (isTesterUIComponent(testerUI, window)) {
				return false;
			}
		}
		if (!window.isVisible()) {
			return false;
		}
		return true;
	}

	public static void closeAllTestableWindows(TesterUI... testerUIs) {
		for (Window w : getAllTestableWindows(testerUIs)) {
			w.dispose();
		}
	}

	public static List<Window> getAllTestableWindows(TesterUI... testerUIs) {
		List<Window> result = new ArrayList<Window>();
		for (Window w : Window.getWindows()) {
			if (isTestableWindow(w, testerUIs)) {
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

	public static File saveAllTestableWindowImages(TesterUI... testerUIs) {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (TesterUI testerUI : testerUIs) {
			for (Window w : getAllTestableWindows(testerUI)) {
				BufferedImage windowImage = getScreenShot(w);
				images.add(windowImage);
			}
		}
		if (images.size() == 0) {
			return null;
		}
		return saveImage(joinImages(images));
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

	public static File saveImage(BufferedImage image) {
		File dir = getSavedImagesDirectory();
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

	public static File getSavedImagesDirectory() {
		return new File(Tester.class.getSimpleName().toLowerCase() + "-saved-images");
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

	public static File saveTestableWindowImage(int windowIndex, TesterUI... testerUIs) {
		return saveImage(getScreenShot(getAllTestableWindows(testerUIs).get(windowIndex)));
	}

	public static File saveImage(Component c) {
		return saveImage(getScreenShot(c));
	}

	public static void purgeSavedImagesDirectory() {
		File dir = getSavedImagesDirectory();
		try {
			FileUtils.deleteDirectory(dir);
		} catch (IOException e) {
			throw new AssertionError("Failed to delete the directory: '" + dir.getAbsolutePath() + "': " + e);
		}
	}

	public static TesterUI[] getTesterUIs(Tester tester) {
		List<TesterUI> result = getKeysFromValue(TesterUI.TESTERS, tester);
		return result.toArray(new TesterUI[result.size()]);
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
			if (TesterUI.class.getResource(imageResourceName) == null) {
				result = NULL_IMAGE;
			} else {
				try {
					result = ImageIO.read(TesterUI.class.getResourceAsStream(imageResourceName));
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
}
