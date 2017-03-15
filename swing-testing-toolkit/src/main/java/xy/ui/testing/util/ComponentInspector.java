package xy.ui.testing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import xy.reflect.ui.util.ReflectionUIUtils;
import xy.ui.testing.TesterUI;
import xy.ui.testing.finder.ComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder;
import xy.ui.testing.finder.PropertyBasedComponentFinder.PropertyValue;

public class ComponentInspector {

	private ComponentInspectorNode root;

	public ComponentInspector(Component c, TesterUI testerUI) {
		this.root = new ComponentInspectorNode(c, testerUI);
	}

	public List<ComponentInspectorNode> getComponentTree() {
		return Collections.singletonList(root);
	}

	public int getWindowIndex() {
		return root.createOrGetUtil().getWindowIndex();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((root == null) ? 0 : root.hashCode());
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
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}

	public class ComponentInspectorNode {

		protected PropertyBasedComponentFinder util;
		protected List<ComponentInspectorNode> chilren;
		protected List<String> visibleStrings;
		protected Component c;
		protected TesterUI testerUI;
		protected String componentTreeVisibleStringsSummary;

		public ComponentInspectorNode(Component c, TesterUI testerUI) {
			this.c = c;
			this.testerUI = testerUI;
			this.visibleStrings = TestingUtils.extractVisibleStrings(c);
			this.visibleStrings.remove("");
		}

		protected PropertyBasedComponentFinder createOrGetUtil() {
			if (util == null) {
				util = new PropertyBasedComponentFinder() {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean initializeOccurrencesToSkip(Window componentWindow, Component c,
							TesterUI testerUI) {
						return true;
					}

				};
				util.initializeFrom(c, testerUI);
			}
			return util;
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
					for (Component child : testerUI.getTester().getChildrenComponents(container)) {
						chilren.add(new ComponentInspectorNode(child, testerUI));
					}
				}
			}
			return chilren;
		}

		public List<String> getComponentTreeVisibleStrings() {
			final List<String> result = new ArrayList<String>(visibleStrings);
			for (ComponentInspectorNode node : getChildren()) {
				result.addAll(node.getComponentTreeVisibleStrings());
			}
			return result;
		}

		public String getComponentTreeVisibleStringsSummary() {
			if (componentTreeVisibleStringsSummary == null) {
				StringBuilder result = new StringBuilder();
				int MAX_VISIBLE_STRING_COUNT = 2;
				int MAX_VISIBLE_STRING_LENGTH = 30;
				boolean stringListReduced = false;
				List<String> allVisibleStrings = getComponentTreeVisibleStrings();
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
				componentTreeVisibleStringsSummary = result.toString();
			}
			return componentTreeVisibleStringsSummary;
		}
		
		public List<ComponentFinder> getCompatibleFinders(){
			List<ComponentFinder> result = new ArrayList<ComponentFinder>();
			for(Class<?> finderClass :testerUI.getComponentFinderClasses()){
				ComponentFinder finder;
				try {
					finder = (ComponentFinder)finderClass.newInstance();
				} catch (Exception e) {
					throw new AssertionError(e);
				} 
				if(finder.initializeFrom(c, testerUI)){
					result.add(finder);
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return getComponentClassName() + getComponentTreeVisibleStringsSummary();

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((c == null) ? 0 : c.hashCode());
			result = prime * result + ((testerUI == null) ? 0 : testerUI.hashCode());
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
			if (testerUI == null) {
				if (other.testerUI != null)
					return false;
			} else if (!testerUI.equals(other.testerUI))
				return false;
			return true;
		}

	}

}
