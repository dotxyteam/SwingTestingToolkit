package xy.ui.testing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
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
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.SwingRenderer;
import xy.reflect.ui.SwingRenderer.NullableControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.ListControl.AutoUpdatingFieldItemPosition;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.InfoCollectionSettingsProxy;
import xy.reflect.ui.info.field.ImplicitListField;
import xy.reflect.ui.info.field.FieldInfoProxy;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.IListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.structure.ListStructuralInfoProxy;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.HiddenNullableFacetsTypeInfoProxyConfiguration;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.custom.BooleanTypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
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
import xy.ui.testing.action.component.combobox.SelectComboBoxItemAction;
import xy.ui.testing.action.component.property.ChangeComponentPropertyAction;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.action.component.table.ClickOnTableCellAction;
import xy.ui.testing.action.component.table.SelectTableRowAction;
import xy.ui.testing.action.component.treetable.ExpandTreetTableToItemAction;
import xy.ui.testing.action.window.CheckWindowVisibleStringsAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.MenuItemComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.VisibleStringComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;
import xy.ui.testing.util.AlternateWindowDecorationsPanel;
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

	public static final WeakHashMap<Window, TesterUI> BY_WINDOW = new WeakHashMap<Window, TesterUI>();
	public static final WeakHashMap<TesterUI, Tester> TESTERS = new WeakHashMap<TesterUI, Tester>();
	public static final TesterUI DEFAULT = new TesterUI(Tester.DEFAULT);

	protected static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	protected Component componentFinderInitializationSource;
	protected Map<String, Image> imageCache = new HashMap<String, Image>();
	protected boolean recordingInsertedAfterSelection = false;
	protected Tester tester;
	protected AWTEventListener recordingListener;
	protected boolean recording = false;

	public TesterUI(Tester tester) {
		this.tester = tester;
		TESTERS.put(this, tester);
		recordingListener = new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				awtEventDispatched(event);
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(recordingListener, AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK + AWTEvent.WINDOW_EVENT_MASK);

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		TestingUtils.removeAWTEventListener(recordingListener);
	}

	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				throw new Exception("Invalid command line arguments. Expected: [<fileName>]");
			} else if (args.length == 1) {
				String fileName = args[0];
				DEFAULT.tester.loadFromFile(new File(fileName));
			}
			DEFAULT.getSwingRenderer().openObjectFrame(DEFAULT.tester);
		} catch (Throwable t) {
			DEFAULT.getSwingRenderer().handleExceptionsFromDisplayedUI(null, t);
		}
	}

	public Tester getTester() {
		return tester;
	}

	public Class<?>[] getTestActionClasses() {
		return new Class[] { CallMainMethodAction.class, WaitAction.class, ExpandTreetTableToItemAction.class,
				SelectComboBoxItemAction.class, SelectTableRowAction.class, ClickOnTableCellAction.class,
				ClickOnMenuItemAction.class, ClickAction.class, SendKeysAction.class, CloseWindowAction.class,
				ChangeComponentPropertyAction.class, CheckComponentPropertyAction.class,
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
		if (!recording) {
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
		if (isComponentIntrospectionRequestEvent(event)) {
			handleComponentIntrospectionRequest(event);
		}
	}

	protected void handleComponentIntrospectionRequest(final AWTEvent event) {
		if (CloseWindowAction.matchIntrospectionRequestEvent(event)) {
			WindowEvent windowEvent = (WindowEvent) event;
			Window window = windowEvent.getWindow();
			stopRecording();
			String title = getObjectKind(tester);
			if (getSwingRenderer().openQuestionDialog(window, "Do you want to record this window closing event?",
					title)) {
				startRecording();
				CloseWindowAction closeAction = new CloseWindowAction();
				closeAction.initializeFrom(window, event, this);
				onTestActionRecordingRequest(closeAction, window, false);
			} else {
				startRecording();
			}
		} else if (ClickOnMenuItemAction.matchIntrospectionRequestEvent(event)) {
			final JMenuItem menuItem = (JMenuItem) event.getSource();
			stopRecording();
			ClickOnMenuItemAction testACtion = new ClickOnMenuItemAction();
			testACtion.initializeFrom(menuItem, event, this);
			String title = getObjectKind(tester);
			if (getSwingRenderer().openQuestionDialog(getTesterWindow(),
					"Do you want to record this menu item activation event?", title)) {
				startRecording();
				onTestActionRecordingRequest(testACtion, menuItem, true);
			} else {
				startRecording();
			}
			return;
		} else {
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
	}

	protected AbstractAction openTestActionMenu(DefaultMutableTreeNode menuRoot) {
		String title = getObjectKind(tester);
		DefaultTreeModel treeModel = new DefaultTreeModel(menuRoot);
		final TreeSelectionDialog dialog = new TreeSelectionDialog(getTesterWindow(), title, null, treeModel,
				getTestActionMenuItemTextAccessor(), getTestActionMenuItemIconAccessor(),
				getTestActionMenuItemSelectableAccessor(), true, ModalityType.DOCUMENT_MODAL) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Container createContentPane(String message) {
				Container result = super.createContentPane(message);
				AlternateWindowDecorationsPanel decorationsPanel = TesterUI.getAlternateWindowDecorationsPanel(this,
						TesterUI.this);
				decorationsPanel.configureWindow(this);
				decorationsPanel.getContentPanel().add(result);
				result = decorationsPanel;
				return result;
			}

		};
		dialog.setVisible(true);
		DefaultMutableTreeNode selected = (DefaultMutableTreeNode) dialog.getSelection();
		if (selected == null) {
			return null;
		}
		return (AbstractAction) selected.getUserObject();
	}

	protected Window getTesterWindow() {
		final JPanel testerForm = ReflectionUIUtils.getKeysFromValue(getSwingRenderer().getObjectByForm(), this).get(0);
		if (testerForm == null) {
			return null;
		}
		return SwingUtilities.getWindowAncestor(testerForm);
	}

	protected void createStopRecordingMenuItem(DefaultMutableTreeNode root, Component c) {
		DefaultMutableTreeNode stopRecordingItem = new DefaultMutableTreeNode(new AbstractAction("Stop Recording") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getSwingRenderer().getFormUpdatingMethod(tester, "stopRecording").invoke(tester, new InvocationData());
			}
		});
		root.add(stopRecordingItem);
	}

	protected void createTestActionMenuItems(DefaultMutableTreeNode root, final Component c, AWTEvent event) {
		DefaultMutableTreeNode recordGroup = new DefaultMutableTreeNode("Execute And Record");
		root.add(recordGroup);
		DefaultMutableTreeNode actionsGroup = new DefaultMutableTreeNode("Actions");
		DefaultMutableTreeNode assertionssGroup = new DefaultMutableTreeNode("Assertion");
		for (final TestAction testAction : getPossibleTestActions(c, event)) {
			String testActionTypeName = getObjectKind(testAction).replaceAll(" Action$", "");
			DefaultMutableTreeNode item = new DefaultMutableTreeNode(new AbstractAction(testActionTypeName) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					onTestActionRecordingRequest(testAction, c, true);
				}

				@Override
				public Object getValue(String key) {
					if (key == AbstractAction.SMALL_ICON) {
						Image image = getIconImage(testAction);
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

	protected boolean onTestActionRecordingRequest(final TestAction testAction, final Component c, boolean execute) {
		if (openSettings(testAction, c, tester)) {
			final List<TestAction> newTestActionListValue = new ArrayList<TestAction>(
					Arrays.asList(tester.getTestActions()));
			if (recordingInsertedAfterSelection) {
				int index = getSelectedActionIndex(tester);
				if (index != -1) {
					newTestActionListValue.add(index + 1, testAction);
				} else {
					newTestActionListValue.add(testAction);
				}
			} else {
				newTestActionListValue.add(testAction);
			}
			IFieldInfo testActionListField = getSwingRenderer().getFormUpdatingField(tester, "testActions");
			testActionListField.setValue(tester,
					newTestActionListValue.toArray(new TestAction[newTestActionListValue.size()]));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					selectTestAction(testAction, tester);
				}
			});
			tester.handleCurrentComponentChange(null);
			if (execute) {
				testAction.execute(c, tester);
			}
			return true;
		}
		return false;
	}

	protected void playActionsAndUpdateUI(final Tester tester, List<TestAction> selectedActions) {
		String methodSignature;
		try {
			methodSignature = ReflectionUIUtils
					.getJavaMethodInfoSignature(Tester.class.getMethod("play", List.class, Listener.class));
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		IMethodInfo playMethod = getSwingRenderer().getFormUpdatingMethod(tester, methodSignature);
		Listener<TestAction> selectingListener = new Listener<TestAction>() {
			@Override
			public void handle(TestAction event) {
				TesterUI.this.beforeEachAction(event, tester);
			}
		};
		playMethod.invoke(tester, new InvocationData(selectedActions, selectingListener));
	}

	@Override
	public SwingRenderer createSwingRenderer() {
		return new SwingRenderer(TesterUI.this) {

			@Override
			public Container createWindowContentPane(Window window, Component content,
					List<? extends Component> toolbarControls) {
				Container result = super.createWindowContentPane(window, content, toolbarControls);
				AlternateWindowDecorationsPanel decorationsPanel = getAlternateWindowDecorationsPanel(window,
						TesterUI.this);
				decorationsPanel.configureWindow(window);
				decorationsPanel.getContentPanel().add(result);
				result = decorationsPanel;
				return result;
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
			public boolean onMethodInvocationRequest(Component activatorComponent, Object object, IMethodInfo method,
					Object[] returnValueArray) {
				if ((object instanceof Tester) && method.getName().equals("startRecording")) {
					Tester tester = (Tester) object;
					ListControl testActionsControl = getTestActionsControl(tester);
					if (testActionsControl.getSelection().size() == 1) {
						String insertMessage = "Insert Recordings After The Current Selection Row";
						String doNotInsertMessage = "Insert Recordings At The End";
						String answer = getSwingRenderer().openSelectionDialog(testActionsControl,
								Arrays.asList(insertMessage, doNotInsertMessage), insertMessage, "Choose", null);
						if (insertMessage.equals(answer)) {
							recordingInsertedAfterSelection = true;
						} else if (doNotInsertMessage.equals(answer)) {
							recordingInsertedAfterSelection = false;
						} else {
							return false;
						}
					}
				}
				return super.onMethodInvocationRequest(activatorComponent, object, method, returnValueArray);
			}

			@Override
			public JFrame createFrame(Component content, String title, Image iconImage,
					List<? extends Component> toolbarControls) {
				JFrame result = super.createFrame(content, title, iconImage, toolbarControls);
				for (JPanel form : SwingRendererUtils.findDescendantForms(result, TesterUI.this)) {
					if (getSwingRenderer().getObjectByForm().get(form) instanceof Tester) {
						result.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						result.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
					}
				}
				return result;
			}

			@Override
			public JPanel createObjectForm(Object object, IInfoCollectionSettings settings) {
				settings = new InfoCollectionSettingsProxy(settings) {

					@Override
					public boolean excludeMethod(IMethodInfo method) {
						if (method.getName().equals("execute")) {
							return true;
						}
						if (method.getName().equals("findComponent")) {
							return true;
						}
						if (method.getName().equals("find")) {
							return true;
						}
						if (method.getName().equals("initialize")) {
							return true;
						}
						if (method.getName().equals("initializeFrom")) {
							return true;
						}
						if (method.getName().equals("extractVisibleString")) {
							return true;
						}
						if (method.getName().equals("getKeyEvents")) {
							return true;
						}
						if (method.getName().equals("play")) {
							return true;
						}
						if (method.getName().equals("collectVisibleStrings")) {
							return true;
						}
						if (method.getName().equals("loadFromStream")) {
							return true;
						}
						if (method.getName().equals("saveToStream")) {
							return true;
						}
						if (method.getName().equals("assertSuccessfulReplay")) {
							return true;
						}
						if (method.getName().equals("matches")) {
							return true;
						}
						if (method.getName().equals("matches")) {
							return true;
						}
						if (method.getName().equals("matchIntrospectionRequestEvent")) {
							return true;
						}
						if (method.getName().equals("macthesComponent")) {
							return true;
						}
						if (method.getName().equals("setPropertyNames")) {
							return true;
						}
						if (method.getName().equals("removePropertyValue")) {
							return true;
						}
						if (method.getName().equals("addPropertyValue")) {
							return true;
						}
						if (method.getName().equals("getPropertyValue")) {
							return true;
						}
						if (method.getName().equals("createPropertyValue")) {
							return true;
						}
						if (method.getName().equals("setPropertyValue")) {
							return true;
						}
						if (method.getName().equals("findPropertyValue")) {
							return true;
						}
						return super.excludeMethod(method);
					}

					@Override
					public boolean excludeField(IFieldInfo field) {
						if (field.getName().equals("keyStrokes")) {
							return true;
						}
						if (field.getName().equals("valueDescription")) {
							return true;
						}
						if (field.getName().equals("componentInformation")) {
							return true;
						}
						if (field.getName().equals("propertyValueList")) {
							return true;
						}
						if (field.getName().equals("propertyValueCount")) {
							return true;
						}
						return super.excludeField(field);
					}

				};
				return super.createObjectForm(object, settings);
			}

		};
	}

	@Override
	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		return new HiddenNullableFacetsTypeInfoProxyConfiguration(TesterUI.this) {

			@Override
			protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
				if (field.getName().equals("checkThrownExceptionAfterSeconds")) {
					return true;
				} else if (field.getName().equals("knownOptions")) {
					return true;
				} else if (field.getName().equals("newPropertyValue")) {
					return true;
				} else if (field.getName().equals("propertyValueExpected")) {
					return true;
				} else {
					return super.isNullable(field, containingType);
				}
			}

			@Override
			protected String toString(ITypeInfo type) {
				if (type.getName().equals(CtrlA.class.getName())) {
					return "CtrlA (Select All)";
				} else if (type.getName().equals(CtrlC.class.getName())) {
					return "CtrlC (Copy)";
				} else if (type.getName().equals(CtrlV.class.getName())) {
					return "CtrlV (Paste)";
				} else if (type.getName().equals(CtrlX.class.getName())) {
					return "CtrlX (Cut)";
				} else {
					return super.toString(type);
				}
			}

			@Override
			protected Image getIconImage(ITypeInfo type, Object object) {
				String imageResourceName = type.getName();
				int lastDotIndex = imageResourceName.lastIndexOf(".");
				if (lastDotIndex != -1) {
					imageResourceName = imageResourceName.substring(lastDotIndex + 1);
				}
				imageResourceName += ".png";
				Image result = imageCache.get(imageResourceName);
				if (result == null) {
					if (TesterUI.class.getResource(imageResourceName) == null) {
						result = NULL_IMAGE;
					} else {
						try {
							result = ImageIO.read(TesterUI.class.getResourceAsStream(imageResourceName));
						} catch (IOException e) {
							throw new AssertionError(e);
						}
					}
					imageCache.put(imageResourceName, result);
				}
				if (result == NULL_IMAGE) {
					return super.getIconImage(type, object);
				}
				return result;
			}

			@Override
			protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
				ITypeInfo itemtype = type.getItemType();
				if (itemtype.getName().equals(TestAction.class.getName())) {
					return new ListStructuralInfoProxy(super.getStructuralInfo(type)) {

						@Override
						public String getCellValue(ItemPosition itemPosition, int columnIndex) {
							if (columnIndex == 0) {
								return Integer.toString(itemPosition.getIndex() + 1);
							} else {
								columnIndex--;
								return super.getCellValue(itemPosition, columnIndex);
							}
						}

						@Override
						public Image getCellIconImage(ItemPosition itemPosition, int columnIndex) {
							if (columnIndex == 1) {
								return super.getCellIconImage(itemPosition, 0);
							} else {
								return null;
							}
						}

						@Override
						public String getColumnCaption(int columnIndex) {
							if (columnIndex == 0) {
								return "N°";
							} else {
								columnIndex--;
								return super.getColumnCaption(columnIndex);
							}
						}

						@Override
						public int getColumnCount() {
							return super.getColumnCount() + 1;
						}

					};
				} else {
					return super.getStructuralInfo(type);
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if ((type instanceof DefaultTypeInfo) && type.getName().equals(Tester.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					result.add(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

						@Override
						public Object getValue(Object object) {
							return recording;
						}

						@Override
						public String getCaption() {
							return "Is Recording";
						}

					});
					result.add(new FieldInfoProxy(IFieldInfo.NULL_FIELD_INFO) {

						@Override
						public Object getValue(Object object) {
							return recordingInsertedAfterSelection;
						}

						@Override
						public void setValue(Object object, Object value) {
							recordingInsertedAfterSelection = (Boolean) value;
						}

						@Override
						public boolean isReadOnly() {
							return false;
						}

						@Override
						public boolean isNullable() {
							return false;
						}

						@Override
						public String getCaption() {
							return "Insert Recording(s) After Selection";
						}

					});
					return result;
				} else if ((type instanceof DefaultTypeInfo)
						&& type.getName().equals(PropertyBasedComponentFinder.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					result.add(new ImplicitListField(TesterUI.this, "propertyValues", type, "createPropertyValue",
							"getPropertyValue", "addPropertyValue", "removePropertyValue", "propertyValueCount"));
					return result;
				} else {
					return super.getFields(type);
				}
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if ((type instanceof DefaultTypeInfo) && type.getName().equals(Tester.class.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					IMethodInfo playAllMethod;
					while ((playAllMethod = ReflectionUIUtils.findInfoByName(result, "playAll")) != null) {
						result.remove(playAllMethod);
					}
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

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
						public String getCaption() {
							return "Stop Recording";
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							stopRecording();
							return null;
						}

					});
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						@Override
						public String getCaption() {
							return "Play All";
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							playActionsAndUpdateUI(tester, Arrays.asList(tester.getTestActions()));
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
						try {
							result.add(getTypeInfo(getTypeInfoSource(clazz.newInstance())));
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
					}
					return result;
				}
				if (type.getName().equals(ComponentFinder.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getComponentFinderClasses()) {
						try {
							ComponentFinder newInstance = (ComponentFinder) clazz.newInstance();
							if (componentFinderInitializationSource != null) {
								if (newInstance.initializeFrom(componentFinderInitializationSource, TesterUI.this)) {
									result.add(getTypeInfo(getTypeInfoSource(newInstance)));
								}
							} else {
								result.add(getTypeInfo(getTypeInfoSource(newInstance)));
							}
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
					}
					return result;
				}
				if (type.getName().equals(KeyboardInteraction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getKeyboardInteractionClasses()) {
						try {
							result.add(getTypeInfo(getTypeInfoSource(clazz.newInstance())));
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
					}
					return result;
				}
				return super.getPolymorphicInstanceSubTypes(type);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
					InvocationData invocationData) {
				if (method.getName().startsWith("play")) {
					return ModificationStack.EMPTY_MODIFICATION;
				}
				if (method.getName().equals("startRecording")) {
					return ModificationStack.EMPTY_MODIFICATION;
				}
				if (method.getName().equals("stopRecording")) {
					return ModificationStack.EMPTY_MODIFICATION;
				}
				return super.getUndoModification(method, containingType, object, invocationData);
			}

			@Override
			protected List<IListAction> getSpecificListActions(IListTypeInfo type, final Object object,
					IFieldInfo field, final List<? extends ItemPosition> selection) {
				if ((object instanceof Tester) && (field.getName().equals("testActions"))) {
					if (selection.size() > 0) {
						List<IListAction> result = new ArrayList<IListAction>();
						result.add(new IListAction() {

							@Override
							public void perform(final Component listControl) {
								try {
									List<TestAction> selectedActions = new ArrayList<TestAction>();
									for (ItemPosition itemPosition : selection) {
										TestAction testAction = (TestAction) itemPosition.getItem();
										selectedActions.add(testAction);
									}
									Tester tester = (Tester) object;
									playActionsAndUpdateUI(tester, selectedActions);
								} catch (Exception e) {
									throw new ReflectionUIError(e);
								}
							}

							@Override
							public String getTitle() {
								return "Play Selected Action(s)";
							}
						});
						if (selection.size() == 1) {
							result.add(new IListAction() {

								@Override
								public void perform(final Component listControl) {
									try {
										List<TestAction> actionsToPlay = new ArrayList<TestAction>();
										ItemPosition singleSelection = selection.get(0);
										for (int i = singleSelection.getIndex(); i < singleSelection
												.getContainingListValue().length; i++) {
											TestAction testAction = (TestAction) singleSelection.getSibling(i)
													.getItem();
											actionsToPlay.add(testAction);
										}
										Tester tester = (Tester) object;
										playActionsAndUpdateUI(tester, actionsToPlay);
									} catch (Exception e) {
										throw new ReflectionUIError(e);
									}
								}

								@Override
								public String getTitle() {
									return "Play From Selection To End";
								}
							});
						}
						return result;
					}
				}
				return super.getSpecificListActions(type, object, field, selection);
			}

			@Override
			protected Object invoke(final Object object, final InvocationData invocationData, final IMethodInfo method,
					final ITypeInfo containingType) {
				if (containingType.getName().equals(Tester.class.getName())) {

					if (method.getName().equals("startRecording")) {
						final JPanel form = getTesterForm((Tester) object);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								SwingUtilities.getWindowAncestor(form).toBack();
							}
						});
					}

					if (method.getName().startsWith("play")) {
						if (isRecording()) {
							stopRecording();
						}
					}

					Object result = super.invoke(object, invocationData, method, containingType);

					return result;
				} else {
					return super.invoke(object, invocationData, method, containingType);
				}
			}

		}.get(super.getTypeInfo(typeSource));
	}

	protected void beforeEachAction(TestAction testAction, Tester tester) {
		selectTestAction(testAction, tester);
	}

	protected JPanel getTesterForm(Tester tester) {
		List<JPanel> result = getSwingRenderer().getForms(tester);
		if (result.size() == 0) {
			return null;
		}
		if (result.size() > 1) {
			throw new AssertionError("More than 1 form was found for: " + tester);
		}
		return result.get(0);
	}

	protected ListControl getTestActionsControl(Tester tester) {
		final JPanel form = getTesterForm(tester);
		if (form == null) {
			return null;
		}
		List<Component> result = getSwingRenderer().getFieldControlsByName(form, "testActions");
		if (result.size() != 1) {
			throw new AssertionError("'testActions' control not found for: " + tester);
		}
		Component c = result.get(0);
		if (c instanceof NullableControl) {
			c = ((NullableControl) c).getSubControl();
		}
		return (ListControl) c;
	}

	protected boolean openSettings(TestAction testAction, Component c, Tester tester) {
		componentFinderInitializationSource = c;
		boolean[] okPressedArray = new boolean[] { false };
		getSwingRenderer().openObjectDialog(c, testAction, getObjectKind(testAction), null, true, null, okPressedArray,
				null, null, IInfoCollectionSettings.DEFAULT);
		componentFinderInitializationSource = null;
		return okPressedArray[0];
	}

	protected void selectTestAction(TestAction testAction, Tester tester) {
		ListControl testActionsControl = getTestActionsControl(tester);
		if (testActionsControl == null) {
			return;
		}
		testActionsControl.setSingleSelection(testActionsControl.findItemPosition(testAction));
	}

	public int getSelectedActionIndex(Tester tester) {
		ListControl testActionsControl = getTestActionsControl(tester);
		AutoUpdatingFieldItemPosition result = testActionsControl.getSingleSelection();
		if (result == null) {
			return -1;
		}
		return result.getIndex();
	}

	protected void createReleaseComponentMenuItem(DefaultMutableTreeNode root, Component c) {
		DefaultMutableTreeNode pauseItem = new DefaultMutableTreeNode(
				new AbstractAction("Pause Recording (5 seconds)") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						tester.handleCurrentComponentChange(null);
						stopRecording();
						new Thread(Tester.class.getSimpleName() + " Restarter") {
							@Override
							public void run() {
								try {
									sleep(5000);
								} catch (InterruptedException e) {
									throw new AssertionError(e);
								}
								startRecording();
							}
						}.start();
					}
				});
		root.add(pauseItem);
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
					return treeCellRenderer.getOpenIcon();
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

	protected boolean isComponentIntrospectionRequestEvent(AWTEvent event) {
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

	public boolean isRecording() {
		return recording;
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
		tester.handleCurrentComponentChange(null);
		recording = false;
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

	protected static AlternateWindowDecorationsPanel getAlternateWindowDecorationsPanel(Window window,
			final TesterUI testerUI) {
		return new AlternateWindowDecorationsPanel(SwingRendererUtils.getWindowTitle(window)) {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getDecorationsBackgroundColor() {
				return Tester.HIGHLIGHT_FOREGROUND;
			}

			@Override
			public Color getDecorationsForegroundColor() {
				return Tester.HIGHLIGHT_BACKGROUND;
			}

			@Override
			public void configureWindow(Window window) {
				super.configureWindow(window);
				BY_WINDOW.put(window, testerUI);
			}

		};
	}

}
