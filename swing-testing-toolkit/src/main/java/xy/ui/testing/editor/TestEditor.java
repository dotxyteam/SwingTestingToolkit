package xy.ui.testing.editor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.swing.DialogBuilder;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.customizer.CustomizingMethodControlPlaceHolder;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ImplicitListFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.factory.GenericEnumerationFactory;
import xy.reflect.ui.info.type.factory.IInfoProxyFactory;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.reflect.ui.util.component.AlternativeWindowDecorationsPanel;
import xy.ui.testing.TestReport;
import xy.ui.testing.Tester;
import xy.ui.testing.action.CallMainMethodAction;
import xy.ui.testing.action.CheckNumberOfOpenWindowsAction;
import xy.ui.testing.action.SystemExitCallInterceptionAction;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.WaitAction;
import xy.ui.testing.action.component.CheckVisibleStringsAction;
import xy.ui.testing.action.component.ClickAction;
import xy.ui.testing.action.component.ClickOnMenuItemAction;
import xy.ui.testing.action.component.SendKeysAction;
import xy.ui.testing.action.component.SendKeysAction.KeyboardInteraction;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey;
import xy.ui.testing.action.component.SendKeysAction.WriteText;
import xy.ui.testing.action.component.property.ChangeComponentPropertyAction;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.action.component.specific.ClickOnTableCellAction;
import xy.ui.testing.action.component.specific.ExpandTreetTableToItemAction;
import xy.ui.testing.action.component.specific.SelectComboBoxItemAction;
import xy.ui.testing.action.component.specific.SelectTabAction;
import xy.ui.testing.action.component.specific.SelectTableRowAction;
import xy.ui.testing.action.window.CheckWindowVisibleStringsAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.MenuItemComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.DisplayedStringComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;
import xy.ui.testing.util.TestingUtils;

public class TestEditor extends JFrame {
	private static final long serialVersionUID = 1L;

	public static final String ALTERNATE_UI_CUSTOMIZATION_FILE_PATH_PROPERTY_KEY = "xy.ui.testing.gui.customizationFile";
	public static final WeakHashMap<TestEditor, Tester> TESTER_BY_EDITOR = new WeakHashMap<TestEditor, Tester>();
	public static final boolean DEBUG = Boolean
			.valueOf(System.getProperty(TestEditor.class.getName() + ".debug", "false"));

	public static final Class<?>[] BUILT_IN_TEST_ACTION_CLASSES = new Class[] { CallMainMethodAction.class,
			SystemExitCallInterceptionAction.class, WaitAction.class, ExpandTreetTableToItemAction.class,
			SelectComboBoxItemAction.class, SelectTableRowAction.class, SelectTabAction.class,
			ClickOnTableCellAction.class, ClickOnMenuItemAction.class, ClickAction.class, SendKeysAction.class,
			CheckVisibleStringsAction.class, CheckWindowVisibleStringsAction.class, ChangeComponentPropertyAction.class,
			CheckComponentPropertyAction.class, CloseWindowAction.class, CheckNumberOfOpenWindowsAction.class };
	public static final Class<?>[] BUILT_IN_COMPONENT_FINDRER_CLASSES = new Class[] {
			DisplayedStringComponentFinder.class, ClassBasedComponentFinder.class, PropertyBasedComponentFinder.class,
			MenuItemComponentFinder.class };
	public static final Class<?>[] BUILT_IN_KEYBOARD_INTERACTION_CLASSES = new Class[] { WriteText.class,
			SpecialKey.class };

	protected static final String TEST_ACTIONS_FIELD_NAME = "testActions";
	protected static final String TEST_REPORT_STEPS_FIELD_NAME = "steps";
	protected static final ResourcePath EXTENSION_IMAGE_PATH = SwingRendererUtils
			.putImageInCache(TestingUtils.loadImageResource("ExtensionAction.png"));

	protected Color decorationsForegroundColor = Tester.HIGHLIGHT_BACKGROUND;
	protected Color decorationsBackgroundColor = Tester.HIGHLIGHT_FOREGROUND;

	protected ReplayWindowSwitch replayWindowSwitch = new ReplayWindowSwitch(this);
	protected RecordingWindowSwitch recordingWindowSwitch = new RecordingWindowSwitch(this);
	protected ComponentInspectionWindowSwitch componentInspectionWindowSwitch = new ComponentInspectionWindowSwitch(
			this);

	protected SwingRenderer swingRenderer;
	protected CustomizedUI reflectionUI;
	protected InfoCustomizations infoCustomizations;
	protected Component componentFinderInitializationSource;

	protected Tester tester;
	protected TestReport testReport;
	protected MainObject mainObject = new MainObject();
	protected JPanel mainForm;

	protected AWTEventListener recordingListener;
	protected Set<Window> allWindows = Collections.newSetFromMap(new WeakHashMap<Window, Boolean>());

	private AWTEventListener modalityChangingListener;

	public TestEditor(Tester tester) {
		TESTER_BY_EDITOR.put(this, tester);
		this.tester = tester;
		this.testReport = new TestReport();
		setupWindowSwitchesEventHandling();
		preventDialogApplicationModality();
		infoCustomizations = createInfoCustomizations();
		reflectionUI = createTesterReflectionUI(infoCustomizations);
		swingRenderer = createSwingRenderer(reflectionUI);
		createControls();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		cleanupWindowSwitchesEventHandling();
		cleanupDialogApplicationModalityPrevention();
	}

	protected void setupWindowSwitchesEventHandling() {
		recordingListener = new AWTEventListener() {
			long currentComponentChangeDisabledUntil = 0;

			@Override
			public void eventDispatched(AWTEvent event) {
				boolean canBeRecordingEvent = recordingWindowSwitch.isActive() && !recordingWindowSwitch.isPaused();
				boolean canBeInspectingEvent = componentInspectionWindowSwitch.isActive()
						&& !componentInspectionWindowSwitch.isPaused()
						&& !componentInspectionWindowSwitch.isInspectorOpen();
				if (!canBeRecordingEvent && !canBeInspectingEvent) {
					return;
				}
				if (event == null) {
					getTester().handleCurrentComponentChange(null);
					return;
				}
				if (!(event.getSource() instanceof Component)) {
					return;
				}
				Component c = getComponent(event);
				if (!c.isShowing()) {
					return;
				}
				if (TestingUtils.isTestEditorComponent(TestEditor.this, c)) {
					return;
				}
				if (isCurrentComponentChangeEvent(event)) {
					if (System.currentTimeMillis() > currentComponentChangeDisabledUntil) {
						getTester().handleCurrentComponentChange(c);
					}
				}
				if (c != getTester().getCurrentComponent()) {
					if (isGenericRecordingRequestEvent(event)) {
						getTester().handleCurrentComponentChange(c);
						currentComponentChangeDisabledUntil = System.currentTimeMillis() + 5000;
					}
				} else {
					if (canBeInspectingEvent) {
						if (isGenericRecordingRequestEvent(event)) {
							componentInspectionWindowSwitch.getWindow().requestFocus();
							componentInspectionWindowSwitch.openComponentInspector(c,
									componentInspectionWindowSwitch.getWindow());
						}
					}
					if (canBeRecordingEvent) {
						if (isWindowClosingRecordingRequestEvent(event)) {
							recordingWindowSwitch.handleWindowClosingRecordingEvent(event);
						} else if (isMenuItemClickRecordingRequestEvent(event)) {
							recordingWindowSwitch.handleMenuItemClickRecordingEvent(event);
						} else if (isGenericRecordingRequestEvent(event)) {
							recordingWindowSwitch.handleGenericRecordingEvent(event);
						}
					}
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(recordingListener, AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK + AWTEvent.WINDOW_EVENT_MASK);

	}

	protected Component getComponent(AWTEvent event) {
		Component result = (Component) event.getSource();
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (mouseEvent.getID() == MouseEvent.MOUSE_MOVED) {
				Component actualEventSource = SwingUtilities.getDeepestComponentAt(result, mouseEvent.getX(),
						mouseEvent.getY());
				if (actualEventSource != null) {
					result = actualEventSource;
				}
			}
		}
		return result;
	}

	protected void cleanupWindowSwitchesEventHandling() {
		SwingRendererUtils.removeAWTEventListener(recordingListener);
	}

	protected void preventDialogApplicationModality() {
		modalityChangingListener = new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent event) {
				HierarchyEvent hierarchyEvent = (HierarchyEvent) event;
				if (hierarchyEvent.getID() == HierarchyEvent.HIERARCHY_CHANGED) {
					if ((hierarchyEvent.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
						if (event.getSource() instanceof Window) {
							Window window = (Window) event.getSource();
							if (window.isVisible() && tester.isTestable(window)) {
								if (tester.getEditingOptions().isTestableWindowsAlwaysOnTopFeatureDisabled()) {
									if (window.isAlwaysOnTop()) {
										window.setAlwaysOnTop(false);
									}
								}
								if (window instanceof Dialog) {
									Dialog dialog = (Dialog) window;
									if (tester.getEditingOptions().isTestableModalWindowsForcedToDocumentModality()) {
										if (dialog.isModal()) {
											dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
										}
									}
								}
							}
						}
					}
				}
			}
		};
		Toolkit.getDefaultToolkit().addAWTEventListener(modalityChangingListener, AWTEvent.HIERARCHY_EVENT_MASK);

	}

	protected void cleanupDialogApplicationModalityPrevention() {
		SwingRendererUtils.removeAWTEventListener(modalityChangingListener);
	}

	public void open() {
		setVisible(true);
	}

	public Tester getTester() {
		return tester;
	}

	public TestReport getTestReport() {
		return testReport;
	}

	public void setTestReport(TestReport testReport) {
		this.testReport = testReport;
		refresh();
	}

	public ReplayWindowSwitch getReplayWindowSwitch() {
		return replayWindowSwitch;
	}

	public RecordingWindowSwitch getRecordingWindowSwitch() {
		return recordingWindowSwitch;
	}

	public ComponentInspectionWindowSwitch getComponentInspectionWindowSwitch() {
		return componentInspectionWindowSwitch;
	}

	public Set<Window> getAllWindows() {
		return allWindows;
	}

	public void logDebug(String msg) {
		if (DEBUG) {
			System.out.println("[" + TestEditor.class.getName() + "] DEBUG - " + msg);
		}
	}

	public void logDebug(Throwable t) {
		logDebug(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	public void logError(String msg) {
		System.out.println("[" + TestEditor.class.getName() + "] ERROR - " + msg);
	}

	public void logError(Throwable t) {
		logError(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	protected void createControls() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		{
			this.mainForm = getSwingRenderer().createForm(mainObject);
			String title = getSwingRenderer().getObjectTitle(getTester());
			List<? extends Component> toolbarControls = getSwingRenderer().createFormCommonToolbarControls(mainForm);
			Image iconImage = getSwingRenderer().getObjectIconImage(getTester());
			getSwingRenderer().setupWindow(this, mainForm, toolbarControls, title, iconImage);
		}
	}

	public boolean isMenuItemClickRecordingRequestEvent(AWTEvent event) {
		return ClickOnMenuItemAction.matchesEvent(event);
	}

	public boolean isWindowClosingRecordingRequestEvent(AWTEvent event) {
		return CloseWindowAction.matchesEvent(event);
	}

	public boolean isGenericRecordingRequestEvent(AWTEvent event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED) {
				if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
					return true;
				}
			}
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

	protected ListControl getTestActionsControl() {
		JPanel testerForm = getTesterForm();
		if (testerForm == null) {
			return null;
		}
		FieldControlPlaceHolder fieldControlPlaceHolder = getSwingRenderer().getFieldControlPlaceHolder(testerForm,
				TEST_ACTIONS_FIELD_NAME);
		if (fieldControlPlaceHolder == null) {
			return null;
		}
		Component c = fieldControlPlaceHolder.getFieldControl();
		if (c instanceof NullableControl) {
			c = ((NullableControl) c).getSubControl();
		}
		return (ListControl) c;
	}

	protected ListControl getTestReportStepsControl() {
		JPanel testReportForm = getTestReportForm();
		if (testReportForm == null) {
			return null;
		}
		FieldControlPlaceHolder fieldControlPlaceHolder = getSwingRenderer().getFieldControlPlaceHolder(testReportForm,
				TEST_REPORT_STEPS_FIELD_NAME);
		if (fieldControlPlaceHolder == null) {
			return null;
		}
		Component c = fieldControlPlaceHolder.getFieldControl();
		if (c instanceof NullableControl) {
			c = ((NullableControl) c).getSubControl();
		}
		return (ListControl) c;
	}

	protected JPanel getTesterForm() {
		if (mainForm == null) {
			return null;
		}
		return SwingRendererUtils.findFirstObjectDescendantForm(tester, mainForm, getSwingRenderer());
	}

	protected JPanel getTestReportForm() {
		if (mainForm == null) {
			return null;
		}
		if (testReport == null) {
			return null;
		}
		return SwingRendererUtils.findFirstObjectDescendantForm(testReport, mainForm, getSwingRenderer());
	}

	public void refresh() {
		getSwingRenderer().refreshForm(mainForm, false);
	}

	public void showReportTab() {
		getSwingRenderer().setDisplayedInfoCategory(mainForm, "Report", -1);
		ListControl stepsControl = getTestReportStepsControl();
		if (stepsControl.getRootListSize() > 0) {
			BufferedItemPosition lastStepItemPosition = stepsControl
					.getRootListItemPosition(stepsControl.getRootListSize() - 1);
			stepsControl.setSingleSelection(lastStepItemPosition);
		}
	}

	public void showSpecificationTab() {
		getSwingRenderer().setDisplayedInfoCategory(mainForm, "Specification", -1);
		ListControl testActionsControl = getTestActionsControl();
		if (testActionsControl.getRootListSize() > 0) {
			BufferedItemPosition firstActionItemPosition = testActionsControl.getRootListItemPosition(0);
			testActionsControl.setSingleSelection(firstActionItemPosition);
		}
	}

	public void setTestActionsAndUpdateUI(TestAction[] testActions) {
		JPanel testerForm = getTesterForm();
		IFieldInfo testActionsField = getSwingRenderer().getFieldControlPlaceHolder(testerForm, TEST_ACTIONS_FIELD_NAME)
				.getField();
		ModificationStack modifStack = getSwingRenderer().getModificationStackByForm().get(testerForm);
		ReflectionUIUtils.setValueThroughModificationStack(new DefaultFieldControlData(getTester(), testActionsField),
				testActions, modifStack, testActionsField);
		refresh();
	}

	public int getSelectedActionIndex() {
		ListControl testActionsControl = getTestActionsControl();
		ItemPosition result = testActionsControl.getSingleSelection();
		if (result == null) {
			return -1;
		}
		return result.getIndex();
	}

	public void setSelectedActionIndex(int index) {
		ListControl testActionsControl = getTestActionsControl();
		if (testActionsControl == null) {
			return;
		}
		testActionsControl.setSingleSelection(testActionsControl.getRootListItemPosition(index));
	}

	protected void startRecording() {
		if (recordingWindowSwitch.isActive()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				recordingWindowSwitch.activate(true);
			}
		});
	}

	protected void startReplay(final List<TestAction> selectedActions) {
		if (replayWindowSwitch.isActive()) {
			return;
		}
		replayWindowSwitch.setActionsToReplay(selectedActions);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				replayWindowSwitch.activate(true);
			}

		});
	}

	public void setComponentFinderInitializationSource(Component componentFinderInitializationSource) {
		this.componentFinderInitializationSource = componentFinderInitializationSource;
	}

	protected InfoCustomizations createInfoCustomizations() {
		InfoCustomizations result = new InfoCustomizations();
		String alternateCustomizationFilePath = getAlternateCustomizationsFilePath();
		try {
			if (alternateCustomizationFilePath != null) {
				result.loadFromFile(new File(alternateCustomizationFilePath), null);
			} else {
				result.loadFromStream(Tester.class.getResourceAsStream("infoCustomizations.icu"), null);
			}
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		return result;
	}

	protected String getAlternateCustomizationsFilePath() {
		return System.getProperty(ALTERNATE_UI_CUSTOMIZATION_FILE_PATH_PROPERTY_KEY);
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

	public Class<?>[] getTestActionClasses() {
		return BUILT_IN_TEST_ACTION_CLASSES;
	}

	public Class<?>[] getComponentFinderClasses() {
		return BUILT_IN_COMPONENT_FINDRER_CLASSES;
	}

	public Class<?>[] getKeyboardInteractionClasses() {
		return BUILT_IN_KEYBOARD_INTERACTION_CLASSES;
	}

	protected SwingRenderer createSwingRenderer(CustomizedUI reflectionUI) {
		return new TestEditorSwingRenderer(reflectionUI);
	}

	protected CustomizedUI createTesterReflectionUI(InfoCustomizations infoCustomizations) {
		return new TestEditorReflectionUI(infoCustomizations);
	}

	protected static AlternativeWindowDecorationsPanel getAlternateWindowDecorationsContentPane(Window window,
			Component initialContentPane, final TestEditor testEditor) {
		AlternativeWindowDecorationsPanel result = new AlternativeWindowDecorationsPanel(
				SwingRendererUtils.getWindowTitle(window), window, initialContentPane) {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getDecorationsBackgroundColor() {
				return testEditor.getDecorationsBackgroundColor();
			}

			@Override
			public Color getDecorationsForegroundColor() {
				return testEditor.getDecorationsForegroundColor();
			}

			@Override
			public void configureWindow(Window window) {
				super.configureWindow(window);
				testEditor.onTestEditorWindowCreation(window);
			}

		};
		return result;
	}

	protected void onTestEditorWindowCreation(Window window) {
		allWindows.add(window);
	}

	public static void main(String[] args) {
		TestEditor testEditor = new TestEditor(new Tester());
		try {
			if (args.length > 1) {
				throw new Exception("Invalid command line arguments. Expected: [<fileName>]");
			} else if (args.length == 1) {
				String fileName = args[0];
				testEditor.getTester().loadFromFile(new File(fileName));
				testEditor.refresh();
			}
			testEditor.open();
		} catch (Throwable t) {
			testEditor.getSwingRenderer().handleExceptionsFromDisplayedUI(null, t);
		}
	}

	protected class TestEditorSwingRenderer extends SwingCustomizer {

		public TestEditorSwingRenderer(CustomizedUI reflectionUI) {
			super(reflectionUI, getAlternateCustomizationsFilePath());
		}

		@Override
		public CustomizingMethodControlPlaceHolder createMethodControlPlaceHolder(JPanel form, IMethodInfo method) {
			return new CustomizingMethodControlPlaceHolder(this, form, method) {

				private static final long serialVersionUID = 1L;

				@Override
				public IMethodControlData indicateWhenBusy(IMethodControlData data) {
					if (method.getName().startsWith("switch")) {
						return data;
					} else {
						return super.indicateWhenBusy(data);
					}
				}

			};
		}

		@Override
		public DialogBuilder getDialogBuilder(Component activatorComponent) {
			return new DialogBuilder(this, activatorComponent) {

				@Override
				public JDialog createDialog() {
					JDialog result = super.createDialog();
					result.setModalityType(ModalityType.APPLICATION_MODAL);
					return result;
				}

			};
		}

		@Override
		public void setContentPane(Window window, Container contentPane) {
			super.setContentPane(window,
					TestEditor.getAlternateWindowDecorationsContentPane(window, contentPane, TestEditor.this));
		}

		@Override
		public void setMenuBar(Window window, JMenuBar menuBar) {
			getBarsContainer(window).add(menuBar, BorderLayout.NORTH);
		}

		@Override
		public void setStatusBar(Window window, Component statusBar) {
			getBarsContainer(window).add(statusBar, BorderLayout.SOUTH);
		}

		protected Container getBarsContainer(Window window) {
			AlternativeWindowDecorationsPanel decorationsPanel = (AlternativeWindowDecorationsPanel) SwingRendererUtils
					.getContentPane(window);
			Container contentPane = decorationsPanel.getContentPane();
			JPanel barsContainer = (JPanel) ((BorderLayout) contentPane.getLayout())
					.getLayoutComponent(BorderLayout.NORTH);
			if (barsContainer == null) {
				barsContainer = new JPanel();
				contentPane.add(barsContainer, BorderLayout.NORTH);
				barsContainer.setLayout(new BorderLayout());
			}
			return barsContainer;
		}

		@Override
		public Object onTypeInstanciationRequest(Component activatorComponent, ITypeInfo type) {
			Object result = super.onTypeInstanciationRequest(activatorComponent, type);
			if (result instanceof ComponentFinder) {
				if (componentFinderInitializationSource != null) {
					((ComponentFinder) result).initializeFrom(componentFinderInitializationSource, TestEditor.this);
				}
			}
			return result;
		}

	}

	protected class TestEditorReflectionUI extends CustomizedUI {

		public TestEditorReflectionUI(InfoCustomizations infoCustomizations) {
			super(infoCustomizations);
		}

		@Override
		protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
			ITypeInfo result = type;
			result = new StandardProxyFactory().wrapType(result);
			result = new ExtensionsProxyFactory().wrapType(result);
			return result;
		}

		@Override
		public void logDebug(String msg) {
			TestEditor.this.logDebug(msg);
		}

		@Override
		public void logError(String msg) {
			TestEditor.this.logDebug(msg);
		}

		@Override
		public void logError(Throwable t) {
			TestEditor.this.logDebug(t);
		}

		protected class ExtensionsProxyFactory extends InfoProxyFactory {

			protected final Pattern encapsulationTypeNamePattern = Pattern
					.compile("^Encapsulation \\[.*, encapsulatedObjectType=(.*)\\]$");

			protected boolean isExtensionTestActionTypeName(String typeName) {
				Class<?> clazz;
				try {
					clazz = ClassUtils.getCachedClassforName(typeName);
				} catch (ClassNotFoundException e) {
					return false;
				}
				if (!TestAction.class.equals(clazz)) {
					if (TestAction.class.isAssignableFrom(clazz)) {
						if (!Arrays.asList(BUILT_IN_TEST_ACTION_CLASSES).contains(clazz)) {
							return true;
						}
					}
				}
				return false;
			}

			protected boolean isExtensionTestActionEncapsulationTypeName(String encapsulationTypeName) {
				Matcher matcher = encapsulationTypeNamePattern.matcher(encapsulationTypeName);
				if (!matcher.matches()) {
					return false;
				}
				String fieldTypeName = matcher.group(1);
				return isExtensionTestActionTypeName(fieldTypeName);
			}

			@Override
			protected ResourcePath getIconImagePath(ITypeInfo type) {
				if (isExtensionTestActionTypeName(type.getName())) {
					return EXTENSION_IMAGE_PATH;
				}
				return super.getIconImagePath(type);
			}

			@Override
			protected boolean isFormControlEmbedded(IFieldInfo field, ITypeInfo containingType) {
				if (isExtensionTestActionEncapsulationTypeName(containingType.getName())) {
					return true;
				}
				return super.isFormControlEmbedded(field, containingType);
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if (isExtensionTestActionTypeName(type.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>();
					for (IFieldInfo field : super.getFields(type)) {
						if (field.getName().equals("componentInformation")) {
							continue;
						}
						if (field.getName().equals("valueDescription")) {
							continue;
						}
						result.add(field);
					}
					return result;
				} else {
					return super.getFields(type);
				}
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (isExtensionTestActionTypeName(type.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>();
					for (IMethodInfo method : super.getMethods(type)) {
						if (method.getName().equals("validate")) {
							continue;
						}
						if (method.getName().equals("matchIntrospectionRequestEvent")) {
							continue;
						}
						if (method.getName().equals("initializeFrom")) {
							continue;
						}
						if (method.getName().equals("execute")) {
							continue;
						}
						if (method.getName().equals("findComponent")) {
							continue;
						}
						result.add(method);
					}
					return result;
				} else {
					return super.getMethods(type);
				}
			}

			@Override
			protected InfoCategory getCategory(IFieldInfo field, ITypeInfo containingType) {
				if (isExtensionTestActionTypeName(containingType.getName())) {
					if (field.getName().equals("componentFinder")) {
						return new InfoCategory("Component Location", 1);
					}
				}
				return super.getCategory(field, containingType);
			}

			@Override
			protected void validate(ITypeInfo type, Object object) throws Exception {
				if (isExtensionTestActionTypeName(type.getName())) {
					((TestAction) object).validate();
				} else {
					super.validate(type, object);
				}
			}

		}

		protected class StandardProxyFactory extends InfoProxyFactory {

			protected final Pattern polymorphicComponentFindeFieldEncapsulationTypeNamePattern = Pattern.compile(
					"^Encapsulation \\[context=FieldContext \\[fieldName=componentFinder.*\\], subContext=PolymorphicInstance.*\\]$");

			protected boolean isTestActionTypeName(String typeName) {
				Class<?> clazz;
				try {
					clazz = ClassUtils.getCachedClassforName(typeName);
				} catch (ClassNotFoundException e) {
					return false;
				}
				if (TestAction.class.isAssignableFrom(clazz)) {
					return true;
				}
				return false;
			}

			protected boolean isTesterOrSubTypeName(String typeName) {
				Class<?> clazz;
				try {
					clazz = ClassUtils.getCachedClassforName(typeName);
				} catch (ClassNotFoundException e) {
					return false;
				}
				if (Tester.class.isAssignableFrom(clazz)) {
					return true;
				}
				return false;
			}

			@Override
			public String toString() {
				return TestEditor.class.getName() + InfoProxyFactory.class.getSimpleName();
			}

			@Override
			protected String getName(ITypeInfo type) {
				String result = super.getName(type);
				if (isTesterOrSubTypeName(result)) {
					result = Tester.class.getName();
				}
				return result;
			}

			@Override
			protected String toString(ITypeInfo type, Object object) {
				if (object instanceof TestAction) {
					TestAction testAction = (TestAction) object;
					String result = super.toString(type, object);
					if (testAction.isDisabled()) {
						result = "[DISABLED] " + result;
					}
					return result;
				} else {
					return super.toString(type, object);
				}
			}

			@Override
			protected boolean isFormControlEmbedded(IFieldInfo field, ITypeInfo containingType) {
				if (polymorphicComponentFindeFieldEncapsulationTypeNamePattern.matcher(containingType.getName())
						.matches()) {
					return true;
				}
				return super.isFormControlEmbedded(field, containingType);
			}

			@Override
			protected void save(ITypeInfo type, Object object, OutputStream out) {
				if (object instanceof Tester) {
					try {
						((Tester) object).saveToStream(out);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				} else if (object instanceof TestReport) {
					try {
						((TestReport) object).saveToStream(out);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
				} else {
					super.save(type, object, out);
				}
			}

			@Override
			protected void load(ITypeInfo type, Object object, InputStream in) {
				if (object instanceof Tester) {
					try {
						((Tester) object).loadFromStream(in);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refresh();
							showSpecificationTab();
						}
					});
				} else if (object instanceof TestReport) {
					try {
						((TestReport) object).loadFromStream(in);
					} catch (Exception e) {
						throw new ReflectionUIError(e);
					}
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							refresh();
							showReportTab();
						}
					});
				} else {
					super.load(type, object, in);
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if (type.getName().equals(PropertyBasedComponentFinder.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					ITypeInfo propertyValueType = getTypeInfo(new JavaTypeInfoSource(PropertyValue.class));
					result.add(new ImplicitListFieldInfo(TestEditor.this.reflectionUI, "propertyValues", type,
							propertyValueType, "createPropertyValue", "getPropertyValue", "addPropertyValue",
							"removePropertyValue", "propertyValueCount"));
					return result;
				} else if (isTestActionTypeName(type.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>();
					for (IFieldInfo field : super.getFields(type)) {
						if (field.getName().equals("disabled")) {
							continue;
						}
						result.add(field);
					}
					return result;
				} else {
					return super.getFields(type);
				}
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (isTesterOrSubTypeName(type.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						String afterSelectionStartOption = "Insert Recordings After The Current Selection Row";
						String atEndStartOption = "Insert Recordings At The End";
						GenericEnumerationFactory startOptionsEnumFactory = new GenericEnumerationFactory(reflectionUI,
								new Object[] { afterSelectionStartOption, atEndStartOption },
								"testActionsRecordingStartOption", "");

						@Override
						public String getName() {
							return "switchToRecording";
						}

						@Override
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
						}

						@Override
						public String getCaption() {
							return "Start Recording";
						}

						@Override
						public boolean isReadOnly() {
							return false;
						}

						@Override
						public List<IParameterInfo> getParameters() {
							List<IParameterInfo> result = new ArrayList<IParameterInfo>();
							result.add(startPositionParameter);
							result.add(mainMethodParameter);
							return result;
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							ListControl testActionsControl = getTestActionsControl();
							if ((testActionsControl != null) && (testActionsControl.getSelection().size() == 1)) {
								String startPosition = (String) startOptionsEnumFactory
										.unwrapInstance(invocationData.getParameterValue(startPositionParameter));
								if (afterSelectionStartOption.equals(startPosition)) {
									recordingWindowSwitch.setRecordingInsertedAfterSelection(true);
								} else if (atEndStartOption.equals(startPosition)) {
									recordingWindowSwitch.setRecordingInsertedAfterSelection(false);
								} else {
									throw new AssertionError();
								}
							}
							final CallMainMethodAction mainMethodCall = (CallMainMethodAction) invocationData
									.getParameterValue(mainMethodParameter);
							if (mainMethodCall != null) {
								mainMethodCall.execute(null, tester);
								recordingWindowSwitch.insertNewTestAction(mainMethodCall);
							}
							startRecording();
							return null;
						}

						IParameterInfo startPositionParameter = new ParameterInfoProxy(
								IParameterInfo.NULL_PARAMETER_INFO) {

							@Override
							public int getPosition() {
								return 0;
							}

							@Override
							public String getName() {
								return "insertPosition";
							}

							@Override
							public String getCaption() {
								return "Insert Position";
							}

							@Override
							public ITypeInfo getType() {
								return reflectionUI.getTypeInfo(startOptionsEnumFactory.getInstanceTypeInfoSource());
							}

							@Override
							public Object getDefaultValue() {
								return startOptionsEnumFactory.getInstance(afterSelectionStartOption);
							}

							@Override
							public boolean isNullValueDistinct() {
								return false;
							}

						};

						IParameterInfo mainMethodParameter = new ParameterInfoProxy(
								IParameterInfo.NULL_PARAMETER_INFO) {

							@Override
							public int getPosition() {
								return 1;
							}

							@Override
							public String getName() {
								return "startCallMainMethodAction";
							}

							@Override
							public String getCaption() {
								return "Start By Calling Main Method";
							}

							@Override
							public ITypeInfo getType() {
								return reflectionUI.getTypeInfo(new JavaTypeInfoSource(CallMainMethodAction.class));
							}

							@Override
							public Object getDefaultValue() {
								return null;
							}

							@Override
							public boolean isNullValueDistinct() {
								return true;
							}

						};

					});
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

						@Override
						public String getName() {
							return "swithToReplay";
						}

						@Override
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
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
						public String getSignature() {
							return ReflectionUIUtils.buildMethodSignature(this);
						}

						@Override
						public String getCaption() {
							return "Inspect Component(s)";
						}

						@Override
						public Object invoke(Object object, InvocationData invocationData) {
							componentInspectionWindowSwitch.activate(true);
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
					final List<? extends ItemPosition> selection, final Object rootListValue) {
				if ((listType.getItemType() != null)
						&& TestAction.class.getName().equals(listType.getItemType().getName())) {
					if (selection.size() > 0) {
						List<AbstractListAction> result = new ArrayList<AbstractListAction>();
						result.add(new AbstractListAction() {

							@Override
							public boolean isNullReturnValueDistinct() {
								return false;
							}

							@Override
							public String getName() {
								return "replay";
							}

							@Override
							public String getCaption() {
								return "Replay";
							}

							@Override
							public boolean isReadOnly() {
								return true;
							}

							@Override
							public String getOnlineHelp() {
								return "Replay selected action(s)";
							}

							@Override
							public Object invoke(Object object, InvocationData invocationData) {
								try {
									List<TestAction> selectedActions = new ArrayList<TestAction>();
									for (ItemPosition itemPosition : selection) {
										TestAction testAction = (TestAction) itemPosition.getItem(rootListValue);
										selectedActions.add(testAction);
									}
									startReplay(selectedActions);
									return null;
								} catch (Exception e) {
									throw new AssertionError(e);
								}
							}

						});
						if (selection.size() == 1) {
							result.add(new AbstractListAction() {

								@Override
								public boolean isNullReturnValueDistinct() {
									return false;
								}

								@Override
								public Object invoke(Object object, InvocationData invocationData) {
									try {
										List<TestAction> actionsToReplay = new ArrayList<TestAction>();
										ItemPosition singleSelection = selection.get(0);
										for (int i = singleSelection.getIndex(); i < singleSelection
												.getContainingListSize(rootListValue); i++) {
											TestAction testAction = (TestAction) singleSelection.getSibling(i)
													.getItem(rootListValue);
											actionsToReplay.add(testAction);
										}
										startReplay(actionsToReplay);
										return null;
									} catch (Exception e) {
										throw new AssertionError(e);
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

								@Override
								public boolean isReadOnly() {
									return true;
								}

								@Override
								public String getOnlineHelp() {
									return "Replay from selected action to end";
								}

							});
						}
						return result;
					}
				}
				return super.getDynamicActions(listType, selection, rootListValue);
			}

			@Override
			protected List<AbstractListProperty> getDynamicProperties(IListTypeInfo listType,
					final List<? extends ItemPosition> selection, final Object rootListValue) {
				if ((listType.getItemType() != null)
						&& TestAction.class.getName().equals(listType.getItemType().getName())) {
					List<AbstractListProperty> result = new ArrayList<AbstractListProperty>(
							super.getDynamicProperties(listType, selection, rootListValue));
					if (selection.size() >= 1) {
						result.add(new AbstractListProperty() {

							String DISABLED = "Disabled";
							String ENABLED = "Enabled";

							@Override
							public String getName() {
								return "disabled";
							}

							@Override
							public String getCaption() {
								return "Enable/Disable";
							}

							@Override
							public String getOnlineHelp() {
								return "Enable/Disable the selected action(s)";
							}

							@Override
							public boolean isNullValueDistinct() {
								return true;
							}

							@Override
							public Object[] getValueOptions(Object object) {
								return new Object[] { ENABLED, DISABLED };
							}

							@Override
							public Object getValue(Object object) {
								Boolean result = null;
								for (ItemPosition itemPosition : selection) {
									TestAction testAction = (TestAction) itemPosition.getItem(rootListValue);
									if (result == null) {
										result = testAction.isDisabled();
									} else {
										if (!result.equals(testAction.isDisabled())) {
											result = null;
											break;
										}
									}
								}
								if (Boolean.TRUE.equals(result)) {
									return DISABLED;
								} else if (Boolean.FALSE.equals(result)) {
									return ENABLED;
								} else {
									return null;
								}
							}

							@Override
							public void setValue(Object object, Object value) {
								Boolean disabled;
								if (DISABLED.equals(value)) {
									disabled = true;
								} else if (ENABLED.equals(value)) {
									disabled = false;
								} else {
									disabled = null;
								}
								if (disabled == null) {
									return;
								}
								for (ItemPosition itemPosition : selection) {
									TestAction testAction = (TestAction) itemPosition.getItem(rootListValue);
									testAction.setDisabled(disabled);
								}
							}

							@Override
							public boolean isGetOnly() {
								return false;
							}

							@Override
							public ValueReturnMode getValueReturnMode() {
								return ValueReturnMode.CALCULATED;
							}

							@Override
							public IInfoProxyFactory getTypeSpecificities() {
								return null;
							}

							@Override
							public ITypeInfo getType() {
								return reflectionUI.getTypeInfo(new JavaTypeInfoSource(String.class));
							}

						});
					}
					return result;
				}
				return super.getDynamicProperties(listType, selection, rootListValue);
			}

		}

	}

	protected class MainObject {

		public Tester getSpecification() {
			return tester;
		}

		public TestReport getExecutionReport() {
			return testReport;
		}

		public void loadReportSpecification() throws IOException {
			tester.loadFromFile(testReport.getSpecificationCopyFile());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					refresh();
					showSpecificationTab();
				}
			});
		}

	}

}
