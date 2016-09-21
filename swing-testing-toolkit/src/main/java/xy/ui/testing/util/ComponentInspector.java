package xy.ui.testing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xy.ui.testing.TesterUI;
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

		private PropertyBasedComponentFinder util;
		List<ComponentInspectorNode> chilren;

		private Component c;
		private TesterUI testerUI;

		public ComponentInspectorNode(Component c, TesterUI testerUI) {
			this.c = c;
			this.testerUI = testerUI;
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

		public List<ComponentInspectorNode> getComponentTree() {
			if (chilren == null) {
				chilren = new ArrayList<ComponentInspectorNode>();
				if (c instanceof Container) {
					Container container = (Container) c;
					for (Component child : container.getComponents()) {
						chilren.add(new ComponentInspectorNode(child, testerUI));
					}
				}
			}
			return chilren;
		}

		@Override
		public String toString() {
			return getComponentClassName();
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
