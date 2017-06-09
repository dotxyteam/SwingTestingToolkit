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

	public TestReport(Tester tester) {
		this.numberOfActions = tester.getTestActions().length;
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
		return Math.round((float) steps.size() / (float) numberOfActions);
	}

	public TestReportStepStatus getFinalStatus() {
		if (steps.size() == 0) {
			return null;
		}
		TestReportStepStatus result = steps.get(steps.size() - 1).getStatus();
		if(result == TestReportStepStatus.SKIPPED){
			result = TestReportStepStatus.SUCCESSFUL;
		}
		return result;
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
	
	public enum TestReportStepStatus{
		SUCCESSFUL, FAILED, SKIPPED, CANCELLED 
	}

	public class TestReportStep {

		protected TestReportStepStatus status;
		protected long startTimestamp;
		protected long endTimestamp;
		protected File windowsImageFile;
		protected String actionSummary;

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
			return windowsImageFile;
		}

		public String getActionSummary() {
			return actionSummary;
		}

		public void starting() {
			startTimestamp = System.currentTimeMillis();
		}

		public void ending() {
			endTimestamp = System.currentTimeMillis();
		}

		public void componentFound(Tester tester) {
			windowsImageFile = TestingUtils.saveAllTestableWindowImages(tester);
		}

	}

}
