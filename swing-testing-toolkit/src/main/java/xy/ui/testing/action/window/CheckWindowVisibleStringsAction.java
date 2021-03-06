package xy.ui.testing.action.window;

import java.awt.Component;
import java.awt.Window;
import java.util.List;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.CheckVisibleStringsAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that checks the strings displayed on a window.
 * 
 * @author olitank
 *
 */
public class CheckWindowVisibleStringsAction extends TargetWindowTestAction {

	private static final long serialVersionUID = 1L;

	protected CheckVisibleStringsAction delegate = new CheckVisibleStringsAction();

	public CheckWindowVisibleStringsAction() {
		delegate.setComponentFinder(windowFinder);
	}

	public boolean isNegated() {
		return delegate.isNegated();
	}

	public void setNegated(boolean negated) {
		delegate.setNegated(negated);
	}

	public List<String> getVisibleStrings() {
		return delegate.getVisibleStrings();
	}

	public void setVisibleStrings(List<String> visibleStrings) {
		delegate.setVisibleStrings(visibleStrings);
	}

	public void loadVisibleStringsFromText(String s) {
		delegate.loadVisibleStringsFromText(s);
	}

	public boolean isCompletenessChecked() {
		return delegate.isCompletenessChecked();
	}

	public void setCompletenessChecked(boolean completenessChecked) {
		delegate.setCompletenessChecked(completenessChecked);
	}

	public boolean isOrderChecked() {
		return delegate.isOrderChecked();
	}

	public void setOrderChecked(boolean orderChecked) {
		delegate.setOrderChecked(orderChecked);
	}

	@Override
	protected boolean initializeSpecificProperties(Window w, TestEditor testEditor) {
		delegate.setVisibleStrings(TestingUtils.extractComponentTreeDisplayedStrings(w, testEditor.getTester()));
		return true;
	}

	@Override
	public Window findComponent(Tester tester) {
		return (Window) delegate.findComponent(tester);
	}

	@Override
	public void execute(Component c, Tester tester) {
		delegate.execute(c, tester);
	}

	@Override
	public void validate() throws ValidationError {
		delegate.validate();
	}

	@Override
	public String getValueDescription() {
		return delegate.getValueDescription();
	}

	@Override
	public String toString() {
		return "Verify " + getValueDescription() + " on " + getComponentInformation();
	}

}
