package xy.ui.testing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.TestReport.TestReportStep;
import xy.ui.testing.TestReport.TestReportStepStatus;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.Listener;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;

public class Tester {

	public static final transient Color HIGHLIGHT_FOREGROUND = TestingUtils.stringToColor(
			System.getProperty(Tester.class.getPackage().getName() + ".highlightForeground", "235,48,33"));
	public static final transient Color HIGHLIGHT_BACKGROUND = TestingUtils.stringToColor(
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
	protected int minimumSecondsToWaitBetwneenActions = 2;
	protected int maximumSecondsToWaitBetwneenActions = 15;
	protected EditingOptions editingOptions = new EditingOptions();

	protected transient Component currentComponent;
	protected transient Color currentComponentBackground;
	protected transient Color currentComponentForeground;
	protected transient MouseListener[] currentComponentMouseListeners;
	protected transient Border currentComponentBorder;

	public Tester() {
	}

	public Component getCurrentComponent() {
		return currentComponent;
	}

	public int getMinimumSecondsToWaitBetwneenActions() {
		return minimumSecondsToWaitBetwneenActions;
	}

	public void setMinimumSecondsToWaitBetwneenActions(int minimumSecondsToWaitBetwneenActions) {
		this.minimumSecondsToWaitBetwneenActions = minimumSecondsToWaitBetwneenActions;
	}

	public int getMaximumSecondsToWaitBetwneenActions() {
		return maximumSecondsToWaitBetwneenActions;
	}

	public void setMaximumSecondsToWaitBetwneenActions(int maximumSecondsToWaitBetwneenActions) {
		this.maximumSecondsToWaitBetwneenActions = maximumSecondsToWaitBetwneenActions;
	}

	public TestAction[] getTestActions() {
		return testActions.toArray(new TestAction[testActions.size()]);
	}

	public void setTestActions(TestAction[] testActions) {
		this.testActions.clear();
		this.testActions.addAll(Arrays.asList(testActions));
	}

	public TestReport replayAll() {
		return replayAll(null);
	}

	public TestReport replayAll(Listener<TestAction> beforeEachAction) {
		return replay(testActions, beforeEachAction);
	}

	public TestReport replay(final List<TestAction> toReplay, Listener<TestAction> beforeEachAction) {
		TestReport report = new TestReport();
		report.begin(this);
		for (int i = 0; i < toReplay.size(); i++) {
			final TestAction testAction = toReplay.get(i);
			logInfo("Replaying action no" + (i + 1) + ": " + testAction);
			TestReportStep reportStep = report.nextStep(testAction);
			reportStep.starting();
			try {
				if (Thread.currentThread().isInterrupted()) {
					logInfo("Action interrupted");
					reportStep.setStatus(TestReportStepStatus.CANCELLED);
					break;
				}
				try {
					if (beforeEachAction != null) {
						beforeEachAction.handle(testAction);
					}
					if (testAction.isDisabled()) {
						logInfo("Action disabled. Skipping...");
						reportStep.log("This action is disabled");
						reportStep.setStatus(TestReportStepStatus.SKIPPED);
					} else {
						Thread.sleep(minimumSecondsToWaitBetwneenActions * 1000);
						reportStep.log("Action delayed for " + minimumSecondsToWaitBetwneenActions + " second(s)");
						testAction.validate();
						Component c;
						try {
							c = findComponentImmediatelyOrRetry(testAction);
						} catch (Throwable t) {
							reportStep.during(this);
							throw t;
						}
						if (c == null) {
							reportStep.log("This action did not search for any component");
							reportStep.during(this);
						} else {
							reportStep.log("Component found: " + c.toString());
							currentComponent = c;
							highlightCurrentComponent();
							reportStep.during(this);
							try {
								Thread.sleep(1000);
							} finally {
								unhighlightCurrentComponent();
								currentComponent = null;
							}
						}
						testAction.execute(c, this);
						reportStep.setStatus(TestReportStepStatus.SUCCESSFUL);
					}
				} catch (Throwable t) {
					if (t instanceof InterruptedException) {
						reportStep.setStatus(TestReportStepStatus.CANCELLED);
						break;
					}
					logError(t);
					reportStep.log("An error occured: " + t.toString());
					reportStep.setStatus(TestReportStepStatus.FAILED);
					break;
				}
			} finally {
				reportStep.ending();
			}
		}
		try {
			Thread.sleep(minimumSecondsToWaitBetwneenActions * 1000);
		} catch (InterruptedException ignore) {
		}
		report.end();
		return report;
	}

	protected String formatLogMessage(String msg) {
		return SimpleDateFormat.getDateTimeInstance().format(new Date()) + " [" + Tester.this + "] " + msg;
	}

	public void logInfo(String msg) {
		System.out.println(formatLogMessage("INFO - " + msg));
	}

	public void logError(String msg) {
		System.err.println(formatLogMessage("ERROR - " + msg));
	}

	public void logError(Throwable t) {
		logError(ReflectionUIUtils.getPrintedStackTrace(t));
	}

	protected Component findComponentImmediatelyOrRetry(TestAction testAction) {
		Component result = null;
		int remainingSeconds = maximumSecondsToWaitBetwneenActions - minimumSecondsToWaitBetwneenActions;
		while (true) {
			try {
				result = testAction.findComponent(this);
				break;
			} catch (TestFailure e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignore) {
					throw e;
				}
				remainingSeconds--;
				if (remainingSeconds == 0) {
					throw e;
				}
			}
		}
		return result;
	}

	protected String getComponentSelectionActionTitle() {
		return "Left Click";
	}

	protected void restoreCurrentComponentListeners() {
		currentComponent.removeMouseListener(DUMMY_MOUSE_LISTENER_TO_ENSURE_EVENT_DISPATCH);
		for (MouseListener l : currentComponentMouseListeners) {
			currentComponent.addMouseListener(l);
		}
	}

	protected void disableCurrentComponentListeners() {
		currentComponentMouseListeners = currentComponent.getMouseListeners();
		for (int i = 0; i < currentComponentMouseListeners.length; i++) {
			currentComponent.removeMouseListener(currentComponentMouseListeners[i]);
		}
		currentComponent.addMouseListener(DUMMY_MOUSE_LISTENER_TO_ENSURE_EVENT_DISPATCH);
	}

	protected void unhighlightCurrentComponent() {
		currentComponent.setBackground(currentComponentBackground);
		currentComponent.setForeground(currentComponentForeground);
		if (currentComponent instanceof JComponent) {
			try {
				((JComponent) currentComponent).setBorder(currentComponentBorder);
			} catch (Throwable ignore) {
			}
		}
	}

	protected void highlightCurrentComponent() {
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
		XStream xstream = getXStream();
		Tester loaded = (Tester) xstream.fromXML(input);
		testActions = loaded.testActions;
		minimumSecondsToWaitBetwneenActions = loaded.minimumSecondsToWaitBetwneenActions;
		maximumSecondsToWaitBetwneenActions = loaded.maximumSecondsToWaitBetwneenActions;
	}

	public void saveToStream(OutputStream output) throws IOException {
		XStream xstream = getXStream();
		Tester toSave = new Tester();
		toSave.testActions = testActions;
		toSave.minimumSecondsToWaitBetwneenActions = minimumSecondsToWaitBetwneenActions;
		toSave.maximumSecondsToWaitBetwneenActions = maximumSecondsToWaitBetwneenActions;
		xstream.toXML(toSave, output);
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

	protected XStream getXStream() {
		XStream result = new XStream();
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		return result;
	}

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

	public boolean isVisible(Component c) {
		if (c instanceof CellRendererPane) {
			return true;
		}
		return c.isVisible();
	}

	public boolean isTestable(Component c) {
		for (TestEditor testEditor : TestingUtils.getTestEditors(this)) {
			if (TestingUtils.isTestEditorComponent(testEditor, c)) {
				return false;
			}
		}
		return true;
	}

	public List<Component> getChildrenComponents(Container container) {
		List<Component> result = new ArrayList<Component>();
		for (Component c : container.getComponents()) {
			result.add(c);
		}
		result = new ArrayList<Component>(result);
		Collections.sort(result, new Comparator<Component>() {
			@Override
			public int compare(Component c1, Component c2) {
				Point location1 = c1.getLocation();
				Point location2 = c2.getLocation();
				int result = new Integer(location1.y).compareTo(new Integer(location2.y));
				if (result == 0) {
					result = new Integer(location1.x).compareTo(new Integer(location2.x));
				}
				return result;
			}
		});
		return result;
	}

	public List<String> extractDisplayedStrings(Component c) {
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
		if (c instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) c;
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				result.add(tabbedPane.getTitleAt(i));
			}
		}
		if (c instanceof JComponent) {
			Border border = ((JComponent) c).getBorder();
			if (border != null) {
				s = extracDisplayedStringFromBorder(border);
				if ((s != null) && (s.trim().length() > 0)) {
					result.add(s);
				}
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

	protected String extracDisplayedStringFromBorder(Border border) {
		if (border instanceof TitledBorder) {
			String s = ((TitledBorder) border).getTitle();
			if ((s != null) && (s.trim().length() > 0)) {
				return s;
			}
		}
		return null;
	}

	protected Collection<String> extractDisplayedStringsFromList(JList list) {
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
					TableCellRenderer cellRenderer = table.getCellRenderer(iRow, iCol);
					Component cellComponent = cellRenderer.getTableCellRendererComponent(table, cellValue, false, false,
							iRow, iCol);
					List<String> cellVisibleStrings = extractDisplayedStrings(cellComponent);
					result.addAll(cellVisibleStrings);
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

	public EditingOptions getEditingOptions() {
		return editingOptions;
	}

	public static class EditingOptions {

		protected boolean testableWindowsAlwaysOnTopFeatureDisabled = true;
		protected boolean testableModalWindowsForcedToDocumentModality = true;

		public boolean isTestableWindowsAlwaysOnTopFeatureDisabled() {
			return testableWindowsAlwaysOnTopFeatureDisabled;
		}

		public void setTestableWindowsAlwaysOnTopFeatureDisabled(boolean testableWindowsAlwaysOnTopFeatureDisabled) {
			this.testableWindowsAlwaysOnTopFeatureDisabled = testableWindowsAlwaysOnTopFeatureDisabled;
		}

		public boolean isTestableModalWindowsForcedToDocumentModality() {
			return testableModalWindowsForcedToDocumentModality;
		}

		public void setTestableModalWindowsForcedToDocumentModality(
				boolean testableModalWindowsForcedToDocumentModality) {
			this.testableModalWindowsForcedToDocumentModality = testableModalWindowsForcedToDocumentModality;
		}

	}

}
