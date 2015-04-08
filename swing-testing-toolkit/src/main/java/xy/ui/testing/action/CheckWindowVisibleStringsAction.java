package xy.ui.testing.action;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xy.ui.testing.finder.WindowFinder;
import xy.ui.testing.util.TestingError;
import xy.ui.testing.util.TestingUtils;

public class CheckWindowVisibleStringsAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected List<String> visibleStrings = new ArrayList<String>();

	public List<String> getVisibleStrings() {
		return visibleStrings;
	}

	public void setVisibleStrings(List<String> visibleStrings) {
		this.visibleStrings = visibleStrings;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c) {
		WindowFinder windowFinder = new WindowFinder();
		Window window = TestingUtils.getWindowAncestorOrSelf(c);
		windowFinder.initializeFrom(window);
		setComponentFinder(windowFinder);
		visibleStrings.addAll(TestingUtils.collectVisibleStrings(window));
		return true;
	}
	
	
	

	@Override
	public Component findComponent() {
		Window window =  (Window) super.findComponent();
		List<String> currentVisibleStrings = TestingUtils
				.collectVisibleStrings(window);
		if (!visibleStrings.equals(currentVisibleStrings)) {
			throw new TestingError(
					"The visible strings have changed: These are the original and the current visible strings:\n"
							+ TestingUtils.formatVisibleStrings(visibleStrings)
							+ "\n"
							+ TestingUtils.formatVisibleStrings(currentVisibleStrings));
		}
		return window;
	}

	@Override
	public void execute(Component c) {		
	}
	
	public void loadVisibleStringsFromText(String s){
		visibleStrings = TestingUtils.parseVisibleStrings(s);
	}

	@Override
	public String getValueDescription() {
		return Arrays.toString(visibleStrings.toArray());
	}

	@Override
	public String toString() {
		return "Check the visible strings of the " + getComponentFinder();
	}
}
