package xy.ui.testing.util;

import java.awt.Component;

/**
 * This interface is used to access the elements of a component tree.
 * 
 * @author olitank
 *
 */
public interface IComponentTreeVisitor {

	boolean visit(Component c);

}
