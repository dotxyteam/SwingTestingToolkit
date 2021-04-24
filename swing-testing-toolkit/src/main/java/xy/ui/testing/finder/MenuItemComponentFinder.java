package xy.ui.testing.finder;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Component finder that can handle a menu or sub-menu item.
 * 
 * @author olitank
 *
 */
public class MenuItemComponentFinder extends ComponentFinder {

	private static final long serialVersionUID = 1L;

	protected List<PropertyBasedComponentFinder> menuItemPath = new ArrayList<PropertyBasedComponentFinder>();

	public List<PropertyBasedComponentFinder> getMenuItemPath() {
		return menuItemPath;
	}

	public void setMenuItemPath(List<PropertyBasedComponentFinder> menuItemPath) {
		this.menuItemPath = menuItemPath;
	}

	@Override
	public Component find(Tester tester) {
		if (menuItemPath.size() == 0) {
			throw new TestFailure("Cannot find menu item: path not set");
		}
		for (int i = 0; i < menuItemPath.size(); i++) {
			PropertyBasedComponentFinder menuItemFinder = menuItemPath.get(i);
			JMenuItem menuItem = (JMenuItem) menuItemFinder.find(tester);
			if (menuItem == null) {
				throw new TestFailure("Unable to find " + menuItemFinder.toString());
			}
			boolean lastMenuItem = i == (menuItemPath.size() - 1);
			if (lastMenuItem) {
				return menuItem;
			} else {
				openMenu((JMenu) menuItem);
			}
		}
		throw new AssertionError();
	}

	protected void openMenu(JMenu menu) {
		MouseEvent mouseEvent = new MouseEvent(menu, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0,
				menu.getWidth() / 2, menu.getHeight() / 2, 1, false, 0);
		for (MouseListener l : menu.getMouseListeners()) {
			if (mouseEvent.isConsumed()) {
				break;
			}
			l.mouseEntered(mouseEvent);
		}
	}

	@Override
	public boolean initializeFrom(Component c, TestEditor testEditor) {
		if (!(c instanceof JMenuItem)) {
			return false;
		}
		JMenuItem menuItem = (JMenuItem) c;
		menuItemPath.clear();
		menuItemPath.add(createMenuItemFinder(menuItem, testEditor));
		List<JMenuItem> ancestors = TestingUtils.getAncestorMenuItems(menuItem);
		for (JMenuItem ancestor : ancestors) {
			if (!(ancestor.getParent() instanceof JPopupMenu)) {
				break;
			}
			menuItemPath.add(0, createMenuItemFinder(ancestor, testEditor));
		}
		return true;
	}

	protected PropertyBasedComponentFinder createMenuItemFinder(JMenuItem menuItem, TestEditor testEditor) {
		PropertyBasedComponentFinder result = new PropertyBasedComponentFinder();
		result.setPropertyNames("Text");
		result.initializeFrom(menuItem, testEditor);
		return result;
	}

	public int getWindowIndex() {
		if (menuItemPath.size() > 0) {
			return menuItemPath.get(0).getWindowIndex();
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("menu ");
		if (menuItemPath.size() == 0) {
			result.append(" <unspecified path> ");
		} else {
			for (int i = 0; i < menuItemPath.size(); i++) {
				PropertyBasedComponentFinder pathElt = menuItemPath.get(i);
				if (i > 0) {
					result.append(" / ");
				}
				String pathEltText = pathElt.getPropertyValue("Text");
				if (pathEltText == null) {
					pathEltText = "<unknown item>";
				} else {
					pathEltText = "\"" + StringEscapeUtils.escapeJava(pathEltText) + "\"";
				}
				result.append(pathEltText);
			}
		}
		return result.toString();
	}

	@Override
	public void validate() throws ValidationError {
		if (menuItemPath.size() == 0) {
			throw new ValidationError("Missing menu item path");
		}
	}

}
