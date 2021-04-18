package xy.ui.testing.editor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.editor.WindowManager;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AbstractControlButton;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.component.ClickOnMenuItemAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.TreeSelectionDialog;
import xy.ui.testing.util.TreeSelectionDialog.INodePropertyAccessor;

public class RecordingWindowSwitch extends AbstractWindowSwitch {

	public enum InsertPosition {
		BeforeSelection, AfterSelection, OverSelection, AfterLast
	}

	protected InsertPosition insertPosition;

	public RecordingWindowSwitch(TestEditor testEditor) {
		super(testEditor);
	}

	@Override
	public String getSwitchTitle() {
		return "Recording Control";
	}

	@Override
	protected void onBegining() {
		setPausedAndUpdateUI(false);
	}

	@Override
	protected void onEnd() {
		getTester().handleCurrentComponentChange(null);
	}

	@Override
	public String getStatus() {
		return "(Waiting) Click on a component to record...";
	}

	public InsertPosition getInsertPosition() {
		return insertPosition;
	}

	public void setInsertPosition(InsertPosition insertPosition) {
		this.insertPosition = insertPosition;
	}

	public void handleWindowClosingRecordingEvent(AWTEvent event) {
		setPausedAndUpdateUI(true);
		try {
			WindowEvent windowEvent = (WindowEvent) event;
			Window window = windowEvent.getWindow();
			String title = getSwingRenderer().getObjectTitle(getTester());
			RecordingWindowSwitch.this.getWindow().requestFocus();
			if (getSwingRenderer().openQuestionDialog(RecordingWindowSwitch.this.getWindow(),
					"Do you want to record this window closing event?", title)) {
				CloseWindowAction closeAction = new CloseWindowAction();
				closeAction.initializeFrom(window, event, testEditor);
				handleNewTestActionInsertionRequest(closeAction, window, false);
			}
		} finally {
			setPausedAndUpdateUI(false);
		}
	}

	public void handleMenuItemClickRecordingEvent(AWTEvent event) {
		setPausedAndUpdateUI(true);
		try {
			final JMenuItem menuItem = (JMenuItem) event.getSource();
			ClickOnMenuItemAction testACtion = new ClickOnMenuItemAction();
			testACtion.initializeFrom(menuItem, event, testEditor);
			String title = getSwingRenderer().getObjectTitle(getTester());
			RecordingWindowSwitch.this.getWindow().requestFocus();
			if (getSwingRenderer().openQuestionDialog(RecordingWindowSwitch.this.getWindow(),
					"Do you want to record this menu item activation event?", title)) {
				handleNewTestActionInsertionRequest(testACtion, menuItem, true);
			}
		} finally {
			setPausedAndUpdateUI(false);
		}
	}

	public void handleGenericRecordingEvent(AWTEvent event) {
		setPausedAndUpdateUI(true);
		try {
			RecordingWindowSwitch.this.getWindow().requestFocus();
			final AbstractAction todo = openTestActionSelectionWindow(event, RecordingWindowSwitch.this.getWindow());
			if (todo == null) {
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					todo.actionPerformed(null);
				}
			});
		} finally {
			setPausedAndUpdateUI(false);
		}
	}

	protected boolean handleNewTestActionInsertionRequest(final TestAction testAction, final Component c,
			boolean execute) {
		if (openRecordingSettingsWindow(testAction, c)) {
			getTester().handleCurrentComponentChange(null);
			insertNewTestAction(testAction);
			if (execute) {
				new Thread("Executor of: " + testAction) {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new AssertionError(e);
						}
						testAction.execute(c, getTester());
					}
				}.start();
			}
			return true;
		}
		return false;
	}

	public void insertNewTestAction(TestAction testAction) {
		final List<TestAction> newTestActionList = new ArrayList<TestAction>(
				Arrays.asList(getTester().getTestActions()));

		List<Integer> selectionIndexes = testEditor.getMultipleSelectedActionIndexes();
		List<Integer> ascendingSelectionIndexes = new ArrayList<Integer>(selectionIndexes);
		Collections.sort(ascendingSelectionIndexes);
		List<Integer> descendingSelectionIndexes = new ArrayList<Integer>(ascendingSelectionIndexes);
		Collections.reverse(descendingSelectionIndexes);

		int insertionIndex;

		if (selectionIndexes.size() > 0) {
			if (insertPosition == InsertPosition.OverSelection) {
				for (int i : descendingSelectionIndexes) {
					newTestActionList.remove(i);
				}
				insertionIndex = ascendingSelectionIndexes.get(0);
			} else if (insertPosition == InsertPosition.BeforeSelection) {
				insertionIndex = ascendingSelectionIndexes.get(0);
			} else if (insertPosition == InsertPosition.AfterSelection) {
				insertionIndex = descendingSelectionIndexes.get(0) + 1;
			} else if (insertPosition == InsertPosition.AfterLast) {
				insertionIndex = newTestActionList.size();
			} else {
				throw new AssertionError();
			}
		} else {
			insertionIndex = newTestActionList.size();
		}

		newTestActionList.add(insertionIndex, testAction);
		testEditor.setTestActionsAndUpdateUI(newTestActionList.toArray(new TestAction[newTestActionList.size()]));
		testEditor.setSelectedActionIndex(insertionIndex);
	}

	protected boolean openRecordingSettingsWindow(TestAction testAction, Component c) {
		testEditor.setComponentFinderInitializationSource(c);
		String title = "New Test Action - " + getSwingRenderer().getObjectTitle(testAction);
		StandardEditorBuilder dialogStatus = getSwingRenderer().openObjectDialog(RecordingWindowSwitch.this.getWindow(),
				testAction, title, getSwingRenderer().getObjectIconImage(testAction), true, true);
		testEditor.setComponentFinderInitializationSource(null);
		return !dialogStatus.isCancelled();
	}

	protected AbstractAction openTestActionSelectionWindow(AWTEvent event, Window parent) {
		final Component c = (Component) event.getSource();
		DefaultMutableTreeNode options = new DefaultMutableTreeNode();
		addPauseRecordingOption(options, c);
		addStopRecordingOption(options, c);
		addInspectComponentRecordingOption(options, c);
		addTestActionCreationRecordingOptions(options, c, event);
		String title = "New Test Action - Selection";
		DefaultTreeModel treeModel = new DefaultTreeModel(options);
		final TreeSelectionDialog dialog = new TreeSelectionDialog(parent, title, null, treeModel,
				getTestActionMenuItemTextAccessor(), getTestActionMenuItemIconAccessor(),
				getTestActionMenuItemSelectableAccessor(), true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected JButton createButton(final String text) {
				final ReflectionUI reflectionUI = testEditor.getSwingRenderer().getReflectionUI();
				JButton result = new AbstractControlButton() {

					private static final long serialVersionUID = 1L;

					@Override
					public String retrieveCaption() {
						return text;
					}

					@Override
					public SwingRenderer getSwingRenderer() {
						return testEditor.getSwingRenderer();
					}

					@Override
					public Image retrieveBackgroundImage() {
						if (reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath() != null) {
							return SwingRendererUtils.loadImageThroughCache(
									reflectionUI.getApplicationInfo().getMainButtonBackgroundImagePath(),
									ReflectionUIUtils.getErrorLogListener(reflectionUI));
						}
						return null;
					}

					@Override
					public Color retrieveBackgroundColor() {
						if (reflectionUI.getApplicationInfo().getMainButtonBackgroundColor() != null) {
							return SwingRendererUtils
									.getColor(reflectionUI.getApplicationInfo().getMainButtonBackgroundColor());
						}
						return null;
					}

					@Override
					public Color retrieveForegroundColor() {
						if (reflectionUI.getApplicationInfo().getMainButtonForegroundColor() != null) {
							return SwingRendererUtils
									.getColor(reflectionUI.getApplicationInfo().getMainButtonForegroundColor());
						}
						return null;
					}

					@Override
					public Color retrieveBorderColor() {
						if (reflectionUI.getApplicationInfo().getMainButtonBorderColor() != null) {
							return SwingRendererUtils
									.getColor(reflectionUI.getApplicationInfo().getMainButtonBorderColor());
						}
						return null;
					}
				};
				return result;
			}

			@Override
			protected JPanel createButtonPane() {
				JPanel result = super.createButtonPane();
				result.setOpaque(false);
				return result;
			}

			@Override
			protected JPanel createMainPane() {
				JPanel result = super.createMainPane();
				result.setOpaque(false);
				return result;
			}

			@Override
			protected JPanel createContentPane() {
				JPanel result = super.createContentPane();
				result.setOpaque(false);
				return result;
			}

			@Override
			protected void layoutContent(Container content) {
				WindowManager windowManager = testEditor.getSwingRenderer().createWindowManager(this);
				windowManager.set(content, null);
			}

		};
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setVisible(true);
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) dialog.getSelection();
		if (selected == null) {
			return null;
		}
		return (AbstractAction) selected.getUserObject();
	}

	protected INodePropertyAccessor<Icon> getTestActionMenuItemIconAccessor() {
		return new INodePropertyAccessor<Icon>() {

			@Override
			public Icon get(Object node) {
				Object object = ((DefaultMutableTreeNode) node).getUserObject();
				if (object instanceof AbstractAction) {
					AbstractAction swingAction = (AbstractAction) object;
					return (Icon) swingAction.getValue(AbstractAction.SMALL_ICON);
				} else if (object instanceof String) {
					return null;
				} else {
					return null;
				}
			}
		};
	}

	protected INodePropertyAccessor<Boolean> getTestActionMenuItemSelectableAccessor() {
		return new INodePropertyAccessor<Boolean>() {

			@Override
			public Boolean get(Object node) {
				Object object = ((DefaultMutableTreeNode) node).getUserObject();
				return object instanceof AbstractAction;
			}
		};
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

	protected void addStopRecordingOption(DefaultMutableTreeNode options, Component c) {
		DefaultMutableTreeNode stopRecordingItem = new DefaultMutableTreeNode(new AbstractAction("Stop Recording") {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue(String key) {
				if (key == AbstractAction.SMALL_ICON) {
					return TestingUtils.TESTER_ICON;
				} else {
					return super.getValue(key);
				}
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				RecordingWindowSwitch.this.activate(false);
			}
		});
		options.add(stopRecordingItem);
	}

	protected void addTestActionCreationRecordingOptions(DefaultMutableTreeNode options, final Component c,
			AWTEvent event) {
		DefaultMutableTreeNode recordGroup = new DefaultMutableTreeNode("(Execute And Record)");
		options.add(recordGroup);
		DefaultMutableTreeNode actionsGroup = new DefaultMutableTreeNode("(Actions)");
		DefaultMutableTreeNode assertionssGroup = new DefaultMutableTreeNode("(Assertion)");
		for (final TestAction testAction : getPossibleTestActions(c, event)) {
			String testActionTypeName = getSwingRenderer().getObjectTitle(testAction).replaceAll(" Action$", "");
			DefaultMutableTreeNode item = new DefaultMutableTreeNode(new AbstractAction(testActionTypeName) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					RecordingWindowSwitch.this.setPausedAndUpdateUI(true);
					try {
						handleNewTestActionInsertionRequest(testAction, c, true);
					} finally {
						RecordingWindowSwitch.this.setPausedAndUpdateUI(false);
					}
				}

				@Override
				public Object getValue(String key) {
					if (key == AbstractAction.SMALL_ICON) {
						Image image = getSwingRenderer().getObjectIconImage(testAction);
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

	protected List<TestAction> getPossibleTestActions(Component c, AWTEvent event) {
		try {
			List<TestAction> result = new ArrayList<TestAction>();
			for (Class<?> testActionClass : testEditor.getTestActionClasses()) {
				TestAction testAction = (TestAction) testActionClass.newInstance();
				try {
					if (testAction.initializeFrom(c, event, testEditor)) {
						result.add(testAction);
					}
				} catch (Throwable t) {
					getTestEditor().logError(new Exception(
							"Failed to initialize " + testActionClass.getName() + " instance: " + t.toString(), t));
				}
			}
			return result;
		} catch (Exception e) {
			throw new AssertionError(e);
		}

	}

	protected void addInspectComponentRecordingOption(DefaultMutableTreeNode options, final Component c) {
		DefaultMutableTreeNode item = new DefaultMutableTreeNode(new AbstractAction("Inspect Component") {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue(String key) {
				if (key == AbstractAction.SMALL_ICON) {
					return TestingUtils.TESTER_ICON;
				} else {
					return super.getValue(key);
				}
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				RecordingWindowSwitch.this.setPausedAndUpdateUI(true);
				try {
					testEditor.getComponentInspectionWindowSwitch().openComponentInspector(c,
							RecordingWindowSwitch.this.getWindow());
				} finally {
					RecordingWindowSwitch.this.setPausedAndUpdateUI(false);
				}
			}
		});
		options.add(item);
	}

	protected void addPauseRecordingOption(DefaultMutableTreeNode options, Component c) {
		DefaultMutableTreeNode pauseItem = new DefaultMutableTreeNode(
				new AbstractAction("Skip (Pause Recording for 5 seconds)") {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getValue(String key) {
						if (key == AbstractAction.SMALL_ICON) {
							return TestingUtils.TESTER_ICON;
						} else {
							return super.getValue(key);
						}
					}

					@Override
					public void actionPerformed(ActionEvent e) {
						getTester().handleCurrentComponentChange(null);
						RecordingWindowSwitch.this.setPausedAndUpdateUI(true);
						new Thread(Tester.class.getSimpleName() + " Restarter") {
							@Override
							public void run() {
								try {
									sleep(5000);
								} catch (InterruptedException e) {
									throw new AssertionError(e);
								}
								RecordingWindowSwitch.this.setPausedAndUpdateUI(false);
							}
						}.start();
					}
				});
		options.add(pauseItem);
	}

}