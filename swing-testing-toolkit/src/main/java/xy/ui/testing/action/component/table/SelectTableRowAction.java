package xy.ui.testing.action.component.table;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import xy.reflect.ui.info.annotation.Validating;
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
		JTable table = (JTable) c;
		if (addedToExistingSelection) {
			table.getSelectionModel().addSelectionInterval(firstItemToSelect,
					lastItemToSelect);
		} else {
			table.getSelectionModel().setSelectionInterval(firstItemToSelect,
					lastItemToSelect);
		}
	}

	@Override
	public String getValueDescription() {
		String result = "";
		if (addedToExistingSelection) {
			result += "Add selection";
		} else {
			result += "Set selection";
		}
		if (firstItemToSelect == lastItemToSelect) {
			result += " of the item n°" + (firstItemToSelect + 1);
		} else {
			result += " from the item n°" + (firstItemToSelect + 1)
					+ " to the item n°" + (lastItemToSelect + 1);
		}		
		return result;
	}

	@Override
	@Validating
	public void validate() throws ValidationError {
		if (firstItemToSelect < 0) {
			throw new ValidationError("Invalid first selection index: Cannot be < 0");
		}
		if (lastItemToSelect < 0) {
			throw new ValidationError("Invalid last selection index: Cannot be < 0");
			}

	}

}
