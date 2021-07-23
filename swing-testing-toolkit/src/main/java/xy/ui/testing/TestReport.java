package xy.ui.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.XStream;

import xy.ui.testing.action.TestAction;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestingUtils;

/**
 * The test execution report class. It essentially holds {@link TestReportStep}
 * objects. Note that a report instance is normally saved to multiples files in
 * a dedicated directory at the end of the test execution.
 * 
 * @author olitank
 *
 */
public class TestReport {

	protected static final String ALL_REPORTS_DIRECTORY_PROPERTY_KEY = "xy.ui.testing.reportsDirectory";

	protected List<TestReportStep> steps = new ArrayList<TestReport.TestReportStep>();
	protected int numberOfActions;
	protected Date instantiationDate = new Date();
	protected String directoryPath;

	/**
	 * Must be called before starting the test session to initialize properly the
	 * report.
	 * 
	 * @param tester The object responsible for the test execution.
	 */
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

	/**
	 * Must be called after the test session to finalize properly the report.
	 */
	public void end() {
		try {
			saveMainFile();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	protected String buildDirectoryPath(Tester tester) {
		String formattedInstanciationDate = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(instantiationDate);
		String fileName = "test-report-" + formattedInstanciationDate + "-" + tester;
		File file = new File(getAllTestReportsDirectory(), fileName);
		return MiscUtils.getOSAgnosticFilePath(file.getPath());
	}

	/**
	 * @return The directory where all reports are stored.
	 */
	public static File getAllTestReportsDirectory() {
		String path = System.getProperty(ALL_REPORTS_DIRECTORY_PROPERTY_KEY, "test-reports");
		return new File(path);
	}

	/**
	 * @return The directory where the current report is or will be stored.
	 */
	public File getDirectory() {
		if (directoryPath == null) {
			return null;
		} else {
			return new File(directoryPath);
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

	/**
	 * @return The main file referencing the other files of the report.
	 */
	public File getMainFile() {
		return new File(getDirectory(), "main.str");
	}

	/**
	 * @return The private file copy of the test specification that was executed to
	 *         produce this report.
	 */
	public File getSpecificationCopyFile() {
		return new File(getDirectory(), "copy.stt");
	}

	/**
	 * Must be called during the test execution before executing each action.
	 * 
	 * @param testAction The next test action.
	 * @return the execution step that will be used for the given test action.
	 */
	public TestReportStep nextStep(TestAction testAction) {
		TestReportStep result = new TestReportStep(testAction);
		steps.add(result);
		return result;
	}

	/**
	 * @return The list of recorded execution steps.
	 */
	public TestReportStep[] getSteps() {
		return steps.toArray(new TestReportStep[steps.size()]);
	}

	/**
	 * @return The total number of actions in the test specification.
	 */
	public int getNumberOfActions() {
		return numberOfActions;
	}

	/**
	 * @return The percentage of executed test actions according to the total number
	 *         of test actions in the test specification.
	 */
	public int getCompletionPercentage() {
		return Math.round(100 * ((float) steps.size() / (float) numberOfActions));
	}

	/**
	 * @return An enumeration item summarizing the execution of the test or nul if
	 *         there is no recorded execution step.
	 */
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

	/**
	 * @return A text summarizing the execution of the test.
	 */
	public String getSummary() {
		if (steps.size() == 0) {
			return "";
		} else {
			return getFinalStatus() + "\n" + getCompletionPercentage() + "% Completed";
		}
	}

	/**
	 * @return The last logs recorded in the report. In case of test failure they
	 *         usually provide useful information.
	 */
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

	/**
	 * Loads the report from the given directory.
	 * 
	 * @param directory The directory where the report is stored.
	 * @throws IOException If a problem occurs during the loading process.
	 */
	public void load(File directory) throws IOException {
		this.directoryPath = directory.getPath();
		FileInputStream stream = new FileInputStream(getMainFile());
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
	 * Saves the report index.
	 * 
	 * @throws IOException If a problem occurs during the saving process.
	 */
	protected void saveMainFile() throws IOException {
		FileOutputStream stream = new FileOutputStream(getMainFile());
		try {
			saveToStream(stream);
		} finally {
			try {
				stream.close();
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * Loads the report from a stream.
	 * 
	 * @param input The input stream.
	 */
	public void loadFromStream(InputStream input)  throws IOException{
		XStream xstream = getXStream();
		TestReport loaded = (TestReport) xstream.fromXML(new InputStreamReader(input, "UTF-8"));
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
	}

	/**
	 * Saves the report to a stream.
	 * 
	 * @param output The output stream.
	 * @throws IOException If a problem occurs during the saving process.
	 */
	public void saveToStream(OutputStream output) throws IOException {
		XStream xstream = getXStream();
		TestReport toSave = new TestReport();
		toSave.steps = steps;
		toSave.numberOfActions = numberOfActions;
		xstream.toXML(toSave, new OutputStreamWriter(output, "UTF-8"));
	}

	/**
	 * Possible statuses of an executed test action.
	 * 
	 * @author olitank
	 *
	 */
	public enum TestReportStepStatus {
		/**
		 * The action was successful.
		 */
		SUCCESSFUL,
		/**
		 * The action failed.
		 */
		FAILED,
		/**
		 * The action was skipped.
		 */
		SKIPPED,
		/**
		 * The action was cancelled.
		 */
		CANCELLED
	}

	/**
	 * Single test action execution report class.
	 * 
	 * @author olitank
	 *
	 */
	public class TestReportStep {

		protected TestReportStepStatus status;
		protected long startTimestamp;
		protected long endTimestamp;
		protected String windowsImageFileName;
		protected String actionSummary;
		protected List<String> logs = new ArrayList<String>();

		/**
		 * Builds an instance for the specified action. This constructor only stores the
		 * specified action textual representation.
		 * 
		 * @param testAction The reported action.
		 */
		public TestReportStep(TestAction testAction) {
			this(testAction.toString());
		}

		/**
		 * The main constructor.
		 * 
		 * @param actionSummary A string summarizing the reported action.
		 */
		public TestReportStep(String actionSummary) {
			this.actionSummary = actionSummary;
		}

		/**
		 * @return The status of the execution of the test action.
		 */
		public TestReportStepStatus getStatus() {
			return status;
		}

		/**
		 * Updates the status of the execution of the test action.
		 * 
		 * @param status The new status.
		 */
		public void setStatus(TestReportStepStatus status) {
			this.status = status;
		}

		/**
		 * @return The timestamp of the the test action execution.
		 */
		public long getStartTimestamp() {
			return startTimestamp;
		}

		/**
		 * @return The timestamp of the the test action execution end.
		 */
		public long getEndTimestamp() {
			return endTimestamp;
		}

		/**
		 * @return The screenshot file of the window(s) on which the test action was
		 *         performed. The tested component should be highlighted in this
		 *         screenshot.
		 */
		public File getWindowsImageFile() {
			if (windowsImageFileName == null) {
				return null;
			}
			return new File(getDirectory(), windowsImageFileName);
		}

		/**
		 * @return A string summarizing the reported action.
		 */
		public String getActionSummary() {
			return actionSummary;
		}

		/**
		 * @return The logs recorded during the execution of the test action.
		 */
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

		/**
		 * Must be called just before executing the test action.
		 */
		public void starting() {
			startTimestamp = System.currentTimeMillis();
		}

		/**
		 * Must be called during the processing (by the UI thread) of the UI event that
		 * executes the test action.
		 * 
		 * @param tester The object responsible for the test execution.
		 */
		public void during(final Tester tester) {
			MiscUtils.expectingToBeInUIThread(new Runnable() {
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

		/**
		 * Must be called just after the test action execution end.
		 */
		public void ending() {
			endTimestamp = System.currentTimeMillis();
		}

		/**
		 * Adds a test action execution log message.
		 * 
		 * @param msg The log message.
		 */
		public void log(String msg) {
			logs.add(SimpleDateFormat.getDateTimeInstance().format(new Date()) + " - " + msg);
		}

	}

}
