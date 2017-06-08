package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.util.ReflectionUIError;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class SelectComboBoxItemAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected String optionToSelect = "";
	protected SelectionMode selectionMode = SelectionMode.BY_LABEL_TEXT;
	protected Option[] knownOptions;

	public SelectionMode getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	public String getOptionToSelect() {
		return optionToSelect;
	}

	public void setOptionToSelect(String optionToSelect) {
		this.optionToSelect = optionToSelect;
	}

	public List<String> getKnownOptions() {
		if (knownOptions == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		for (Option option : knownOptions) {
			if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
				result.add(option.getLabel());
			} else if (selectionMode == SelectionMode.BY_POSITION) {
				result.add(Integer.toString(option.getPosition()));
			} else {
				throw new ReflectionUIError();
			}
		}
		return result;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent, TestEditor testEditor) {
		if (!(c instanceof JComboBox)) {
			return false;
		}
		JComboBox comboBox = (JComboBox) c;
		if (comboBox.getItemCount() == 0) {
			return false;
		}
		List<String> allOptions = getAllOptions(comboBox);
		knownOptions = new Option[allOptions.size()];
		for (int i = 0; i < allOptions.size(); i++) {
			knownOptions[i] = new Option(i, allOptions.get(i));
		}
		String labelText = getLabelText(comboBox.getModel(), comboBox.getRenderer(), 0);
		if (labelText != null) {
			selectionMode = SelectionMode.BY_LABEL_TEXT;
			if (allOptions.size() > 0) {
				optionToSelect = allOptions.get(0);
			}
		} else {
			selectionMode = SelectionMode.BY_POSITION;
			if (allOptions.size() > 0) {
				optionToSelect = "0";
			}
		}
		return true;
	}

	protected String getOption(ComboBoxModel model, ListCellRenderer renderer, int i) {
		if (selectionMode == SelectionMode.BY_POSITION) {
			return Integer.toString(i);
		} else if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
			return getLabelText(model, renderer, i);
		} else {
			throw new AssertionError();
		}

	}

	protected List<String> getAllOptions(JComboBox comboBox) {
		List<String> result = new ArrayList<String>();
		ComboBoxModel model = comboBox.getModel();
		ListCellRenderer renderer = comboBox.getRenderer();
		for (int i = 0; i < model.getSize(); i++) {
			result.add(getOption(model, renderer, i));
		}
		return result;
	}

	protected String getLabelText(ComboBoxModel model, ListCellRenderer renderer, int i) {
		Object item = model.getElementAt(i);
		Component cellRenderer = renderer.getListCellRendererComponent(new JList(), item, 0, false, false);
		if (!(cellRenderer instanceof JLabel)) {
			return null;
		}
		return ((JLabel) cellRenderer).getText();
	}

	@Override
	public void execute(Component c, Tester tester) {
		final JComboBox comboBox = (JComboBox) c;
		List<String> options = getAllOptions(comboBox);
		int indexToSelect = -1;
		for (int i = 0; i < options.size(); i++) {
			String option = options.get(i);
			if (option.equals(optionToSelect)) {
				indexToSelect = i;
			}
		}
		if (indexToSelect == -1) {
			throw new TestFailure("Could not select the combo box item '" + optionToSelect + "': Item not found." + "\n"
					+ "Selection Mode=" + selectionMode + "\n" + "Found items: " + options);
		}
		final int finalIndexToSelect = indexToSelect;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				comboBox.setSelectedIndex(finalIndexToSelect);
			}
		});

	}

	@Override
	public String getValueDescription() {
		if (selectionMode == SelectionMode.BY_POSITION) {
			return "Item n°" + (Integer.valueOf(optionToSelect) + 1);
		} else if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
			return "\"" + StringEscapeUtils.escapeJava(optionToSelect) + "\"";
		} else {
			throw new AssertionError();
		}

	}

	public enum SelectionMode {
		BY_LABEL_TEXT, BY_POSITION

	}

	@Override
	public void validate() throws ValidationError {
		if (optionToSelect == null) {
			throw new ValidationError("Missing option to select");
		}
		if (selectionMode == SelectionMode.BY_POSITION) {
			try {
				if (Integer.valueOf(optionToSelect) < 0) {
					throw new ValidationError("Negative number forbidden");
				}
			} catch (NumberFormatException e) {
				throw new ValidationError("'Option to select': " + e.toString()
						+ ". Positive number expected when selection mode is " + SelectionMode.BY_POSITION, e);
			}
		}

		if (selectionMode == null) {
			throw new ValidationError("Missing selection mode");
		}

	}

	public class Option implements Serializable{
		private static final long serialVersionUID = 1L;
		
		private int position;
		private String label;

		protected Option(int position, String label) {
			super();
			this.position = position;
			this.label = label;
		}

		public int getPosition() {
			return position;
		}

		public String getLabel() {
			return label;
		}

		public void selectThisOptionByLabel() {
			selectionMode = SelectionMode.BY_LABEL_TEXT;
			optionToSelect = label;
		}

		public void selectThisOptionByPosition() {
			selectionMode = SelectionMode.BY_POSITION;
			optionToSelect = Integer.toString(position);
		}

		@Override
		public String toString() {
			return "" + position + " - " + label;
		}

	}


	@Override
	public String toString() {
		String optionToSelectText = (optionToSelect == null) ? "<none>" : optionToSelect;
		String selectionModeText = (selectionMode == null) ? "<unspecified selection mode>"
				: selectionMode.toString().toLowerCase().replace('_', ' ');
		return "Select " + selectionModeText + " item <" + optionToSelectText + "> of "
				+ getComponentInformation();
	}

}
