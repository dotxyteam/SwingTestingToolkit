package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
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

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.util.AlternateWindowDecorationsPanel;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.TreeSelectionDialog;
import xy.ui.testing.util.TreeSelectionDialog.INodePropertyAccessor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

public class Tester {

	public static final Color HIGHLIGHT_FOREGROUND = new Color(255, 0, 0);
	public static final Color HIGHLIGHT_BACKGROUND = new Color(255, 220, 220);

	protected List<TestAction> testActions = new ArrayList<TestAction>();
	protected int minimumSecondsToWaitBetwneenActions = 1;
	protected int maximumSecondsToWaitBetwneenActions = 15;

	protected AWTEventListener recordingListener;
	protected Component currentComponent;
	protected Color currentComponentBackground;
	protected Color currentComponentForeground;
	protected MouseListener[] currentComponentMouseListeners;
	protected boolean recording = false;
	protected Border currentComponentBorder;

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
				throw new TestingError("Test Action n°"
						+ (testActions.indexOf(testAction) + 1) + ": "
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
		if (isCurrentComponentChangeEvent(event)) {
			handleCurrentComponentChange(c);
		}
		if (isComponentIntrospectionRequestEvent(event)) {
			handleComponentIntrospectionRequest(event);
		}
	}

	protected void handleComponentIntrospectionRequest(final AWTEvent event) {
		if (event instanceof MouseEvent) {
			DefaultMutableTreeNode menuRoot = new DefaultMutableTreeNode();
			final Component c = (Component) event.getSource();
			createReleaseComponentMenuItem(menuRoot, c);
			createStopRecordingMenuItem(menuRoot, c);
			createTestActionMenuItems(menuRoot, c, event);
			AbstractAction todo = openTestActionMenu(menuRoot);
			if (todo == null) {
				return;
			}
			todo.actionPerformed(null);
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
				startRecording();
				CloseWindowAction closeAction = new CloseWindowAction();
				closeAction.initializeFrom(window, event);
				onTestActionSelection(closeAction, window);
			} else {
				startRecording();
			}
		}
	}

	protected AbstractAction openTestActionMenu(DefaultMutableTreeNode menuRoot) {
		final JPanel testerForm = ReflectionUIUtils.getKeysFromValue(
				TesterUI.INSTANCE.getObjectByForm(), this).get(0);
		Window parentWindow = SwingUtilities.getWindowAncestor(testerForm);
		String title = TesterUI.INSTANCE.getObjectKind(this);
		DefaultTreeModel treeModel = new DefaultTreeModel(menuRoot);
		final TreeSelectionDialog dialog = new TreeSelectionDialog(
				parentWindow, title, null, treeModel,
				getTestActionMenuItemTextAccessor(),
				getTestActionMenuItemIconAccessor(), true,
				ModalityType.DOCUMENT_MODAL) {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected Container createContentPane(String message) {
				Container result = super.createContentPane(message);
				AlternateWindowDecorationsPanel decorationsPanel = TesterUI
						.getAlternateWindowDecorationsPanel(this);
				decorationsPanel.configureWindow(this);
				decorationsPanel.getContentPanel().add(result);
				result = decorationsPanel;
				return result;
			}

		};
		dialog.setVisible(true);
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) dialog.getSelection();
		if(selected == null){
			return null;
		}
		return (AbstractAction) selected.getUserObject();
	}

	protected INodePropertyAccessor<String> getTestActionMenuItemTextAccessor() {
		return new INodePropertyAccessor<String>() {

			@Override
			public String get(Object node) {
				Object object = ((DefaultMutableTreeNode) node).getUserObject();
				if (object instanceof AbstractAction) {
					AbstractAction swingAction = (AbstractAction) object;
					return (String) swingAction.getValue(AbstractAction.NAME);
				} else if (object instanceof String) {
					return (String) object;
				} else {
					return null;
				}
			}
		};
	}

	protected INodePropertyAccessor<Icon> getTestActionMenuItemIconAccessor() {
		return new INodePropertyAccessor<Icon>() {

			@Override
			public Icon get(Object node) {
				Object object = ((DefaultMutableTreeNode) node).getUserObject();
				if (object instanceof AbstractAction) {
					AbstractAction swingAction = (AbstractAction) object;
					return (Icon) swingAction
							.getValue(AbstractAction.SMALL_ICON);
				} else {
					return null;
				}
			}
		};
	}

	protected void createReleaseComponentMenuItem(DefaultMutableTreeNode root,
			Component c) {
		DefaultMutableTreeNode pauseItem = new DefaultMutableTreeNode(
				new AbstractAction("Pause Recording (5 seconds)") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						handleCurrentComponentChange(null);
						stopRecording();
						new Thread(Tester.class.getSimpleName() + " Restarter") {
							@Override
							public void run() {
								try {
									sleep(5000);
								} catch (InterruptedException e) {
									throw new TestingError(e);
								}
								startRecording();
							}
						}.start();
					}
				});
		root.add(pauseItem);
	}

	protected void createStopRecordingMenuItem(DefaultMutableTreeNode root,
			Component c) {
		DefaultMutableTreeNode stopRecordingItem = new DefaultMutableTreeNode(
				new AbstractAction("Stop Recording") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						TesterUI.INSTANCE.getFormsUpdatingMethod(Tester.this,
								"stopRecording").invoke(Tester.this,
								Collections.<Integer, Object> emptyMap());
					}
				});
		root.add(stopRecordingItem);
	}

	protected void createTestActionMenuItems(DefaultMutableTreeNode root,
			final Component c, AWTEvent event) {
		DefaultMutableTreeNode recordGroup = new DefaultMutableTreeNode(
				"Execute And Record");
		TreeSelectionDialog.setGrouNode(recordGroup, true);
		root.add(recordGroup);
		DefaultMutableTreeNode actionsGroup = new DefaultMutableTreeNode(
				"Actions");
		TreeSelectionDialog.setGrouNode(actionsGroup, true);
		DefaultMutableTreeNode assertionssGroup = new DefaultMutableTreeNode(
				"Assertion");
		TreeSelectionDialog.setGrouNode(assertionssGroup, true);
		for (final TestAction testAction : getPossibleTestActions(c, event)) {
			String testActionTypeName = TesterUI.INSTANCE.getObjectKind(
					testAction).replaceAll(" Action$", "");
			DefaultMutableTreeNode item = new DefaultMutableTreeNode(
					new AbstractAction(testActionTypeName) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							onTestActionSelection(testAction, c);
						}

						@Override
						public Object getValue(String key) {
							if (key == AbstractAction.SMALL_ICON) {
								Image image = TesterUI.INSTANCE
										.getObjectIconImage(testAction);
								return new ImageIcon(image);
							} else {
								return super.getValue(key);
							}
						}

					});
			if (testActionTypeName.startsWith("Check")) {
				assertionssGroup.add(item);
			} else {
				actionsGroup.add(item);
			}
			if (actionsGroup.getChildCount() > 0) {
				recordGroup.add(actionsGroup);
			}
			if (assertionssGroup.getChildCount() > 0) {
				recordGroup.add(assertionssGroup);
			}

		}
	}

	protected void onTestActionSelection(final TestAction testAction,
			final Component c) {
		if (TesterUI.INSTANCE.openSettings(testAction, c)) {
			TesterUI.INSTANCE.setLastExecutedTestAction(testAction);
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

	protected List<TestAction> getPossibleTestActions(Component c,
			AWTEvent event) {
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

	protected boolean isComponentIntrospectionRequestEvent(AWTEvent event) {
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
			restoreCurrentComponentListeners();
			currentComponent = null;
		}
		if (c == null) {
			return;
		}
		currentComponent = c;
		highlightCurrentComponent();
		disableCurrentComponentListeners();
	}

	protected void restoreCurrentComponentListeners() {
		for (MouseListener l : currentComponentMouseListeners) {
			currentComponent.addMouseListener(l);
		}
	}

	protected void disableCurrentComponentListeners() {
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
