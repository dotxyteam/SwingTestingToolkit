package xy.ui.testing.editor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

@SuppressWarnings("unused")
public abstract class AbstractWindowSwitch {

	protected TestEditor testEditor;
	protected JFrame controlWindow;
	protected JPanel statusControlForm;
	protected StatusControlObject statusControlObject = new StatusControlObject();
	protected Rectangle lastBounds;
	protected boolean controlWindowAlwaysOnTopLastly = true;

	protected abstract void onBegining();

	protected abstract void onEnd();

	public abstract Object getStatus();

	public abstract String getSwitchTitle();

	public AbstractWindowSwitch(TestEditor testEditor) {
		this.testEditor = testEditor;
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

	public JPanel getStatusControlForm() {
		return statusControlForm;
	}

	public JFrame getWindow() {
		return controlWindow;
	}

	public class StatusControlObject {

		public Object getStatus() {
			return AbstractWindowSwitch.this.getStatus();
		}

		public boolean isWindowAlwaysOnTop() {
			if (AbstractWindowSwitch.this.controlWindow == null) {
				return controlWindowAlwaysOnTopLastly;
			}
			return AbstractWindowSwitch.this.controlWindow.isAlwaysOnTop();
		}

		public void setWindowAlwaysOnTop(boolean b) {
			if (AbstractWindowSwitch.this.controlWindow == null) {
				return;
			}
			AbstractWindowSwitch.this.controlWindow.setAlwaysOnTop(b);
		}

		public void stop() {
			if (!isActive()) {
				return;
			}
			activate(false);
		}
	}

	public class StatusControlWindow extends JFrame {
		private static final long serialVersionUID = 1L;
		boolean disposed = false;

		public StatusControlWindow() {
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			statusControlForm = getSwingRenderer().createForm(statusControlObject);
			getSwingRenderer().setupWindow(this, statusControlForm, null, getSwitchTitle(), testEditor.getIconImage());
			if (lastBounds != null) {
				setBounds(lastBounds);
			} else {
				setLocation(getInitialLocation());
			}
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					setAlwaysOnTop(controlWindowAlwaysOnTopLastly);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							onBegining();
						}
					});
				}

				@Override
				public void windowClosing(WindowEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							onEnd();
						}
					});
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