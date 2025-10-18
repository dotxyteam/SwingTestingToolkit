package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeSelectionModel;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that checks the current selection of a {@link JTable} or
 * {@link JTree} or {@link JList} control.
 * 
 * @author olitank
 *
 */
@SuppressWarnings("rawtypes")
public class CheckSelectionAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;
	private List<Integer> selectedIndexes = new ArrayList<Integer>();

	public List<Integer> getSelectedIndexes() {
		return selectedIndexes;
	}

	public void setSelectedIndexes(List<Integer> selectedIndexes) {
		this.selectedIndexes = selectedIndexes;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		if (c instanceof JTable) {
			JTable table = (JTable) c;
			ListSelectionModel selectionModel = table.getSelectionModel();
			if (!selectionModel.isSelectionEmpty()) {
				for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
					if (selectionModel.isSelectedIndex(i)) {
						selectedIndexes.add(i);
					}
				}
			}
			return true;
		}
		if (c instanceof JList) {
			JList list = (JList) c;
			ListSelectionModel selectionModel = list.getSelectionModel();
			if (!selectionModel.isSelectionEmpty()) {
				for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
					if (selectionModel.isSelectedIndex(i)) {
						selectedIndexes.add(i);
					}
				}
			}
			return true;
		}
		if (c instanceof JTree) {
			JTree tree = (JTree) c;
			TreeSelectionModel selectionModel = tree.getSelectionModel();
			if (!selectionModel.isSelectionEmpty()) {
				for (int i = selectionModel.getMinSelectionRow(); i <= selectionModel.getMaxSelectionRow(); i++) {
					if (selectionModel.isRowSelected(i)) {
						selectedIndexes.add(i);
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		if (c instanceof JTable) {
			final JTable table = (JTable) c;
			ListSelectionModel selectionModel = table.getSelectionModel();
			for (int i : selectedIndexes) {
				if (!selectionModel.isSelectedIndex(i)) {
					throw new TestFailure(
							"Selection checking failed: There was no selection at the index " + i);
				}
			}
			if (!selectionModel.isSelectionEmpty()) {
				for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
					if (selectionModel.isSelectedIndex(i)) {
						if(selectedIndexes.indexOf(i) == -1) {
							throw new TestFailure(
									"Selection checking failed: There should not have been a selection at index " + i);
						}
					}
				}
			}
		} else if (c instanceof JList) {
			final JList list = (JList) c;
			ListSelectionModel selectionModel = list.getSelectionModel();
			for (int i : selectedIndexes) {
				if (!selectionModel.isSelectedIndex(i)) {
					throw new TestFailure(
							"Selection checking failed: There was no selection at the index " + i);
				}
			}
			if (!selectionModel.isSelectionEmpty()) {
				for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
					if (selectionModel.isSelectedIndex(i)) {
						if(selectedIndexes.indexOf(i) == -1) {
							throw new TestFailure(
									"Selection checking failed: There should not have been a selection at index " + i);
						}
					}
				}
			}
		} else if (c instanceof JTree) {
			final JTree tree = (JTree) c;
			TreeSelectionModel selectionModel = tree.getSelectionModel();
			for (int i : selectedIndexes) {
				if (!selectionModel.isRowSelected(i)) {
					throw new TestFailure(
							"Selection checking failed: There was no selection at the index " + i);
				}
			}
			if (!selectionModel.isSelectionEmpty()) {
				for (int i = selectionModel.getMinSelectionRow(); i <= selectionModel.getMaxSelectionRow(); i++) {
					if (selectionModel.isRowSelected(i)) {
						if(selectedIndexes.indexOf(i) == -1) {
							throw new TestFailure(
									"Selection checking failed: There should not have been a selection at index " + i);
						}
					}
				}
			}
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public void validate() throws ValidationError {
		for (int i : selectedIndexes) {
			if (i < 0) {
				throw new TestFailure("Invalid selection index found: " + i);
			}
		}
		super.validate();
	}

	@Override
	public String getValueDescription() {
		return "selected indexes " + selectedIndexes;
	}

	@Override
	public String toString() {
		return "Check  " + getValueDescription() + " from " + getComponentInformation();
	}

}
