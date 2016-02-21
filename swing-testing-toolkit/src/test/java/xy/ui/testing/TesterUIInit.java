package xy.ui.testing;

import java.awt.Color;

public class TesterUIInit {
	public static void main(String[] args) {
		TesterUI testerUI = new TesterUI(new Tester());
		testerUI.setDecorationsBackgroundColor(new Color(68, 61, 205));
		testerUI.setDecorationsForegroundColor(new Color(216, 214, 245));
		testerUI.open();
	}

}
