package xy.ui.testing.util;

/**
 * Allows to define a relative (percent) or absolute value.
 * 
 * @author olitank
 *
 */
public class Value {

	protected Value.Unit unit = Unit.PERCENT;
	protected int amount = 50;

	public Value.Unit getUnit() {
		return unit;
	}

	public void setUnit(Value.Unit unit) {
		this.unit = unit;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int compute(int minimum, int maximum) {
		if (unit == Unit.ABSOLUTE) {
			return amount;
		} else if (unit == Unit.PERCENT) {
			return minimum + Math.round((maximum - minimum) * (amount / 100.0f));
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + amount;
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
		Value other = (Value) obj;
		if (unit != other.unit)
			return false;
		if (amount != other.amount)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Value [unit=" + unit + ", amount=" + amount + "]";
	}

	public enum Unit {
		ABSOLUTE, PERCENT
	}

}