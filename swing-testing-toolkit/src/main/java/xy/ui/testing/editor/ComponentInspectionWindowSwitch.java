package xy.ui.testing.editor;

import java.awt.Component;
import java.util.List;

import javax.swing.JDialog;

import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.editor.StandardEditorBuilder;
import xy.reflect.ui.control.swing.renderer.Form;
import xy.reflect.ui.info.type.iterable.item.BufferedItemPosition;
import xy.reflect.ui.util.Listener;
import xy.reflect.ui.util.SwingRendererUtils;
import xy.ui.testing.util.ComponentInspector;
import xy.ui.testing.util.ComponentInspector.ComponentInspectorNode;

public class ComponentInspectionWindowSwitch extends AbstractWindowSwitch {

	protected boolean inspectorOpen = false;

	public ComponentInspectionWindowSwitch(TestEditor testEditor) {
		super(testEditor);
	}

	@Override
	public String getSwitchTitle() {
		return "Component(s) Inspection";
	}

	@Override
	public Object getStatus() {
		return "(Waiting) Choose a component to inspect...";
	}

	@Override
	protected void onBegining() {
	}

	@Override
	protected void onEnd() {
		getTester().handleCurrentComponentChange(null);
	}

	public boolean isInspectorOpen() {
		return inspectorOpen;
	}

	public void openComponentInspector(Component c, Component activatorComponent) {
		inspectorOpen = true;
		try {
			ComponentInspector inspector = new ComponentInspector(c, testEditor);
			StandardEditorBuilder inspectorDialogBuilder = getSwingRenderer().getEditorBuilder(activatorComponent,
					inspector, getSwingRenderer().getObjectTitle(inspector),
					getSwingRenderer().getObjectIconImage(inspector), false);
			JDialog inspectorDialog = inspectorDialogBuilder.createDialog();
			highlightComponentOnSelectionFromInspectorDialog(inspector, inspectorDialog);
			getSwingRenderer().showDialog(inspectorDialog, true);
		} finally {
			inspectorOpen = false;
		}

	}

	protected void highlightComponentOnSelectionFromInspectorDialog(ComponentInspector inspector,
			JDialog inspectorDialog) {
		Form inpectorForm = SwingRendererUtils.findFirstObjectDescendantForm(inspector, inspectorDialog,
				getSwingRenderer());
		final ListControl componentTreeControl = (ListControl) inpectorForm.getFieldControlPlaceHolder("rootNode")
				.getFieldControl();
		componentTreeControl.addListControlSelectionListener(new Listener<List<BufferedItemPosition>>() {
			@Override
			public void handle(List<BufferedItemPosition> newSelection) {
				if (newSelection.size() == 1) {
					ComponentInspectorNode node = (ComponentInspectorNode) newSelection.get(0).getItem();
					Component targetComponent = node.getComponent();
					getTester().handleCurrentComponentChange(targetComponent);
				}
			}
		});
	}

}