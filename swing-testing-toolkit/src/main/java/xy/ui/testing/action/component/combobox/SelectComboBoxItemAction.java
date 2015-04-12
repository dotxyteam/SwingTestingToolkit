package xy.ui.testing.action.component.combobox;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import xy.ui.testing.action.component.TargetComponentTestAction;

public class SelectComboBoxItemAction extends TargetComponentTestAction {

	private static final long serialVersionUID = 1L;
	protected int optionToSelect = 0;
	protected String knownOptions;

	public int getOptionToSelect() {
		return optionToSelect;
	}

	public void setOptionToSelect(int optionToSelect) {
		this.optionToSelect = optionToSelect;
	}

	public String getKnownOptions() {
		return knownOptions;
	}

	public void setKnownOptions(String knownOptions) {
		this.knownOptions = knownOptions;
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
		knownOptions = StringUtils.join(collectOptions(comboBox), "\n");
		optionToSelect = 0;
		return true;
	}

	protected List<String> collectOptions(JComboBox comboBox) {
		List<String> result = new ArrayList<String>();
		ComboBoxModel model = comboBox.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			result.add(StringEscapeUtils.escapeJava(i + " - "
					+ model.getElementAt(i)));
		}
		return result;
	}

	@Override
	public void execute(Component c) {
		JComboBox comboBox = (JComboBox) c;
		comboBox.setSelectedIndex(optionToSelect);
	}

	@Override
	public String getValueDescription() {
		if (knownOptions != null) {
			String[] array = knownOptions.split("\n");
			if (array.length > optionToSelect) {
				return array[optionToSelect];
			}
		}
		return "Item n°" + (optionToSelect + 1);
	}

}
