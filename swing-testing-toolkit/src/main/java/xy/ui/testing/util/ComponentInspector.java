package xy.ui.testing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;

public class ComponentInspector {

	private ComponentInspectorNode rootNode;

	public ComponentInspector(Component c, TestEditor testEditor) {
		this.rootNode = new ComponentInspectorNode(c, testEditor);
	}

	public ComponentInspectorNode getRootNode() {
		return rootNode;
	}

	public int getWindowIndex() {
		return rootNode.createOrGetUtil().getWindowIndex();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rootNode == null) ? 0 : rootNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComponentInspector other = (ComponentInspector) obj;
		if (rootNode == null) {
			if (other.rootNode != null)
				return false;
		} else if (!rootNode.equals(other.rootNode))
			return false;
		return true;
	}

	public class ComponentInspectorNode {

		protected PropertyBasedComponentFinder util;
		protected List<ComponentInspectorNode> chilren;
		protected List<String> visibleStrings;
		protected Component c;
		protected TestEditor testEditor;
		protected String componentTreeDisplayedStringsSummary;

		public ComponentInspectorNode(Component c, TestEditor testEditor) {
			this.c = c;
			this.testEditor = testEditor;
			this.visibleStrings = testEditor.getTester().extractDisplayedStrings(c);
			this.visibleStrings.remove("");
		}

		protected PropertyBasedComponentFinder createOrGetUtil() {
			if (util == null) {
				util = new PropertyBasedComponentFinder() {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean initializeOccurrencesToSkip(Window componentWindow, Component c,
							TestEditor testEditor) {
						return true;
					}

				};
				util.initializeFrom(c, testEditor);
			}
			return util;
		}

		public Component getComponent() {
			return c;
		}

		public String getComponentClassName() {
			return c.getClass().getName();
		}

		public PropertyValue[] getPropertyValues() {
			List<PropertyValue> result = createOrGetUtil().getPropertyValueList();
			return result.toArray(new PropertyValue[result.size()]);
		}

		public List<ComponentInspectorNode> getChildren() {
			if (chilren == null) {
				chilren = new ArrayList<ComponentInspectorNode>();
				if (c instanceof Container) {
					Container container = (Container) c;
					for (Component child : testEditor.getTester().getChildrenComponents(container)) {
						chilren.add(new ComponentInspectorNode(child, testEditor));
					}
				}
			}
			return chilren;
		}

		public List<String> getComponentTreeDisplayedStrings() {
			final List<String> result = new ArrayList<String>(visibleStrings);
			for (ComponentInspectorNode node : getChildren()) {
				result.addAll(node.getComponentTreeDisplayedStrings());
			}
			return result;
		}

		public String getComponentTreeDisplayedStringsSummary() {
			if (componentTreeDisplayedStringsSummary == null) {
				StringBuilder result = new StringBuilder();
				int MAX_VISIBLE_STRING_COUNT = 2;
				int MAX_VISIBLE_STRING_LENGTH = 30;
				boolean stringListReduced = false;
				List<String> allVisibleStrings = getComponentTreeDisplayedStrings();
				if (allVisibleStrings.size() > MAX_VISIBLE_STRING_COUNT) {
					allVisibleStrings = allVisibleStrings.subList(0, MAX_VISIBLE_STRING_COUNT);
					stringListReduced = true;
				}
				if (allVisibleStrings.size() > 0) {
					result.append(" (");
					for (int i = 0; i < allVisibleStrings.size(); i++) {
						String s = allVisibleStrings.get(i);
						s = ReflectionUIUtils.truncateNicely(s, MAX_VISIBLE_STRING_LENGTH);
						if (i > 0) {
							result.append(", ");
						}
						result.append("\"" + StringEscapeUtils.escapeJava(s) + "\"");
					}
					if (stringListReduced) {
						result.append(", ...");
					}
					result.append(")");
				}
				componentTreeDisplayedStringsSummary = result.toString();
			}
			return componentTreeDisplayedStringsSummary;
		}

		public List<ComponentFinder> getCompatibleFinders() {
			List<ComponentFinder> result = new ArrayList<ComponentFinder>();
			for (Class<?> finderClass : testEditor.getComponentFinderClasses()) {
				ComponentFinder finder;
				try {
					finder = (ComponentFinder) finderClass.newInstance();
				} catch (Exception e) {
					throw new AssertionError(e);
				}
				if (finder.initializeFrom(c, testEditor)) {
					result.add(finder);
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return getComponentClassName() + getComponentTreeDisplayedStringsSummary();

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((c == null) ? 0 : c.hashCode());
			result = prime * result + ((testEditor == null) ? 0 : testEditor.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ComponentInspectorNode other = (ComponentInspectorNode) obj;
			if (c == null) {
				if (other.c != null)
					return false;
			} else if (!c.equals(other.c))
				return false;
			if (testEditor == null) {
				if (other.testEditor != null)
					return false;
			} else if (!testEditor.equals(other.testEditor))
				return false;
			return true;
		}

	}

}
