package xy.ui.testing.editor;

import java.util.List;

import javax.swing.SwingUtilities;

import xy.ui.testing.TestReport;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.util.Listener;

/**
 * Helper class that replaces temporarily the test editor by the small status
 * window to allow test actions execution.
 * 
 * @author olitank
 */
public class ReplayWindowSwitch extends AbstractWindowSwitch {

	protected Thread replayThread;
	protected List<TestAction> actionsToReplay;
	protected ReplayStatus replayStatus = new ReplayStatus();
	protected String currentActionDescription;
	protected TestReport lastTestReport;
	protected Throwable replayThreadError;

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
		lastTestReport = null;
		replayThreadError = null;
		replayThread = new Thread(TestEditor.class.getName() + " Replay Thread") {

			@Override
			public void run() {
				try {
					lastTestReport = getTester().replay(actionsToReplay, new Listener<TestAction>() {
						@Override
						public void handle(TestAction testAction) {
							currentActionDescription = testAction.toString();
							int actionIndex = indexOfActionByReference(testAction);
							currentActionDescription = (actionIndex + 1) + " - " + currentActionDescription;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									getStatusControlForm().refresh(false);
									testEditor.setSelectedActionIndex(actionIndex);
								}
							});
							while (ReplayWindowSwitch.this.isPaused()) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									throw new AssertionError(e);
								}
							}
						}

					});
				} catch (final Throwable t) {
					getTester().logError(t);
					replayThreadError = t;
				} finally {
					if (ReplayWindowSwitch.this.isActive()) {
						getStatusControlObject().stop();
					}
				}
			}
		};
		replayThread.start();
	}

	@Override
	protected void onEnd() {
		while (replayThread.isAlive()) {
			replayThread.interrupt();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new AssertionError(e);
			}
		}
		if (replayThreadError != null) {
			getSwingRenderer().handleExceptionsFromDisplayedUI(ReplayWindowSwitch.this.getWindow(), replayThreadError);
		}
		if (lastTestReport != null) {
			testEditor.setTestReport(lastTestReport);
			testEditor.showReportTab();
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