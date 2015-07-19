package xy.ui.testing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.info.IInfoCollectionSettings;
import xy.reflect.ui.info.InfoCategory;
import xy.reflect.ui.info.InfoCollectionSettingsProxy;
import xy.reflect.ui.info.field.ImplicitListField;
import xy.reflect.ui.info.field.IFieldInfo;
import xy.reflect.ui.info.method.IMethodInfo;
import xy.reflect.ui.info.type.iterable.IListTypeInfo;
import xy.reflect.ui.info.type.iterable.util.IListAction;
import xy.reflect.ui.info.type.iterable.util.ItemPosition;
import xy.reflect.ui.info.type.iterable.util.structure.IListStructuralInfo;
import xy.reflect.ui.info.type.iterable.util.structure.ListStructuralInfoProxy;
import xy.reflect.ui.info.type.source.ITypeInfoSource;
import xy.reflect.ui.info.type.source.JavaTypeInfoSource;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.action.CallMainMethodAction;
import xy.ui.testing.action.CheckNumberOfOpenWindowsAction;
import xy.ui.testing.action.WaitAction;
import xy.ui.testing.action.component.ClickAction;
import xy.ui.testing.action.component.ClickOnMenuItemAction;
import xy.ui.testing.action.component.SendKeysAction;
import xy.ui.testing.action.component.SendKeysAction.KeyboardInteraction;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlC;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlV;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlX;
import xy.ui.testing.action.component.SendKeysAction.WriteText;
import xy.ui.testing.action.component.SendKeysAction.SpecialKey.CtrlA;
import xy.ui.testing.action.component.combobox.SelectComboBoxItemAction;
import xy.ui.testing.action.component.property.ChangeComponentPropertyAction;
import xy.ui.testing.action.component.property.CheckComponentPropertyAction;
import xy.ui.testing.action.component.table.ClickOnTableCellAction;
import xy.ui.testing.action.component.table.SelectTableRowAction;
import xy.ui.testing.action.component.treetable.ExpandTreetTableToItemAction;
import xy.ui.testing.action.window.CheckWindowVisibleStringsAction;
import xy.ui.testing.action.window.CloseWindowAction;
import xy.ui.testing.action.TestAction;
import xy.ui.testing.finder.ClassBasedComponentFinder;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.VisibleStringComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyCriteria;
import xy.ui.testing.util.AlternateWindowDecorationsPanel;
import xy.ui.testing.util.TestingUtils;
import xy.reflect.ui.info.method.InvocationData;
import xy.reflect.ui.info.method.InvocationData;

@SuppressWarnings("unused")
public class TesterUI extends ReflectionUI {

	public final static TesterUI INSTANCE = new TesterUI();
	public static final Class<?>[] TEST_ACTION_CLASSESS = new Class[] {
			CallMainMethodAction.class, WaitAction.class,
			ExpandTreetTableToItemAction.class, SelectComboBoxItemAction.class,
			SelectTableRowAction.class, ClickOnTableCellAction.class,
			ClickOnMenuItemAction.class, ClickAction.class,
			SendKeysAction.class, CloseWindowAction.class,
			ChangeComponentPropertyAction.class,
			CheckComponentPropertyAction.class,
			CheckWindowVisibleStringsAction.class,
			CheckNumberOfOpenWindowsAction.class };
	public static final Class<?>[] COMPONENT_FINDER_CLASSESS = new Class[] {
			VisibleStringComponentFinder.class,
			ClassBasedComponentFinder.class, PropertyBasedComponentFinder.class };
	public static final Class<?>[] KEYBOARD_INTERACTION_CLASSESS = new Class[] {
			WriteText.class, SpecialKey.class, CtrlA.class, CtrlC.class,
			CtrlV.class, CtrlX.class };

	protected Component componentFinderInitializationSource;
	protected Map<String, Image> imageCache = new HashMap<String, Image>();
	protected TestAction lastExecutedTestAction;

	public static void main(String[] args) {
		try {
			Tester tester = new Tester();
			if (args.length > 1) {
				throw new Exception(
						"Invalid command line arguments. Expected: [<fileName>]");
			} else if (args.length == 1) {
				String fileName = args[0];
				tester.loadFromFile(new File(fileName));
			}
			INSTANCE.openObjectFrame(tester);
		} catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, t.toString(), null,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected TesterUI() {
	}

	public TestAction getLastExecutedTestAction() {
		return lastExecutedTestAction;
	}

	public void setLastExecutedTestAction(TestAction lastExecutedTestAction) {
		this.lastExecutedTestAction = lastExecutedTestAction;
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
			protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
				ITypeInfo itemtype = type.getItemType();
				if (itemtype.getName().equals(TestAction.class.getName())) {
					return new ListStructuralInfoProxy(
							super.getStructuralInfo(type)) {

						@Override
						public String getCellValue(ItemPosition itemPosition,
								int columnIndex) {
							if (columnIndex == 0) {
								return Integer
										.toString(itemPosition.getIndex() + 1);
							} else {
								columnIndex--;
								return super.getCellValue(itemPosition,
										columnIndex);
							}
						}

						@Override
						public Image getCellIconImage(
								ItemPosition itemPosition, int columnIndex) {
							if (columnIndex == 1) {
								return super.getCellIconImage(itemPosition, 0);
							} else {
								return null;
							}
						}

						@Override
						public String getColumnCaption(int columnIndex) {
							if (columnIndex == 0) {
								return "N°";
							} else {
								columnIndex--;
								return super.getColumnCaption(columnIndex);
							}
						}

						@Override
						public int getColumnCount() {
							return super.getColumnCount() + 1;
						}

					};
				} else {
					return super.getStructuralInfo(type);
				}
			}

			@Override
			protected List<IFieldInfo> getFields(ITypeInfo type) {
				if ((type instanceof DefaultTypeInfo)
						&& type.getName().equals(
								PropertyBasedComponentFinder.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(
							super.getFields(type));
					result.add(new ImplicitListField(INSTANCE,
							"propertyCriterias", type,
							"createPropertyCriteria", "getPropertyCriteria",
							"addPropertyCriteria", "removePropertyCriteria",
							"propertyCriteriaCount"));
					return result;
				} else {
					return super.getFields(type);
				}
			}

			/*
			 * @Override protected List<IMethodInfo> getMethods(ITypeInfo type)
			 * { if (type.getName().equals(
			 * PropertyBasedComponentFinder.class.getName())) {
			 * List<IMethodInfo> result = new ArrayList<IMethodInfo>(
			 * super.getMethods(type));
			 * result.remove(ReflectionUIUtils.findInfoByName(result,
			 * "createPropertyCriteria"));
			 * result.remove(ReflectionUIUtils.findInfoByName(result,
			 * "getPropertyCriteria"));
			 * result.remove(ReflectionUIUtils.findInfoByName(result,
			 * "addPropertyCriteria"));
			 * result.remove(ReflectionUIUtils.findInfoByName(result,
			 * "removePropertyCriteria"));
			 * result.remove(ReflectionUIUtils.findInfoByName(result,
			 * "getPropertyCriteriaCount")); return result; } else { return
			 * super.getMethods(type); } }
			 */

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
							throw new ReflectionUIError(e);
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
							throw new ReflectionUIError(e);
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
							throw new ReflectionUIError(e);
						}
					}
					return result;
				}
				return super.getPolymorphicInstanceSubTypes(type);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method,
					ITypeInfo containingType, Object object,
					InvocationData invocationData) {
				if (method.getName().equals("startRecording")) {
					return ModificationStack.NULL_MODIFICATION;
				}
				if (method.getName().equals("stopRecording")) {
					return ModificationStack.NULL_MODIFICATION;
				}
				return super.getUndoModification(method, containingType,
						object, invocationData);
			}

			@Override
			protected List<IListAction> getSpecificListActions(
					IListTypeInfo type, final Object object, IFieldInfo field,
					final List<? extends ItemPosition> selection) {
				if ((object instanceof Tester)
						&& (field.getName().equals("testActions"))) {
					if (selection.size() > 0) {
						List<IListAction> result = new ArrayList<IListAction>();
						result.add(new IListAction() {

							@Override
							public void perform(final Component listControl) {
								try {
									List<TestAction> selectedActions = new ArrayList<TestAction>();
									for (ItemPosition itemPosition : selection) {
										TestAction testAction = (TestAction) itemPosition
												.getItem();
										selectedActions.add(testAction);
									}
									Tester tester = (Tester) object;
									IMethodInfo playMethod = getFormsUpdatingMethod(
											object,
											ReflectionUIUtils
													.getJavaMethodSignature(Tester.class
															.getMethod(
																	"play",
																	List.class,
																	Runnable.class)));
									InvocationData invocationData = new InvocationData(selectedActions);
									playMethod.invoke(tester,
											invocationData);
								} catch (Exception e) {
									throw new ReflectionUIError(e);
								}
							}

							@Override
							public String getTitle() {
								return "Play Selected Action(s)";
							}
						});
						return result;
					}
				}
				return super.getSpecificListActions(type, object, field,
						selection);
			}

			@Override
			protected Object invoke(final Object object,
					final InvocationData invocationData,
					final IMethodInfo method, final ITypeInfo containingType) {
				if (containingType.getName().equals(Tester.class.getName())) {
					if (method.getName().startsWith("play")) {
						for (final JPanel form : getForms(object)) {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									SwingUtilities.getWindowAncestor(form)
											.toBack();
								}
							});
							Object result = super.invoke(object,
									invocationData, method,
									containingType);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									onSuccessfulPlay((Tester) object, form);
								}
							});
							return result;
						}
					}
				}
				/*
				 * if (containingType.getName().equals(Tester.class.getName()))
				 * { if (method.getName().equals("startRecording")) { for (final
				 * JPanel form : getForms(object)) {
				 * SwingUtilities.invokeLater(new Runnable() {
				 * 
				 * @Override public void run() {
				 * SwingUtilities.getWindowAncestor(form) .toBack(); } });
				 * return super.invoke(object, invocationData, method,
				 * containingType); } } }
				 */
				return super.invoke(object, invocationData, method,
						containingType);
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

	@Override
	public JPanel createObjectForm(Object object,
			IInfoCollectionSettings settings) {
		settings = new InfoCollectionSettingsProxy(settings) {

			@Override
			public boolean excludeMethod(IMethodInfo method) {
				if (method.getName().equals("execute")) {
					return true;
				}
				if (method.getName().equals("findComponent")) {
					return true;
				}
				if (method.getName().equals("find")) {
					return true;
				}
				if (method.getName().equals("initializeFrom")) {
					return true;
				}
				if (method.getName().equals("extractVisibleString")) {
					return true;
				}
				if (method.getName().equals("getKeyEvents")) {
					return true;
				}
				if (method.getName().equals("play")) {
					return true;
				}
				if (method.getName().equals("collectVisibleStrings")) {
					return true;
				}
				if (method.getName().equals("loadFromStream")) {
					return true;
				}
				if (method.getName().equals("saveToStream")) {
					return true;
				}
				if (method.getName().equals("assertSuccessfulReplay")) {
					return true;
				}
				if (method.getName().equals("matches")) {
					return true;
				}
				return super.excludeMethod(method);
			}

			@Override
			public boolean excludeField(IFieldInfo field) {
				if (field.getName().equals("keyStrokes")) {
					return true;
				}
				if (field.getName().equals("valueDescription")) {
					return true;
				}
				if (field.getName().equals("componentInformation")) {
					return true;
				}
				return super.excludeField(field);
			}

		};
		return super.createObjectForm(object, settings);
	}

	@Override
	public Image getObjectIconImage(Object object) {
		if (object == null) {
			return null;
		}
		String imageResourceName = object.getClass().getSimpleName() + ".png";
		if (TesterUI.class.getResource(imageResourceName) != null) {
			Image result = imageCache.get(imageResourceName);
			if (result == null) {
				try {
					result = ImageIO.read(TesterUI.class
							.getResourceAsStream(imageResourceName));
				} catch (IOException e) {
					throw new AssertionError(e);
				}
				imageCache.put(imageResourceName, result);
			}
			if (object instanceof TestAction) {
				if (object != lastExecutedTestAction) {
					imageResourceName = "unhiglighted-" + imageResourceName;
					Image unhighlighted = imageCache.get(imageResourceName);
					if (unhighlighted == null) {
						unhighlighted = unhighlightIconImage(result);
						imageCache.put(imageResourceName, unhighlighted);
					}
					result = unhighlighted;
				}
			}
			return result;

		} else {
			return super.getObjectIconImage(object);
		}
	}

	protected Image unhighlightIconImage(Image image) {
		BufferedImage result = new BufferedImage(image.getWidth(null),
				image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = result.getGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		for (int i = 0; i < result.getWidth(); i++) {
			for (int j = 0; j < result.getHeight(); j++) {
				Color color = new Color(result.getRGB(i, j), true);
				int grayLevel = Math
						.round((color.getRed() + color.getGreen() + color
								.getBlue()) / 3f);
				Color newColor = new Color(grayLevel, grayLevel, grayLevel,
						color.getAlpha());
				result.setRGB(i, j, newColor.getRGB());
			}
		}
		return result;
	}

	public void upadateTestActionsControl(Tester tester) {
		for (JPanel form : getForms(tester)) {
			refreshFieldControl(form, "testActions");
		}
	}

	@Override
	public Container createWindowContentPane(Window window, Component content,
			List<? extends Component> toolbarControls) {
		Container result = super.createWindowContentPane(window, content,
				toolbarControls);
		AlternateWindowDecorationsPanel decorationsPanel = getAlternateWindowDecorationsPanel(window);
		decorationsPanel.configureWindow(window);
		decorationsPanel.getContentPanel().add(result);
		result = decorationsPanel;
		return result;
	}

	public static AlternateWindowDecorationsPanel getAlternateWindowDecorationsPanel(
			Window window) {
		return new AlternateWindowDecorationsPanel(
				ReflectionUIUtils.getWindowTitle(window)) {

			private static final long serialVersionUID = 1L;

			@Override
			public Color getDecorationsBackgroundColor() {
				return Tester.HIGHLIGHT_FOREGROUND;
			}

			@Override
			public Color getDecorationsForegroundColor() {
				return Tester.HIGHLIGHT_BACKGROUND;
			}

		};
	}

	protected void onSuccessfulPlay(Tester tester, Component activatorComponent) {
		showMessageDialog(activatorComponent,
				"The test action(s) completed successfully!",
				getObjectKind(tester));

	}

}
