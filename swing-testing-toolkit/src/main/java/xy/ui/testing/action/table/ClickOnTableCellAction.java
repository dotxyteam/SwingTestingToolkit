package xy.ui.testing.action.table;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;

import xy.ui.testing.action.component.ClickAction;

public class ClickOnTableCellAction extends ClickAction {

	private static final long serialVersionUID = 1L;
	protected int rowIndex = 0;
	protected int columnIndex = 0;

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		if(!super.initializeSpecificProperties(c, event)){
			return false;
		}
		if (!(c instanceof JTable)) {
			return false;
		}
		JTable table = (JTable) c;
		MouseEvent mouseEvt = (MouseEvent) event;
		rowIndex = table.rowAtPoint(mouseEvt.getPoint());
		if (rowIndex == -1) {
			return false;
		}
		columnIndex = table.columnAtPoint(mouseEvt.getPoint());
		if (columnIndex == -1) {
			return false;
		}
		return true;
	}

	@Override
	public void execute(Component c) {
		JTable table = (JTable) c;
		Rectangle cellBounds = table.getCellRect(rowIndex, columnIndex, false);
		int clickCount = isDoubleClick() ? 2 : 1;
		Point clickPoint = new Point(cellBounds.x + cellBounds.width / 2,
				cellBounds.y + cellBounds.height / 2);
		MouseEvent clickEvent = new MouseEvent(table, MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(), 0, clickPoint.x, clickPoint.y,
				clickCount, false, getButtonMask());
		for (MouseListener l : table.getMouseListeners()) {
			l.mouseClicked(clickEvent);
		}
	}

	@Override
	public String getValueDescription() {
		return super.getValueDescription() + " at the cell(" + (rowIndex + 1)
				+ ", " + (columnIndex + 1) + ")";
	}

}
