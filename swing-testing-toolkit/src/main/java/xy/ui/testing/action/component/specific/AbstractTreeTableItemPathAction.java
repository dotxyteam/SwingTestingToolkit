package xy.ui.testing.action.component.specific;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.ValidationError;

/**
 * Base class of test actions that are based on {@link TreePath}.
 * 
 * @author olitank
 *
 */
public abstract class AbstractTreeTableItemPathAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected List<Integer> itemPath = new ArrayList<Integer>();

	public List<Integer> getItemPath() {
		return itemPath;
	}

	public void setItemPath(List<Integer> itemPath) {
		this.itemPath = itemPath;
	}

	protected List<Integer> treePathToIndexes(TreePath treePath, TreeModel treeModel) {
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

	protected TreePath indexesToTreePath(List<Integer> indexes, TreeModel treeModel) {
		TreePath result = new TreePath(treeModel.getRoot());
		Object pathElementParent = treeModel.getRoot();
		for (int index : indexes) {
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
	public void validate() throws ValidationError {
		if (itemPath.size() == 0) {
			throw new ValidationError("Item path not defined");
		}
		super.validate();
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

}