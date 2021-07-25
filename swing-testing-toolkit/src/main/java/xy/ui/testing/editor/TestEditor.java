package xy.ui.testing.editor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.StackLayout;

import xy.reflect.ui.CustomizedUI;
import xy.reflect.ui.control.DefaultFieldControlData;
import xy.reflect.ui.control.IMethodControlData;
import xy.reflect.ui.control.IMethodControlInput;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.MethodAction;
import xy.reflect.ui.control.swing.NullableControl;
import xy.reflect.ui.control.swing.builder.DialogBuilder;
import xy.reflect.ui.control.swing.customizer.CustomizingFieldControlPlaceHolder;
import xy.reflect.ui.control.swing.customizer.CustomizingForm;
import xy.reflect.ui.control.swing.customizer.SwingCustomizer;
import xy.reflect.ui.control.swing.menu.AbstractFileMenuItem;
import xy.reflect.ui.control.swing.renderer.FieldControlPlaceHolder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.AlternativeWindowDecorationsPanel;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WindowManager;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.ResourcePath;
import xy.reflect.ui.info.ValueReturnMode;
import xy.reflect.ui.info.app.IApplicationInfo;
import xy.reflect.ui.info.custom.InfoCustomizations;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.field.ImplicitListFieldInfo;
import xy.reflect.ui.info.filter.IInfoFilter;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.MethodInfoProxy;
import xy.reflect.ui.info.parameter.IParameterInfo;
import xy.reflect.ui.info.parameter.ParameterInfoProxy;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfo.CategoriesStyle;
import xy.reflect.ui.info.type.factory.InfoProxyFactory;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.info.type.iterable.item.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.AbstractListAction;
import xy.reflect.ui.info.type.iterable.util.AbstractListProperty;
import xy.reflect.ui.info.type.iterable.util.IDynamicListAction;
import xy.reflect.ui.info.type.iterable.util.IDynamicListProperty;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.undo.AbstractSimpleModificationListener;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.IModificationListener;
import xy.reflect.ui.undo.ListModificationFactory;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ClassUtils;
import xy.reflect.ui.util.Mapper;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.TestReport;
import xy.ui.testing.Tester;
import xy.ui.testing.Tester.ControlsTheme;
import xy.ui.testing.Tester.EditingOptions;
import xy.ui.testing.action.CallMainMethodAction;
import xy.ui.testing.action.CheckNumberOfOpenWindowsAction;
import xy.ui.testing.action.SystemExitCallInterceptionAction;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.WaitAction;
import xy.ui.testing.action.component.CheckVisibleStringsAction;
import xy.ui.testing.action.component.ClickAction;
import xy.ui.testing.action.component.ClickOnMenuItemAction;
import xy.ui.testing.action.component.FocusAction;
import xy.ui.testing.action.component.SendKeysAction;
import xy.ui.testing.action.component.SendKeysAction.KeyboardInteraction;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey;
import xy.ui.testing.action.component.SendKeysAction.WriteText;
import xy.ui.testing.action.component.property.ChangeComponentPropertyAction;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.action.component.specific.ClickOnTableCellAction;
import xy.ui.testing.action.component.specific.CollapseTreetTableItemAction;
import xy.ui.testing.action.component.specific.ExpandTreetTableToItemAction;
import xy.ui.testing.action.component.specific.SelectComboBoxItemAction;
import xy.ui.testing.action.component.specific.SelectTabAction;
import xy.ui.testing.action.component.specific.SelectTableRowAction;
import xy.ui.testing.action.window.CheckWindowVisibleStringsAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.editor.RecordingWindowSwitch.InsertPosition;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.DisplayedStringComponentFinder;
import xy.ui.testing.finder.MenuItemComponentFinder;
import xy.ui.testing.finder.NameBasedComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;
import xy.ui.testing.theme.ClassInThemePackage;
import xy.ui.testing.util.Analytics;
import xy.ui.testing.util.MiscUtils;

/**
 * The main window that allows to edit and execute test specifications.
 * 
 * @author olitank
 *
 */
public class TestEditor extends JFrame {

	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					TestEditor testEditor = new TestEditor(new Tester());
					if (args.length > 1) {
						throw new Exception("Invalid command line arguments. Expected: [<fileName>]");
					} else if (args.length == 1) {
						String fileName = args[0];
						testEditor.getTester().loadFromFile(new File(fileName));
						testEditor.refresh();
					}
					testEditor.open();
				} catch (Throwable t) {
					throw new RuntimeException(t);
				}
			}
		});
	}

	private static final long serialVersionUID = 1L;

	public static final String ALTERNATE_UI_CUSTOMIZATION_FILE_PATH_PROPERTY_KEY = "xy.ui.testing.gui.customizationFile";
	public static final WeakHashMap<TestEditor, Tester> TESTER_BY_EDITOR = new WeakHashMap<TestEditor, Tester>();
	public static final boolean DEBUG = Boolean
			.valueOf(System.getProperty(TestEditor.class.getName() + ".debug", "false"));

	public static final Class<?>[] BUILT_IN_TEST_ACTION_CLASSES = new Class[] { CallMainMethodAction.class,
			SystemExitCallInterceptionAction.class, WaitAction.class, ExpandTreetTableToItemAction.class,
			CollapseTreetTableItemAction.class, SelectComboBoxItemAction.class, SelectTableRowAction.class,
			SelectTabAction.class, ClickOnTableCellAction.class, ClickOnMenuItemAction.class, ClickAction.class,
			SendKeysAction.class, FocusAction.class, CheckVisibleStringsAction.class,
			CheckWindowVisibleStringsAction.class, ChangeComponentPropertyAction.class,
			CheckComponentPropertyAction.class, CloseWindowAction.class, CheckNumberOfOpenWindowsAction.class };
	public static final Class<?>[] BUILT_IN_COMPONENT_FINDRER_CLASSES = new Class[] { MenuItemComponentFinder.class,
			NameBasedComponentFinder.class, DisplayedStringComponentFinder.class, ClassBasedComponentFinder.class,
			PropertyBasedComponentFinder.class };
	public static final Class<?>[] BUILT_IN_KEYBOARD_INTERACTION_CLASSES = new Class[] { WriteText.class,
			SpecialKey.class };

	protected static final String TEST_ACTIONS_FIELD_NAME = "testActions";
	protected static final String TEST_REPORT_STEPS_FIELD_NAME = "steps";
	protected static final ResourcePath EXTENSION_IMAGE_PATH = SwingRendererUtils
			.putImageInCache(MiscUtils.loadImageResource("ExtensionAction.png"));

	protected Color decorationsForegroundColor;
	protected Color decorationsBackgroundColor;

	protected ReplayWindowSwitch replayWindowSwitch = new ReplayWindowSwitch(this);
	protected RecordingWindowSwitch recordingWindowSwitch = new RecordingWindowSwitch(this);
	protected ComponentInspectionWindowSwitch componentInspectionWindowSwitch = new ComponentInspectionWindowSwitch(
			this);

	protected SwingCustomizer swingRenderer;
	protected CustomizedUI reflectionUI;
	protected InfoCustomizations infoCustomizations;
	protected Component componentFinderInitializationSource;

	protected Tester tester;
	protected TestReport testReport;
	protected MainObject mainObject = new MainObject();
	protected Form mainForm;
	protected WindowManager windowManager;

	protected AWTEventListener recordingListener;
	protected Set<Window> allWindows = new HashSet<Window>();

	protected AWTEventListener modalityChangingListener;
	protected Analytics analytics = new Analytics() {

		@Override
		public void track(String event, String... details) {
			logDebug(event + " " + Arrays.asList(details));
			super.track(event, details);
		}

		@Override
		public void sendTracking(Date when, String used, String... details) {
			if (!tester.getEditingOptions().isAnalyticsEnabled()) {
				return;
			}
			super.sendTracking(when, used, details);
		}

	};
	protected IModificationListener backgroundImageUpdater = new AbstractSimpleModificationListener() {
		@Override
		protected void handleAnyEvent(IModification modification) {
			windowManager.refreshWindowStructureAsMuchAsPossible();
		}
	};

	public TestEditor(Tester tester) {
		TESTER_BY_EDITOR.put(this, tester);
		this.tester = tester;
		this.testReport = new TestReport();
		analytics.initialize();
		analytics.track("Initializing test editor...", "tester=" + tester);
		setupWindowSwitchesEventHandling();
		preventDialogApplicationModality();
		infoCustomizations = createInfoCustomizations();
		reflectionUI = createTesterReflectionUI(infoCustomizations);
		swingRenderer = createSwingRenderer(reflectionUI);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		addComponents();
	}

	@Override
	public void dispose() {
		removeComponents();
		cleanupWindowSwitchesEventHandling();
		cleanupDialogApplicationModalityPrevention();
		swingRenderer = null;
		reflectionUI = null;
		infoCustomizations = null;
		analytics.track("Finalizing test editor...", "tester=" + tester);
		analytics.shutdown();
		this.tester = null;
		this.testReport = null;
		TESTER_BY_EDITOR.remove(this);
		super.dispose();
	}

	protected void addComponents() {
		this.mainForm = getSwingRenderer().createForm(mainObject);
		String title = getSwingRenderer().getObjectTitle(getTester());
		List<Component> toolbarControls = mainForm.createButtonBarControls();
		Image iconImage = getSwingRenderer().getObjectIconImage(getTester());
		this.windowManager = getSwingRenderer().createWindowManager(this);
		windowManager.install(mainForm, toolbarControls, title, iconImage);
		mainForm.getModificationStack().addListener(backgroundImageUpdater);
	}

	protected void removeComponents() {
		mainForm.getModificationStack().removeListener(backgroundImageUpdater);
		this.windowManager.uninstall();
		this.mainForm = null;
		this.windowManager = null;
	}

	public File getLastTesterFile() {
		return AbstractFileMenuItem.getLastFileByForm().get(mainForm);
	}

	public void setLastTesterFile(File file) {
		AbstractFileMenuItem.getLastFileByForm().put(getTesterForm(), file);
		mainForm.refresh(false);
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
				if (!getTester().isTestable(c)) {
					return;
				}
				if (isCurrentComponentChangeEvent(event)) {
					if (System.currentTimeMillis() > currentComponentChangeDisabledUntil) {
						getTester().handleCurrentComponentChange(c);
					}
				}
				if (c != getTester().getCurrentComponent()) {
					if (isWindowClosingRecordingRequestEvent(event)) {
						recordingWindowSwitch.handleWindowClosingRecordingEvent(event);
					} else if (isGenericRecordingRequestEvent(event)) {
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

	protected void cleanupWindowSwitchesEventHandling() {
		SwingRendererUtils.removeAWTEventListener(recordingListener);
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
		logDebug(xy.reflect.ui.util.MiscUtils.getPrintedStackTrace(t));
	}

	public void logError(String msg) {
		System.out.println("[" + TestEditor.class.getName() + "] ERROR - " + msg);
	}

	public void logError(Throwable t) {
		logError(xy.reflect.ui.util.MiscUtils.getPrintedStackTrace(t));
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
		Form testerForm = getTesterForm();
		if (testerForm == null) {
			return null;
		}
		FieldControlPlaceHolder fieldControlPlaceHolder = testerForm
				.getFieldControlPlaceHolder(TEST_ACTIONS_FIELD_NAME);
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
		Form testReportForm = getTestReportForm();
		if (testReportForm == null) {
			return null;
		}
		FieldControlPlaceHolder fieldControlPlaceHolder = testReportForm
				.getFieldControlPlaceHolder(TEST_REPORT_STEPS_FIELD_NAME);
		if (fieldControlPlaceHolder == null) {
			return null;
		}
		Component c = fieldControlPlaceHolder.getFieldControl();
		if (c instanceof NullableControl) {
			c = ((NullableControl) c).getSubControl();
		}
		return (ListControl) c;
	}

	protected Form getTesterForm() {
		if (mainForm == null) {
			return null;
		}
		return SwingRendererUtils.findFirstObjectDescendantForm(tester, mainForm, getSwingRenderer());
	}

	protected Form getTestReportForm() {
		if (mainForm == null) {
			return null;
		}
		if (testReport == null) {
			return null;
		}
		return SwingRendererUtils.findFirstObjectDescendantForm(testReport, mainForm, getSwingRenderer());
	}

	public void refresh() {
		mainForm.refresh(false);
		windowManager.refreshWindowStructureAsMuchAsPossible();
	}

	public void showReportTab() {
		mainForm.setDisplayedCategory(new InfoCategory("Report", -1, null));
		ListControl stepsControl = getTestReportStepsControl();
		if (stepsControl.getRootListSize() > 0) {
			BufferedItemPosition lastStepItemPosition = stepsControl
					.getRootListItemPosition(stepsControl.getRootListSize() - 1);
			stepsControl.setSingleSelection(lastStepItemPosition);
			stepsControl.scrollTo(lastStepItemPosition);
		}
	}

	public void showSpecificationTab() {
		mainForm.setDisplayedCategory(new InfoCategory("Specification", -1, null));
		ListControl testActionsControl = getTestActionsControl();
		if (testActionsControl.getRootListSize() > 0) {
			BufferedItemPosition firstActionItemPosition = testActionsControl.getRootListItemPosition(0);
			testActionsControl.setSingleSelection(firstActionItemPosition);
			testActionsControl.scrollTo(firstActionItemPosition);
		}
	}

	public void setTestActionsAndUpdateUI(TestAction[] testActions) {
		Form testerForm = getTesterForm();
		IFieldInfo testActionsField = testerForm.getFieldControlPlaceHolder(TEST_ACTIONS_FIELD_NAME).getField();
		ModificationStack modifStack = testerForm.getModificationStack();
		ReflectionUIUtils.setFieldValueThroughModificationStack(
				new DefaultFieldControlData(getSwingRenderer().getReflectionUI(), getTester(), testActionsField),
				testActions, modifStack, ReflectionUIUtils.getDebugLogListener(getSwingRenderer().getReflectionUI()));
		refresh();
	}

	public List<Integer> getMultipleSelectedActionIndexes() {
		ListControl testActionsControl = getTestActionsControl();
		List<Integer> result = new ArrayList<Integer>();
		for (ItemPosition itemPosition : testActionsControl.getSelection()) {
			result.add(itemPosition.getIndex());
		}
		return result;
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
		BufferedItemPosition itemPosition = testActionsControl.getRootListItemPosition(index);
		testActionsControl.setSingleSelection(itemPosition);
		testActionsControl.scrollTo(itemPosition);
	}

	protected void startRecording() {
		if (recordingWindowSwitch.isActive()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				recordingWindowSwitch.activate();
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
				replayWindowSwitch.activate();
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

	protected SwingCustomizer createSwingRenderer(CustomizedUI reflectionUI) {
		return new TestEditorSwingRenderer(reflectionUI);
	}

	protected CustomizedUI createTesterReflectionUI(InfoCustomizations infoCustomizations) {
		return new TestEditorReflectionUI(infoCustomizations);
	}

	protected void onTestEditorWindowCreation(Window window) {
		allWindows.add(window);
	}

	protected void onTestEditorWindowDestruction(Window window) {
		allWindows.remove(window);
	}

	protected class TestEditorSwingRenderer extends SwingCustomizer {

		public TestEditorSwingRenderer(CustomizedUI reflectionUI) {
			super(reflectionUI, getAlternateCustomizationsFilePath());
		}

		@Override
		public WindowManager createWindowManager(Window window) {
			return new WindowManager(this, window) {

				@Override
				public void install(Component content, List<Component> toolbarControls, String title, Image iconImage) {
					onTestEditorWindowCreation(window);
					super.install(content, toolbarControls, title, iconImage);
				}

				@Override
				public void uninstall() {
					onTestEditorWindowDestruction(window);
					super.uninstall();
				}

				@Override
				protected void layoutContentPane(Container contentPane) {
					if ((TestEditor.this.getDecorationsBackgroundColor() != null)
							|| (TestEditor.this.getDecorationsForegroundColor() != null)) {
						alternativeDecorationsPanel = createAlternativeWindowDecorationsPanel(window, contentPane);
						rootPane.add(alternativeDecorationsPanel, StackLayout.TOP);
					} else {
						super.layoutContentPane(contentPane);
					}
				}

				@Override
				protected AlternativeWindowDecorationsPanel createAlternativeWindowDecorationsPanel(Window window,
						Component windowContent) {
					String title = SwingRendererUtils.getWindowTitle(window);
					ImageIcon icon;
					if (window.getIconImages().size() == 0) {
						icon = null;
					} else {
						Image iconImage = window.getIconImages().get(0);
						if (SwingRendererUtils.isNullImage(iconImage)) {
							icon = null;
						} else {
							icon = SwingRendererUtils.getSmallIcon(new ImageIcon(iconImage));
						}
					}
					return new CustomWindowDecorationsPanel(title, icon, window, windowContent) {

						private static final long serialVersionUID = 1L;

						@Override
						public Color getTitleBarColor() {
							if (TestEditor.this.getDecorationsBackgroundColor() != null) {
								return TestEditor.this.getDecorationsBackgroundColor();
							}
							return super.getTitleBarColor();
						}

						@Override
						public Color getDecorationsForegroundColor() {
							if (TestEditor.this.getDecorationsForegroundColor() != null) {
								return TestEditor.this.getDecorationsForegroundColor();
							}
							return super.getDecorationsForegroundColor();
						}

					};
				}

			};
		}

		@Override
		public CustomizingForm createForm(Object object, IInfoFilter infoFilter) {
			return new CustomizingForm(swingRenderer, object, infoFilter) {

				private static final long serialVersionUID = 1L;

				@Override
				public CustomizingFieldControlPlaceHolder createFieldControlPlaceHolder(IFieldInfo field) {
					return new CustomizingFieldControlPlaceHolder(this, field) {

						private static final long serialVersionUID = 1L;

						@Override
						public Component createFieldControl() {
							if ((form.getObject() instanceof Tester)
									&& field.getName().equals(TEST_ACTIONS_FIELD_NAME)) {
								return new ListControl(swingRenderer, this) {

									private static final long serialVersionUID = 1L;

									final float BIGGER = 1.1f;

									@Override
									protected TableCellRenderer createTableCellRenderer() {
										return new ItemTableCellRenderer() {

											Font changeFont(Font font) {
												return new Font(font.getName(), Font.BOLD,
														Math.round(font.getSize() * BIGGER));
											}

											@Override
											public Component getTableCellRendererComponent(JTable table, Object value,
													boolean isSelected, boolean hasFocus, int row, int column) {
												JLabel result = (JLabel) super.getTableCellRendererComponent(table,
														value, isSelected, hasFocus, row, column);
												result.setFont(changeFont(result.getFont()));
												return result;
											}

										};
									}

									@Override
									protected void initializeTreeTableModelAndControl() {
										super.initializeTreeTableModelAndControl();
										treeTableComponent.setRowHeight(
												Math.round(treeTableComponent.getRowHeight() * BIGGER * 1.2f));
									}

								};
							} else {
								return super.createFieldControl();
							}
						}

					};
				}

			};
		}

		@Override
		public DialogBuilder createDialogBuilder(Component activatorComponent) {
			return new ApplicationDialogBuilder(activatorComponent) {

				@Override
				public RenderedDialog createDialog() {
					RenderedDialog result = super.createDialog();
					result.setModalityType(ModalityType.APPLICATION_MODAL);
					return result;
				}

			};
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

		@Override
		public MethodAction createMethodAction(IMethodControlInput input) {
			return new MethodAction(this, input) {

				private static final long serialVersionUID = 1L;

				@Override
				protected IMethodControlData indicateWhenBusy(IMethodControlData data, Component activatorComponent) {
					if (data.getCaption().startsWith("Switch")) {
						return data;
					} else {
						return super.indicateWhenBusy(data, activatorComponent);
					}
				}
			};
		}

	}

	protected class TestEditorReflectionUI extends CustomizedUI {

		public TestEditorReflectionUI(InfoCustomizations infoCustomizations) {
			super(infoCustomizations);
		}

		@Override
		protected ITypeInfo getTypeInfoBeforeCustomizations(ITypeInfo type) {
			ITypeInfo result = type;
			result = new StandardProxyFactory().wrapTypeInfo(result);
			result = new ExtensionsProxyFactory().wrapTypeInfo(result);
			return result;
		}

		@Override
		public IApplicationInfo getApplicationInfo() {
			IApplicationInfo result = super.getApplicationInfo();
			result = new StandardProxyFactory().wrapApplicationInfo(result);
			result = new ExtensionsProxyFactory().wrapApplicationInfo(result);
			return result;
		}

		@Override
		public void logDebug(String msg) {
			TestEditor.this.logDebug(msg);
		}

		@Override
		public void logError(String msg) {
			TestEditor.this.logError(msg);
		}

		@Override
		public void logError(Throwable t) {
			TestEditor.this.logError(t);
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
						return new InfoCategory("Component Location", 0, null);
					} else {
						return new InfoCategory("General", 1, null);
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

			@Override
			protected void setValue(Object object, Object value, IFieldInfo field, ITypeInfo containingType) {
				if ((object instanceof Tester) || (object instanceof TestReport) || (object instanceof TestAction)
						|| (object instanceof ComponentFinder)) {
					analytics.track("Setting(" + object + " - " + field.getName() + ")", String.valueOf(value));
				}
				super.setValue(object, value, field, containingType);
			}

			@Override
			protected Object invoke(Object object, InvocationData invocationData, IMethodInfo method,
					ITypeInfo containingType) {
				if (!method.getName().equals("validate")) {
					if ((object instanceof Tester) || (object instanceof TestReport) || (object instanceof TestAction)
							|| (object instanceof ComponentFinder)) {
						analytics.track(
								"Invoking " + method.getName() + "(" + invocationData.toString() + ") on " + object);
					}
				}
				if (object instanceof TestReport) {
					if (method.getName().startsWith("load")) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								showReportTab();
							}
						});
					}
				}
				return super.invoke(object, invocationData, method, containingType);
			}

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
			protected ResourcePath getMainBackgroundImagePath(IApplicationInfo appInfo) {
				ControlsTheme theme = TestEditor.this.tester.getEditingOptions().getControlsTheme();
				if (theme == ControlsTheme.classic) {
					return null;
				} else {
					return new ResourcePath(ClassInThemePackage.class, theme.name() + "-background.png");
				}
			}

			@Override
			protected CategoriesStyle getCategoriesStyle(ITypeInfo type) {
				return CategoriesStyle.MODERN;
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
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if (type.getName().equals(PropertyBasedComponentFinder.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					ITypeInfo propertyValueType = buildTypeInfo(
							new JavaTypeInfoSource(TestEditorReflectionUI.this, PropertyValue.class, null));
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
			protected boolean isHidden(IFieldInfo field, ITypeInfo containingType) {
				if (Analytics.TRACKINGS_DELIVERY_URL == null) {
					if (containingType.getName().equals(EditingOptions.class.getName())) {
						if (field.getName().equals("analyticsEnabled")) {
							return true;
						}
					}
				}
				return super.isHidden(field, containingType);
			}

			@Override
			protected boolean isHidden(IMethodInfo method, ITypeInfo containingType) {
				if (isTestActionTypeName(containingType.getName())) {
					if (method.getSignature().equals(ReflectionUIUtils.buildMethodSignature("void", "prepare",
							Arrays.asList(Tester.class.getName())))) {
						return true;
					}
				}
				return super.isHidden(method, containingType);
			}

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				if (isTesterOrSubTypeName(type.getName())) {
					List<IMethodInfo> result = new ArrayList<IMethodInfo>(super.getMethods(type));
					result.add(new MethodInfoProxy(IMethodInfo.NULL_METHOD_INFO) {

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
							InsertPosition insertPosition;
							if (!invocationData.getProvidedParameterValues()
									.containsKey(startPositionParameter.getPosition())) {
								insertPosition = (InsertPosition) startPositionParameter.getDefaultValue(object);
							} else {
								insertPosition = (InsertPosition) invocationData
										.getParameterValue(startPositionParameter.getPosition());
							}
							recordingWindowSwitch.setInsertPosition(insertPosition);
							final CallMainMethodAction mainMethodCall = (CallMainMethodAction) invocationData
									.getParameterValue(mainMethodParameter.getPosition());
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
								return reflectionUI.buildTypeInfo(new JavaTypeInfoSource(TestEditorReflectionUI.this,
										InsertPosition.class, null));
							}

							@Override
							public Object getDefaultValue(Object object) {
								return InsertPosition.AfterSelection;
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
								return reflectionUI.buildTypeInfo(
										new JavaTypeInfoSource(reflectionUI, CallMainMethodAction.class, null));
							}

							@Override
							public Object getDefaultValue(Object object) {
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
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									componentInspectionWindowSwitch.activate();
								}
							});
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
						result.add(buildTypeInfo(new JavaTypeInfoSource(TestEditorReflectionUI.this, clazz, null)));
					}
					return result;
				} else if (type.getName().equals(ComponentFinder.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getComponentFinderClasses()) {
						result.add(buildTypeInfo(new JavaTypeInfoSource(TestEditorReflectionUI.this, clazz, null)));
					}
					return result;
				} else if (type.getName().equals(KeyboardInteraction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : getKeyboardInteractionClasses()) {
						result.add(buildTypeInfo(new JavaTypeInfoSource(TestEditorReflectionUI.this, clazz, null)));
					}
					return result;
				} else {
					return super.getPolymorphicInstanceSubTypes(type);
				}
			}

			@Override
			protected List<IDynamicListAction> getDynamicActions(IListTypeInfo listType,
					final List<? extends ItemPosition> selection,
					final Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
				if ((listType.getItemType() != null)
						&& TestAction.class.getName().equals(listType.getItemType().getName())) {
					if (selection.size() > 0) {
						List<IDynamicListAction> result = new ArrayList<IDynamicListAction>();
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
							public boolean isEnabled(Object objet) {
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
										TestAction testAction = (TestAction) itemPosition.getItem();
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
												.getContainingListSize(); i++) {
											TestAction testAction = (TestAction) singleSelection.getSibling(i)
													.getItem();
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
								public boolean isEnabled(Object objet) {
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
				return super.getDynamicActions(listType, selection, listModificationFactoryAccessor);
			}

			@Override
			protected List<IDynamicListProperty> getDynamicProperties(IListTypeInfo listType,
					final List<? extends ItemPosition> selection,
					final Mapper<ItemPosition, ListModificationFactory> listModificationFactoryAccessor) {
				if ((listType.getItemType() != null)
						&& TestAction.class.getName().equals(listType.getItemType().getName())) {
					List<IDynamicListProperty> result = new ArrayList<IDynamicListProperty>(
							super.getDynamicProperties(listType, selection, listModificationFactoryAccessor));
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
							public boolean hasValueOptions(Object object) {
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
									TestAction testAction = (TestAction) itemPosition.getItem();
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
									TestAction testAction = (TestAction) itemPosition.getItem();
									testAction.setDisabled(disabled);
								}
							}

							@Override
							public boolean isGetOnly() {
								return false;
							}

							@Override
							public boolean isTransient() {
								return false;
							}

							@Override
							public ValueReturnMode getValueReturnMode() {
								return ValueReturnMode.CALCULATED;
							}

							@Override
							public ITypeInfo getType() {
								return reflectionUI.buildTypeInfo(
										new JavaTypeInfoSource(TestEditorReflectionUI.this, String.class, null));
							}

						});
					}
					return result;
				}
				return super.getDynamicProperties(listType, selection, listModificationFactoryAccessor);
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
