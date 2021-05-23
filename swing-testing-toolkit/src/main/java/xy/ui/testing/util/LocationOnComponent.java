package xy.ui.testing.util;

import java.awt.Component;
import java.awt.Point;

/**
 * Allows to define a relative (percent) or absolute 2D location on a component.
 * 
 * @author olitank
 *
 */
public class LocationOnComponent {

	protected Value x = new Value();
	protected Value y = new Value();

	public Value getX() {
		return x;
	}

	public void setX(Value x) {
		this.x = x;
	}

	public Value getY() {
		return y;
	}

	public void setY(Value y) {
		this.y = y;
	}

	public Point getPoint(Component c) {
		Point result = new Point();
		result.x = getX().compute(0, c.getWidth());
		result.y = getY().compute(0, c.getHeight());
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
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
		LocationOnComponent other = (LocationOnComponent) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Location [x=" + x + ", y=" + y + "]";
	}

}
