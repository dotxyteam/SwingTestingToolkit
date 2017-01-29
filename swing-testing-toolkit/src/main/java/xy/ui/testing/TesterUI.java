package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.control.swing.SwingCustomizer;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.ObjectDialogBuilder;
import xy.reflect.ui.control.swing.SwingRenderer;
import xy.reflect.ui.control.swing.SwingRenderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.ListControl.AutoFieldValueUpdatingItemPosition;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.field.ImplicitListField;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.filter.InfoFilterProxy;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.structure.ListStructuralInfoProxy;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.HiddenNullableFacetsTypeInfoProxyFactory;
import xy.reflect.ui.info.type.util.InfoCustomizations;
import xy.reflect.ui.info.type.util.TypeInfoProxyFactory;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.util.Accessor;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.SystemProperties;
import xy.ui.testing.action.CallMainMethodAction;
import xy.ui.testing.action.CheckNumberOfOpenWindowsAction;
import xy.ui.testing.action.WaitAction;
import xy.ui.testing.action.component.ClickAction;
import xy.ui.testing.action.component.ClickOnMenuItemAction;
import xy.ui.testing.action.component.SendKeysAction;
import xy.ui.testing.action.component.SendKeysAction.KeyboardInteraction;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlC;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlV;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlX;
import xy.ui.testing.action.component.SendKeysAction.WriteText;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlA;
import xy.ui.testing.action.component.property.ChangeComponentPropertyAction;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.action.component.specific.ClickOnTableCellAction;
import xy.ui.testing.action.component.specific.ExpandTreetTableToItemAction;
import xy.ui.testing.action.component.specific.SelectComboBoxItemAction;
import xy.ui.testing.action.component.specific.SelectTabAction;
import xy.ui.testing.action.component.specific.SelectTableRowAction;
import xy.ui.testing.action.window.CheckWindowVisibleStringsAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.MenuItemComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.VisibleStringComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;
import xy.ui.testing.util.AlternateWindowDecorationsContentPane;
import xy.ui.testing.util.ComponentInspector;
import xy.ui.testing.util.Listener;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.TreeSelectionDialog;
import xy.ui.testing.util.TreeSelectionDialog.INodePropertyAccessor;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class TesterUI extends ReflectionUI {

	public static final String CUSTOM_UI_CUSTOMIZATION_FILE_PATH = "xy.ui.testing.gui.customizationFile";
	public static final WeakHashMap<Window, TesterUI> BY_WINDOW = new WeakHashMap<Window, TesterUI>();
	public static final WeakHashMap<TesterUI, Tester> TESTERS = new WeakHashMap<TesterUI, Tester>();
	public static final TesterUI DEFAULT = new TesterUI(Tester.DEFAULT);

	protected Component componentFinderInitializationSource;
	protected boolean recordingInsertedAfterSelection = false;
	protected Tester tester;
	protected AWTEventListener recordingListener;
	protected Color decorationsForegroundColor = Tester.HIGHLIGHT_BACKGROUND;
	protected Color decorationsBackgroundColor = Tester.HIGHLIGHT_FOREGROUND;
	protected JPanel testerForm;
	protected SwingRenderer swingRenderer;
	protected InfoCustomizations infoCustomizations;

	protected ReplayManagement replayManagement = new ReplayManagement();
	protected JFrame replayManagementWindow;

	protected RecordingManagement recordingManagement = new RecordingManagement();
	protected JPanel recordingManagementForm;
	protected JFrame recordingManagementWindow;

	protected ComponentInspectionModeManagement componentInspectionModeManagement = new ComponentInspectionModeManagement();
	protected JFrame componentInspectionModeManagementWindow;

	public static void main(String[] args) {
		TesterUI testerUI = new TesterUI(new Tester());
		try {
			if (args.length > 1) {
				throw new Exception("Invalid command line arguments. Expected: [<fileName>]");
			} else if (args.length == 1) {
				String fileName = args[0];
				testerUI.getTester().loadFromFile(new File(fileName));
			}
			testerUI.open();
		} catch (Throwable t) {
			testerUI.getSwingRenderer().handleExceptionsFromDisplayedUI(null, t);
		}
	}

	public TesterUI(Tester tester) {
		this.tester = tester;
		TESTERS.put(this, tester);
		infoCustomizations = createInfoCustomizations();
		swingRenderer = createSwingRenderer();
		setupRecordingEventHandling();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		cleanupRecordingEventHandling();
	}

	protected void setupRecordingEventHandling() {
		recordingListener = new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				awtEventDispatched(event);
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(recordingListener, AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK + AWTEvent.WINDOW_EVENT_MASK);
	}

	protected void cleanupRecordingEventHandling() {
		TestingUtils.removeAWTEventListener(recordingListener);
	}

	protected InfoCustomizations createInfoCustomizations() {
		InfoCustomizations result = new InfoCustomizations();
		String alternateCustomizationFilePath = getAlternateCustomizationsFilePath();
		try {
			if (alternateCustomizationFilePath != null) {
				result.loadFromFile(new File(alternateCustomizationFilePath));
			} else {
				result.loadFromStream(TesterUI.class.getResourceAsStream("infoCustomizations.icu"));
			}
		} catch (IOException e) {
			throw new ReflectionUIError(e);
		}
		return result;
	}

	protected String getAlternateCustomizationsFilePath() {
		return System.getProperty(CUSTOM_UI_CUSTOMIZATION_FILE_PATH);
	}

	public Tester getTester() {
		return tester;
	}

	public SwingRenderer getSwingRenderer() {
		return swingRenderer;
	}

	public Color getDecorationsForegroundColor() {
		return decorationsForegroundColor;
	}

	public void setDecorationsForegroundColor(Color decorationsForegroundColor) {
		this.decorationsForegroundColor = decorationsForegroundColor;
	}

	public Color getDecorationsBackgroundColor() {
		return decorationsBackgroundColor;
	}

	public void setDecorationsBackgroundColor(Color decorationsBackgroundColor) {
		this.decorationsBackgroundColor = decorationsBackgroundColor;
	}

	public void open() {
		getSwingRenderer().openObjectFrame(tester);
	}

	public Class<?>[] getTestActionClasses() {
		return new Class[] { CallMainMethodAction.class, WaitAction.class, ExpandTreetTableToItemAction.class,
				SelectComboBoxItemAction.class, SelectTableRowAction.class, SelectTabAction.class,
				ClickOnTableCellAction.class, ClickOnMenuItemAction.class, ClickAction.class, SendKeysAction.class,
				CloseWindowAction.class, ChangeComponentPropertyAction.class, CheckComponentPropertyAction.class,
				CheckWindowVisibleStringsAction.class, CheckNumberOfOpenWindowsAction.class };
	}

	public Class<?>[] getComponentFinderClasses() {
		return new Class[] { VisibleStringComponentFinder.class, ClassBasedComponentFinder.class,
				PropertyBasedComponentFinder.class, MenuItemComponentFinder.class };
	}

	public Class<?>[] getKeyboardInteractionClasses() {
		return new Class[] { WriteText.class, SpecialKey.class, CtrlA.class, CtrlC.class, CtrlV.class, CtrlX.class };
	}

	protected void awtEventDispatched(AWTEvent event) {
		if (!(isRecording() && !recordingManagement.isRecordingPaused()) && !isInComponentInspectionMode()) {
			return;
		}
		if (event == null) {
			tester.handleCurrentComponentChange(null);
			return;
		}
		if (!(event.getSource() instanceof Component)) {
			return;
		}
		Component c = (Component) event.getSource();
		if (!c.isShowing()) {
			return;
		}
		if (TestingUtils.isTesterUIComponent(this, c)) {
			return;
		}
		if (isCurrentComponentChangeEvent(event)) {
			tester.handleCurrentComponentChange(c);
		}
		if (isComponentIntrospectionEvent(event)) {
			if (isInComponentInspectionMode()) {
				openComponentInspector(c);
			} else {
				handleRecordingEvent(event);
			}
		}
	}

	protected void handleRecordingEvent(final AWTEvent event) {
		setRecordingPausedAndUpdateUI(true);
		try {
			if (CloseWindowAction.matchIntrospectionRequestEvent(event)) {
				WindowEvent windowEvent = (WindowEvent) event;
				Window window = windowEvent.getWindow();
				String title = getSwingRenderer().getObjectTitle(tester);
				if (getSwingRenderer().openQuestionDialog(recordingManagementWindow,
						"Do you want to record this window closing event?", title)) {
					CloseWindowAction closeAction = new CloseWindowAction();
					closeAction.initializeFrom(window, event, this);
					handleTestActionInsertionRequest(closeAction, window, false);
				} else {
					setRecordingPausedAndUpdateUI(false);
				}
			} else if (ClickOnMenuItemAction.matchIntrospectionRequestEvent(event)) {
				final JMenuItem menuItem = (JMenuItem) event.getSource();
				ClickOnMenuItemAction testACtion = new ClickOnMenuItemAction();
				testACtion.initializeFrom(menuItem, event, this);
				String title = getSwingRenderer().getObjectTitle(tester);
				if (getSwingRenderer().openQuestionDialog(recordingManagementWindow,
						"Do you want to record this menu item activation event?", title)) {
					handleTestActionInsertionRequest(testACtion, menuItem, true);
				} else {
					setRecordingPausedAndUpdateUI(false);
				}
				return;
			} else {
				final AbstractAction todo = openTestActionSelectionWindow(event, recordingManagementWindow);
				if (todo == null) {
					return;
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						todo.actionPerformed(null);
					}
				});
			}
		} finally {
			setRecordingPausedAndUpdateUI(false);
		}
	}

	protected void setRecordingPausedAndUpdateUI(boolean b) {
		IFieldInfo recordingPausedField = getSwingRenderer().getFormAwareField(recordingManagementForm,
				"recordingPaused");
		recordingPausedField.setValue(recordingManagement, b);
	}

	protected AbstractAction openTestActionSelectionWindow(AWTEvent event, Window parent) {
		final Component c = (Component) event.getSource();
		DefaultMutableTreeNode options = new DefaultMutableTreeNode();
		addPauseRecordingOption(options, c);
		addStopRecordingOption(options, c);
		addInspectComponentOption(options, c);
		addTestActionOptions(options, c, event);
		String title = getSwingRenderer().getObjectTitle(tester);
		DefaultTreeModel treeModel = new DefaultTreeModel(options);
		final TreeSelectionDialog dialog = new TreeSelectionDialog(parent, title, null, treeModel,
				getTestActionMenuItemTextAccessor(), getTestActionMenuItemIconAccessor(),
				getTestActionMenuItemSelectableAccessor(), true, ModalityType.APPLICATION_MODAL) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Container createContentPane(String message) {
				return TesterUI.getAlternateWindowDecorationsContentPane(this, super.createContentPane(message),
						TesterUI.this);
			}

		};
		dialog.setVisible(true);
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) dialog.getSelection();
		if (selected == null) {
			return null;
		}
		return (AbstractAction) selected.getUserObject();
	}

	protected JFrame getTesterWindow() {
		if (testerForm == null) {
			return null;
		}
		return (JFrame) SwingUtilities.getWindowAncestor(testerForm);
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
				stopRecording();
			}
		});
		options.add(stopRecordingItem);
	}

	protected void addTestActionOptions(DefaultMutableTreeNode options, final Component c, AWTEvent event) {
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
					handleTestActionInsertionRequest(testAction, c, true);
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

	protected boolean handleTestActionInsertionRequest(final TestAction testAction, final Component c, boolean execute) {
		if (openRecordingSettingsWindow(testAction, c)) {
			final List<TestAction> newTestActionListValue = new ArrayList<TestAction>(
					Arrays.asList(tester.getTestActions()));
			if (recordingInsertedAfterSelection) {
				int index = getSelectedActionIndex();
				if (index != -1) {
					newTestActionListValue.add(index + 1, testAction);
				} else {
					newTestActionListValue.add(testAction);
				}
			} else {
				newTestActionListValue.add(testAction);
			}
			IFieldInfo testActionListField = getSwingRenderer().getFormAwareField(testerForm, "testActions");
			testActionListField.setValue(tester,
					newTestActionListValue.toArray(new TestAction[newTestActionListValue.size()]));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					selectTestAction(testAction);
				}
			});
			tester.handleCurrentComponentChange(null);
			if (execute) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new AssertionError(e);
						}
						testAction.execute(c, tester);
					}
				});
			}
			return true;
		}
		return false;
	}

	protected SwingRenderer createSwingRenderer() {
		SwingCustomizer result = new SwingCustomizer(this, infoCustomizations, getAlternateCustomizationsFilePath()) {

			@Override
			protected boolean areCustomizationsEditable(Object object) {
				return getAlternateCustomizationsFilePath() != null;
			}

			@Override
			public DialogBuilder createDialogBuilder(Component activatorComponent) {
				return new DialogBuilder(this, activatorComponent) {

					@Override
					public JDialog build() {
						JDialog result = super.build();
						result.setModalityType(ModalityType.APPLICATION_MODAL);
						return result;
					}

				};
			}

			@Override
			public Container createWindowContentPane(Window window, Component content,
					List<? extends Component> toolbarControls) {
				Container result = super.createWindowContentPane(window, content, toolbarControls);
				return TesterUI.getAlternateWindowDecorationsContentPane(window, result, TesterUI.this);
			}

			@Override
			public Object onTypeInstanciationRequest(Component activatorComponent, ITypeInfo type, boolean silent) {
				Object result = super.onTypeInstanciationRequest(activatorComponent, type, silent);
				if (result instanceof ComponentFinder) {
					if (componentFinderInitializationSource != null) {
						((ComponentFinder) result).initializeFrom(componentFinderInitializationSource, TesterUI.this);
					}
				}
				return result;
			}

			@Override
			public MethodAction createMethodAction(Object object, IMethodInfo method) {
				return new MethodAction(swingRenderer, object, method) {
					protected static final long serialVersionUID = 1L;

					@Override
					protected boolean onMethodInvocationRequest(Component activatorComponent) {
						if ((object instanceof Tester) && method.getCaption().equals("Start Recording")) {
							ListControl testActionsControl = getTestActionsControl();
							if (testActionsControl.getSelection().size() == 1) {
								String insertMessage = "Insert Recordings After The Current Selection Row";
								String doNotInsertMessage = "Insert Recordings At The End";
								String answer = getSwingRenderer().openSelectionDialog(testActionsControl,
										Arrays.asList(insertMessage, doNotInsertMessage), insertMessage, "Choose",
										null);
								if (insertMessage.equals(answer)) {
									recordingInsertedAfterSelection = true;
								} else if (doNotInsertMessage.equals(answer)) {
									recordingInsertedAfterSelection = false;
								} else {
									return false;
								}
							}
						}
						return super.onMethodInvocationRequest(activatorComponent);
					}

				};
			}

			@Override
			public JPanel createForm(Object object, IInfoFilter infoFilter) {
				JPanel result = super.createForm(object, infoFilter);
				if (object == tester) {
					if (testerForm != null) {
						throw new AssertionError("Tester form cannot be created more than 1 time");
					}
					testerForm = result;
				}
				return result;
			}

		};
		return result;
	}

	protected boolean isTesterWindow(Window window) {
		for (JPanel form : SwingRendererUtils.findDescendantForms(window, getSwingRenderer())) {
			if (getSwingRenderer().getObjectByForm().get(form) instanceof Tester) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		ITypeInfo result = super.getTypeInfo(typeSource);
		result = new TypeInfoProxyFactory() {

			@Override
			public String toString() {
				return TesterUI.class.getName() + TypeInfoProxyFactory.class.getSimpleName();
			}

			@Override
			protected String getCaption(ITypeInfo type) {
				if (type.getName().equals(CtrlA.class.getName())) {
					return "CtrlA (Select All)";
				} else if (type.getName().equals(CtrlC.class.getName())) {
					return "CtrlC (Copy)";
				} else if (type.getName().equals(CtrlV.class.getName())) {
					return "CtrlV (Paste)";
				} else if (type.getName().equals(CtrlX.class.getName())) {
					return "CtrlX (Cut)";
				} else {
					return super.getCaption(type);
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if (type.getName().equals(PropertyBasedComponentFinder.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					ITypeInfo propertyValueType = getTypeInfo(new JavaTypeInfoSource(PropertyValue.class));
					result.add(new ImplicitListField(TesterUI.this, "propertyValues", type, propertyValueType,
							"createPropertyValue", "getPropertyValue", "addPropertyValue", "removePropertyValue",
							"propertyValueCount"));
					return result;
				} else {
					return super.getFields(type);
				}
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (type.getName().equals(Tester.class.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					IMethodInfo replayAllMethod;
					while ((replayAllMethod = ReflectionUIUtils.findInfoByName(result, "replayAll")) != null) {
						result.remove(replayAllMethod);
					}
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						@Override
						public String getName() {
							return "startRecording";
						}

						@Override
						public String getCaption() {
							return "Start Recording";
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							startRecording();
							return null;
						}
					});
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						@Override
						public String getName() {
							return "replayAll";
						}

						@Override
						public String getCaption() {
							return "Replay All";
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							startReplay(Arrays.asList(tester.getTestActions()));
							return null;
						}

					});
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						@Override
						public String getName() {
							return "inspectComponents";
						}

						@Override
						public String getCaption() {
							return "Inspect Component(s)";
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							startComponentInspectionMode();
							return null;
						}
					});
					return result;
				} else {
					return super.getMethods(type);
				}
			}

			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				if (type.getName().equals(TestAction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getTestActionClasses()) {
						result.add(getTypeInfo(new JavaTypeInfoSource(clazz)));
					}
					return result;
				} else if (type.getName().equals(ComponentFinder.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getComponentFinderClasses()) {
						result.add(getTypeInfo(new JavaTypeInfoSource(clazz)));
					}
					return result;
				} else if (type.getName().equals(KeyboardInteraction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getKeyboardInteractionClasses()) {
						result.add(getTypeInfo(new JavaTypeInfoSource(clazz)));
					}
					return result;
				} else {
					return super.getPolymorphicInstanceSubTypes(type);
				}
			}

			@Override
			protected List<AbstractListAction> getDynamicActions(IListTypeInfo listType,
					final List<? extends ItemPosition> selection) {
				if ((listType.getItemType() != null)
						&& TestAction.class.getName().equals(listType.getItemType().getName())) {
					if (selection.size() > 0) {
						List<AbstractListAction> result = new ArrayList<AbstractListAction>();
						result.add(new AbstractListAction() {

							@Override
							public String getName() {
								return "replay";
							}

							@Override
							public String getCaption() {
								return "Replay";
							}

							@Override
							public Object invoke(Object object, InvocationData invocationData) {
								try {
									List<TestAction> selectedActions = new ArrayList<TestAction>();
									for (ItemPosition itemPosition : selection) {
										TestAction testAction = (TestAction) itemPosition.getItem();
										selectedActions.add(testAction);
									}
									startReplay(selectedActions);
									return null;
								} catch (Exception e) {
									throw new ReflectionUIError(e);
								}
							}

						});
						if (selection.size() == 1) {
							result.add(new AbstractListAction() {

								@Override
								public Object invoke(Object object, InvocationData invocationData) {
									try {
										List<TestAction> actionsToReplay = new ArrayList<TestAction>();
										ItemPosition singleSelection = selection.get(0);
										for (int i = singleSelection.getIndex(); i < singleSelection
												.getContainingListRawValue().length; i++) {
											TestAction testAction = (TestAction) singleSelection.getSibling(i)
													.getItem();
											actionsToReplay.add(testAction);
										}
										startReplay(actionsToReplay);
										return null;
									} catch (Exception e) {
										throw new ReflectionUIError(e);
									}
								}

								@Override
								public String getName() {
									return "resume";
								}

								@Override
								public String getCaption() {
									return "Resume";
								}
							});
						}
						return result;
					}
				}
				return super.getDynamicActions(listType, selection);
			}

		}.get(result);
		return infoCustomizations.get(this, result);
	}

	protected ListControl getTestActionsControl() {
		if (testerForm == null) {
			return null;
		}
		List<FieldControlPlaceHolder> result = getSwingRenderer().getFieldControlPlaceHoldersByName(testerForm,
				"testActions");
		if (result.size() != 1) {
			throw new AssertionError("'testActions' control not found for: " + tester);
		}
		Component c = result.get(0).getFieldControl();
		if (c instanceof NullableControl) {
			c = ((NullableControl) c).getSubControl();
		}
		return (ListControl) c;
	}

	protected void openComponentInspector(Component c) {
		ComponentInspector inspector = new ComponentInspector(c, this);
		getSwingRenderer().openObjectDialog(recordingManagementWindow, inspector);
	}

	protected boolean openRecordingSettingsWindow(TestAction testAction, Component c) {
		componentFinderInitializationSource = c;
		ObjectDialogBuilder dialogStatus = getSwingRenderer().openObjectDialog(recordingManagementWindow, testAction,
				getSwingRenderer().getObjectTitle(testAction), getSwingRenderer().getObjectIconImage(testAction), true,
				false);
		componentFinderInitializationSource = null;
		if (dialogStatus.isOkPressed()) {
			return true;
		} else {
			dialogStatus.getModificationStack().undoAll();
			return false;
		}
	}

	protected void selectTestAction(TestAction testAction) {
		ListControl testActionsControl = getTestActionsControl();
		if (testActionsControl == null) {
			return;
		}
		testActionsControl.setSingleSelection(testActionsControl.findItemPosition(testAction));
	}

	public int getSelectedActionIndex() {
		ListControl testActionsControl = getTestActionsControl();
		AutoFieldValueUpdatingItemPosition result = testActionsControl.getSingleSelection();
		if (result == null) {
			return -1;
		}
		return result.getIndex();
	}

	protected void addInspectComponentOption(DefaultMutableTreeNode options, final Component c) {
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
				openComponentInspector(c);
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
						tester.handleCurrentComponentChange(null);
						setRecordingPausedAndUpdateUI(true);
						new Thread(Tester.class.getSimpleName() + " Restarter") {
							@Override
							public void run() {
								try {
									sleep(5000);
								} catch (InterruptedException e) {
									throw new AssertionError(e);
								}
								setRecordingPausedAndUpdateUI(false);
							}
						}.start();
					}
				});
		options.add(pauseItem);
	}

	protected INodePropertyAccessor<Icon> getTestActionMenuItemIconAccessor() {
		return new INodePropertyAccessor<Icon>() {

			DefaultTreeCellRenderer treeCellRenderer = new DefaultTreeCellRenderer();

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

	protected boolean isComponentIntrospectionEvent(AWTEvent event) {
		if (CloseWindowAction.matchIntrospectionRequestEvent(event)) {
			return true;
		}
		if (ClickOnMenuItemAction.matchIntrospectionRequestEvent(event)) {
			return true;
		}
		if (TargetComponentTestAction.matchIntrospectionRequestEvent(event)) {
			return true;
		}
		return false;
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

	protected List<TestAction> getPossibleTestActions(Component c, AWTEvent event) {
		try {
			List<TestAction> result = new ArrayList<TestAction>();
			for (Class<?> testActionClass : getTestActionClasses()) {
				TestAction testAction = (TestAction) testActionClass.newInstance();
				if (testAction.initializeFrom(c, event, this)) {
					result.add(testAction);
				}
			}
			return result;
		} catch (Exception e) {
			throw new AssertionError(e);
		}

	}

	protected static AlternateWindowDecorationsContentPane getAlternateWindowDecorationsContentPane(Window window,
			Component initialContentPane, final TesterUI testerUI) {
		AlternateWindowDecorationsContentPane result = new AlternateWindowDecorationsContentPane(
				SwingRendererUtils.getWindowTitle(window), window, initialContentPane) {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getDecorationsBackgroundColor() {
				return testerUI.getDecorationsBackgroundColor();
			}

			@Override
			public Color getDecorationsForegroundColor() {
				return testerUI.getDecorationsForegroundColor();
			}

			@Override
			public void configureWindow(Window window) {
				super.configureWindow(window);
				testerUI.onTesterWindowCreation(window);
			}

		};
		return result;
	}

	protected void onTesterWindowCreation(Window window) {
		BY_WINDOW.put(window, this);
		if (isTesterWindow(window)) {
			((JFrame) window).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			((JFrame) window).setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		}
	}

	protected boolean isRecording() {
		return recordingManagementWindow != null;
	}

	protected void startRecording() {
		if (isRecording()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switchToRecordingControlWindow(true);
			}

		});
	}

	protected void stopRecording() {
		if (!isRecording()) {
			return;
		}
		recordingManagementWindow.dispose();
	}

	protected boolean isReplaying() {
		return replayManagementWindow != null;
	}

	protected void startReplay(final List<TestAction> selectedActions) {
		if (isReplaying()) {
			return;
		}
		replayManagement.setActionsToReplay(selectedActions);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switchToReplayWindow(true);
			}

		});
	}

	protected void stopReplay() {
		if (!isReplaying()) {
			return;
		}
		replayManagementWindow.dispose();
	}

	protected boolean isInComponentInspectionMode() {
		return componentInspectionModeManagementWindow != null;
	}

	protected void startComponentInspectionMode() {
		if (isInComponentInspectionMode()) {
			return;
		}
		switchToComponentInspectionWindow(true);
	}

	protected void stopComponentInspectionMode() {
		if (!isInComponentInspectionMode()) {
			return;
		}
		componentInspectionModeManagementWindow.dispose();
	}

	protected void switchToComponentInspectionWindow(boolean b) {
		if (b == isInComponentInspectionMode()) {
			return;
		}
		final JFrame testerWindow = getTesterWindow();
		if (b) {
			componentInspectionModeManagementWindow = new JFrame() {
				private static final long serialVersionUID = 1L;

				{
					setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					JPanel form = getSwingRenderer().createForm(componentInspectionModeManagement);
					getSwingRenderer().setupWindow(this, form, null,
							getSwingRenderer().getObjectTitle(componentInspectionModeManagement),
							testerWindow.getIconImage());
					addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							componentInspectionModeManagement.quit();
						}

					});
				}

				@Override
				public void dispose() {
					super.dispose();
					tester.handleCurrentComponentChange(null);
					testerWindow.setLocation(componentInspectionModeManagementWindow.getLocation());
					componentInspectionModeManagementWindow = null;
					testerWindow.setVisible(true);
				}
			};
			testerWindow.setVisible(false);
			componentInspectionModeManagementWindow.setLocation(testerWindow.getLocation());
			componentInspectionModeManagementWindow.setVisible(true);
		} else {
			TestingUtils.sendWindowClosingEvent(componentInspectionModeManagementWindow);
		}
	}

	protected void switchToRecordingControlWindow(boolean b) {
		if (b == isRecording()) {
			return;
		}
		final JFrame testerWindow = getTesterWindow();
		if (b) {
			recordingManagementWindow = new JFrame() {
				private static final long serialVersionUID = 1L;

				{
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					recordingManagementForm = getSwingRenderer().createForm(recordingManagement);
					getSwingRenderer().setupWindow(this, recordingManagementForm, null,
							getSwingRenderer().getObjectTitle(recordingManagement), testerWindow.getIconImage());
				}

				@Override
				public void dispose() {
					super.dispose();
					tester.handleCurrentComponentChange(null);
					testerWindow.setLocation(recordingManagementWindow.getLocation());
					recordingManagementWindow = null;
					testerWindow.setVisible(true);
				}

			};
			testerWindow.setVisible(false);
			recordingManagementWindow.setLocation(testerWindow.getLocation());
			recordingManagementWindow.setVisible(true);
		} else {
			TestingUtils.sendWindowClosingEvent(recordingManagementWindow);
		}
	}

	protected void switchToReplayWindow(boolean b) {
		if (b == isReplaying()) {
			return;
		}
		final JFrame testerWindow = getTesterWindow();
		if (b) {
			replayManagementWindow = new JFrame() {
				private static final long serialVersionUID = 1L;

				{
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					JPanel form = getSwingRenderer().createForm(replayManagement);
					getSwingRenderer().setupWindow(this, form, null,
							getSwingRenderer().getObjectTitle(replayManagement), testerWindow.getIconImage());
					addWindowListener(new WindowAdapter() {
						@Override
						public void windowOpened(WindowEvent e) {
							replayManagement.startReplayThread();
						}

						@Override
						public void windowClosing(WindowEvent e) {
							replayManagement.stopReplayThread();
						}

					});
				}

				@Override
				public void dispose() {
					super.dispose();
					testerWindow.setLocation(replayManagementWindow.getLocation());
					replayManagementWindow = null;
					testerWindow.setVisible(true);
				}
			};
			testerWindow.setVisible(false);
			replayManagementWindow.setLocation(testerWindow.getLocation());
			replayManagementWindow.setVisible(true);
		} else {
			TestingUtils.sendWindowClosingEvent(replayManagementWindow);
		}
	}

	protected int indexOfACtion(TestAction testAction) {
		int i = 0;
		for (TestAction a : tester.getTestActions()) {
			if (a == testAction) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public class RecordingManagement {

		private boolean recordingPaused = false;

		public void stopRecording() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					switchToRecordingControlWindow(false);
				}
			});
		}

		public boolean isRecordingPaused() {
			return recordingPaused;
		}

		public void setRecordingPaused(boolean b) {
			tester.handleCurrentComponentChange(null);
			this.recordingPaused = b;
		}

	}

	public class ReplayManagement {

		protected String currentActionDescription;
		protected List<TestAction> actionsToReplay;
		protected Thread replayThread;

		public String getCurrentActionDescription() {
			return currentActionDescription;
		}

		protected void setActionsToReplay(List<TestAction> actionsToReplay) {
			this.actionsToReplay = actionsToReplay;
			currentActionDescription = "<initializing...>";
		}

		public void stop() {
			switchToReplayWindow(false);
		}

		protected void startReplayThread() {
			replayThread = new Thread(TesterUI.class.getName() + " Replay Thread") {
				@Override
				public void run() {
					try {
						Listener<TestAction> listener = new Listener<TestAction>() {
							@Override
							public void handle(TestAction testAction) {
								currentActionDescription = testAction.toString();
								int actionPosition = indexOfACtion(testAction) + 1;
								currentActionDescription = actionPosition + " - " + currentActionDescription;
								for (JPanel form : getSwingRenderer().getForms(replayManagement)) {
									getSwingRenderer().refreshAllFieldControls(form, false);
								}
								selectTestAction(testAction);
							}

						};
						tester.replay(actionsToReplay, listener);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								getSwingRenderer().openMessageDialog(testerForm, "Action(s) replayed successfully",
										getSwingRenderer().getObjectTitle(tester), null);
								ReplayManagement.this.stop();
							}
						});
					} catch (final Throwable t) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								getSwingRenderer().handleExceptionsFromDisplayedUI(replayManagementWindow, t);
								ReplayManagement.this.stop();
							}
						});
					}
				}
			};
			replayThread.start();
		}

		protected void stopReplayThread() {
			replayThread.interrupt();
			try {
				replayThread.join();
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			}
		}

	}

	public class ComponentInspectionModeManagement {

		public String getDescription(){
			return "Choose component to inspect...";
		}
		
		public void quit() {
			switchToComponentInspectionWindow(false);
		}
		
		

	}

}
