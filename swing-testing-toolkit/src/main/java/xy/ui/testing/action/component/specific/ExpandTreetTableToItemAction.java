package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

import xy.reflect.ui.info.annotation.Validating;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.TestingUtils;
import xy.ui.testing.util.ValidationError;

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
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		if (!(c instanceof JXTreeTable)) {
			return false;
		}
		JXTreeTable treeTable = (JXTreeTable) c;
		MouseEvent mouseEvt = (MouseEvent) event;
		TreePath treePath = treeTable.getPathForLocation(mouseEvt.getX(), mouseEvt.getY());
		if (treePath == null) {
			return false;
		}
		itemPath = fromTreePathToIntPath(treePath, treeTable);
		if (itemPath == null) {
			return false;
		}
		return true;
	}

	protected List<Integer> fromTreePathToIntPath(TreePath treePath, JXTreeTable treeTable) {
		List<Integer> result = new ArrayList<Integer>();
		TreeTableModel model = treeTable.getTreeTableModel();
		Object pathElementParent = null;
		for (Object pathElement : treePath.getPath()) {
			if (pathElementParent != null) {
				int index = model.getIndexOfChild(pathElementParent, pathElement);
				if (index == -1) {
					return null;
				}
				result.add(index);
			}
			pathElementParent = pathElement;
		}
		return result;
	}

	protected TreePath fromIntPathToTreePath(List<Integer> intPath, JXTreeTable treeTable) {
		TreeTableModel model = treeTable.getTreeTableModel();
		TreePath result = new TreePath(model.getRoot());
		Object pathElementParent = model.getRoot();
		for (int index : intPath) {
			Object pathElement = model.getChild(pathElementParent, index);
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
		JXTreeTable treeTable = (JXTreeTable) c;
		treeTable.collapseAll();
		TreePath treePath = fromIntPathToTreePath(itemPath, treeTable);
		if (treePath == null) {
			throw new TestFailure("Cannot expand to the specified item: The path is not valid: " + itemPath,
					"Component", TestingUtils.saveImage(c));
		}
		treeTable.expandPath(treePath.getParentPath());
	}

	@Override
	public String getValueDescription() {
		return "Path = " + itemPath;
	}

	@Override
	@Validating
	public void validate() throws ValidationError {
		if (itemPath.size() == 0) {
			throw new ValidationError("Item path not defined");
		}
	}

	@Override
	public String getComponentInformation() {
		return "tree-table " + super.getComponentInformation();
	}

	@Override
	public String toString() {
		return "Expand the item " + itemPath + " of the " + getComponentInformation();
	}

}
