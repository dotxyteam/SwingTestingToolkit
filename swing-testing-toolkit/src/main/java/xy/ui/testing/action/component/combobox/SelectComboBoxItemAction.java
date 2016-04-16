package xy.ui.testing.action.component.combobox;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.info.annotation.Validating;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

public class SelectComboBoxItemAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected String optionToSelect = "";
	protected SelectionMode selectionMode = SelectionMode.BY_LABEL_TEXT;

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

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent event) {
		if (!(c instanceof JComboBox)) {
			return false;
		}
		JComboBox comboBox = (JComboBox) c;
		if (comboBox.getItemCount() == 0) {
			return false;
		}
		String labelText = getLabelText(comboBox, 0);
		if (labelText != null) {
			selectionMode = SelectionMode.BY_LABEL_TEXT;
		} else {
			selectionMode = SelectionMode.BY_POSITION;
		}
		optionToSelect = "<Choose 1 of these options>";
		boolean first = true;
		for (String option : getAllOptions(comboBox)) {
			if (!first) {
				optionToSelect += ",";
			}
			optionToSelect += " " + option;
			first = false;
		}
		return true;
	}

	protected String getOption(JComboBox comboBox, int i) {
		if (selectionMode == SelectionMode.BY_POSITION) {
			return Integer.toString(i);
		} else if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
			return getLabelText(comboBox, i);
		} else if (selectionMode == SelectionMode.BY_STRING_VALUE) {
			return comboBox.getModel().getElementAt(i).toString();
		} else {
			throw new AssertionError();
		}

	}

	protected List<String> getAllOptions(JComboBox comboBox) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			result.add(getOption(comboBox, i));
		}
		return result;
	}

	protected String getLabelText(JComboBox comboBox, int i) {
		Object item = comboBox.getModel().getElementAt(i);
		Component cellRenderer = comboBox.getRenderer().getListCellRendererComponent(new JList(), item, 0, false,
				false);
		if (!(cellRenderer instanceof JLabel)) {
			return null;
		}
		return ((JLabel) cellRenderer).getText();
	}

	@Override
	public void execute(Component c, Tester tester) {
		JComboBox comboBox = (JComboBox) c;
		List<String> options = getAllOptions(comboBox);
		boolean found = false;
		int i = 0;
		for (String option : options) {
			if (option.equals(optionToSelect)) {
				comboBox.setSelectedIndex(i);
				found = true;
				break;
			}
			i++;
		}
		if (!found) {
			throw new TestFailure("Could not select the combo box item '" + optionToSelect + "': Item not found." + "\n"
					+ "Selection Mode=" + selectionMode + "\n" + "Found items: " + options);
		}

	}

	@Override
	public String getValueDescription() {
		if (selectionMode == SelectionMode.BY_POSITION) {
			return "Item n°" + (Integer.valueOf(optionToSelect) + 1);
		} else if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
			return StringEscapeUtils.escapeJava(optionToSelect);
		} else if (selectionMode == SelectionMode.BY_STRING_VALUE) {
			return StringEscapeUtils.escapeJava(optionToSelect);
		} else {
			throw new AssertionError();
		}

	}

	public enum SelectionMode {
		BY_LABEL_TEXT, BY_POSITION, BY_STRING_VALUE

	}

	@Override
	@Validating
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

}
