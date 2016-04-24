package xy.ui.testing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.ui.testing.action.TestAction;
import xy.ui.testing.util.Listener;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;

public class Tester {

	public static final Tester DEFAULT = new Tester();

	public static final Color HIGHLIGHT_FOREGROUND = TestingUtils.stringToColor(
			System.getProperty(Tester.class.getPackage().getName() + ".highlightForeground", "235,48,33"));
	public static final Color HIGHLIGHT_BACKGROUND = TestingUtils.stringToColor(
			System.getProperty(Tester.class.getPackage().getName() + ".highlightBackground", "245,216,214"));

	protected List<TestAction> testActions = new ArrayList<TestAction>();
	protected int minimumSecondsToWaitBetwneenActions = 2;
	protected int maximumSecondsToWaitBetwneenActions = 15;

	protected Component currentComponent;
	protected Color currentComponentBackground;
	protected Color currentComponentForeground;
	protected MouseListener[] currentComponentMouseListeners;
	protected Border currentComponentBorder;

	public Tester() {
	}

	public static void assertSuccessfulReplay(File replayFile) throws IOException {
		Tester tester = new Tester();
		tester.loadFromFile(replayFile);
		tester.playAll();
	}

	public static void assertSuccessfulReplay(InputStream replayStream) throws IOException {
		Tester tester = new Tester();
		tester.loadFromStream(replayStream);
		tester.playAll();
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

	public void playAll() {
		playAll(null);
	}

	public void playAll(Listener<TestAction> beforeEachAction) {
		play(testActions, beforeEachAction);
	}

	public void play(final List<TestAction> toReplay, Listener<TestAction> beforeEachAction) {
		for (int i = 0; i < toReplay.size(); i++) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			final TestAction testAction = toReplay.get(i);
			try {
				if (beforeEachAction != null) {
					beforeEachAction.handle(testAction);
				}
				Thread.sleep(minimumSecondsToWaitBetwneenActions * 1000);
				Component c = findComponentImmediatelyOrRetry(testAction);
				if (c != null) {
					currentComponent = c;
					highlightCurrentComponent();
					Thread.sleep(1000);
					unhighlightCurrentComponent();
					currentComponent = null;
				}
				testAction.validate();
				testAction.execute(c, this);
			} catch (Throwable t) {
				if (t instanceof InterruptedException) {
					if (currentComponent != null) {
						unhighlightCurrentComponent();
						currentComponent = null;
					}
					break;
				}
				throw new TestFailure("Test Action n°" + (testActions.indexOf(testAction) + 1) + ": " + t.toString(),
						t);
			}
		}
		try {
			Thread.sleep(minimumSecondsToWaitBetwneenActions * 1000);
		} catch (InterruptedException ignore) {
		}		
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
		for (MouseListener l : currentComponentMouseListeners) {
			currentComponent.addMouseListener(l);
		}
	}

	protected void disableCurrentComponentListeners() {
		currentComponentMouseListeners = currentComponent.getMouseListeners();
		while (currentComponent.getMouseListeners().length > 0) {
			currentComponent.removeMouseListener(currentComponent.getMouseListeners()[0]);
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
			currentComponentBorder = ((JComponent) currentComponent).getBorder();
			((JComponent) currentComponent).setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(HIGHLIGHT_FOREGROUND, 1), currentComponentBorder));
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
		XStream xstream = getXStream();
		xstream.toXML(this, output);
	}

	protected XStream getXStream() {
		XStream result = new XStream();
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		return result;
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

}
