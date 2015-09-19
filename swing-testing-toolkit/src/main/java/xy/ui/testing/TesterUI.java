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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xy.reflect.ui.ReflectionUI;
import xy.reflect.ui.SwingRenderer;
import xy.reflect.ui.SwingRenderer.NullableControl;
import xy.reflect.ui.control.swing.ListControl;
import xy.reflect.ui.control.swing.ListControl.AutoUpdatingFieldItemPosition;
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
import xy.reflect.ui.info.type.util.HiddenNullableFacetsTypeInfoProxyConfiguration;
import xy.reflect.ui.info.type.util.TypeInfoProxyConfiguration;
import xy.reflect.ui.info.type.DefaultTypeInfo;
import xy.reflect.ui.info.type.ITypeInfo;
import xy.reflect.ui.undo.IModification;
import xy.reflect.ui.undo.ModificationStack;
import xy.reflect.ui.util.ReflectionUIError;
import xy.reflect.ui.util.ReflectionUIUtils;
import xy.reflect.ui.util.SwingRendererUtils;
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
import xy.ui.testing.finder.MenuItemComponentFinder;
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
	public static final Class<?>[] TEST_ACTION_CLASSESS = new Class[] { CallMainMethodAction.class, WaitAction.class,
			ExpandTreetTableToItemAction.class, SelectComboBoxItemAction.class, SelectTableRowAction.class,
			ClickOnTableCellAction.class, ClickOnMenuItemAction.class, ClickAction.class, SendKeysAction.class,
			CloseWindowAction.class, ChangeComponentPropertyAction.class, CheckComponentPropertyAction.class,
			CheckWindowVisibleStringsAction.class, CheckNumberOfOpenWindowsAction.class };
	public static final Class<?>[] COMPONENT_FINDER_CLASSESS = new Class[] { VisibleStringComponentFinder.class,
			ClassBasedComponentFinder.class, PropertyBasedComponentFinder.class, MenuItemComponentFinder.class };
	public static final Class<?>[] KEYBOARD_INTERACTION_CLASSESS = new Class[] { WriteText.class, SpecialKey.class,
			CtrlA.class, CtrlC.class, CtrlV.class, CtrlX.class };
	private static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	protected Component componentFinderInitializationSource;
	protected Map<String, Image> imageCache = new HashMap<String, Image>();
	protected boolean recordingInsertedAfterSelection = false;

	public static void main(String[] args) {
		try {
			Tester tester = new Tester();
			if (args.length > 1) {
				throw new Exception("Invalid command line arguments. Expected: [<fileName>]");
			} else if (args.length == 1) {
				String fileName = args[0];
				tester.loadFromFile(new File(fileName));
			}
			INSTANCE.getSwingRenderer().openObjectFrame(tester);
		} catch (Throwable t) {
			INSTANCE.getSwingRenderer().handleExceptionsFromDisplayedUI(null, t);
		}
	}

	protected TesterUI() {
	}

	public boolean isRecordingInsertedAfterSelection() {
		return recordingInsertedAfterSelection;
	}

	public void setRecordingInsertedAfterSelection(boolean b) {
		this.recordingInsertedAfterSelection = b;
	}

	protected void playActionsAndUpdateUI(Tester tester, List<TestAction> selectedActions) {
		String methodSignature;
		try {
			methodSignature = ReflectionUIUtils
					.getJavaMethodInfoSignature(Tester.class.getMethod("play", List.class, Runnable.class));
		} catch (Exception e) {
			throw new AssertionError(e);
		}
		IMethodInfo playMethod = getSwingRenderer().getFormUpdatingMethod(tester, methodSignature);
		playMethod.invoke(tester, new InvocationData(selectedActions));
	}

	@Override
	public SwingRenderer createSwingRenderer() {
		return new SwingRenderer(TesterUI.this) {

			@Override
			public Container createWindowContentPane(Window window, Component content,
					List<? extends Component> toolbarControls) {
				Container result = super.createWindowContentPane(window, content, toolbarControls);
				AlternateWindowDecorationsPanel decorationsPanel = getAlternateWindowDecorationsPanel(window);
				decorationsPanel.configureWindow(window);
				decorationsPanel.getContentPanel().add(result);
				result = decorationsPanel;
				return result;
			}

			@Override
			public Object onTypeInstanciationRequest(Component activatorComponent, ITypeInfo type, boolean silent) {
				Object result = super.onTypeInstanciationRequest(activatorComponent, type, silent);
				if (result instanceof ComponentFinder) {
					if (componentFinderInitializationSource != null) {
						((ComponentFinder) result).initializeFrom(componentFinderInitializationSource);
					}
				}
				return result;
			}

			@Override
			public boolean onMethodInvocationRequest(Component activatorComponent, Object object, IMethodInfo method,
					Object[] returnValueArray) {
				if ((object instanceof Tester) && method.getName().equals("startRecording")) {
					Tester tester = (Tester) object;
					ListControl testActionsControl = getTestActionsControl(tester);
					if (testActionsControl.getSelection().size() == 1) {
						String insertMessage = "Insert Recordings After The Current Selection Row";
						String doNotInsertMessage = "Insert Recordings At The End";
						String answer = getSwingRenderer().openSelectionDialog(testActionsControl,
								Arrays.asList(insertMessage, doNotInsertMessage), insertMessage, "Choose", null);
						if (insertMessage.equals(answer)) {
							recordingInsertedAfterSelection = true;
						} else if (doNotInsertMessage.equals(answer)) {
							recordingInsertedAfterSelection = false;
						} else {
							return false;
						}
					}
				}
				return super.onMethodInvocationRequest(activatorComponent, object, method, returnValueArray);
			}

			@Override
			public JFrame createFrame(Component content, String title, Image iconImage,
					List<? extends Component> toolbarControls) {
				JFrame result = super.createFrame(content, title, iconImage, toolbarControls);
				for (JPanel form : SwingRendererUtils.findDescendantForms(result, TesterUI.INSTANCE)) {
					if (TesterUI.INSTANCE.getSwingRenderer().getObjectByForm().get(form) instanceof Tester) {
						result.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						result.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
					}
				}
				return result;
			}

			@Override
			public JPanel createObjectForm(Object object, IInfoCollectionSettings settings) {
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
						if (method.getName().equals("matches")) {
							return true;
						}
						if (method.getName().equals("matchIntrospectionRequestEvent")) {
							return true;
						}
						if (method.getName().equals("macthesComponent")) {
							return true;
						}
						if (method.getName().equals("setPropertyNames")) {
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
						if (field.getName().equals("propertyCriteriaList")) {
							return true;
						}
						return super.excludeField(field);
					}

				};
				return super.createObjectForm(object, settings);
			}

		};
	}

	@Override
	public ITypeInfo getTypeInfo(ITypeInfoSource typeSource) {
		return new HiddenNullableFacetsTypeInfoProxyConfiguration(TesterUI.this) {

			@Override
			protected boolean isNullable(IFieldInfo field, ITypeInfo containingType) {
				if (field.getName().equals("checkThrownExceptionAfterSeconds")) {
					return true;
				} else if (field.getName().equals("knownOptions")) {
					return true;
				} else if (field.getName().equals("newPropertyValue")) {
					return true;
				} else if (field.getName().equals("propertyValueExpected")) {
					return true;
				} else {
					return super.isNullable(field, containingType);
				}
			}

			@Override
			protected String toString(ITypeInfo type) {
				if (type.getName().equals(CtrlA.class.getName())) {
					return "CtrlA (Select All)";
				} else if (type.getName().equals(CtrlC.class.getName())) {
					return "CtrlC (Copy)";
				} else if (type.getName().equals(CtrlV.class.getName())) {
					return "CtrlV (Paste)";
				} else if (type.getName().equals(CtrlX.class.getName())) {
					return "CtrlX (Cut)";
				} else {
					return super.toString(type);
				}
			}

			@Override
			protected Image getIconImage(ITypeInfo type, Object object) {
				String imageResourceName = type.getName();
				int lastDotIndex = imageResourceName.lastIndexOf(".");
				if (lastDotIndex != -1) {
					imageResourceName = imageResourceName.substring(lastDotIndex + 1);
				}
				imageResourceName += ".png";
				Image result = imageCache.get(imageResourceName);
				if (result == null) {
					if (TesterUI.class.getResource(imageResourceName) == null) {
						result = NULL_IMAGE;
					} else {
						try {
							result = ImageIO.read(TesterUI.class.getResourceAsStream(imageResourceName));
						} catch (IOException e) {
							throw new AssertionError(e);
						}
					}
					imageCache.put(imageResourceName, result);
				}
				if (result == NULL_IMAGE) {
					return super.getIconImage(type, object);
				}
				return result;
			}

			@Override
			protected IListStructuralInfo getStructuralInfo(IListTypeInfo type) {
				ITypeInfo itemtype = type.getItemType();
				if (itemtype.getName().equals(TestAction.class.getName())) {
					return new ListStructuralInfoProxy(super.getStructuralInfo(type)) {

						@Override
						public String getCellValue(ItemPosition itemPosition, int columnIndex) {
							if (columnIndex == 0) {
								return Integer.toString(itemPosition.getIndex() + 1);
							} else {
								columnIndex--;
								return super.getCellValue(itemPosition, columnIndex);
							}
						}

						@Override
						public Image getCellIconImage(ItemPosition itemPosition, int columnIndex) {
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
						&& type.getName().equals(PropertyBasedComponentFinder.class.getName())) {
					List<IFieldInfo> result = new ArrayList<IFieldInfo>(super.getFields(type));
					result.add(new ImplicitListField(INSTANCE, "propertyCriterias", type, "createPropertyCriteria",
							"getPropertyCriteria", "addPropertyCriteria", "removePropertyCriteria",
							"propertyCriteriaCount"));
					return result;
				} else {
					return super.getFields(type);
				}
			}

			@Override
			protected List<ITypeInfo> getPolymorphicInstanceSubTypes(ITypeInfo type) {
				if (type.getName().equals(TestAction.class.getName())) {
					List<ITypeInfo> result = new ArrayList<ITypeInfo>();
					for (Class<?> clazz : TEST_ACTION_CLASSESS) {
						try {
							result.add(getTypeInfo(getTypeInfoSource(clazz.newInstance())));
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
							ComponentFinder newInstance = (ComponentFinder) clazz.newInstance();
							if (componentFinderInitializationSource != null) {
								if (newInstance.initializeFrom(componentFinderInitializationSource)) {
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
							result.add(getTypeInfo(getTypeInfoSource(clazz.newInstance())));
						} catch (Exception e) {
							throw new ReflectionUIError(e);
						}
					}
					return result;
				}
				return super.getPolymorphicInstanceSubTypes(type);
			}

			@Override
			protected IModification getUndoModification(IMethodInfo method, ITypeInfo containingType, Object object,
					InvocationData invocationData) {
				if (method.getName().startsWith("play")) {
					return ModificationStack.EMPTY_MODIFICATION;
				}
				if (method.getName().equals("startRecording")) {
					return ModificationStack.EMPTY_MODIFICATION;
				}
				if (method.getName().equals("stopRecording")) {
					return ModificationStack.EMPTY_MODIFICATION;
				}
				return super.getUndoModification(method, containingType, object, invocationData);
			}

			@Override
			protected List<IListAction> getSpecificListActions(IListTypeInfo type, final Object object,
					IFieldInfo field, final List<? extends ItemPosition> selection) {
				if ((object instanceof Tester) && (field.getName().equals("testActions"))) {
					if (selection.size() > 0) {
						List<IListAction> result = new ArrayList<IListAction>();
						result.add(new IListAction() {

							@Override
							public void perform(final Component listControl) {
								try {
									List<TestAction> selectedActions = new ArrayList<TestAction>();
									for (ItemPosition itemPosition : selection) {
										TestAction testAction = (TestAction) itemPosition.getItem();
										selectedActions.add(testAction);
									}
									Tester tester = (Tester) object;
									playActionsAndUpdateUI(tester, selectedActions);
								} catch (Exception e) {
									throw new ReflectionUIError(e);
								}
							}

							@Override
							public String getTitle() {
								return "Play Selected Action(s)";
							}
						});
						if (selection.size() == 1) {
							result.add(new IListAction() {

								@Override
								public void perform(final Component listControl) {
									try {
										List<TestAction> actionsToPlay = new ArrayList<TestAction>();
										ItemPosition singleSelection = selection.get(0);
										for (int i = singleSelection.getIndex(); i < singleSelection
												.getContainingListValue().length; i++) {
											TestAction testAction = (TestAction) singleSelection.getSibling(i)
													.getItem();
											actionsToPlay.add(testAction);
										}
										Tester tester = (Tester) object;
										playActionsAndUpdateUI(tester, actionsToPlay);
									} catch (Exception e) {
										throw new ReflectionUIError(e);
									}
								}

								@Override
								public String getTitle() {
									return "Play From Selection To End";
								}
							});
						}
						return result;
					}
				}
				return super.getSpecificListActions(type, object, field, selection);
			}

			@Override
			protected Object invoke(final Object object, final InvocationData invocationData, final IMethodInfo method,
					final ITypeInfo containingType) {
				if (containingType.getName().equals(Tester.class.getName())) {

					if (method.getName().equals("startRecording")) {
						final JPanel form = getTesterForm((Tester) object);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								SwingUtilities.getWindowAncestor(form).toBack();
							}
						});
					}

					Object result = super.invoke(object, invocationData, method, containingType);

					return result;
				} else {
					return super.invoke(object, invocationData, method, containingType);
				}
			}

		}.get(super.getTypeInfo(typeSource));
	}

	protected JPanel getTesterForm(Tester tester) {
		List<JPanel> result = getSwingRenderer().getForms(tester);
		if (result.size() == 0) {
			return null;
		}
		if (result.size() > 1) {
			throw new AssertionError("More than 1 form was found for: " + tester);
		}
		return result.get(0);
	}

	protected ListControl getTestActionsControl(Tester tester) {
		final JPanel form = getTesterForm(tester);
		if (form == null) {
			return null;
		}
		List<Component> result = getSwingRenderer().getFieldControlsByName(form, "testActions");
		if (result.size() != 1) {
			throw new AssertionError("'testActions' control not found for: " + tester);
		}
		Component c = result.get(0);
		if (c instanceof NullableControl) {
			c = ((NullableControl) c).getSubControl();
		}
		return (ListControl) c;
	}

	public boolean openSettings(TestAction testAction, Component c, Tester tester) {
		componentFinderInitializationSource = c;
		boolean[] okPressedArray = new boolean[] { false };
		TesterUI.INSTANCE.getSwingRenderer().openObjectDialog(c, testAction,
				TesterUI.INSTANCE.getObjectKind(testAction), null, true, null, okPressedArray, null, null,
				IInfoCollectionSettings.DEFAULT);
		componentFinderInitializationSource = null;
		return okPressedArray[0];
	}

	public void selectTestAction(TestAction testAction, Tester tester) {
		ListControl testActionsControl = getTestActionsControl(tester);
		if (testActionsControl == null) {
			return;
		}
		testActionsControl.setSingleSelection(testActionsControl.findItemPosition(testAction));
	}

	public int getSelectedActionIndex(Tester tester) {
		ListControl testActionsControl = getTestActionsControl(tester);
		AutoUpdatingFieldItemPosition result = testActionsControl.getSingleSelection();
		if (result == null) {
			return -1;
		}
		return result.getIndex();
	}

	public static AlternateWindowDecorationsPanel getAlternateWindowDecorationsPanel(Window window) {
		return new AlternateWindowDecorationsPanel(SwingRendererUtils.getWindowTitle(window)) {

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

}
