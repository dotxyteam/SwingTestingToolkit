package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.ValidationError;

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
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		if (!(c instanceof JTable)) {
			return false;
		}
		JTable table = (JTable) c;
		MouseEvent mouseEvt = (MouseEvent) event;
		int rowIndex = table.rowAtPoint(mouseEvt.getPoint());
		if (rowIndex == -1) {
			return false;
		}
		firstItemToSelect = rowIndex;
		lastItemToSelect = rowIndex;
		return true;
	}

	@Override
	public void execute(Component c, Tester tester) {
		final JTable table = (JTable) c;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (addedToExistingSelection) {
					table.getSelectionModel().addSelectionInterval(firstItemToSelect, lastItemToSelect);
				} else {
					table.getSelectionModel().setSelectionInterval(firstItemToSelect, lastItemToSelect);
				}
			}
		});
	}

	@Override
	public String getValueDescription() {
		String result = "";
		if (firstItemToSelect == lastItemToSelect) {
			result += "Row" + (firstItemToSelect + 1);
		} else {
			result += "Rows n°" + (firstItemToSelect + 1) + " To " + (lastItemToSelect + 1);
		}
		return result;
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
	public String toString() {
		return (addedToExistingSelection ? "Add" : "Set") + " selection from row " + firstItemToSelect + " to row "
				+ lastItemToSelect + " on " + getComponentInformation();
	}

}
