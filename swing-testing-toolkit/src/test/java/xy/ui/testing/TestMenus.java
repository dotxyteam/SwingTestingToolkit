package xy.ui.testing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.junit.Test;

import xy.ui.testing.util.TestingUtils;

public class TestMenus extends JFrame {

	@Test
	public void testMenus() throws Exception {
		TestingUtils.assertSuccessfulReplay(new File(System.getProperty("swing-testing-toolkit.project.directory", "./")
				+ "test-specifications/testMenus.stt"));
	}

	private static final long serialVersionUID = 1L;
	JTextArea output;
	JScrollPane scrollPane;
	JMenuBar menuBar;
	JMenu menu, submenu;
	JMenuItem menuItem;
	JRadioButtonMenuItem radioButtonMenuItem;
	JCheckBoxMenuItem checkBoxMenuItem;

	public TestMenus() throws HeadlessException {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setJMenuBar(createMenuBar());
		setContentPane(createContentPane());
		setSize(450, 260);

	}

	public JMenuBar createMenuBar() {
		menuBar = new JMenuBar();
		menu = new JMenu("A Menu");
		menuBar.add(menu);
		fillMenu(menu);
		return menuBar;
	}

	public void fillMenu(Container menu) {
		menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);
		addMenuItemAction(menuItem);
		menu.add(menuItem);

		menuItem = new JMenuItem("Both text and icon");
		addMenuItemAction(menuItem);
		menu.add(menuItem);

		ButtonGroup group = new ButtonGroup();

		radioButtonMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		addMenuItemAction(radioButtonMenuItem);
		radioButtonMenuItem.setSelected(true);
		group.add(radioButtonMenuItem);
		menu.add(radioButtonMenuItem);

		radioButtonMenuItem = new JRadioButtonMenuItem("Another one");
		addMenuItemAction(radioButtonMenuItem);
		group.add(radioButtonMenuItem);
		menu.add(radioButtonMenuItem);

		checkBoxMenuItem = new JCheckBoxMenuItem("A check box menu item");
		addMenuItemAction(checkBoxMenuItem);
		menu.add(checkBoxMenuItem);

		checkBoxMenuItem = new JCheckBoxMenuItem("Another one");
		addMenuItemAction(checkBoxMenuItem);
		menu.add(checkBoxMenuItem);

		submenu = new JMenu("A submenu");
		addMenuItemAction(submenu);

		menuItem = new JMenuItem("An item in the submenu");
		addMenuItemAction(menuItem);
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		addMenuItemAction(menuItem);
		submenu.add(menuItem);
		menu.add(submenu);
	}

	public void addMenuItemAction(final JMenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				output.setText("This menu item was activated: text='" + menuItem.getText() + "', selected="
						+ menuItem.isSelected());
			}
		});
	}

	public Container createContentPane() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		output = new JTextArea(5, 30);
		output.setEditable(false);
		output.addMouseListener(new PopupListener());
		scrollPane = new JScrollPane(output);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		return contentPane;
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TestMenus frame = new TestMenus();
				frame.setVisible(true);
			}
		});
	}

	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popup = new JPopupMenu();
				fillMenu(popup);
				popup.show(output, e.getX(), e.getY());
			}
		}
	}

}
