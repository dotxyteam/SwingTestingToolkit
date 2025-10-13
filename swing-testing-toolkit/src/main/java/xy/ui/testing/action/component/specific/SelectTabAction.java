package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTabbedPane;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that selects a {@link JTabbedPane} tab.
 * 
 * @author olitank
 *
 */
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

	public void setKnownTabs(String[] knownTabs) {
		this.knownTabs = knownTabs;
	}

	public List<TabNameChooser> getKnownTabNameChoosers() {
		if (knownTabs == null) {
			return null;
		}
		return Arrays.stream(knownTabs).map(TabNameChooser::new).collect(Collectors.toList());
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
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
		final JTabbedPane tabbedPane = (JTabbedPane) c;
		int indexToSelect = -1;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			String currentTabTitle = tabbedPane.getTitleAt(i);
			if (currentTabTitle.equals(tabToSelect)) {
				indexToSelect = i;
				break;
			}
		}
		if (indexToSelect == -1) {
			throw new TestFailure("Could not select the tab '" + tabToSelect + "': Tab not found");
		}
		final int finalIndexToSelect = indexToSelect;
		MiscUtils.expectingToBeInUIThread(new Runnable() {
			@Override
			public void run() {
				tabbedPane.setSelectedIndex(finalIndexToSelect);
			}
		});
	}

	@Override
	public String getValueDescription() {
		return "tab '" + ((tabToSelect == null) ? "<unspecified>" : tabToSelect) + "'";
	}

	@Override
	public void validate() throws ValidationError {
		if (tabToSelect == null) {
			throw new ValidationError("Missing tab to select");
		}
	}

	@Override
	public String toString() {
		return "Select " + getValueDescription() + " from " + getComponentInformation();
	}

	public class TabNameChooser {
		protected String tabName;

		public TabNameChooser(String tabName) {
			this.tabName = tabName;
		}

		public String getTabName() {
			return tabName;
		}

		public void choose() {
			SelectTabAction.this.tabToSelect = this.tabName;
		}
	}
}
