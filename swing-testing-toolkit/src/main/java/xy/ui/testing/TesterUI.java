package xy.ui.testing;

import java.awt.Component;
import java.awt.Image;
import java.awt.Dialog.ModalExclusionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.IListTypeInfo;
import xy.reflect.ui.info.type.IListTypeInfo.IItemPosition;
import xy.reflect.ui.info.type.IListTypeInfo.IListAction;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.info.type.ITypeInfoSource;
import xy.reflect.ui.info.type.TypeInfoProxyConfiguration;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.action.SendClickAction;
import xy.ui.testing.action.SendKeysAction;
import xy.ui.testing.action.SendKeysAction.KeyboardInteraction;
import xy.ui.testing.action.SendKeysAction.SpecialKey;
import xy.ui.testing.action.SendKeysAction.WriteText;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.VisibleStringComponentFinder;
import xy.ui.testing.util.TestingUtils;

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
		try {
			Tester tester = new Tester();
			INSTANCE.openObjectFrame(tester, INSTANCE.getObjectKind(tester),
					null);
			if (args.length > 1) {
				throw new Exception(
						"Invalid command line arguments. Expected: [<mainClassName>]");
			} else if (args.length == 1) {
				String mainClassName = args[0];
				TestingUtils.launchClassMainMethod(mainClassName);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, t.toString(), null,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected TesterUI() {
	}

	@Override
	public JFrame createFrame(Component content, String title, Image iconImage,
			List<? extends Component> toolbarControls) {
		JFrame result = super.createFrame(content, title, iconImage,
				toolbarControls);
		for (JPanel form : ReflectionUIUtils.findDescendantForms(result,
				TesterUI.INSTANCE)) {
			if (TesterUI.INSTANCE.getObjectByForm().get(form) instanceof Tester) {
				result.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				result.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
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
						"getKeyEvents"));
				result.remove(ReflectionUIUtils
						.findInfoByName(result, "replay"));
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

			@Override
			protected List<IListAction> getSpecificListActions(
					IListTypeInfo type, final Object object, IFieldInfo field,
					final List<? extends IItemPosition> selection) {
				if ((object instanceof Tester)
						&& (field.getName().equals("testActions"))) {
					if (selection.size() > 0) {
						List<IListAction> result = new ArrayList<IListTypeInfo.IListAction>();
						result.add(new IListAction() {

							@Override
							public void perform(final Component listControl) {
								List<TestAction> selectedActions = new ArrayList<TestAction>();
								for (IItemPosition itemPosition : selection) {
									TestAction testAction = (TestAction) itemPosition
											.getItem();
									selectedActions.add(testAction);
								}
								Tester tester = (Tester) object;
								tester.replay(selectedActions, null);
							}

							@Override
							public String getTitle() {
								return "Replay Selected Action(s)";
							}
						});
						return result;
					}
				}
				return super.getSpecificListActions(type, object, field,
						selection);
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
		componentFinderInitializationSource = c;
		boolean[] okPressedArray = new boolean[] { false };
		TesterUI.INSTANCE.openObjectDialog(c, testAction,
				TesterUI.INSTANCE.getObjectKind(testAction), null, true, null,
				okPressedArray, null, null, IInfoCollectionSettings.DEFAULT);
		componentFinderInitializationSource = null;
		return okPressedArray[0];
	}

}
