package xy.ui.testing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.StrBuilder;

import xy.reflect.ui.control.swing.renderer.SwingRenderer;
import xy.ui.testing.Tester;

/**
 * Various utilities.
 * 
 * @author olitank
 *
 */
public class MiscUtils {

	private static Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();
	private static final Image NULL_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	public static <K, V> List<K> getKeysFromValue(Map<K, V> map, Object value) {
		List<K> result = new ArrayList<K>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (MiscUtils.equalsOrBothNull(entry.getValue(), value)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	public static boolean equalsOrBothNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		} else {
			return o1.equals(o2);
		}
	}

	public static Color stringToColor(String s) {
		try {
			String[] rgb = s.split(",");
			return new Color(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
		} catch (Exception e) {
			return Color.decode("0x" + s);
		}
	}

	public static String colorToString(Color c) {
		return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	}

	public static Image loadImage(File file) {
		try {
			return ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Image loadImageResource(String imageResourceName) {
		Image result = MiscUtils.IMAGE_CACHE.get(imageResourceName);
		if (result == null) {
			if (Tester.class.getResource(imageResourceName) == null) {
				result = MiscUtils.NULL_IMAGE;
			} else {
				try {
					result = ImageIO.read(Tester.class.getResourceAsStream(imageResourceName));
				} catch (IOException e) {
					throw new AssertionError(e);
				}
			}
			MiscUtils.IMAGE_CACHE.put(imageResourceName, result);
		}
		if (result == MiscUtils.NULL_IMAGE) {
			return null;
		}
		return result;
	}

	public static String getOSAgnosticFilePath(String path) {
		return path.replace(File.separator, "/");
	}

	public static boolean isDirectOrIndirectOwner(Window ownerWindow, Window window) {
		while ((window = window.getOwner()) != null) {
			if (ownerWindow == window) {
				return true;
			}
		}
		return false;
	}

	public static void sortWindowsByOwnershipDepth(List<Window> openWindows) {
		Collections.sort(openWindows, new Comparator<Window>() {
			@Override
			public int compare(Window w1, Window w2) {
				if (isDirectOrIndirectOwner(w1, w2)) {
					return -1;
				}
				if (isDirectOrIndirectOwner(w2, w1)) {
					return 1;
				}
				return 0;
			}
		});
	}

	public static File saveTimestampedImageFile(File directory, BufferedImage image) {
		String fileExtension = "png";
		File outputfile = new File(directory,
				"screenschot-" + new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date()) + "." + fileExtension);
		if (outputfile.exists()) {
			int newOutputFileNameIndex = 0;
			File newOutputFile;
			while (true) {
				newOutputFile = new File(outputfile.getPath().replaceAll("\\." + fileExtension + "$",
						"-" + newOutputFileNameIndex + "." + fileExtension));
				if (!newOutputFile.exists()) {
					outputfile = newOutputFile;
					break;
				}
				newOutputFileNameIndex++;
			}
		}
		try {
			ImageIO.write(image, fileExtension, outputfile);
		} catch (IOException e) {
			throw new AssertionError("Failed to save the image file: '" + outputfile.getAbsolutePath() + "': " + e);
		}
		return outputfile;
	}

	public static boolean askWithTimeout(final SwingRenderer swingRenderer, final Component activatorComponent,
			final String question, final String title, final String yesCaption, final String noCaption,
			final int timeoutSeconds, boolean defaultAnswer) {
		final Boolean[] ok = new Boolean[] { null };
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ok[0] = swingRenderer.openQuestionDialog(activatorComponent, question, title, yesCaption, noCaption);
			}
		});
		for (int i = 0; i < timeoutSeconds; i++) {
			if (ok[0] != null) {
				return ok[0];
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return defaultAnswer;
	}

	/**
	 * Checks that the current thread is the UI thread and executes the specified
	 * action.
	 * 
	 * @param runnable The action to execute.
	 */
	public static void expectingToBeInUIThread(final Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new AssertionError("This method must be invoked from the UI Thread");
		}
		runnable.run();
	}

	/**
	 * Schedules the specified action to be executed in the UI thread and waits
	 * until the execution end. This method allows to avoid that the calling thread
	 * runs too fast according to the state of the UI components that it modifies
	 * (may happen when using {@link SwingUtilities#invokeLater(Runnable)}) or
	 * blocks while waiting for user interactions (may happen when using
	 * {@link SwingUtilities#invokeAndWait(Runnable)}).
	 * 
	 * @param runnable The action to execute.
	 * @throws Throwable If the action throws an exception (rethrows).
	 */
	public static void executeSafelyInUIThread(final Runnable runnable) throws Throwable {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new AssertionError("This method cannot be invoked from the UI Thread");
		}
		final Throwable[] runnableError = new Throwable[1];
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (Throwable t) {
					runnableError[0] = t;
				}
			}
		});
		final boolean[] runnableEnded = new boolean[] { false };
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				runnableEnded[0] = true;
			}
		});
		while (!runnableEnded[0]) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}
			Thread.sleep(100);
		}

		if (runnableError[0] != null) {
			throw runnableError[0];
		}
	}

	public static String formatOccurrence(String s, int index) {
		return s + "[" + index + "]";
	}

	/**
	 * Calls the main method of the given class with the specified arguments.
	 * 
	 * @param mainClassName The name of the class.
	 * @param arguments     The list of arguments provided to the main method.
	 * @throws Throwable If a problem occurs.
	 */
	public static void launchClassMainMethod(String mainClassName, String[] arguments) throws Throwable {
		try {
			Class.forName(mainClassName).getMethod("main", new Class[] { String[].class }).invoke(null,
					new Object[] { arguments });
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	/**
	 * @param strings The list of strings.
	 * @return A formatted text listing the given strings.
	 */
	public static String formatStringList(List<String> strings) {
		if(strings.size() == 0) {
			return "<EMPTY_LIST>";
		}
		StrBuilder result = new StrBuilder();
		for (int i = 0; i < strings.size(); i++) {
			if (i > 0) {
				result.append(", ");
			}
			String s = strings.get(i);
			s = "\"" + StringEscapeUtils.escapeJava(s) + "\"";
			result.append(s);
		}
		return result.toString();
	}

	/**
	 * @param formattedStringList The formatted string list to parse.
	 * @return A list of strings created from the given formatted string list.
	 */
	public static List<String> parseStringList(String formattedStringList) {
		List<String> result = new ArrayList<String>();
		String QUOTED_STRING = "\".*?(?<!\\\\)\"";
		String MULTIPLE_QUOTED_STRINGS = "((" + QUOTED_STRING + "\\s*,\\s*)*" + QUOTED_STRING + ")?";
		if (!formattedStringList.matches(MULTIPLE_QUOTED_STRINGS)) {
			throw new AssertionError("Invalid string list:\n" + formattedStringList
					+ "\nExpected string list formatted as: \"string 1\", \"string 2\", ... \"string n\"");
		}
		Pattern p = Pattern.compile(QUOTED_STRING);
		Matcher m = p.matcher(formattedStringList);
		while (m.find()) {
			String s = m.group();
			s = s.substring(1, s.length() - 1);
			s = StringEscapeUtils.unescapeJava(s);
			result.add(s);
		}
		return result;
	}

	public static void repaintImmediately(JComponent c) {
		c.paintImmediately(0, 0, c.getWidth(), c.getHeight());
	}

	public static String truncateNicely(String s, int length) {
		if (s.length() > length) {
			s = s.substring(0, length - 3) + "...";
		}
		return s;
	}

}
