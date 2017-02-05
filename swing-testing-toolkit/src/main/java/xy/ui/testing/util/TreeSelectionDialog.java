package xy.ui.testing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TreeSelectionDialog extends JDialog {

	protected static final long serialVersionUID = 1L;

	protected final JPanel contentPanel = new JPanel();
	protected JTree tree;
	protected boolean okPressed = false;
	protected JButton okButton;
	protected JButton cancelButton;
	protected JLabel messageControl;

	public static void main(String[] args) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("child1");
		root.add(child1);
		DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("child2");
		root.add(child2);
		DefaultMutableTreeNode child3 = new DefaultMutableTreeNode("child3");
		child2.add(child3);
		DefaultMutableTreeNode child4 = new DefaultMutableTreeNode("child4");
		child3.add(child4);
		INodePropertyAccessor<Boolean> selectable = new INodePropertyAccessor<Boolean>() {
			@Override
			public Boolean get(Object node) {
				return ((DefaultMutableTreeNode) node).getChildCount() == 0;
			}
		};
		open(null, null, null, new DefaultTreeModel(root), null, null, selectable, true);
	}

	public static Object open(Component parent, String title, String message, TreeModel treeModel,
			final INodePropertyAccessor<String> textAccessor, final INodePropertyAccessor<Icon> iconAccessor,
			final INodePropertyAccessor<Boolean> selectableAccessor, boolean expandAll) {
		Window parentWindow = null;
		{
			if (parent != null) {
				if (parent instanceof Window) {
					parentWindow = (Window) parent;
				} else {
					parentWindow = SwingUtilities.getWindowAncestor(parent);
				}
			}
		}
		TreeSelectionDialog dialog = new TreeSelectionDialog(parentWindow, title, message, treeModel, textAccessor,
				iconAccessor, selectableAccessor, expandAll);

		dialog.setVisible(true);

		if (dialog.okPressed) {
			return dialog.tree.getLastSelectedPathComponent();
		} else {
			return null;
		}
	}

	public TreeSelectionDialog(Window parent, String title, String message, TreeModel treeModel,
			final INodePropertyAccessor<String> textAccessor, final INodePropertyAccessor<Icon> iconAccessor,
			final INodePropertyAccessor<Boolean> selectableAccessor, boolean expandAll) {
		super(parent);
		if (title != null) {
			setTitle(title);
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		initializeTree(treeModel, textAccessor, iconAccessor, selectableAccessor, expandAll);
		setContentPane(createContentPane(message));
		pack();
	}

	protected Container createContentPane(String message) {
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(10, 10));
		{
			contentPanel.add(new JScrollPane(tree));
		}
		{
			messageControl = new JLabel("Choose:");
			messageControl.setHorizontalAlignment(SwingConstants.CENTER);
			if (message != null) {
				messageControl.setText(message);
			}
			contentPanel.add(messageControl, BorderLayout.NORTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			contentPane.add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setEnabled(false);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOK();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						onCancel();
					}

				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		return contentPane;
	}

	protected void initializeTree(TreeModel treeModel, final INodePropertyAccessor<String> textAccessor,
			final INodePropertyAccessor<Icon> iconAccessor, final INodePropertyAccessor<Boolean> selectableAccessor,
			boolean expandAll) {
		tree = new JTree();
		tree.setVisibleRowCount(10);
		tree.setRootVisible(false);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				onTreeSelectionChange(selectableAccessor);
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getClickCount() == 2) {
					TreePath treePath = tree.getPathForLocation(me.getX(), me.getY());
					if (treePath != null) {
						onTreeItemDoubleClick(treePath, selectableAccessor);
					}
				}
			}
		});
		tree.setModel(treeModel);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			protected static final long serialVersionUID = 1L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row,
						focused);
				if (textAccessor != null) {
					label.setText(textAccessor.get(value));
				}
				if (iconAccessor != null) {
					Icon icon = iconAccessor.get(value);
					label.setIcon(icon);
				} else {
					label.setIcon(null);
				}
				return label;
			}
		});
		if (expandAll)

		{
			expandAll();
		}

	}

	protected void onTreeItemDoubleClick(TreePath treePath, INodePropertyAccessor<Boolean> selectableAccessor) {
		if (treePath.equals(tree.getSelectionPath())) {
			if (okButton.isEnabled()) {
				okPressed = true;
				TreeSelectionDialog.this.dispose();
			}
		}
	}

	public JTree getTree() {
		return tree;
	}

	public Object getSelection() {
		if (!okPressed) {
			return null;
		}
		return tree.getLastSelectedPathComponent();
	}

	protected void onTreeSelectionChange(INodePropertyAccessor<Boolean> selectableAccessor) {
		if (tree.getSelectionCount() == 1) {
			if (selectableAccessor != null) {
				if (Boolean.TRUE.equals(selectableAccessor.get(tree.getLastSelectedPathComponent()))) {
					okButton.setEnabled(true);
					return;
				}
			} else {
				okButton.setEnabled(true);
				return;
			}
		}
		okButton.setEnabled(false);
	}

	protected void onCancel() {
		dispose();
	}

	protected void onOK() {
		okPressed = true;
		dispose();
	}

	public void expandAll() {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	public static interface INodePropertyAccessor<T> {
		T get(Object node);
	}

}
