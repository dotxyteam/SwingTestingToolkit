package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.SendClickAction;
import xy.ui.testing.util.TestingUtils;

public class Tester {

	protected List<TestAction> testActions = new ArrayList<TestAction>();
	protected AWTEventListener recordingListener;
	protected Component currentComponent;
	protected Color currentComponentBackground;
	protected Color currentComponentForeground;
	protected MouseListener[] currentComponentMouseListeners;
	protected JPopupMenu popupMenu = new JPopupMenu();
	protected boolean recording = false;

	public Tester() {
		recordingListener = new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				awtEventDispatched(event);
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(
				recordingListener,
				AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK
						+ AWTEvent.KEY_EVENT_MASK);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		TestingUtils.removeAWTEventListener(recordingListener);
	}

	public TestAction[] getTestActions() {
		return testActions.toArray(new TestAction[testActions.size()]);
	}

	public void setTestActions(TestAction[] testActions) {
		this.testActions.clear();
		this.testActions.addAll(Arrays.asList(testActions));
	}

	public void startRecording() {
		if (isRecording()) {
			return;
		}
		recording = true;
	}

	public void stopRecording() {
		if (!isRecording()) {
			return;
		}
		recording = false;
	}

	public boolean isRecording() {
		return recording;
	}

	protected void awtEventDispatched(AWTEvent event) {
		if (!recording) {
			return;
		}
		if (event == null) {
			handleCurrentComponentChange(null);
			return;
		}
		if (!(event.getSource() instanceof Component)) {
			return;
		}
		Component c = (Component) event.getSource();
		if (!c.isShowing()) {
			return;
		}
		if (TestingUtils.isTesterUIComponent(c)) {
			return;
		}
		if (SwingUtilities.getAncestorOfClass(JPopupMenu.class, c) == popupMenu) {
			return;
		}
		if (isCurrentComponentChangeEvent(event)) {
			handleCurrentComponentChange(event);
		}
		if (isComponentSelectionEvent(event)) {
			handleComponentSelection(event);
		}
	}

	protected void handleComponentSelection(final AWTEvent event) {
		popupMenu.removeAll();
		final Component c = (Component) event.getSource();
		createComponentSelectionMenuItems(c);
		createDoNotRecordMenuItem(c);
		createStopRecordingMenuItem(c);
		createReleaseComponentMenuItem(c);
		popupMenu.add(new JMenuItem("Cancel"));
		MouseEvent mouseEvt = (MouseEvent) event;
		popupMenu.show(c, mouseEvt.getX(), mouseEvt.getY());
	}

	protected void createReleaseComponentMenuItem(Component c) {
		JMenuItem menuItem = new JMenuItem("Release Component");
		{
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleCurrentComponentChange(null);
					stopRecording();
					new Thread(Tester.class.getSimpleName() + " Restarter"){
						@Override
						public void run() {
							try {
								sleep(3000);
							} catch (InterruptedException e) {
								throw new AssertionError(e);
							}
							startRecording();
						}						
					}.start();
				}
			});
			popupMenu.add(menuItem);
		}
	}

	protected void createStopRecordingMenuItem(Component c) {
		JMenuItem stopRecordingItem = new JMenuItem("Stop Recording");
		{
			stopRecordingItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TesterUI.INSTANCE.getFormsUpdatingmethod(Tester.this,
							"stopRecording").invoke(Tester.this,
							Collections.<String, Object> emptyMap());
				}
			});
			popupMenu.add(stopRecordingItem);
		}
	}

	protected void createDoNotRecordMenuItem(final Component c) {
		JMenuItem doNotRecordItem = new JMenuItem("Just "
				+ getComponentSelectionActionTitle());
		{
			doNotRecordItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					stopRecording();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							executeInitialActionReplacedByComponentSelection(c);
						}
					});
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							startRecording();
						}
					});
				}
			});
			popupMenu.add(doNotRecordItem);
		}
	}

	protected void createComponentSelectionMenuItems(final Component c) {
		for (final TestAction testAction : getPossibleTestActions(c)) {
			JMenuItem item = new JMenuItem("Record "
					+ TesterUI.INSTANCE.getObjectKind(testAction));
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onTestActionSelection(testAction, c);
				}
			});
			popupMenu.add(item);
		}
	}

	protected void onTestActionSelection(final TestAction testAction,
			final Component c) {
		if (TesterUI.INSTANCE.openSettings(testAction, c)) {
			IFieldInfo testActionListField = TesterUI.INSTANCE
					.getFormsUpdatingField(Tester.this, "testActions");
			final List<TestAction> newTestActionListValue = new ArrayList<TestAction>(
					testActions);
			newTestActionListValue.add(testAction);
			testActionListField.setValue(Tester.this, newTestActionListValue
					.toArray(new TestAction[newTestActionListValue.size()]));
			testAction.execute(c);
		}
	}

	protected List<TestAction> getPossibleTestActions(Component c) {
		try {
			List<TestAction> result = new ArrayList<TestAction>();
			for (Class<?> testActionClass : TesterUI.TEST_ACTION_CLASSESS) {
				TestAction testAction = (TestAction) testActionClass
						.newInstance();
				if (testAction.initializeFrom(c)) {
					result.add(testAction);
				}
			}
			return result;
		} catch (Exception e) {
			throw new AssertionError(e);
		}

	}

	protected void executeInitialActionReplacedByComponentSelection(Component c) {
		SendClickAction action = new SendClickAction();
		action.setButton(SendClickAction.ButtonId.LEFT_BUTTON);
		if (!action.initializeFrom(c)) {
			throw new AssertionError();
		}
		action.execute(c);
	}

	protected boolean isComponentSelectionEvent(AWTEvent event) {
		if (!(event instanceof MouseEvent)) {
			return false;
		}
		MouseEvent mouseEvent = (MouseEvent) event;
		if (mouseEvent.getID() != MouseEvent.MOUSE_CLICKED) {
			return false;
		}
		if (mouseEvent.getButton() != MouseEvent.BUTTON1) {
			return false;
		}
		return true;
	}

	protected String getComponentSelectionActionTitle() {
		return "Left Click";
	}

	protected boolean isCurrentComponentChangeEvent(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (mouseEvent.getID() == MouseEvent.MOUSE_MOVED) {
				return true;
			}
		}
		return false;
	}

	protected void handleCurrentComponentChange(final AWTEvent event) {
		if (currentComponent != null) {
			unhighlightCurrentComponent();
			restoreComponentListeners();
			currentComponent = null;
		}
		if (event == null) {
			return;
		}
		currentComponent = (Component) event.getSource();
		highlightCurrentComponent();
		disableComponentListeners();
	}

	protected void restoreComponentListeners() {
		for (MouseListener l : currentComponentMouseListeners) {
			currentComponent.addMouseListener(l);
		}
	}

	protected void disableComponentListeners() {
		currentComponentMouseListeners = currentComponent.getMouseListeners();
		while (currentComponent.getMouseListeners().length > 0) {
			currentComponent.removeMouseListener(currentComponent
					.getMouseListeners()[0]);
		}
	}

	protected void unhighlightCurrentComponent() {
		currentComponent.setBackground(currentComponentBackground);
		currentComponent.setForeground(currentComponentForeground);
	}

	protected void highlightCurrentComponent() {
		currentComponentBackground = currentComponent.getBackground();
		if (currentComponentBackground != null) {
			currentComponent.setBackground(new Color(255, 200, 0));
		}
		currentComponentForeground = currentComponent.getForeground();
		if (currentComponentForeground != null) {
			currentComponent.setForeground(new Color(136, 0, 21));
		}
	}
}
