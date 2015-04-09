package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public class Tester {

	public static final Color HIGHLIGHT_FOREGROUND = new Color(255, 0, 0);
	public static final Color HIGHLIGHT_BACKGROUND = new Color(255, 220, 220);

	protected List<TestAction> testActions = new ArrayList<TestAction>();
	protected int minimumSecondsToWaitBetwneenActions = 2;
	protected int maximumSecondsToWaitBetwneenActions = 15;

	protected AWTEventListener recordingListener;
	protected Component currentComponent;
	protected Color currentComponentBackground;
	protected Color currentComponentForeground;
	protected MouseListener[] currentComponentMouseListeners;
	protected JPopupMenu popupMenu = new JPopupMenu();
	protected boolean recording = false;
	protected Border currentComponentBorder;

	public static void assertSuccessfulReplay(File replayFile)
			throws IOException {
		Tester tester = new Tester();
		tester.loadFromFile(replayFile);
		tester.playAll();
	}

	public static void assertSuccessfulReplay(InputStream replayStream)
			throws IOException {
		Tester tester = new Tester();
		tester.loadFromStream(replayStream);
		tester.playAll();
	}

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
						+ AWTEvent.KEY_EVENT_MASK + AWTEvent.WINDOW_EVENT_MASK);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		TestingUtils.removeAWTEventListener(recordingListener);
	}

	public int getMinimumSecondsToWaitBetwneenActions() {
		return minimumSecondsToWaitBetwneenActions;
	}

	public void setMinimumSecondsToWaitBetwneenActions(
			int minimumSecondsToWaitBetwneenActions) {
		this.minimumSecondsToWaitBetwneenActions = minimumSecondsToWaitBetwneenActions;
	}

	public int getMaximumSecondsToWaitBetwneenActions() {
		return maximumSecondsToWaitBetwneenActions;
	}

	public void setMaximumSecondsToWaitBetwneenActions(
			int maximumSecondsToWaitBetwneenActions) {
		this.maximumSecondsToWaitBetwneenActions = maximumSecondsToWaitBetwneenActions;
	}

	public TestAction[] getTestActions() {
		return testActions.toArray(new TestAction[testActions.size()]);
	}

	public void setTestActions(TestAction[] testActions) {
		this.testActions.clear();
		this.testActions.addAll(Arrays.asList(testActions));
	}

	public void playAll() {
		play(testActions, null);
	}

	public void play(final List<TestAction> toReplay,
			Runnable runBeforeEachAction) {
		if (isRecording()) {
			stopRecording();
		}
		for (int i = 0; i < toReplay.size(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			if (runBeforeEachAction != null) {
				runBeforeEachAction.run();
			}
			final TestAction testAction = toReplay.get(i);
			try {
				TesterUI.INSTANCE.setLastExecutedTestAction(testAction);
				TesterUI.INSTANCE.upadateTestActionsControl(Tester.this);
				Thread.sleep(minimumSecondsToWaitBetwneenActions * 1000);
				Component c = findComponentImmediatelyOrRetry(testAction);
				if (c != null) {
					currentComponent = c;
					highlightCurrentComponent();
					Thread.sleep(1000);
					unhighlightCurrentComponent();
					currentComponent = null;
				}
				testAction.execute(c);
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					if (currentComponent != null) {
						unhighlightCurrentComponent();
						currentComponent = null;
					}
					break;
				}
				throw new TestingError("Test Action n°" + (i + 1) + ": "
						+ e.toString(), e);
			}
		}
	}

	protected Component findComponentImmediatelyOrRetry(TestAction testAction) {
		Component result = null;
		int remainingSeconds = maximumSecondsToWaitBetwneenActions
				- minimumSecondsToWaitBetwneenActions;
		while (true) {
			try {
				result = testAction.findComponent();
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					throw new TestingError(e);
				}
				remainingSeconds--;
				if (remainingSeconds == 0) {
					throw new TestingError(e);
				}
			}
		}
		return result;
	}

	public void record() {
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
		if (isFocusOnComponentEvent(event)) {
			handleFocusOnComponent(event);
		}
	}

	protected void handleFocusOnComponent(final AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvt = (MouseEvent) event;
			popupMenu.removeAll();
			Component c = (Component) event.getSource();
			createReleaseComponentMenuItem(c);
			createTestActionMenuItems(c, event);
			createStopRecordingMenuItem(c);
			popupMenu.add(new JMenuItem("Cancel"));
			popupMenu.show(c, mouseEvt.getX(), mouseEvt.getY());
		}
		if (event instanceof WindowEvent) {
			WindowEvent windowEvent = (WindowEvent) event;
			Window window = windowEvent.getWindow();
			stopRecording();
			ImageIcon icon = new ImageIcon(
					TesterUI.INSTANCE.getObjectIconImage(Tester.this));
			String message = "Do you want to record this window closing event?";
			String title = TesterUI.INSTANCE.getObjectKind(Tester.this);
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(window,
					message, title, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, icon)) {
				record();
				CloseWindowAction closeAction = new CloseWindowAction();
				closeAction.initializeFrom(window, event);
				onTestActionSelection(closeAction, window);
			} else {
				record();
			}
		}
	}

	protected void createReleaseComponentMenuItem(Component c) {
		JMenuItem menuItem = new JMenuItem("Do not record the next action");
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
								throw new TestingError(e);
							}
							record();
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
					TesterUI.INSTANCE.getFormsUpdatingMethod(Tester.this,
							"stopRecording").invoke(Tester.this,
							Collections.<Integer, Object> emptyMap());
				}
			});
			popupMenu.add(stopRecordingItem);
		}
	}

	protected void createTestActionMenuItems(final Component c, AWTEvent event) {
		for (final TestAction testAction : getPossibleTestActions(c, event)) {
			JMenuItem item = new JMenuItem("(Execute and Record) "
					+ TesterUI.INSTANCE.getObjectKind(testAction).replaceAll(
							" Action$", ""));
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

	protected List<TestAction> getPossibleTestActions(Component c, AWTEvent event) {
		try {
			List<TestAction> result = new ArrayList<TestAction>();
			for (Class<?> testActionClass : TesterUI.TEST_ACTION_CLASSESS) {
				TestAction testAction = (TestAction) testActionClass
						.newInstance();
				if (testAction.initializeFrom(c, event)) {
					result.add(testAction);
				}
			}
			return result;
		} catch (Exception e) {
			throw new TestingError(e);
		}

	}

	protected boolean isFocusOnComponentEvent(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
				if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
					return true;
				}
			}
		}
		if (event instanceof WindowEvent) {
			WindowEvent windowEvent = (WindowEvent) event;
			if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
				return true;
			}
		}
		return false;
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
		if (currentComponent instanceof JComponent) {
			((JComponent) currentComponent).setBorder(currentComponentBorder);
		}
	}

	protected void highlightCurrentComponent() {
		currentComponentBackground = currentComponent.getBackground();
		currentComponent.setBackground(HIGHLIGHT_BACKGROUND);

		currentComponentForeground = currentComponent.getForeground();
		currentComponent.setForeground(HIGHLIGHT_FOREGROUND);

		if (currentComponent instanceof JComponent) {
			currentComponentBorder = ((JComponent) currentComponent)
					.getBorder();
			((JComponent) currentComponent).setBorder(BorderFactory
					.createCompoundBorder(BorderFactory.createLineBorder(
							HIGHLIGHT_FOREGROUND, 1), currentComponentBorder));
		}
	}

	public void loadFromFile(File input) throws IOException {
		FileInputStream stream = new FileInputStream(input);
		try {
			loadFromStream(stream);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void loadFromStream(InputStream input) {
		XStream xstream = new XStream();
		xstream.registerConverter(new JavaBeanConverter(xstream.getMapper()),
				-20);
		Tester loaded = (Tester) xstream.fromXML(input);
		testActions = loaded.testActions;
		minimumSecondsToWaitBetwneenActions = loaded.minimumSecondsToWaitBetwneenActions;
		maximumSecondsToWaitBetwneenActions = loaded.maximumSecondsToWaitBetwneenActions;
	}

	public void saveToFile(File output) throws IOException {
		FileOutputStream stream = new FileOutputStream(output);
		try {
			saveToStream(stream);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	public void saveToStream(OutputStream output) throws IOException {
		XStream xstream = new XStream();
		xstream.registerConverter(new JavaBeanConverter(xstream.getMapper()),
				-20);
		xstream.toXML(this, output);
	}

}
