package xy.ui.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;

import xy.ui.testing.action.TestAction;
import xy.ui.testing.util.TestingUtils;

public class TestReport {

	protected List<TestReportStep> steps = new ArrayList<TestReport.TestReportStep>();
	protected int numberOfActions;
	protected String specificationCopyFilePath;

	public TestReport(Tester tester) {
		this.numberOfActions = tester.getTestActions().length;
		this.specificationCopyFilePath = TestingUtils
				.getOSAgnosticFilePath(tester.getReportSpecificationCopyFile().getPath());
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

	public File getSpecificationCopyFile() {
		if (specificationCopyFilePath == null) {
			return null;
		}
		return new File(specificationCopyFilePath);
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
		result.registerConverter(new JavaBeanConverter(result.getMapper()), -20);
		return result;
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
		TestReport loaded = (TestReport) xstream.fromXML(input);
		steps = loaded.steps;
		numberOfActions = loaded.numberOfActions;
		specificationCopyFilePath = loaded.specificationCopyFilePath;
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

	public enum TestReportStepStatus {
		SUCCESSFUL, FAILED, SKIPPED, CANCELLED
	}

	public class TestReportStep {

		protected TestReportStepStatus status;
		protected long startTimestamp;
		protected long endTimestamp;
		protected String windowsImageFilePath;
		protected String actionSummary;
		protected List<String> logs = new ArrayList<String>();

		public TestReportStep(TestAction testAction) {
			actionSummary = testAction.toString();
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
			if (windowsImageFilePath == null) {
				return null;
			}
			return new File(windowsImageFilePath);
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

		public void ending() {
			endTimestamp = System.currentTimeMillis();
		}

		public void during(Tester tester) {
			File file = TestingUtils.saveAllTestableWindowImages(tester);
			if (file == null) {
				windowsImageFilePath = null;
			} else {
				windowsImageFilePath = TestingUtils.getOSAgnosticFilePath(file.getPath());
			}
		}

		public void log(String msg) {
			logs.add(msg);
		}

	}

}
