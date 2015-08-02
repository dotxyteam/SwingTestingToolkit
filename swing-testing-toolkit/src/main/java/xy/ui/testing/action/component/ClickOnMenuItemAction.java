package xy.ui.testing.action.component;

import java.awt.AWTEvent;
import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.MenuItemComponentFinder;
import xy.ui.testing.util.TestingError;

public class ClickOnMenuItemAction extends TestAction {
	private static final long serialVersionUID = 1L;
	
	protected MenuItemComponentFinder componentFinder = new MenuItemComponentFinder();

	public MenuItemComponentFinder getComponentFinder() {
		return componentFinder;
	}

	public void setComponentFinder(MenuItemComponentFinder componentFinder) {
		this.componentFinder = componentFinder;
	}

	@Override
	public boolean initializeFrom(Component c,
			AWTEvent introspectionRequestEvent) {
		if (!macthesComponent(c)) {
			return false;
		}
		if (!componentFinder.initializeFrom(c)) {
			return false;
		}
		return true;
	}

	@Override
	public Component findComponent() {
		Component c = componentFinder.find();
		if (c == null) {
			throw new TestingError("Unable to find "
					+ componentFinder.toString());
		}
		return c;
	}

	@Override
	public void execute(final Component c) {
		JMenuItem menuItem = (JMenuItem) c;
		new ClickAction().execute(menuItem);
	}

	@Override
	public String getComponentInformation() {
		if (componentFinder == null) {
			return null;
		}
		return componentFinder.toString();
	}

	@Override
	public String getValueDescription() {
		return "";
	}

	@Override
	public String toString() {
		return "Click on the " + componentFinder + "\"";
	}

	public static boolean matchIntrospectionRequestEvent(AWTEvent event) {
		if (!TargetComponentTestAction.matchIntrospectionRequestEvent(event)) {
			return false;
		}
		Component c = (Component) event.getSource();
		if (!macthesComponent(c)) {
			return false;
		}
		return true;
	}

	public static boolean macthesComponent(Component c) {
		if (!(c instanceof JMenuItem)) {
			return false;
		}
		JMenuItem menuItem = (JMenuItem) c;
		if (menuItem instanceof JMenu) {
			return false;
		}
		return true;
	}



}
