package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;

import xy.ui.testing.Tester;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;

/**
 * Test action that collapses a {@link TreePath} node.
 * 
 * @author olitank
 *
 */
public class CollapseTreetTableItemAction extends AbstractTreeTableItemPathAction {

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
			return true;
		}
		return false;

	}

	@Override
	public void execute(Component c, Tester tester) {
		if (c instanceof JXTreeTable) {
			final JXTreeTable treeTable = (JXTreeTable) c;
			final TreePath treePath = indexesToTreePath(itemPath, treeTable.getTreeTableModel());
			if (treePath == null) {
				throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath);
			}
			MiscUtils.expectingToBeInUIThread(new Runnable() {
				@Override
				public void run() {
					treeTable.collapsePath(treePath);
				}
			});
		} else if (c instanceof JTree) {
			final JTree tree = (JTree) c;
			final TreePath treePath = indexesToTreePath(itemPath, tree.getModel());
			if (treePath == null) {
				throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath);
			}
			MiscUtils.expectingToBeInUIThread(new Runnable() {
				@Override
				public void run() {
					tree.collapsePath(treePath);
				}
			});
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public String toString() {
		return "Collapse " + getValueDescription() + " from " + getComponentInformation();
	}

}
