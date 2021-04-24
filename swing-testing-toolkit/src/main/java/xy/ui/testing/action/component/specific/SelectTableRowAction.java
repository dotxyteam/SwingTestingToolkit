package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JTable;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that selects a {@link JTable} row.
 * 
 * @author olitank
 *
 */
@SuppressWarnings("rawtypes")
public class SelectTableRowAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;
	protected int firstItemToSelect = 0;
	protected int lastItemToSelect = 0;
	protected boolean addedToExistingSelection = false;

	public int getFirstItemToSelect() {
		return firstItemToSelect;
	}

	public void setFirstItemToSelect(int firstItemToSelect) {
		this.firstItemToSelect = firstItemToSelect;
	}

	public int getLastItemToSelect() {
		return lastItemToSelect;
	}

	public void setLastItemToSelect(int lastItemToSelect) {
		this.lastItemToSelect = lastItemToSelect;
	}

	public boolean isAddedToExistingSelection() {
		return addedToExistingSelection;
	}

	public void setAddedToExistingSelection(boolean addedToExistingSelection) {
		this.addedToExistingSelection = addedToExistingSelection;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		if (c instanceof JTable) {
			JTable table = (JTable) c;
			MouseEvent mouseEvt = (MouseEvent) introspectionRequestEvent;
			int rowIndex = table.rowAtPoint(mouseEvt.getPoint());
			if (rowIndex == -1) {
				return false;
			}
			firstItemToSelect = rowIndex;
			lastItemToSelect = rowIndex;
			return true;
		}
		if (c instanceof JList) {
			JList list = (JList) c;
			MouseEvent mouseEvt = (MouseEvent) introspectionRequestEvent;
			int rowIndex = list.locationToIndex(mouseEvt.getPoint());
			if (rowIndex == -1) {
				return false;
			}
			firstItemToSelect = rowIndex;
			lastItemToSelect = rowIndex;
			return true;
		}
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		if (c instanceof JTable) {
			final JTable table = (JTable) c;
			MiscUtils.ensureStartedInUIThread(new Runnable() {
				@Override
				public void run() {
					if (addedToExistingSelection) {
						table.getSelectionModel().addSelectionInterval(firstItemToSelect, lastItemToSelect);
					} else {
						table.getSelectionModel().setSelectionInterval(firstItemToSelect, lastItemToSelect);
					}
				}
			});
		} else if (c instanceof JList) {
			final JList list = (JList) c;
			MiscUtils.ensureStartedInUIThread(new Runnable() {
				@Override
				public void run() {
					if (addedToExistingSelection) {
						list.getSelectionModel().addSelectionInterval(firstItemToSelect, lastItemToSelect);
					} else {
						list.getSelectionModel().setSelectionInterval(firstItemToSelect, lastItemToSelect);
					}
				}
			});
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public void validate() throws ValidationError {
		if (firstItemToSelect < 0) {
			throw new ValidationError("Invalid first selection index: Cannot be < 0");
		}
		if (lastItemToSelect < 0) {
			throw new ValidationError("Invalid last selection index: Cannot be < 0");
		}

	}

	@Override
	public String getValueDescription() {
		String result = "";
		if (firstItemToSelect == lastItemToSelect) {
			result += MiscUtils.formatOccurrence("row", firstItemToSelect);
		} else {
			result += MiscUtils.formatOccurrence("row", firstItemToSelect) + " To "
					+ MiscUtils.formatOccurrence("row", lastItemToSelect);
		}
		return result;
	}

	@Override
	public String toString() {
		return "Select " + getValueDescription() + " from " + getComponentInformation();
	}

}
