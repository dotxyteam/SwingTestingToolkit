package xy.ui.testing.util;

public class TestingError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TestingError() {
		super();
	}

	public TestingError(String message, Throwable cause) {
		super(message, cause);
	}

	public TestingError(String message) {
		super(message);
	}

	public TestingError(Throwable cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return getMessage();
	}

	
}
