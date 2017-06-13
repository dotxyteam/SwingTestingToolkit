package xy.ui.testing.editor;

import java.util.List;

import javax.swing.SwingUtilities;

import xy.ui.testing.TestReport;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.util.Listener;

public class ReplayWindowSwitch extends AbstractWindowSwitch {

	protected Thread replayThread;
	protected List<TestAction> actionsToReplay;
	protected ReplayStatus replayStatus = new ReplayStatus();
	protected String currentActionDescription;

	public ReplayWindowSwitch(TestEditor testEditor) {
		super(testEditor);
	}

	@Override
	public String getSwitchTitle() {
		return "Replay Control";
	}

	public void setActionsToReplay(List<TestAction> actionsToReplay) {
		this.actionsToReplay = actionsToReplay;
		currentActionDescription = "<initializing...>";
	}

	@Override
	public ReplayStatus getStatus() {
		return replayStatus;
	}

	@Override
	protected void onBegining() {
		replayThread = new Thread(TestEditor.class.getName() + " Replay Thread") {
			@Override
			public void run() {
				try {
					Listener<TestAction> listener = new Listener<TestAction>() {
						@Override
						public void handle(TestAction testAction) {
							currentActionDescription = testAction.toString();
							int actionIndex = indexOfActionByReference(testAction);
							currentActionDescription = (actionIndex + 1) + " - " + currentActionDescription;
							getSwingRenderer().refreshAllFieldControls(getStatusControlForm(), false);
							testEditor.setSelectedActionIndex(actionIndex);
						}

					};
					final TestReport testReport = getTester().replay(actionsToReplay, listener);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							testEditor.setTestReport(testReport);
							testEditor.showReportTab();
							getStatusControlObject().stop();							
						}
					});
				} catch (final Throwable t) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							getTester().logError(t);
							getSwingRenderer().handleExceptionsFromDisplayedUI(ReplayWindowSwitch.this.getWindow(), t);
							getStatusControlObject().stop();
						}
					});
				}
			}
		};
		replayThread.start();
	}

	@Override
	protected void onEnd() {
		replayThread.interrupt();
		try {
			replayThread.join();
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
	}

	protected int indexOfActionByReference(TestAction testAction) {
		int i = 0;
		for (TestAction a : getTester().getTestActions()) {
			if (a == testAction) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public class ReplayStatus {

		public String getCurrentActionDescription() {
			return currentActionDescription;
		}

	}

}