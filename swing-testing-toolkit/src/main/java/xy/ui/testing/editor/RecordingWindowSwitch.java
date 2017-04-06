package xy.ui.testing.editor;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import xy.reflect.ui.control.swing.StandardEditorBuilder;
import xy.ui.testing.Tester;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.component.ClickOnMenuItemAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.TreeSelectionDialog;
import xy.ui.testing.util.TreeSelectionDialog.INodePropertyAccessor;

public class RecordingWindowSwitch extends AbstractWindowSwitch {

	protected boolean recordingInsertedAfterSelection = false;
	protected RecordingStatus recordingStatus = new RecordingStatus();

	public RecordingWindowSwitch(TesterEditor testerEditor) {
		super(testerEditor);
	}

	@Override
	public String getSwitchTitle() {
		return "Recording Control";
	}

	@Override
	protected void onBegining() {
		setRecordingPausedAndUpdateUI(false);
	}

	@Override
	protected void onEnd() {
		getTester().handleCurrentComponentChange(null);
	}

	@Override
	public RecordingStatus getStatus() {
		return recordingStatus;
	}

	public void setRecordingInsertedAfterSelection(boolean recordingInsertedAfterSelection) {
		this.recordingInsertedAfterSelection = recordingInsertedAfterSelection;
	}

	public void handleWindowClosingRecordingEvent(AWTEvent event) {
		setRecordingPausedAndUpdateUI(true);
		try {
			WindowEvent windowEvent = (WindowEvent) event;
			Window window = windowEvent.getWindow();
			String title = getSwingRenderer().getObjectTitle(getTester());
			RecordingWindowSwitch.this.getWindow().requestFocus();
			if (getSwingRenderer().openQuestionDialog(RecordingWindowSwitch.this.getWindow(),
					"Do you want to record this window closing event?", title)) {
				CloseWindowAction closeAction = new CloseWindowAction();
				closeAction.initializeFrom(window, event, testerEditor);
				handleNewTestActionInsertionRequest(closeAction, window, false);
			}
		} finally {
			setRecordingPausedAndUpdateUI(false);
		}
	}

	public void handleMenuItemClickRecordingEvent(AWTEvent event) {
		setRecordingPausedAndUpdateUI(true);
		try {
			final JMenuItem menuItem = (JMenuItem) event.getSource();
			ClickOnMenuItemAction testACtion = new ClickOnMenuItemAction();
			testACtion.initializeFrom(menuItem, event, testerEditor);
			String title = getSwingRenderer().getObjectTitle(getTester());
			RecordingWindowSwitch.this.getWindow().requestFocus();
			if (getSwingRenderer().openQuestionDialog(RecordingWindowSwitch.this.getWindow(),
					"Do you want to record this menu item activation event?", title)) {
				handleNewTestActionInsertionRequest(testACtion, menuItem, true);
			}
		} finally {
			setRecordingPausedAndUpdateUI(false);
		}
	}

	public void handleGenericRecordingEvent(AWTEvent event) {
		setRecordingPausedAndUpdateUI(true);
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
			setRecordingPausedAndUpdateUI(false);
		}
	}

	protected boolean handleNewTestActionInsertionRequest(final TestAction testAction, final Component c,
			boolean execute) {
		if (openRecordingSettingsWindow(testAction, c)) {
			final List<TestAction> newTestActionList = new ArrayList<TestAction>(
					Arrays.asList(getTester().getTestActions()));
			int selectionIndex = testerEditor.getSelectedActionIndex();
			int insertionIndex;
			if ((selectionIndex != -1) && recordingInsertedAfterSelection) {
				insertionIndex = selectionIndex + 1;
			} else {
				insertionIndex = newTestActionList.size();
			}
			newTestActionList.add(insertionIndex, testAction);
			testerEditor.setTestActionsAndUpdateUI(newTestActionList.toArray(new TestAction[newTestActionList.size()]));
			testerEditor.setSelectedActionIndex(insertionIndex);
			getTester().handleCurrentComponentChange(null);
			if (execute) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new AssertionError(e);
						}
						testAction.execute(c, getTester());
					}
				});
			}
			return true;
		}
		return false;
	}

	protected boolean openRecordingSettingsWindow(TestAction testAction, Component c) {
		testerEditor.setComponentFinderInitializationSource(c);
		StandardEditorBuilder dialogStatus = getSwingRenderer().openObjectDialog(RecordingWindowSwitch.this.getWindow(),
				testAction, getSwingRenderer().getObjectTitle(testAction),
				getSwingRenderer().getObjectIconImage(testAction), true, true);
		testerEditor.setComponentFinderInitializationSource(null);
		if (dialogStatus.wasOkPressed()) {
			return true;
		} else {
			dialogStatus.getSubObjectModificationStack().undoAll();
			return false;
		}
	}

	protected AbstractAction openTestActionSelectionWindow(AWTEvent event, Window parent) {
		final Component c = (Component) event.getSource();
		DefaultMutableTreeNode options = new DefaultMutableTreeNode();
		addPauseRecordingOption(options, c);
		addStopRecordingOption(options, c);
		addInspectComponentRecordingOption(options, c);
		addTestActionCreationRecordingOptions(options, c, event);
		String title = getSwingRenderer().getObjectTitle(getTester());
		DefaultTreeModel treeModel = new DefaultTreeModel(options);
		final TreeSelectionDialog dialog = new TreeSelectionDialog(parent, title, null, treeModel,
				getTestActionMenuItemTextAccessor(), getTestActionMenuItemIconAccessor(),
				getTestActionMenuItemSelectableAccessor(), true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Container createContentPane(String message) {
				return TesterEditor.getAlternateWindowDecorationsContentPane(this, super.createContentPane(message),
						testerEditor);
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
					RecordingWindowSwitch.this.setRecordingPausedAndUpdateUI(true);
					try {
						handleNewTestActionInsertionRequest(testAction, c, true);
					} finally {
						RecordingWindowSwitch.this.setRecordingPausedAndUpdateUI(false);
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
			for (Class<?> testActionClass : testerEditor.getTestActionClasses()) {
				TestAction testAction = (TestAction) testActionClass.newInstance();
				try {
					if (testAction.initializeFrom(c, event, testerEditor)) {
						result.add(testAction);
					}
				} catch (Throwable t) {
					getTesterEditor().logError(new Exception(
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
				RecordingWindowSwitch.this.setRecordingPausedAndUpdateUI(true);
				try {
					testerEditor.getComponentInspectionWindowSwitch().openComponentInspector(c,
							RecordingWindowSwitch.this.getWindow());
				} finally {
					RecordingWindowSwitch.this.setRecordingPausedAndUpdateUI(false);
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
						RecordingWindowSwitch.this.setRecordingPausedAndUpdateUI(true);
						new Thread(Tester.class.getSimpleName() + " Restarter") {
							@Override
							public void run() {
								try {
									sleep(5000);
								} catch (InterruptedException e) {
									throw new AssertionError(e);
								}
								RecordingWindowSwitch.this.setRecordingPausedAndUpdateUI(false);
							}
						}.start();
					}
				});
		options.add(pauseItem);
	}

	public void setRecordingPausedAndUpdateUI(boolean b) {
		recordingStatus.setRecordingPaused(b);
		getSwingRenderer().refreshAllFieldControls(statusControlForm, false);
	}

	public class RecordingStatus {
		protected boolean recordingPaused = false;

		public boolean isRecordingPaused() {
			return recordingPaused;
		}

		public void setRecordingPaused(boolean b) {
			getTester().handleCurrentComponentChange(null);
			this.recordingPaused = b;
		}
	}

}