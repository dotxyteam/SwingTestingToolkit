package xy.ui.testing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.util.MiscUtils;
import xy.ui.testing.editor.TestEditor;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;

/**
 * This class allows to inspect a component properties, hierarchical structure,
 * displayed strings, etc.
 * 
 * @author olitank
 *
 */
public class ComponentInspector {

	private ComponentInspectorNode rootNode;

	public ComponentInspector(Component c, TestEditor testEditor) {
		this.rootNode = new ComponentInspectorNode(null, c, testEditor);
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

	public class InspectedPropertyValue {

		protected PropertyValue underlyingPropertyValue;

		public InspectedPropertyValue(PropertyValue underlyingPropertyValue) {
			this.underlyingPropertyValue = underlyingPropertyValue;
		}

		public String getPropertyName() {
			return underlyingPropertyValue.getPropertyName();
		}

		public String getValue() {
			return underlyingPropertyValue.getPropertyValueExpected();
		}

		@Override
		public String toString() {
			return underlyingPropertyValue.toString();
		}
	}

	public class ComponentInspectorNode {

		protected PropertyBasedComponentFinder util;
		protected ComponentInspectorNode parent;
		protected List<ComponentInspectorNode> chilren;
		protected List<ComponentInspectorNode> ancestors;
		protected List<String> visibleStrings;
		protected Component c;
		protected TestEditor testEditor;
		protected String componentTreeDisplayedStringsSummary;

		public ComponentInspectorNode(ComponentInspectorNode parent, Component c, TestEditor testEditor) {
			this.parent = parent;
			this.c = c;
			this.testEditor = testEditor;
			this.visibleStrings = testEditor.getTester().extractDisplayedStrings(c);
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

		public ComponentInspectorNode getParent() {
			return parent;
		}

		public Component getComponent() {
			return c;
		}

		public Class<?> getComponentClass() {
			return c.getClass();
		}

		public InspectedPropertyValue[] getPropertyValues() {
			List<InspectedPropertyValue> result = new ArrayList<InspectedPropertyValue>();
			for (PropertyValue propertyValue : createOrGetUtil().getPropertyValueList()) {
				result.add(new InspectedPropertyValue(propertyValue));
			}
			return result.toArray(new InspectedPropertyValue[result.size()]);
		}

		public List<ComponentInspectorNode> getChildren() {
			if (chilren == null) {
				chilren = new ArrayList<ComponentInspectorNode>();
				if (c instanceof Container) {
					Container container = (Container) c;
					for (Component child : testEditor.getTester().getChildrenComponents(container)) {
						chilren.add(new ComponentInspectorNode(this, child, testEditor));
					}
				}
			}
			return chilren;
		}

		public List<ComponentInspectorNode> getAncestors() {
			if (ancestors == null) {
				ancestors = new ArrayList<ComponentInspectorNode>();
				ComponentInspectorNode ancestor = parent;
				while (ancestor != null) {
					ancestors.add(ancestor);
					ancestor = ancestor.getParent();
				}
			}
			return ancestors;
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
					for (int i = 0; i < allVisibleStrings.size(); i++) {
						String s = allVisibleStrings.get(i);
						s = MiscUtils.truncateNicely(s, MAX_VISIBLE_STRING_LENGTH);
						if (i > 0) {
							result.append(", ");
						}
						result.append("\"" + StringEscapeUtils.escapeJava(s) + "\"");
					}
					if (stringListReduced) {
						result.append(", ...");
					}
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
			return getComponentClass().getName() + " (" + getComponentTreeDisplayedStringsSummary() + ")";

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
