package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;

import javax.swing.JTabbedPane;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class SelectTabAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected String tabToSelect = "";
	protected String[] knownTabs;

	public String getTabToSelect() {
		return tabToSelect;
	}

	public void setTabToSelect(String tabToSelect) {
		this.tabToSelect = tabToSelect;
	}

	public String[] getKnownTabs() {
		return knownTabs;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		if (!(c instanceof JTabbedPane)) {
			return false;
		}
		JTabbedPane tabbedPane = (JTabbedPane) c;
		if (tabbedPane.getTabCount() == 0) {
			return false;
		}
		knownTabs = new String[tabbedPane.getTabCount()];
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			knownTabs[i] = tabbedPane.getTitleAt(i);
		}
		tabToSelect = knownTabs[0];
		return true;
	}

	@Override
	public void execute(Component c, Tester tester) {
		JTabbedPane tabbedPane = (JTabbedPane) c;
		boolean found = false;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			String currentTabTitle = tabbedPane.getTitleAt(i);
			if (currentTabTitle.equals(tabToSelect)) {
				tabbedPane.setSelectedIndex(i);
				found = true;
				break;
			}
		}
		if (!found) {
			throw new TestFailure("Could not select the tab '" + tabToSelect + "': Tab not found");
		}
	}

	@Override
	public String getValueDescription() {
		return "tab " + "\"" + StringEscapeUtils.escapeJava(tabToSelect) + "\"";
	}

	@Override
	public void validate() throws ValidationError {
		if (tabToSelect == null) {
			throw new ValidationError("Missing tab to select");
		}
	}

	@Override
	public String getComponentInformation() {
		return "tabbed-pane " + super.getComponentInformation();
	}

	@Override
	public String toString() {
		String tabToSelectText = (tabToSelect == null) ? "<none>" : tabToSelect;
		return "Select the tab <" + tabToSelectText + "> of the " + getComponentInformation();
	}
}
