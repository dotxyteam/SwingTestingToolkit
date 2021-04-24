package xy.ui.testing.util;

/**
 * Validation error exception class.
 * 
 * @author olitank
 *
 */
public class ValidationError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected static String buildMessage(String target, String errorMessage) {
		String result = "";
		if (target != null) {
			result += target + ": ";
		}
		result += errorMessage;
		return result;
	}

	public ValidationError(String errorMessage, String target, Throwable cause) {
		super(buildMessage(target, errorMessage), cause);
	}

	public ValidationError(String errorMessage, String target) {
		this(errorMessage, target, null);
	}

	public ValidationError(String errorMessage) {
		this(errorMessage, null, null);
	}

	public ValidationError(String errorMessage, Throwable cause) {
		this(errorMessage, null, cause);
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
