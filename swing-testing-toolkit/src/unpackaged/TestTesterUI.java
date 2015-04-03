import javax.management.monitor.CounterMonitor;

import xy.reflect.ui.ReflectionUI;
import xy.ui.testing.Tester;
import xy.ui.testing.TesterUI;

public class TestTesterUI {

	public static void main(String[] args) {
		new ReflectionUI().openObjectFrame(new CounterMonitor(), null, null);
		Tester tester = new Tester();
		TesterUI.INSTANCE.openObjectFrame(tester,
				TesterUI.INSTANCE.getObjectKind(tester), null);
	}

}
