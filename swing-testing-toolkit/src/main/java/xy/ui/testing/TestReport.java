package xy.ui.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import com.thoughtworks.xstream.XStream;

import xy.ui.testing.action.TestAction;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;

@SuppressWarnings("unused")
public class TestReport {

	protected static final String ALL_REPORTS_DIRECTORY_PROPERTY_KEY = "xy.ui.testing.reportsDirectory";

	protected List<TestReportStep> steps = new ArrayList<TestReport.TestReportStep>();
	protected int numberOfActions;
	protected Date instantiationDate = new Date();
	protected String directoryPath;

	public void begin(Tester tester) {
		this.numberOfActions = tester.getTestActions().length;
		this.directoryPath = buildDirectoryPath(tester);
		requireDirectory();
		try {
			tester.saveToFile(getSpecificationCopyFile());
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public void end() {
		try {
			saveToFile(getMainFile());
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	protected String buildDirectoryPath(Tester tester) {
		String formattedInstanciationDate = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(instantiationDate);
		String fileName = "test-report-" + formattedInstanciationDate + "-" + tester;
		File file = new File(getAllTestReportsDirectory(), fileName);
		return TestingUtils.getOSAgnosticFilePath(file.getPath());
	}

	public static File getAllTestReportsDirectory() {
		String path = System.getProperty(ALL_REPORTS_DIRECTORY_PROPERTY_KEY, "test-reports");
		return new File(path);
	}

	public File getDirectory() {
		if (directoryPath == null) {
			return null;
		} else {
			return new File(directoryPath);
		}
	}

	public void setDirectory(File directory) {
		if (directory == null) {
			directoryPath = null;
		} else {
			directoryPath = TestingUtils.getOSAgnosticFilePath(directory.getPath());
		}
	}

	protected void requireDirectory() {
		File dir;
		dir = getAllTestReportsDirectory();
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new AssertionError("Failed to create the directory: '" + dir.getAbsolutePath() + "'");
			}
		}
		dir = getDirectory();
		if (!dir.exists()) {
			if (!dir.mkdir()) {
				throw new AssertionError("Failed to create the directory: '" + dir.getAbsolutePath() + "'");
			}
		}
	}

	public File getMainFile() {
		return new File(getDirectory(), "main.str");
	}

	public File getSpecificationCopyFile() {
		return new File(getDirectory(), "copy.stt");
	}

	public TestReportStep nextStep(TestAction testAction) {
		TestReportStep result = new TestReportStep(testAction);
		steps.add(result);
		return result;
	}

	public TestReportStep[] getSteps() {
		return steps.toArray(new TestReportStep[steps.size()]);
	}

	public int getNumberOfActions() {
		return numberOfActions;
	}

	public int getCompletionPercentage() {
		return Math.round(100 * ((float) steps.size() / (float) numberOfActions));
	}

	public TestReportStepStatus getFinalStatus() {
		if (steps.size() == 0) {
			return null;
		}
		TestReportStepStatus result = steps.get(steps.size() - 1).getStatus();
		if (result == TestReportStepStatus.SKIPPED) {
			result = TestReportStepStatus.SUCCESSFUL;
		}
		return result;
	}

	public String getSummary() {
		if (steps.size() == 0) {
			return "";
		} else {
			return getFinalStatus() + "\n" + getCompletionPercentage() + "% Completed";
		}
	}

	public String getLastLogs() {
		if (steps.size() == 0) {
			return null;
		} else {
			return steps.get(steps.size() - 1).getLogs();
		}
	}

	protected XStream getXStream() {
		XStream result = new XStream();
		return result;
	}

	public void loadFromFile(File mainReportFile) throws IOException {
		FileInputStream stream = new FileInputStream(mainReportFile);
		try {
			loadFromStream(stream);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
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

	public void loadFromStream(InputStream input) {
		XStream xstream = getXStream();
		TestReport loaded = (TestReport) xstream.fromXML(input);
		steps = new ArrayList<TestReport.TestReportStep>();
		for (TestReportStep loadedStep : loaded.steps) {
			TestReportStep step = new TestReportStep(loadedStep.actionSummary);
			step.startTimestamp = loadedStep.startTimestamp;
			step.endTimestamp = loadedStep.endTimestamp;
			step.logs = loadedStep.logs;
			step.status = loadedStep.status;
			step.windowsImageFileName = loadedStep.windowsImageFileName;
			steps.add(step);
		}
		numberOfActions = loaded.numberOfActions;
		directoryPath = loaded.directoryPath;
	}

	public void saveToStream(OutputStream output) throws IOException {
		XStream xstream = getXStream();
		TestReport toSave = new TestReport();
		toSave.steps = steps;
		toSave.numberOfActions = numberOfActions;
		toSave.directoryPath = directoryPath;
		xstream.toXML(toSave, output);
	}

	public enum TestReportStepStatus {
		SUCCESSFUL, FAILED, SKIPPED, CANCELLED
	}

	public class TestReportStep {

		protected TestReportStepStatus status;
		protected long startTimestamp;
		protected long endTimestamp;
		protected String windowsImageFileName;
		protected String actionSummary;
		protected List<String> logs = new ArrayList<String>();

		public TestReportStep(TestAction testAction) {
			this(testAction.toString());
		}

		public TestReportStep(String actionSummary) {
			this.actionSummary = actionSummary;
		}

		public TestReportStepStatus getStatus() {
			return status;
		}

		public void setStatus(TestReportStepStatus status) {
			this.status = status;
		}

		public long getStartTimestamp() {
			return startTimestamp;
		}

		public long getEndTimestamp() {
			return endTimestamp;
		}

		public File getWindowsImageFile() {
			if (windowsImageFileName == null) {
				return null;
			}
			return new File(getDirectory(), windowsImageFileName);
		}

		public String getActionSummary() {
			return actionSummary;
		}

		public String getLogs() {
			StringBuilder result = new StringBuilder();
			int i = 0;
			for (String log : logs) {
				if (i > 0) {
					result.append("\n");
				}
				result.append("- " + log);
				i++;
			}
			return result.toString();
		}

		public void starting() {
			startTimestamp = System.currentTimeMillis();
		}

		public void during(final Tester tester) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					File file = TestingUtils.saveAllTestableWindowsScreenshot(tester, getDirectory());
					if (file == null) {
						windowsImageFileName = null;
					} else {
						windowsImageFileName = file.getName();
					}
				}
			});

		}

		public void ending() {
			endTimestamp = System.currentTimeMillis();
		}

		public void log(String msg) {
			logs.add(msg);
		}

	}

}
