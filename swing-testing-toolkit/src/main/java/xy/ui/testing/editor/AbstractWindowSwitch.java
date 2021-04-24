package xy.ui.testing.editor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.editor.WindowManager;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.control.swing.util.SwingRendererUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

/**
 * Base class of helper objects that replace temporarily the test editor by the
 * small status window.
 * 
 * @author olitank
 *
 */
public abstract class AbstractWindowSwitch {

	protected StatusControlObject statusControlObject = new StatusControlObject();
	protected boolean paused = false;

	protected TestEditor testEditor;
	protected JFrame controlWindow;
	protected Form statusControlForm;

	protected Rectangle lastBounds;
	protected boolean controlWindowAlwaysOnTopLastly = true;

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

	public void activate(boolean b) {
		if (b == isActive()) {
			return;
		}
		if (b) {
			controlWindow = new StatusControlWindow();
			testEditor.setVisible(false);
			controlWindow.setVisible(true);
		} else {
			TestingUtils.sendWindowClosingEvent(AbstractWindowSwitch.this.controlWindow);
		}
	}

	public boolean isActive() {
		return controlWindow != null;
	}

	public Form getStatusControlForm() {
		return statusControlForm;
	}

	public JFrame getWindow() {
		return controlWindow;
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

		public boolean isWindowAlwaysOnTop() {
			if (AbstractWindowSwitch.this.controlWindow == null) {
				return controlWindowAlwaysOnTopLastly;
			}
			return AbstractWindowSwitch.this.controlWindow.isAlwaysOnTop();
		}

		public void setWindowAlwaysOnTop(final boolean b) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (AbstractWindowSwitch.this.controlWindow == null) {
						return;
					}
					AbstractWindowSwitch.this.controlWindow.setAlwaysOnTop(b);
				}
			});

		}

		public void stop() {
			if (!AbstractWindowSwitch.this.isActive()) {
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					AbstractWindowSwitch.this.activate(false);
				}
			});
		}
	}

	public class StatusControlWindow extends JFrame {
		private static final long serialVersionUID = 1L;
		boolean disposed = false;

		public StatusControlWindow() {
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			statusControlForm = getSwingRenderer().createForm(statusControlObject);
			WindowManager windowManager = getSwingRenderer().createWindowManager(this);
			windowManager.set(statusControlForm, null, getSwitchTitle(), testEditor.getIconImage());
			if (lastBounds != null) {
				setBounds(lastBounds);
			} else {
				setLocation(getInitialLocation());
			}
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					setAlwaysOnTop(controlWindowAlwaysOnTopLastly);
					onBegining();
				}

				@Override
				public void windowClosing(WindowEvent e) {
					onEnd();
					controlWindowAlwaysOnTopLastly = isAlwaysOnTop();
				}

			});
		}

		@Override
		public void dispose() {
			synchronized (this) {
				if (disposed) {
					return;
				}
				disposed = true;
			}
			lastBounds = getBounds();
			super.dispose();
			AbstractWindowSwitch.this.controlWindow = null;
			testEditor.invalidate();
			testEditor.setVisible(true);
		}

		protected Point getInitialLocation() {
			Rectangle screenBounds = SwingRendererUtils.getScreenBounds(this);
			Dimension currentSize = getSize();
			int x = screenBounds.x + screenBounds.width - currentSize.width;
			int y = screenBounds.y + screenBounds.height - currentSize.height;
			return new Point(x, y);
		}

	}
}