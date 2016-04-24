package xy.ui.testing.util;

import java.io.File;

public class TestFailure extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String imageDescription;
	private File imageFile;

	public TestFailure(String message, String imageDescription, File imageFile, Throwable cause) {
		super(message, cause);
		this.imageDescription = imageDescription;
		this.imageFile = imageFile;
	}

	public TestFailure(String message, String imageDescription, File imageFile) {
		this(message, imageDescription, imageFile, null);
	}

	public TestFailure(String message, Throwable cause) {
		this(message, null, null, cause);
	}

	public TestFailure(String message) {
		this(message, null, null, null);
	}

	public TestFailure() {
		this(null, null, null, null);
	}

	public TestFailure(Throwable cause) {
		this(null, null, null, cause);
	}

	public String getImageDescription() {
		return imageDescription;
	}

	public File getImageFile() {
		return imageFile;
	}

	@Override
	public String getMessage() {
		String result = super.getMessage();
		if (result == null) {
			if(getCause() != null){
				result = getCause().toString();
			}
		}
		if (result == null) {
			result = "Test Failure";
		}
		if ((imageDescription != null) && (imageFile != null)) {
			result += "\n\n- " + imageDescription + " image:\n" + imageFile.getAbsolutePath();
		}
		return result;
	}

	@Override
	public String toString() {
		return getMessage();
	}

}
