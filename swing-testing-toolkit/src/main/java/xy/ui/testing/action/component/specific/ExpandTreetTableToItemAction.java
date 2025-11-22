package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;

/**
 * Test action that expands the tree of a {@link JTree} or {@link JXTreeTable}
 * control.
 * 
 * @author olitank
 *
 */
public class ExpandTreetTableToItemAction extends AbstractTreeTableItemPathAction {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		if (c instanceof JXTreeTable) {
			JXTreeTable treeTable = (JXTreeTable) c;
			MouseEvent mouseEvt = (MouseEvent) introspectionRequestEvent;
			TreePath treePath = treeTable.getPathForLocation(mouseEvt.getX(), mouseEvt.getY());
			if (treePath == null) {
				return false;
			}
			itemPath = treePathToIndexes(treePath, treeTable.getTreeTableModel());
			if (itemPath == null) {
				return false;
			}
			if (treeTable.getTreeTableModel().getChildCount(treePath.getLastPathComponent()) > 0) {
				itemPath.add(0);
			}
			return true;
		}
		if (c instanceof JTree) {
			JTree tree = (JTree) c;
			MouseEvent mouseEvt = (MouseEvent) introspectionRequestEvent;
			TreePath treePath = tree.getPathForLocation(mouseEvt.getX(), mouseEvt.getY());
			if (treePath == null) {
				return false;
			}
			itemPath = treePathToIndexes(treePath, tree.getModel());
			if (itemPath == null) {
				return false;
			}
			if (tree.getModel().getChildCount(treePath.getLastPathComponent()) > 0) {
				itemPath.add(0);
			}
			return true;
		}
		return false;
	}

	@Override
	public void execute(Component c, Tester tester) {
		if (c instanceof JXTreeTable) {
			final JXTreeTable treeTable = (JXTreeTable) c;
			treeTable.collapseAll();
			final TreePath treePath = indexesToTreePath(itemPath, treeTable.getTreeTableModel());
			if (treePath == null) {
				throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath);
			}
			MiscUtils.expectingToBeInUIThread(new Runnable() {
				@Override
				public void run() {
					treeTable.expandPath(treePath.getParentPath());
				}
			});
		} else if (c instanceof JTree) {
			final JTree tree = (JTree) c;
			for (int row = tree.getRowCount() - 1; row > 0; row--) {
				tree.collapseRow(row);
			}
			final TreePath treePath = indexesToTreePath(itemPath, tree.getModel());
			if (treePath == null) {
				throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath);
			}
			MiscUtils.expectingToBeInUIThread(new Runnable() {
				@Override
				public void run() {
					tree.expandPath(treePath.getParentPath());
				}
			});
		} else {
			throw new TestFailure("Cannot expand to the specified item: Unexpected target control type: " + c.getClass()
					+ ". Expected type compatible with one of " + Arrays.asList(JTree.class, JXTreeTable.class));
		}
	}

	@Override
	public String toString() {
		return "Expand To " + getValueDescription() + " from " + getComponentInformation();
	}

}
