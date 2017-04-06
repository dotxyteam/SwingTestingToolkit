package xy.ui.testing.editor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.control.swing.SwingRenderer;
import xy.ui.testing.Tester;
import xy.ui.testing.util.TestingUtils;

public abstract class AbstractWindowSwitch {

	protected TesterEditor testerEditor;
	protected JFrame window;
	protected JPanel statusControlForm;
	protected StatusControlObject statusControlObject = new StatusControlObject();

	protected abstract void onBegining();

	protected abstract void onEnd();

	public abstract Object getStatus();

	public abstract String getSwitchTitle();

	public AbstractWindowSwitch(TesterEditor testerEditor) {
		this.testerEditor = testerEditor;
	}

	public StatusControlObject getStatusControlObject() {
		return statusControlObject;
	}

	protected SwingRenderer getSwingRenderer() {
		return testerEditor.getSwingRenderer();
	}

	public TesterEditor getTesterEditor() {
		return testerEditor;
	}

	protected Tester getTester() {
		return testerEditor.getTester();
	}

	public void activate(boolean b) {
		if (b == isActive()) {
			return;
		}
		if (b) {
			AbstractWindowSwitch.this.window = new JFrame() {
				private static final long serialVersionUID = 1L;
				boolean disposed = false;
				{
					setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					statusControlForm = getSwingRenderer().createForm(statusControlObject);
					getSwingRenderer().setupWindow(this, statusControlForm, null, getSwitchTitle(),
							testerEditor.getIconImage());
					addWindowListener(new WindowAdapter() {
						@Override
						public void windowOpened(WindowEvent e) {
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
					super.dispose();
					testerEditor.setLocation(AbstractWindowSwitch.this.window.getLocation());
					AbstractWindowSwitch.this.window = null;
					testerEditor.invalidate();
					testerEditor.setVisible(true);
				}
			};
			testerEditor.setVisible(false);
			AbstractWindowSwitch.this.window.setLocation(testerEditor.getLocation());
			AbstractWindowSwitch.this.window.setVisible(true);
		} else {
			TestingUtils.sendWindowClosingEvent(AbstractWindowSwitch.this.window);
		}
	}

	protected boolean isActive() {
		return window != null;
	}

	public JPanel getStatusControlForm() {
		return statusControlForm;
	}

	public JFrame getWindow() {
		return window;
	}

	public class StatusControlObject {

		public Object getStatus() {
			return AbstractWindowSwitch.this.getStatus();
		}

		public void stop() {
			if (!isActive()) {
				return;
			}
			activate(false);
		}
	}
}