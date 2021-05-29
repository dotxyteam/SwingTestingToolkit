package xy.ui.testing.action.component.specific;

import java.awt.AWTEvent;
import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.util.ReflectionUIError;
import xy.ui.testing.Tester;
import xy.ui.testing.action.component.TargetComponentTestAction;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.util.MiscUtils;
import xy.ui.testing.util.TestFailure;
import xy.ui.testing.util.ValidationError;

/**
 * Test action that selects a {@link JComboBox} item.
 * 
 * @author olitank
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SelectComboBoxItemAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;

	protected String optionToSelect = null;
	protected SelectionMode selectionMode = SelectionMode.BY_LABEL_TEXT;
	protected OptionPair[] knownOptionPairs;
	protected boolean optionListChecked = false;

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

	public boolean isOptionListChecked() {
		return optionListChecked;
	}

	public void setOptionListChecked(boolean optionListChecked) {
		this.optionListChecked = optionListChecked;
	}

	public List<String> getKnownOptions() {
		if (knownOptionPairs == null) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<String>();
		for (OptionPair optionPair : knownOptionPairs) {
			if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
				result.add(optionPair.getLabel());
			} else if (selectionMode == SelectionMode.BY_POSITION) {
				result.add(Integer.toString(optionPair.getPosition()));
			} else {
				throw new ReflectionUIError();
			}
		}
		return result;
	}

	public void setKnownOptions(List<String> newKnownOptions) {
		if (newKnownOptions.size() == 0) {
			knownOptionPairs = null;
			return;
		}
		OptionPair[] newKnownOptionPairs = new OptionPair[newKnownOptions.size()];
		for (int i = 0; i < newKnownOptions.size(); i++) {
			if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
				newKnownOptionPairs[i] = new OptionPair(i, newKnownOptions.get(i));
			} else if (selectionMode == SelectionMode.BY_POSITION) {
				newKnownOptionPairs[i] = new OptionPair(Integer.valueOf(newKnownOptions.get(i)),
						"<unknown label " + i + ">");
			} else {
				throw new ReflectionUIError();
			}
		}
		knownOptionPairs = newKnownOptionPairs;
	}

	@Override
	protected boolean initializeSpecificProperties(Component c, AWTEvent introspectionRequestEvent,
			TestEditor testEditor) {
		if (!(c instanceof JComboBox)) {
			return false;
		}
		JComboBox comboBox = (JComboBox) c;
		List<String> allOptions = getAllOptions(comboBox);
		if (allOptions.size() == 0) {
			return false;
		}
		String labelText = getLabelText(comboBox.getModel(), comboBox.getRenderer(), 0);
		if (labelText != null) {
			selectionMode = SelectionMode.BY_LABEL_TEXT;
			optionToSelect = allOptions.get(0);
		} else {
			selectionMode = SelectionMode.BY_POSITION;
			optionToSelect = "0";
		}
		knownOptionPairs = new OptionPair[allOptions.size()];
		for (int i = 0; i < allOptions.size(); i++) {
			knownOptionPairs[i] = new OptionPair(i, allOptions.get(i));
		}
		optionListChecked = false;
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

	protected List<String> getAllOptionsFromUIThread(final JComboBox comboBox) {
		final List<String>[] result = new List[1];
		MiscUtils.expectingToBeInUIThread(new Runnable() {
			@Override
			public void run() {
				result[0] = getAllOptions(comboBox);
			}
		});
		return result[0];
	}

	protected String getLabelText(ComboBoxModel model, ListCellRenderer renderer, int i) {
		Object item = (i == -1) ? null : model.getElementAt(i);
		Component cellRenderer = renderer.getListCellRendererComponent(new JList(), item, 0, false, false);
		if (!(cellRenderer instanceof JLabel)) {
			return null;
		}
		return ((JLabel) cellRenderer).getText();
	}

	@Override
	public void execute(Component c, Tester tester) {
		final JComboBox comboBox = (JComboBox) c;
		List<String> options = getAllOptionsFromUIThread(comboBox);
		if (optionListChecked) {
			List<String> knownOptions = getKnownOptions();
			if (!options.equals(knownOptions)) {
				throw new TestFailure("The combo box items have changed. These are the expected and actual items:"
						+ "\n" + MiscUtils.formatStringList(knownOptions) + "\n" + MiscUtils.formatStringList(options));
			}
		}
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
		MiscUtils.expectingToBeInUIThread(new Runnable() {
			@Override
			public void run() {
				comboBox.setSelectedIndex(finalIndexToSelect);
			}
		});

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

		List<String> options = getKnownOptions();
		if ((options != null) && (options.size() > 0)) {
			if (!options.contains(optionToSelect)) {
				throw new ValidationError(
						"The option to select is not valid: '" + optionToSelect + "'. Expected: " + options);
			}
		}

	}

	@Override
	public String getValueDescription() {
		if (optionToSelect == null) {
			return "<unspecified option>";
		} else {
			if (selectionMode == SelectionMode.BY_POSITION) {
				try {
					return MiscUtils.formatOccurrence("item", Integer.valueOf(optionToSelect));
				} catch (NumberFormatException e) {
					return "<invalid item number>";
				}
			} else if (selectionMode == SelectionMode.BY_LABEL_TEXT) {
				return "\"" + StringEscapeUtils.escapeJava(optionToSelect) + "\"";
			} else {
				return "<unspecified selection mode>";
			}
		}
	}

	@Override
	public String toString() {
		return "Select option  " + getValueDescription() + " from " + getComponentInformation();
	}

	public class OptionPair implements Serializable {
		private static final long serialVersionUID = 1L;

		private int position;
		private String label;

		protected OptionPair(int position, String label) {
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
}
