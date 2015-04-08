package xy.ui.testing.finder;

import java.awt.Component;
import java.awt.Window;

public class WindowFinder extends ComponentFinder {

	private static final long serialVersionUID = 1L;
	protected MatchingComponentFinder subFinder = new MatchingComponentFinder() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean matchesInContainingWindow(Component c) {
			return c instanceof Window;
		}

		@Override
		protected boolean initializeSpecificCriterias(Component c) {
			return true;
		}
	};

	public int getWindowIndex() {
		return subFinder.getWindowIndex();
	}

	public void setWindowIndex(int index) {
		subFinder.setWindowIndex(index);
	}

	@Override
	public Component find() {
		return subFinder.find();
	}

	@Override
	public boolean initializeFrom(Component c) {
		return subFinder.initializeFrom(c);
	}

	@Override
	public String toString() {
		return "Window n°" + (getWindowIndex() + 1);
	}

}
