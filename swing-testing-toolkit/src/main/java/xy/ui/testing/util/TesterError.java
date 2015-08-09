package xy.ui.testing.util;

public class TesterError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TesterError() {
		super();
	}

	public TesterError(String message, Throwable cause) {
		super(message, cause);
	}

	public TesterError(String message) {
		super(message);
	}

	public TesterError(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return getMessage();
	}

	
}
