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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.thoughtworks.xstream.XStream;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class Tester {

	protected List<TestAction> testActions = new ArrayList<TestAction>();
	protected int millisecondsBetwneenActions = 3000;

	transient protected AWTEventListener recordingListener;
	transient protected Component currentComponent;
	transient protected Color currentComponentBackground;
	transient protected Color currentComponentForeground;
	transient protected MouseListener[] currentComponentMouseListeners;
	transient protected JPopupMenu popupMenu = new JPopupMenu();
	transient protected boolean recording = false;

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

	public int getMillisecondsBetwneenActions() {
		return millisecondsBetwneenActions;
	}

	public void setMillisecondsBetwneenActions(int millisecondsBetwneenActions) {
		this.millisecondsBetwneenActions = millisecondsBetwneenActions;
	}

	public TestAction[] getTestActions() {
		return testActions.toArray(new TestAction[testActions.size()]);
	}

	public void setTestActions(TestAction[] testActions) {
		this.testActions.clear();
		this.testActions.addAll(Arrays.asList(testActions));
	}

	public void launchClass(final String className) {
		new Thread(className) {
			@Override
			public void run() {
				try {
					TestingUtils.launchClassMainMethod(className);
				} catch (Exception e) {
					TesterUI.INSTANCE.handleExceptionsFromDisplayedUI(null, e);
				}
			}
		}.start();
	}

	public void replayAll() {
		replay(testActions, null);
	}

	public void replay(final List<TestAction> toReplay,
			Runnable runBeforeEachAction) {
		if (isRecording()) {
			stopRecording();
		}
		try {
			for (int i = 0; i < toReplay.size(); i++) {
				if (runBeforeEachAction != null) {
					runBeforeEachAction.run();
				}
				TestAction testAction = toReplay.get(i);
				try {
					if(i>0){
						Thread.sleep(millisecondsBetwneenActions);
					}
					ComponentFinder componentFinder = testAction
							.getComponentFinder();
					Component c = componentFinder.find();
					if (c == null) {
						throw new TestingError("Unable to find "
								+ componentFinder.toString());
					}
					if (i > 0) {
						unhighlightCurrentComponent();
					}
					currentComponent = c;
					highlightCurrentComponent();
					Thread.sleep(1000);
					testAction.execute(c);
				} catch (Exception e) {
					handleCurrentComponentChange(null);
					throw new TestingError("Test Action n°" + (i + 1) + ": "
							+ e.toString(), e);
				}
			}
		} finally {
			if (currentComponent != null) {
				unhighlightCurrentComponent();
				currentComponent = null;
			}
		}
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
		handleCurrentComponentChange(null);
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
			handleCurrentComponentChange(c);
		}
		if (isComponentSelectionEvent(event)) {
			handleComponentSelection(event);
		}
	}

	protected void handleComponentSelection(final AWTEvent event) {
		popupMenu.removeAll();
		final Component c = (Component) event.getSource();
		createReleaseComponentMenuItem(c);
		createComponentSelectionMenuItems(c);
		createStopRecordingMenuItem(c);
		popupMenu.add(new JMenuItem("Cancel"));
		MouseEvent mouseEvt = (MouseEvent) event;
		popupMenu.show(c, mouseEvt.getX(), mouseEvt.getY());
	}

	protected void createReleaseComponentMenuItem(Component c) {
		JMenuItem menuItem = new JMenuItem("Do Not Record The Next Action");
		{
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handleCurrentComponentChange(null);
					stopRecording();
					new Thread(Tester.class.getSimpleName() + " Restarter") {
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

	protected void createComponentSelectionMenuItems(final Component c) {
		for (final TestAction testAction : getPossibleTestActions(c)) {
			JMenuItem item = new JMenuItem("Record "
					+ TesterUI.INSTANCE.getObjectKind(testAction)
					+ " To This Component");
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
			handleCurrentComponentChange(null);
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

	protected void handleCurrentComponentChange(Component c) {
		if (currentComponent != null) {
			unhighlightCurrentComponent();
			restoreComponentListeners();
			currentComponent = null;
		}
		if (c == null) {
			return;
		}
		currentComponent = c;
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

	public void loadFromFile(File input) {
		XStream xstream = new XStream();
		Tester loaded = (Tester) xstream.fromXML(input);
		testActions = loaded.testActions;
		millisecondsBetwneenActions = loaded.millisecondsBetwneenActions;
	}

	public void saveToFile(File output) throws IOException {
		XStream xstream = new XStream();
		FileWriter fileWriter = new FileWriter(output);
		xstream.toXML(this, fileWriter);
		fileWriter.flush();
		fileWriter.close();
	}

}
