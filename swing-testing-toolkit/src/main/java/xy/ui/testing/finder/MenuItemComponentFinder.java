package xy.ui.testing.finder;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Component finder that can find popup menu or sub-menu items.
 * 
 * Note that for historical reasons a list of
 * {@link PropertyBasedComponentFinder} is used to identify each menu items. The
 * problem is that it stores the menu item class name and the popup container
 * window index that are useless and sometimes wrong because swing may reuse the
 * current window as the popup container or create a new window according to
 * undetermined circumstances.
 * 
 * Note also that this component finder can find {@link JMenu} instances on a
 * menu bar when it should only be used to find popup menu items. This feature
 * should be removed since it complicates the code.
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
	public Component find(Tester tester) {
		if (menuItemPath.size() == 0) {
			throw new TestFailure("Cannot find menu item: path not set");
		}
		List<MenuElement> allMenuElements = new ArrayList<MenuElement>();
		JMenuItem lastMenuItem = null;
		PropertyBasedComponentFinder lastMenuItemFinder = null;
		for (int i = 0; i < menuItemPath.size(); i++) {
			lastMenuItemFinder = menuItemPath.get(i);
			if (i == 0) {
				try {
					lastMenuItem = (JMenuItem) lastMenuItemFinder.find(tester);
				} catch (TestFailure testFailure) {
					lastMenuItem = null;
				}
				if (lastMenuItem == null) {
					MenuElement[] menuPath = MenuSelectionManager.defaultManager().getSelectedPath();
					if ((menuPath.length == 1) && (menuPath[0] instanceof JPopupMenu)) {
						JPopupMenu rootPopupMenu = (JPopupMenu) menuPath[0];
						int windowIndex = 0;
						boolean windowIndexFound = false;
						Window componentWindow = SwingUtilities.getWindowAncestor(rootPopupMenu);
						for (Window window : Window.getWindows()) {
							if (!tester.isTestable(window) || !tester.isVisible(window)) {
								continue;
							}
							if (window == componentWindow) {
								windowIndexFound = true;
								break;
							}
							windowIndex++;
						}
						if (windowIndexFound) {
							PropertyBasedComponentFinder dynamicPopupWindowMenuItemFinder = new PropertyBasedComponentFinder();
							dynamicPopupWindowMenuItemFinder
									.setComponentClassName(lastMenuItemFinder.getComponentClassName());
							dynamicPopupWindowMenuItemFinder
									.setOccurrencesToSkip(lastMenuItemFinder.getOccurrencesToSkip());
							dynamicPopupWindowMenuItemFinder
									.setPropertyValueList(lastMenuItemFinder.getPropertyValueList());
							dynamicPopupWindowMenuItemFinder.setWindowIndex(windowIndex);
							lastMenuItem = (JMenuItem) dynamicPopupWindowMenuItemFinder.find(tester);
						}
					}

				}
				if (lastMenuItem == null) {
					throw new TestFailure("Unable to find " + lastMenuItemFinder.toString());
				}
				if (lastMenuItem.getParent() instanceof JPopupMenu) {
					allMenuElements.add((JPopupMenu) lastMenuItem.getParent());
				}
			} else {
				String expectedSubMenuItemText = lastMenuItemFinder.getPropertyValueExpected("Text");
				JMenu subMenu = (JMenu) lastMenuItem;
				allMenuElements.add(subMenu.getPopupMenu());
				lastMenuItem = null;
				for (int iMenuItem = 0; iMenuItem < subMenu.getItemCount(); iMenuItem++) {
					JMenuItem subMenuItem = subMenu.getItem(iMenuItem);
					if (MiscUtils.equalsOrBothNull(expectedSubMenuItemText, subMenuItem.getText())) {
						lastMenuItem = subMenuItem;
						break;
					}
				}
				if (lastMenuItem == null) {
					throw new TestFailure("Unable to find sub-menu item '" + expectedSubMenuItemText + "'");
				}
			}
			allMenuElements.add(lastMenuItem);
		}
		MenuSelectionManager.defaultManager()
				.setSelectedPath(allMenuElements.toArray(new MenuElement[allMenuElements.size()]));
		if (!lastMenuItem.isDisplayable()) {
			throw new TestFailure("Unable to show " + lastMenuItemFinder.toString());
		}
		return lastMenuItem;
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
				String pathEltText = pathElt.getPropertyValueExpected("Text");
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
