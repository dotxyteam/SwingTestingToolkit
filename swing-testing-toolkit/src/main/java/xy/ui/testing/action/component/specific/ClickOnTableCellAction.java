package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.AbstractClickAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that sends mouse-click events on a {@link JTable} cell.
 * 
 * @author olitank
 *
 */
public class ClickOnTableCellAction extends AbstractClickAction {

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
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		if (!(c instanceof JTable)) {
			return false;
		}
		JTable table = (JTable) c;
		MouseEvent mouseEvt = (MouseEvent) introspectionRequestEvent;
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
	public void execute(Component c, Tester tester) {
		final JTable table = (JTable) c;
		Rectangle cellBounds = table.getCellRect(rowIndex, columnIndex, false);
		int clickCount = isDoubleClick() ? 2 : 1;
		Point clickPoint = new Point(cellBounds.x + cellBounds.width / 2, cellBounds.y + cellBounds.height / 2);
		final MouseEvent clickEvent = new MouseEvent(table, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
				clickPoint.x, clickPoint.y, clickCount, false, getButtonMask());
		MiscUtils.expectingToBeInUIThread(new Runnable() {
			@Override
			public void run() {
				for (MouseListener l : table.getMouseListeners()) {
					l.mouseClicked(clickEvent);
				}
			}
		});
	}

	@Override
	public String getValueDescription() {
		return super.getValueDescription() + " on Cell[" + (rowIndex + 1) + ", " + (columnIndex + 1) + "]";
	}

	@Override
	public void validate() throws ValidationError {
		super.validate();
		if (rowIndex < 0) {
			throw new ValidationError("Invalid row index: Cannot be < 0");
		}
		if (columnIndex < 0) {
			throw new ValidationError("Invalid column index: Cannot be < 0");
		}
	}

}
