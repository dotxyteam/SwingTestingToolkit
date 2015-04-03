package xy.ui.testing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.AWTEventListenerProxy;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
		return new Color(red, green, blue);
	}

	public static Window getWindowAncestorOrSelf(Component c) {
		if (c instanceof Window) {
			return (Window) c;
		}
		return SwingUtilities.getWindowAncestor(c);
	}

	public static boolean isTesterUIComponent(Component c) {
		for (JPanel testerForm : TesterUI.INSTANCE.getObjectByForm().keySet()) {
			Window componentWindow = SwingUtilities.getWindowAncestor(c);
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

	public static void  insertMouseListener(Component c,int position,
			MouseListener listenerToInsert) {
		MouseListener[] currentListeners = c.getMouseListeners();
		while (c.getMouseListeners().length > 0) {
			c.removeMouseListener(c.getMouseListeners()[0]);
		}
		c.addMouseListener(listenerToInsert);
		for (int i=0; i<currentListeners.length; i++) {
			MouseListener l = currentListeners[i];
			if(i == position){
				c.addMouseListener(listenerToInsert);
			}
			c.addMouseListener(l);
		}
		if(position == -1){
			c.addMouseListener(listenerToInsert);
		}			
	}
}
