import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestGarbageCollection {

	public static void main(String[] args) throws Exception {
		test();
		System.out.println("test finished");
	}

	private static void test() throws Exception {
		JFrame frame = new JFrame();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
			}
		});
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				frame.dispose();
			}
		});

		System.out.println(frame);
	}

}
