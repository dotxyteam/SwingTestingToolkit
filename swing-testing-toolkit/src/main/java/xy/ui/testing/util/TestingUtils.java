package xy.ui.testing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrBuilder;

import xy.ui.testing.TesterUI;

public class TestingUtils {

	public static boolean visitComponentTree(Component treeRoot,
			IComponentTreeVisitor visitor) {
		if (!visitor.visit(treeRoot)) {
			return false;
		}
		if (treeRoot instanceof Container) {
			for (Component childComponent : ((Container) treeRoot)
					.getComponents()) {
				if (!visitComponentTree(childComponent, visitor)) {
					return false;
				}
			}
		}
		return true;
	}

	public static int removeAWTEventListener(AWTEventListener listener) {
		final List<AWTEventListener> listenersToRemove = new ArrayList<AWTEventListener>();
		for (AWTEventListener l : Toolkit.getDefaultToolkit()
				.getAWTEventListeners()) {
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

	public static Color shiftColor(Color color, int redOffset, int greenOffset,
			int blueOffset) {
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

	public static boolean isTesterUIComponent(Component c) {
		for (JPanel testerForm : TesterUI.INSTANCE.getObjectByForm().keySet()) {
			Window componentWindow = TestingUtils.getWindowAncestorOrSelf(c);
			if (componentWindow != null) {
				Window testerWindow = SwingUtilities
						.getWindowAncestor(testerForm);
				if (testerWindow == componentWindow) {
					return true;
				}
				while ((componentWindow = componentWindow.getOwner()) != null) {
					if (testerWindow == componentWindow) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void insertMouseListener(Component c, int position,
			MouseListener listenerToInsert) {
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

	public static void launchClassMainMethod(String mainClassName)
			throws Exception {
		Class.forName(mainClassName)
				.getMethod("main", new Class[] { String[].class })
				.invoke(null, new Object[] { new String[0] });
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

	private static String extractVisibleStringFromBorder(Border border) {
		if (border instanceof TitledBorder) {
			String s = ((TitledBorder) border).getTitle();
			if ((s != null) && (s.trim().length() > 0)) {
				return s;
			}
		}
		return null;
	}

	private static Collection<String> extractVisibleStringsFromList(
			JList list) {
		List<String> result = new ArrayList<String>();
		ListModel model = list.getModel();
		ListCellRenderer cellRenderer = list.getCellRenderer();
		for (int i = 0; i < model.getSize(); i++) {
			Object item = model.getElementAt(i);
			Component cellComponent = cellRenderer
					.getListCellRendererComponent(list, item, i, false, false);
			result.addAll(extractVisibleStrings(cellComponent));
		}
		return result;
	}

	private static List<String> extractVisibleStringsFromTable(JTable table) {
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
				Object cellValue = model.getValueAt(iRow, iCol);
				TableCellRenderer cellRenderer = table.getCellRenderer(iRow,
						iCol);
				Component cellComponent = cellRenderer
						.getTableCellRendererComponent(table, cellValue, false,
								false, iRow, iCol);
				result.addAll(extractVisibleStrings(cellComponent));
			}
		}
		return result;
	}

	private static Collection<? extends String> extractVisibleStringsFromTree(
			JTree tree) {
		List<String> result = new ArrayList<String>();
		result.addAll(extractVisibleStringsFromTree(0, tree.getModel()
				.getRoot(), tree));
		return result;
	}

	private static List<String> extractVisibleStringsFromTree(int currentRow,
			Object currentNode, JTree tree) {
		List<String> result = new ArrayList<String>();
		TreeModel model = tree.getModel();
		String s = tree.convertValueToText(currentNode, false, true,
				model.isLeaf(currentNode), currentRow, false);
		if ((s != null) && (s.trim().length() > 0)) {
			result.add(s);
		}
		for (int i = 0; i < model.getChildCount(currentNode); i++) {
			Object childNode = model.getChild(currentNode, i);
			result.addAll(extractVisibleStringsFromTree(currentRow + 1,
					childNode, tree));
		}
		return result;
	}

	private static String extractVisibleStringThroughMethod(Component c,
			String methodName) {
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

	@SuppressWarnings("unchecked")
	public static List<String> parseVisibleStrings(
			String formattedVisibleStrings) {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		try {
			return (List<String>) engine.eval("java.util.Arrays.asList("
					+ formattedVisibleStrings + ")");
		} catch (ScriptException e) {
			throw new TestingError("The string list is invalid", e);
		}
	}
}
