package xy.ui.testing;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.action.SendClickAction;
import xy.ui.testing.action.SendKeysAction;
import xy.ui.testing.action.SendKeysAction.KeyboardInteraction;
import xy.ui.testing.action.SendKeysAction.SpecialKey;
import xy.ui.testing.action.SendKeysAction.WriteText;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.VisibleStringComponentFinder;

public class TesterUI extends ReflectionUI {

	public final static TesterUI INSTANCE = new TesterUI();
	public static final Class<?>[] TEST_ACTION_CLASSESS = new Class[] {
			SendClickAction.class, SendKeysAction.class };
	public static final Class<?>[] COMPONENT_FINDER_CLASSESS = new Class[] {
			ClassBasedComponentFinder.class, VisibleStringComponentFinder.class };
	public static final Class<?>[] KEYBOARD_INTERACTION_CLASSESS = new Class[] {
			WriteText.class, SpecialKey.class };

	private Component componentFinderInitializationSource;

	public static void main(String[] args) {
		Tester tester = new Tester();
		INSTANCE.openObjectFrame(tester, INSTANCE.getObjectKind(tester), null);

	}

	protected TesterUI() {
	}

	@Override
	public JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		JFrame result = super.createFrame(content, title, iconImage, toolbarControls);
		for (JPanel form : ReflectionUIUtils.findDescendantForms(result,
				TesterUI.INSTANCE)) {
			if(TesterUI.INSTANCE.getObjectByForm().get(form) instanceof Tester){
				result.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		}
		return result;
	}

	@Override
	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		return new TypeInfoProxyConfiguration() {

			@Override
			protected List<IMethodInfo> getMethods(ITypeInfo type) {
				List<IMethodInfo> result = new ArrayList<IMethodInfo>(
						super.getMethods(type));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"openSettings"));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"execute"));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"openWindow"));
				result.remove(ReflectionUIUtils.findInfoByName(result, "find"));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"initializeFrom"));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"extractVisibleString"));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"getKeyStrokes"));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"findComponentAndExecute"));
				return result;
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				List<IFieldInfo> result = new ArrayList<IFieldInfo>(
						super.getFields(type));
				result.remove(ReflectionUIUtils.findInfoByName(result,
						"keyStrokes"));
				return result;
			}

			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(
					ITypeInfo type) {
				if (type.getName().equals(TestAction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : TEST_ACTION_CLASSESS) {
						try {
							result.add(getTypeInfo(getTypeInfoSource(clazz
									.newInstance())));
						} catch (Exception e) {
							throw new AssertionError(e);
						}
					}
					return result;
				}
				if (type.getName().equals(ComponentFinder.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : COMPONENT_FINDER_CLASSESS) {
						try {
							ComponentFinder newInstance = (ComponentFinder) clazz
									.newInstance();
							if (componentFinderInitializationSource != null) {
								if (newInstance
										.initializeFrom(componentFinderInitializationSource)) {
									result.add(getTypeInfo(getTypeInfoSource(newInstance)));
								}
							} else {
								result.add(getTypeInfo(getTypeInfoSource(newInstance)));
							}
						} catch (Exception e) {
							throw new AssertionError(e);
						}
					}
					return result;
				}
				if (type.getName().equals(KeyboardInteraction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : KEYBOARD_INTERACTION_CLASSESS) {
						try {
							result.add(getTypeInfo(getTypeInfoSource(clazz
									.newInstance())));
						} catch (Exception e) {
							throw new AssertionError(e);
						}
					}
					return result;
				}
				return super.getPolymorphicInstanceSubTypes(type);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method,
					ITypeInfo containingType, Object object,
					Map<String, Object> valueByParameterName) {
				if (method.getName().equals("startRecording")) {
					return ModificationStack.NULL_MODIFICATION;
				}
				if (method.getName().equals("stopRecording")) {
					return ModificationStack.NULL_MODIFICATION;
				}
				return super.getUndoModification(method, containingType,
						object, valueByParameterName);
			}

		}.get(super.getTypeInfo(typeSource));
	}

	@Override
	public Object onTypeInstanciationRequest(Component activatorComponent,
			ITypeInfo type, boolean silent) {
		Object result = super.onTypeInstanciationRequest(activatorComponent,
				type, silent);
		if (result instanceof ComponentFinder) {
			if (componentFinderInitializationSource != null) {
				((ComponentFinder) result)
						.initializeFrom(componentFinderInitializationSource);
			}
		}
		return result;
	}

	public boolean openSettings(TestAction testAction, Component c) {
		boolean[] okPressedArray = new boolean[] { false };
		componentFinderInitializationSource = c;
		TesterUI.INSTANCE.openObjectDialog(null, testAction,
				TesterUI.INSTANCE.getObjectKind(testAction), null, true, null,
				okPressedArray, null, null, IInfoCollectionSettings.DEFAULT);
		componentFinderInitializationSource = null;
		return okPressedArray[0];
	}

}
