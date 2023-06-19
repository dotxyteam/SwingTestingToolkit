package xy.ui.testing.editor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.reflect.ui.control.swing.util.WindowManager;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

/**
 * Base class of helper objects that replace temporarily the test editor by a
 * small status window.
 * 
 * @author olitank
 *
 */
public abstract class AbstractWindowSwitch {

	protected StatusControlObject statusControlObject = new StatusControlObject();
	protected boolean paused = false;

	protected TestEditor testEditor;

	protected JFrame statusControlWindow;
	protected Form statusControlForm;
	protected Rectangle laststatusControlWindowBounds;
	protected boolean statusControlWindowAlwaysOnTopLastly = true;

	protected abstract void onBegining();

	protected abstract void onEnd();

	public abstract Object getStatus();

	public abstract String getSwitchTitle();

	public AbstractWindowSwitch(TestEditor testEditor) {
		this.testEditor = testEditor;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean b) {
		getTester().handleCurrentComponentChange(null);
		this.paused = b;
	}

	public StatusControlObject getStatusControlObject() {
		return statusControlObject;
	}

	protected SwingRenderer getSwingRenderer() {
		return testEditor.getSwingRenderer();
	}

	public TestEditor getTestEditor() {
		return testEditor;
	}

	protected Tester getTester() {
		return testEditor.getTester();
	}

	public void activate() {
		if (isActive()) {
			return;
		}
		statusControlWindow = new StatusControlWindow();
		statusControlWindow.setVisible(true);
	}

	public void requestDeactivation() {
		if (!isActive()) {
			return;
		}
		TestingUtils.sendWindowClosingEvent(statusControlWindow);
	}

	public boolean isActive() {
		return (statusControlWindow != null) && statusControlWindow.isVisible();
	}

	public Form getStatusControlForm() {
		return statusControlForm;
	}

	public JFrame getWindow() {
		return statusControlWindow;
	}

	public void setPausedAndUpdateUI(boolean b) {
		setPaused(b);
		statusControlForm.refresh(false);
	}

	public class StatusControlObject {

		public boolean isPaused() {
			return AbstractWindowSwitch.this.isPaused();
		}

		public void setPaused(boolean b) {
			getTester().handleCurrentComponentChange(null);
			AbstractWindowSwitch.this.setPaused(b);
		}

		public Object getStatus() {
			return AbstractWindowSwitch.this.getStatus();
		}

		public void stop() {
			if (!AbstractWindowSwitch.this.isActive()) {
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindowSwitch.this.requestDeactivation();
				}
			});
		}

		@Override
		public String toString() {
			return "StatusControl [of=" + AbstractWindowSwitch.this.toString() + "]";
		}

	}

	public class StatusControlWindow extends JFrame {
		private static final long serialVersionUID = 1L;

		protected boolean disposed = false;
		protected WindowManager windowManager;
		protected WindowListener windowListener = new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				setAlwaysOnTop(statusControlWindowAlwaysOnTopLastly);
				onBegining();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				onEnd();
				statusControlWindowAlwaysOnTopLastly = isAlwaysOnTop();
			}

		};

		public StatusControlWindow() {
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			statusControlForm = getSwingRenderer().createForm(statusControlObject);
			windowManager = getSwingRenderer().createWindowManager(this);
			windowManager.install(statusControlForm, null, getSwitchTitle(), testEditor.getIconImage());
			if (laststatusControlWindowBounds != null) {
				setBounds(laststatusControlWindowBounds);
			} else {
				setLocation(getInitialLocation());
			}
			addWindowListener(windowListener);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					hideParent();
				}
			});
		}

		@Override
		public void dispose() {
			if (disposed) {
				return;
			}
			disposed = true;
			removeWindowListener(windowListener);
			laststatusControlWindowBounds = getBounds();
			windowManager.uninstall();
			statusControlForm = null;
			super.dispose();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					showParent();
				}
			});
		}

		protected void hideParent() {
			testEditor.setVisible(false);
		}

		protected void showParent() {
			testEditor.invalidate();
			testEditor.setVisible(true);
		}

		protected Point getInitialLocation() {
			Rectangle screenBounds = SwingRendererUtils.getScreenBounds(testEditor);
			Dimension currentSize = getSize();
			int x = screenBounds.x + screenBounds.width - currentSize.width;
			int y = screenBounds.y + screenBounds.height - currentSize.height;
			return new Point(x, y);
		}

	}
}