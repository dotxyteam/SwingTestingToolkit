package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;

import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that expands a {@link JXTreeTable} node.
 * 
 * @author olitank
 *
 */
public class ExpandTreetTableToItemAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;
	protected List<Integer> itemPath = new ArrayList<Integer>();

	public List<Integer> getItemPath() {
		return itemPath;
	}

	public void setItemPath(List<Integer> itemPath) {
		this.itemPath = itemPath;
	}

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
			itemPath = fromTreePathToIntPath(treePath, treeTable.getTreeTableModel());
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
			itemPath = fromTreePathToIntPath(treePath, tree.getModel());
			if (itemPath == null) {
				return false;
			}
			return true;
		}
		return false;

	}

	protected List<Integer> fromTreePathToIntPath(TreePath treePath, TreeModel treeModel) {
		List<Integer> result = new ArrayList<Integer>();
		Object pathElementParent = null;
		for (Object pathElement : treePath.getPath()) {
			if (pathElementParent != null) {
				int index = treeModel.getIndexOfChild(pathElementParent, pathElement);
				if (index == -1) {
					return null;
				}
				result.add(index);
			}
			pathElementParent = pathElement;
		}
		return result;
	}

	protected TreePath fromIntPathToTreePath(List<Integer> intPath, TreeModel treeModel) {
		TreePath result = new TreePath(treeModel.getRoot());
		Object pathElementParent = treeModel.getRoot();
		for (int index : intPath) {
			Object pathElement = treeModel.getChild(pathElementParent, index);
			if (pathElement == null) {
				return null;
			}
			result = result.pathByAddingChild(pathElement);
			pathElementParent = pathElement;
		}
		return result;
	}

	@Override
	public void execute(Component c, Tester tester) {
		if (c instanceof JXTreeTable) {
			final JXTreeTable treeTable = (JXTreeTable) c;
			treeTable.collapseAll();
			final TreePath treePath = fromIntPathToTreePath(itemPath, treeTable.getTreeTableModel());
			if (treePath == null) {
				throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath);
			}
			MiscUtils.ensureStartedInUIThread(new Runnable() {
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
			final TreePath treePath = fromIntPathToTreePath(itemPath, tree.getModel());
			if (treePath == null) {
				throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath);
			}
			MiscUtils.ensureStartedInUIThread(new Runnable() {
				@Override
				public void run() {
					tree.expandPath(treePath.getParentPath());
				}
			});
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public void validate() throws ValidationError {
		if (itemPath.size() == 0) {
			throw new ValidationError("Item path not defined");
		}
	}

	@Override
	public String getValueDescription() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < itemPath.size(); i++) {
			int index = itemPath.get(i);
			if (i > 0) {
				result.append(" / ");
			}
			result.append(MiscUtils.formatOccurrence("item", index));
		}
		return result.toString();
	}

	@Override
	public String toString() {
		return "Expand " + getValueDescription() + " from " + getComponentInformation();
	}

}
