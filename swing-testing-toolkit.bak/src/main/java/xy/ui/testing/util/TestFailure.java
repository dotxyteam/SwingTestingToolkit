package xy.ui.testing.util;

public class TestFailure extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TestFailure() {
		super();
	}

	public TestFailure(String message, Throwable cause) {
		super(message, cause);
	}

	public TestFailure(String message) {
		super(message);
	}

	public TestFailure(Throwable cause) {
		super(cause);
	}

}
