package xy.ui.testing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.security.AnyTypePermission;

import xy.reflect.ui.control.swing.util.HyperlinkTooltip;
import xy.ui.testing.TestReport.TestReportStep;
import xy.ui.testing.TestReport.TestReportStepStatus;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.component.specific.SelectComboBoxItemAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.Listener;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;

/**
 * This class is the test specification and execution class.
 * 
 * @author olitank
 *
 */
public class Tester {

	/**
	 * The foreground color that will be set to the current component being tested.
	 */
	public static final transient Color HIGHLIGHT_FOREGROUND = MiscUtils.stringToColor(
			System.getProperty(Tester.class.getPackage().getName() + ".highlightForeground", "235,48,33"));
	/**
	 * The background color that will be set to the current component being tested.
	 */
	public static final transient Color HIGHLIGHT_BACKGROUND = MiscUtils.stringToColor(
			System.getProperty(Tester.class.getPackage().getName() + ".highlightBackground", "245,216,214"));

	protected static final transient MouseListener DUMMY_MOUSE_LISTENER_TO_ENSURE_EVENT_DISPATCH = new MouseAdapter() {
	};

	protected final transient Object CURRENT_COMPONENT_MUTEX = new Object() {
		@Override
		public String toString() {
			return Tester.class.getName() + ".CURRENT_COMPONENT_MUTEX";
		}
	};

	protected List<TestAction> testActions = new ArrayList<TestAction>();
	protected double minimumSecondsToWaitBetwneenActions = 0.1;
	protected double maximumSecondsToWaitBetwneenActions = 15.0;
	protected EditingOptions editingOptions = new EditingOptions();

	protected transient Component currentComponent;
	protected boolean currentComponentOpaque;
	protected transient Color currentComponentBackground;
	protected transient Color currentComponentForeground;
	protected transient MouseListener[] currentComponentMouseListeners;
	protected transient Border currentComponentBorder;

	/**
	 * The default constructor. Builds an empty (0 actions) instance.
	 */
	public Tester() {
	}

	/**
	 * @return The component being tested at the moment of this method call.
	 */
	public Component getCurrentComponent() {
		return currentComponent;
	}

	/**
	 * @return The minimum time (seconds) to wait between actions when executing the
	 *         test specification.
	 */
	public double getMinimumSecondsToWaitBetwneenActions() {
		return minimumSecondsToWaitBetwneenActions;
	}

	/**
	 * Updates the minimum time (seconds) to wait between actions when executing the
	 * test specification.
	 * 
	 * @param minimumSecondsToWaitBetwneenActions The new value (seconds).
	 */
	public void setMinimumSecondsToWaitBetwneenActions(double minimumSecondsToWaitBetwneenActions) {
		this.minimumSecondsToWaitBetwneenActions = minimumSecondsToWaitBetwneenActions;
	}

	/**
	 * @return The maximum time (seconds) to wait between actions when executing the
	 *         test specification.
	 */
	public double getMaximumSecondsToWaitBetwneenActions() {
		return maximumSecondsToWaitBetwneenActions;
	}

	/**
	 * Updates the maximum time (seconds) to wait between actions when executing the
	 * test specification.
	 * 
	 * @param maximumSecondsToWaitBetwneenActions The new value (seconds).
	 */
	public void setMaximumSecondsToWaitBetwneenActions(double maximumSecondsToWaitBetwneenActions) {
		this.maximumSecondsToWaitBetwneenActions = maximumSecondsToWaitBetwneenActions;
	}

	/**
	 * @return The list of test actions.
	 */
	public TestAction[] getTestActions() {
		return testActions.toArray(new TestAction[testActions.size()]);
	}

	/**
	 * Updates the list of test actions.
	 * 
	 * @param testActions The new list.
	 */
	public void setTestActions(TestAction[] testActions) {
		this.testActions.clear();
		this.testActions.addAll(Arrays.asList(testActions));
	}

	/**
	 * Replays all test actions.
	 * 
	 * @return The execution report.
	 */
	public TestReport replayAll() {
		return replayAll(null);
	}

	/**
	 * Replays all test actions.
	 * 
	 * @param beforeEachAction An action to be executed before each test action or
	 *                         null.
	 * @return The execution report.
	 */
	public TestReport replayAll(Listener<TestAction> beforeEachAction) {
		return replay(testActions, beforeEachAction);
	}

	/**
	 * Replays only the given list of test actions.
	 * 
	 * @param toReplay         The list of test actions that will be executed.
	 * @param beforeEachAction An action to be executed before each test action or
	 *                         null.
	 * @return The execution report.
	 */
	public TestReport replay(final List<TestAction> toReplay, Listener<TestAction> beforeEachAction) {
		TestReport report = new TestReport();
		report.begin(this);
		for (int i = 0; i < toReplay.size(); i++) {
			final TestAction testAction = toReplay.get(i);
			logInfo("Replaying action no" + (i + 1) + ": " + testAction);
			TestReportStep reportStep = report.nextStep(testAction);
			reportStep.starting();
			try {
				if (beforeEachAction != null) {
					beforeEachAction.handle(testAction);
				}
				if (testAction.isDisabled()) {
					logInfo("Action disabled. Skipping...");
					reportStep.log("This action is disabled");
					reportStep.setStatus(TestReportStepStatus.SKIPPED);
				} else {
					try {
						reportStep.log("Action delayed for " + minimumSecondsToWaitBetwneenActions + " second(s)");
						Thread.sleep(Math.round(minimumSecondsToWaitBetwneenActions * 1000));
						orchestrateTestAction(testAction, reportStep);
						if (Thread.currentThread().isInterrupted()) {
							throw new InterruptedException();
						}
						reportStep.log("Action executed successfully");
						reportStep.setStatus(TestReportStepStatus.SUCCESSFUL);
					} catch (Throwable t) {
						if (t instanceof InterruptedException) {
							logInfo("Replay interrupted");
							reportStep.log("This action was interrupted");
							reportStep.setStatus(TestReportStepStatus.CANCELLED);
							break;
						} else {
							logError(t);
							reportStep.log("An error occurred: " + t.toString());
							reportStep.setStatus(TestReportStepStatus.FAILED);
							break;
						}
					}
				}

			} finally {
				reportStep.ending();
			}
		}
		try {
			Thread.sleep(Math.round(minimumSecondsToWaitBetwneenActions * 1000));
		} catch (InterruptedException ignore) {
		}
		report.end();
		return report;
	}

	/**
	 * Executes the phases (preparation, component search, execution) of a test
	 * action. Multiple attempts (period given by
	 * {@link #getSecondsToWaitBeforeRetryingToFindComponent()}) are made when there
	 * are failures.
	 * 
	 * @param testAction The current test action.
	 * @param reportStep The execution report step associated with the test action.
	 * @throws Throwable If despite of the multiple attempts the test action still
	 *                   throws an exception (which is rethrown).
	 */
	protected void orchestrateTestAction(TestAction testAction, TestReportStep reportStep) throws Throwable {
		testAction.validate();
		final long startTime = System.currentTimeMillis();
		final Throwable[] error = new Throwable[1];
		while (true) {
			error[0] = null;
			try {
				testAction.prepare(Tester.this);
			} catch (Throwable t) {
				error[0] = t;
			}
			if (error[0] == null) {
				MiscUtils.executeSafelyInUIThread(new Runnable() {
					@Override
					public void run() {
						try {
							Component c = testAction.findComponent(Tester.this);
							if (c == null) {
								reportStep.log("This action did not search for any component");
								reportStep.during(Tester.this);
							} else {
								reportStep.log("Component found: " + c.toString());
								orchestrateComponentHighlighting(c, new Runnable() {
									public void run() {
										reportStep.during(Tester.this);
									}
								});
							}
							testAction.execute(c, Tester.this);
						} catch (Throwable t) {
							error[0] = t;
							reportStep.during(Tester.this);
						}
					}
				});
				if (error[0] == null) {
					break;
				} else {
					reportStep.log("The action execution failed");
				}
			}
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			if (error[0] instanceof InterruptedException) {
				break;
			}
			if ((error[0] != null) && !(error[0] instanceof TestFailure)) {
				break;
			}
			double elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			double remainingSeconds = (maximumSecondsToWaitBetwneenActions - minimumSecondsToWaitBetwneenActions)
					- elapsedSeconds;
			if (remainingSeconds <= 0) {
				break;
			}
			Thread.sleep(Math.round(getSecondsToWaitBeforeRetryingToFindComponent() * 1000));
		}
		if (error[0] != null) {
			throw error[0];
		}
	}

	/**
	 * Highlights the given component for a short period (depends on
	 * {@link #getComponentHighlightingDurationSeconds()}).
	 * 
	 * @param c                  The component to highlight.
	 * @param duringHighlighting An action to be executed during the highlighting or
	 *                           null.
	 * @throws InterruptedException If the current thread is interrupted.
	 */
	protected void orchestrateComponentHighlighting(Component c, Runnable duringHighlighting)
			throws InterruptedException {
		currentComponent = c;
		highlightCurrentComponent();
		if (c instanceof JComponent) {
			MiscUtils.repaintImmediately((JComponent) c);
		}
		try {
			if (duringHighlighting != null) {
				duringHighlighting.run();
			}
			Thread.sleep(Math.round(getComponentHighlightingDurationSeconds() * 1000));
		} finally {
			unhighlightCurrentComponent();
			if (c instanceof JComponent) {
				MiscUtils.repaintImmediately((JComponent) c);
			}
			currentComponent = null;
		}
	}

	/**
	 * @return The duration of component highlighting during a replay.
	 */
	protected double getComponentHighlightingDurationSeconds() {
		return 0.25;
	}

	/**
	 * @return The delay between each attempt to execute a test action.
	 */
	protected double getSecondsToWaitBeforeRetryingToFindComponent() {
		return 1.0;
	}

	/**
	 * Formats the given message (used for logging).
	 * 
	 * @param msg The message to format.
	 * @return A formatted message.
	 */
	protected String formatLogMessage(String msg) {
		return SimpleDateFormat.getDateTimeInstance().format(new Date()) + " [" + Tester.this + "] " + msg;
	}

	/**
	 * Logs the given information message to the console output stream by default.
	 * 
	 * @param msg The message.
	 */
	public void logInfo(String msg) {
		System.out.println(formatLogMessage("INFO - " + msg));
	}

	/**
	 * Logs the given error message to the console error stream by default.
	 * 
	 * @param msg The message.
	 */
	public void logError(String msg) {
		System.err.println(formatLogMessage("ERROR - " + msg));
	}

	/**
	 * Logs the given exception to the console error stream by default.
	 * 
	 * @param t The exception.
	 */
	public void logError(Throwable t) {
		logError(xy.reflect.ui.util.MiscUtils.getPrintedStackTrace(t));
	}

	/**
	 * Restores the listeners removed by
	 * {@link #disableCurrentComponentListeners()}. Useful for the current component
	 * ({@link #getCurrentComponent()}) action/assertion recording or inspection.
	 */
	protected void restoreCurrentComponentListeners() {
		currentComponent.removeMouseListener(DUMMY_MOUSE_LISTENER_TO_ENSURE_EVENT_DISPATCH);
		for (MouseListener l : currentComponentMouseListeners) {
			currentComponent.addMouseListener(l);
		}
	}

	/**
	 * Disables the listeners of the current component
	 * ({@link #getCurrentComponent()}). Useful for the current component
	 * action/assertion recording or inspection.
	 */
	protected void disableCurrentComponentListeners() {
		currentComponentMouseListeners = currentComponent.getMouseListeners();
		for (int i = 0; i < currentComponentMouseListeners.length; i++) {
			currentComponent.removeMouseListener(currentComponentMouseListeners[i]);
		}
		currentComponent.addMouseListener(DUMMY_MOUSE_LISTENER_TO_ENSURE_EVENT_DISPATCH);
	}

	/**
	 * Highlights the current component ({@link #getCurrentComponent()}).
	 */
	protected void highlightCurrentComponent() {
		if (currentComponent instanceof JComponent) {
			currentComponentOpaque = ((JComponent) currentComponent).isOpaque();
			((JComponent) currentComponent).setOpaque(true);
		}

		currentComponentBackground = currentComponent.getBackground();
		currentComponent.setBackground(HIGHLIGHT_BACKGROUND);

		currentComponentForeground = currentComponent.getForeground();
		currentComponent.setForeground(HIGHLIGHT_FOREGROUND);

		if (currentComponent instanceof JComponent) {
			currentComponentBorder = ((JComponent) currentComponent).getBorder();
			try {
				((JComponent) currentComponent).setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(HIGHLIGHT_FOREGROUND, 1), currentComponentBorder));
			} catch (Throwable ignore) {
			}
		}
	}

	/**
	 * Remove the highlighting of the current component
	 * ({@link #getCurrentComponent()}).
	 */
	protected void unhighlightCurrentComponent() {
		if (currentComponent instanceof JComponent) {
			((JComponent) currentComponent).setOpaque(currentComponentOpaque);
		}
		currentComponent.setBackground(currentComponentBackground);
		currentComponent.setForeground(currentComponentForeground);
		if (currentComponent instanceof JComponent) {
			try {
				((JComponent) currentComponent).setBorder(currentComponentBorder);
			} catch (Throwable ignore) {
			}
		}
	}

	/**
	 * Loads a test specification file.
	 * 
	 * @param input The input file.
	 * @throws IOException If an error occurs during the loading process.
	 */
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

	/**
	 * Loads the test specification from a stream.
	 * 
	 * @param input The input stream.
	 */
	public void loadFromStream(InputStream input) throws IOException {
		XStream xstream = getXStream();
		Tester loaded = (Tester) xstream.fromXML(new InputStreamReader(input, "UTF-8"));
		testActions = loaded.testActions;
		minimumSecondsToWaitBetwneenActions = loaded.minimumSecondsToWaitBetwneenActions;
		maximumSecondsToWaitBetwneenActions = loaded.maximumSecondsToWaitBetwneenActions;
		editingOptions = loaded.editingOptions;
	}

	/**
	 * Saves the test specification to a stream.
	 * 
	 * @param output The output stream.
	 * @throws IOException If an error occurs during the saving process.
	 */
	public void saveToStream(OutputStream output) throws IOException {
		XStream xstream = getXStream();
		Tester toSave = new Tester();
		toSave.testActions = testActions;
		toSave.minimumSecondsToWaitBetwneenActions = minimumSecondsToWaitBetwneenActions;
		toSave.maximumSecondsToWaitBetwneenActions = maximumSecondsToWaitBetwneenActions;
		toSave.editingOptions = editingOptions;
		xstream.toXML(toSave, new OutputStreamWriter(output, "UTF-8"));
	}

	/**
	 * Saves the test specification to a file.
	 * 
	 * @param output The output file.
	 * @throws IOException If an error occurs during the saving process.
	 */
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

	protected XStream getXStream() {
		XStream result = new XStream();
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		result.addPermission(AnyTypePermission.ANY);
		result.ignoreUnknownElements();
		return result;
	}

	/**
	 * Prepares the given component for action/assertion recording or inspection
	 * (highlights it, adapts its event management) and sets it as the current
	 * component. It also restores the previous component state if there is any,
	 * 
	 * @param c The target component.
	 */
	public void handleCurrentComponentChange(Component c) {
		synchronized (CURRENT_COMPONENT_MUTEX) {
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
	}

	/**
	 * @param c The tested component.
	 * @return Whether a component is considered as visible or not during the
	 *         execution of test actions. This method is used instead of
	 *         {@link Component#isVisible()} by test actions. Can then be overridden
	 *         typically when testing custom components that need custom testing
	 *         behavior.
	 */
	public boolean isVisible(Component c) {
		if (c instanceof CellRendererPane) {
			return true;
		}
		return c.isVisible();
	}

	/**
	 * @param c The tested component.
	 * @return Whether a component is considered as testable or not. Normally all
	 *         components that are not part or owned by the test editor are
	 *         testable. This method is used by test actions. Can then be overridden
	 *         typically when testing custom components that need custom testing
	 *         behavior.
	 */
	public boolean isTestable(Component c) {
		for (TestEditor testEditor : TestingUtils.getTestEditors(this)) {
			if (TestingUtils.isTestEditorComponent(testEditor, c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param container The inspected container.
	 * @return The sorted list of children components of the given container. Note
	 *         that the position of a component among similar ones (needed by test
	 *         actions) is computed with this method.
	 */
	public List<Component> getChildComponents(Container container) {
		if (container instanceof JComboBox) {
			return new ArrayList<Component>();
		}
		List<Component> result = new ArrayList<Component>(Arrays.asList(container.getComponents()));
		Collections.sort(result, new Comparator<Component>() {
			@Override
			public int compare(Component c1, Component c2) {
				Rectangle bounds1 = c1.getBounds();
				Rectangle bounds2 = c2.getBounds();
				Point2D.Double location1 = new Point2D.Double(bounds1.getCenterX(), bounds1.getCenterY());
				Point2D.Double location2 = new Point2D.Double(bounds2.getCenterX(), bounds2.getCenterY());
				int result = 0;
				boolean verticalIntersection = new Rectangle(0, bounds1.y, 1, bounds1.height)
						.intersects(new Rectangle(0, bounds2.y, 1, bounds2.height));
				if (verticalIntersection) {
					result = 0;
				} else {
					result = new Double(location1.y).compareTo(new Double(location2.y));
					if (result != 0) {
						return result;
					}
				}
				boolean horizontalIntersection = new Rectangle(bounds1.x, 0, bounds1.width, 1)
						.intersects(new Rectangle(bounds2.x, 0, bounds2.width, 1));
				if (horizontalIntersection) {
					result = 0;
				} else {
					result = new Double(location1.x).compareTo(new Double(location2.x));
					if (result != 0) {
						return result;
					}
				}
				result = new Integer(container.getComponentZOrder(c1))
						.compareTo(new Integer(container.getComponentZOrder(c2)));
				if (result != 0) {
					return result;
				}
				result = new Integer(Arrays.asList(container.getComponents()).indexOf(c1))
						.compareTo(new Integer(Arrays.asList(container.getComponents()).indexOf(c2)));
				return result;
			}
		});
		return result;
	}

	/**
	 * @param c The inspected component.
	 * @return The list of strings displayed on the given component. This method is
	 *         used by test actions that depend on visible strings. Can then be
	 *         overridden typically when testing custom components that need custom
	 *         testing behavior.
	 */
	@SuppressWarnings({ "rawtypes" })
	public List<String> extractDisplayedStrings(final Component c) {
		if (c instanceof JComboBox) {
			final List<String> result = new ArrayList<String>();
			new SelectComboBoxItemAction() {
				private static final long serialVersionUID = 1L;
				{
					JComboBox comboBox = (JComboBox) c;
					int i = comboBox.getSelectedIndex();
					String text = getLabelText(comboBox.getModel(), comboBox.getRenderer(), i);
					if ((text != null) && (text.length() > 0)) {
						result.add(text);
					}
				}
			};
			String tooltipText = ((JComboBox) c).getToolTipText();
			if ((tooltipText != null) && (tooltipText.length() > 0)) {
				result.add(tooltipText);
			}
			HyperlinkTooltip hyperlinkTooltip = HyperlinkTooltip.get(c);
			if (hyperlinkTooltip != null) {
				result.add(hyperlinkTooltip.getMessage());
			}
			return result;
		}
		List<String> result = new ArrayList<String>();
		String s;
		s = extractDisplayedStringThroughMethod(c, "getTitle");
		if (s != null) {
			result.add(s);
		}
		s = extractDisplayedStringThroughMethod(c, "getText");
		if (s != null) {
			result.add(s);
		}
		s = extractDisplayedStringThroughMethod(c, "getToolTipText");
		if (s != null) {
			result.add(s);
		}
		HyperlinkTooltip hyperlinkTooltip = HyperlinkTooltip.get(c);
		if (hyperlinkTooltip != null) {
			result.add(hyperlinkTooltip.getMessage());
		}
		if (c instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) c;
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				result.add(tabbedPane.getTitleAt(i));
			}
		}
		if (c instanceof JComponent) {
			Border border = ((JComponent) c).getBorder();
			if (border != null) {
				result.addAll(extracDisplayedStringsFromBorder(border));
			}
		}
		if (c instanceof JTable) {
			JTable table = (JTable) c;
			result.addAll(extractDisplayedStringsFromTable(table));
		}
		if (c instanceof JTree) {
			JTree tree = (JTree) c;
			result.addAll(extractDisplayedStringsFromTree(tree));
		}
		if (c instanceof JList) {
			JList list = (JList) c;
			result.addAll(extractDisplayedStringsFromList(list));
		}
		result.removeAll(Arrays.asList(""));
		return result;
	}

	protected List<String> extracDisplayedStringsFromBorder(Border border) {
		List<String> result = new ArrayList<String>();
		if (border instanceof TitledBorder) {
			String s = ((TitledBorder) border).getTitle();
			if ((s != null) && (s.trim().length() > 0)) {
				result.add(s);
			}
		} else if (border instanceof CompoundBorder) {
			Border insideBorder = ((CompoundBorder) border).getInsideBorder();
			if (insideBorder != null) {
				result.addAll(extracDisplayedStringsFromBorder(insideBorder));
			}
			Border outsideBorder = ((CompoundBorder) border).getOutsideBorder();
			if (outsideBorder != null) {
				result.addAll(extracDisplayedStringsFromBorder(outsideBorder));
			}
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<String> extractDisplayedStringsFromList(JList list) {
		List<String> result = new ArrayList<String>();
		ListModel model = list.getModel();
		ListCellRenderer cellRenderer = list.getCellRenderer();
		for (int i = 0; i < model.getSize(); i++) {
			try {
				Object item = model.getElementAt(i);
				Component cellComponent = cellRenderer.getListCellRendererComponent(list, item, i, false, false);
				result.addAll(extractDisplayedStrings(cellComponent));
			} catch (Exception ignore) {
			}
		}
		return result;
	}

	protected List<String> extractDisplayedStringsFromTable(JTable table) {
		List<String> result = new ArrayList<String>();
		TableModel model = table.getModel();
		String s;
		for (int i = 0; i < model.getColumnCount(); i++) {
			s = model.getColumnName(i);
			if ((s != null) && (s.trim().length() > 0)) {
				result.add(s);
			}
		}
		for (int iRow = 0; iRow < model.getRowCount(); iRow++) {
			for (int iCol = 0; iCol < model.getColumnCount(); iCol++) {
				try {
					Object cellValue = model.getValueAt(iRow, iCol);
					if (cellValue instanceof String) {
						result.add((String) cellValue);
					} else {
						TableCellRenderer cellRenderer = table.getCellRenderer(iRow, iCol);
						Component cellComponent = cellRenderer.getTableCellRendererComponent(table, cellValue, false,
								false, iRow, iCol);
						List<String> cellVisibleStrings = extractDisplayedStrings(cellComponent);
						result.addAll(cellVisibleStrings);
					}
				} catch (Exception ignore) {
				}
			}
		}
		return result;
	}

	protected List<String> extractDisplayedStringsFromTree(JTree tree) {
		List<String> result = new ArrayList<String>();
		result.addAll(extractDisplayedStringsFromTree(0, tree.getModel().getRoot(), tree));
		return result;
	}

	protected List<String> extractDisplayedStringsFromTree(int currentRow, Object currentNode, JTree tree) {
		List<String> result = new ArrayList<String>();
		TreeModel model = tree.getModel();
		try {
			String s = tree.convertValueToText(currentNode, false, true, model.isLeaf(currentNode), currentRow, false);
			if ((s != null) && (s.trim().length() > 0)) {
				result.add(s);
			}
		} catch (Exception ignore) {
		}
		for (int i = 0; i < model.getChildCount(currentNode); i++) {
			Object childNode = model.getChild(currentNode, i);
			result.addAll(extractDisplayedStringsFromTree(currentRow + 1, childNode, tree));
		}
		return result;
	}

	protected String extractDisplayedStringThroughMethod(Component c, String methodName) {
		try {
			Method method = c.getClass().getMethod(methodName);
			String result = (String) method.invoke(c);
			if (result == null) {
				return null;
			}
			if (result.trim().length() == 0) {
				return null;
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return The global options object.
	 */
	public EditingOptions getEditingOptions() {
		return editingOptions;
	}

	/**
	 * Updates the global options object.
	 * 
	 * @param editingOptions The new global options object.
	 */
	public void setEditingOptions(EditingOptions editingOptions) {
		this.editingOptions = editingOptions;
	}

	/**
	 * Global options about test specification and execution.
	 * 
	 * @author olitank
	 *
	 */
	public static class EditingOptions {

		protected boolean testableWindowsAlwaysOnTopFeatureDisabled = true;
		protected boolean testableModalWindowsForcedToDocumentModality = true;
		protected ControlsTheme controlsTheme = ControlsTheme.classic;

		/**
		 * @return The current test editor window theme.
		 */
		public ControlsTheme getControlsTheme() {
			return controlsTheme;
		}

		/**
		 * Updates the test editor window theme.
		 * 
		 * @param controlsTheme The new theme.
		 */
		public void setControlsTheme(ControlsTheme controlsTheme) {
			this.controlsTheme = controlsTheme;
		}

		/**
		 * @return Whether the "always on top" feature should be disabled on testable
		 *         windows (all windows except the tester windows) during a test
		 *         execution. Note that this feature may cause some issues during the
		 *         test execution.
		 */
		public boolean isTestableWindowsAlwaysOnTopFeatureDisabled() {
			return testableWindowsAlwaysOnTopFeatureDisabled;
		}

		/**
		 * Updates whether the "always on top" feature should be disabled on testable
		 * windows (all windows except the tester windows) during a test execution. Note
		 * that this feature may cause some issues during the test execution.
		 * 
		 * @param testableWindowsAlwaysOnTopFeatureDisabled The new flag.
		 */
		public void setTestableWindowsAlwaysOnTopFeatureDisabled(boolean testableWindowsAlwaysOnTopFeatureDisabled) {
			this.testableWindowsAlwaysOnTopFeatureDisabled = testableWindowsAlwaysOnTopFeatureDisabled;
		}

		/**
		 * @return Whether the modality type should be set to
		 *         {@link ModalityType#DOCUMENT_MODAL} on testable dialogs (all dialogs
		 *         except the tester dialogs) during a test execution. Note that other
		 *         modality types may cause some issues during the test execution.
		 */
		public boolean isTestableModalWindowsForcedToDocumentModality() {
			return testableModalWindowsForcedToDocumentModality;
		}

		/**
		 * Updates whether the modality type should be set to
		 * {@link ModalityType#DOCUMENT_MODAL} on testable dialogs (all dialogs except
		 * the tester dialogs) during a test execution. Note that other modality types
		 * may cause some issues during the test execution.
		 * 
		 * @param testableModalWindowsForcedToDocumentModality The new flag.
		 */
		public void setTestableModalWindowsForcedToDocumentModality(
				boolean testableModalWindowsForcedToDocumentModality) {
			this.testableModalWindowsForcedToDocumentModality = testableModalWindowsForcedToDocumentModality;
		}

	}

	/**
	 * Available themes.
	 * 
	 * @author olitank
	 *
	 */
	public enum ControlsTheme {
		classic, versatile, cloudy, gradient
	}

}
