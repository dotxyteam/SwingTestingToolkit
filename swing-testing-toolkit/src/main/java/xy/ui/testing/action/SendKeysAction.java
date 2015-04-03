package xy.ui.testing.action;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.KeyStroke;

import org.apache.commons.lang3.StringEscapeUtils;

public class SendKeysAction extends TestAction {

	protected List<KeyboardInteraction> keyboardInteractions = new ArrayList<KeyboardInteraction>();

	public KeyboardInteraction[] getKeyboardInteractions() {
		return keyboardInteractions
				.toArray(new KeyboardInteraction[keyboardInteractions.size()]);
	}

	public void setKeyboardInteractions(
			KeyboardInteraction[] keyboardInteractions) {
		this.keyboardInteractions.clear();
		this.keyboardInteractions.addAll(Arrays.asList(keyboardInteractions));
	}

	@Override
	public void execute(Component c) {
		for (KeyboardInteraction interaction : keyboardInteractions) {
			for (KeyStroke keyStroke : interaction.getKeyStrokes()) {
				if (keyStroke.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
					KeyEvent keEvt = new KeyEvent(c, KeyEvent.KEY_PRESSED,
							System.currentTimeMillis(), keyStroke.getModifiers(),
							keyStroke.getKeyCode(), keyStroke.getKeyChar());
					c.dispatchEvent(keEvt);
					System.out.println(keEvt);
				} else {
					KeyEvent keEvt = new KeyEvent(c, KeyEvent.KEY_TYPED,
							System.currentTimeMillis(), 0,
							keyStroke.getKeyCode(), keyStroke.getKeyChar());
					c.dispatchEvent(keEvt);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Send " + Arrays.toString(keyboardInteractions.toArray())
				+ " event(s) to the " + getComponentFinder();
	}

	public static abstract class KeyboardInteraction {

		public abstract List<KeyStroke> getKeyStrokes();

	}

	public static class WriteText extends KeyboardInteraction {
		protected String text = "";

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public List<KeyStroke> getKeyStrokes() {
			List<KeyStroke> result = new ArrayList<KeyStroke>();
			if (text != null) {
				for (char ch : text.toCharArray()) {
					result.add(KeyStroke.getKeyStroke(ch));
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return "Type \"" + StringEscapeUtils.escapeJava(text) + "\"";
		}

	}

	public static class SpecialKey extends KeyboardInteraction {
		public enum KeyName {
			KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9, KEY_A, KEY_ACCEPT, KEY_ADD, KEY_AGAIN, KEY_ALL_CANDIDATES, KEY_ALPHANUMERIC, KEY_ALT, KEY_ALT_GRAPH, KEY_AMPERSAND, KEY_ASTERISK, KEY_AT, KEY_B, KEY_BACK_QUOTE, KEY_BACK_SLASH, KEY_BACK_SPACE, KEY_BEGIN, KEY_BRACELEFT, KEY_BRACERIGHT, KEY_C, KEY_CANCEL, KEY_CAPS_LOCK, KEY_CIRCUMFLEX, KEY_CLEAR, KEY_CLOSE_BRACKET, KEY_CODE_INPUT, KEY_COLON, KEY_COMMA, KEY_COMPOSE, KEY_CONTEXT_MENU, KEY_CONTROL, KEY_CONVERT, KEY_COPY, KEY_CUT, KEY_D, KEY_DEAD_ABOVEDOT, KEY_DEAD_ABOVERING, KEY_DEAD_ACUTE, KEY_DEAD_BREVE, KEY_DEAD_CARON, KEY_DEAD_CEDILLA, KEY_DEAD_CIRCUMFLEX, KEY_DEAD_DIAERESIS, KEY_DEAD_DOUBLEACUTE, KEY_DEAD_GRAVE, KEY_DEAD_IOTA, KEY_DEAD_MACRON, KEY_DEAD_OGONEK, KEY_DEAD_SEMIVOICED_SOUND, KEY_DEAD_TILDE, KEY_DEAD_VOICED_SOUND, KEY_DECIMAL, KEY_DELETE, KEY_DIVIDE, KEY_DOLLAR, KEY_DOWN, KEY_E, KEY_END, KEY_ENTER, KEY_EQUALS, KEY_ESCAPE, KEY_EURO_SIGN, KEY_EXCLAMATION_MARK, KEY_F, KEY_F1, KEY_F10, KEY_F11, KEY_F12, KEY_F13, KEY_F14, KEY_F15, KEY_F16, KEY_F17, KEY_F18, KEY_F19, KEY_F2, KEY_F20, KEY_F21, KEY_F22, KEY_F23, KEY_F24, KEY_F3, KEY_F4, KEY_F5, KEY_F6, KEY_F7, KEY_F8, KEY_F9, KEY_FINAL, KEY_FIND, KEY_FULL_WIDTH, KEY_G, KEY_GREATER, KEY_H, KEY_HALF_WIDTH, KEY_HELP, KEY_HIRAGANA, KEY_HOME, KEY_I, KEY_INPUT_METHOD_ON_OFF, KEY_INSERT, KEY_INVERTED_EXCLAMATION_MARK, KEY_J, KEY_JAPANESE_HIRAGANA, KEY_JAPANESE_KATAKANA, KEY_JAPANESE_ROMAN, KEY_K, KEY_KANA, KEY_KANA_LOCK, KEY_KANJI, KEY_KATAKANA, KEY_KP_DOWN, KEY_KP_LEFT, KEY_KP_RIGHT, KEY_KP_UP, KEY_L, KEY_LEFT, KEY_LEFT_PARENTHESIS, KEY_LESS, KEY_M, KEY_META, KEY_MINUS, KEY_MODECHANGE, KEY_MULTIPLY, KEY_N, KEY_NONCONVERT, KEY_NUMBER_SIGN, KEY_NUMPAD0, KEY_NUMPAD1, KEY_NUMPAD2, KEY_NUMPAD3, KEY_NUMPAD4, KEY_NUMPAD5, KEY_NUMPAD6, KEY_NUMPAD7, KEY_NUMPAD8, KEY_NUMPAD9, KEY_NUM_LOCK, KEY_O, KEY_OPEN_BRACKET, KEY_P, KEY_PAGE_DOWN, KEY_PAGE_UP, KEY_PASTE, KEY_PAUSE, KEY_PERIOD, KEY_PLUS, KEY_PREVIOUS_CANDIDATE, KEY_PRINTSCREEN, KEY_PROPS, KEY_Q, KEY_QUOTE, KEY_QUOTEDBL, KEY_R, KEY_RIGHT, KEY_RIGHT_PARENTHESIS, KEY_ROMAN_CHARACTERS, KEY_S, KEY_SCROLL_LOCK, KEY_SEMICOLON, KEY_SEPARATER, KEY_SEPARATOR, KEY_SHIFT, KEY_SLASH, KEY_SPACE, KEY_STOP, KEY_SUBTRACT, KEY_T, KEY_TAB, KEY_U, KEY_UNDEFINED, KEY_UNDERSCORE, KEY_UNDO, KEY_UP, KEY_V, KEY_W, KEY_WINDOWS, KEY_X, KEY_Y, KEY_Z;

			@Override
			public String toString() {
				return super.toString().substring("KEY_".length());
			}

		}

		protected KeyName keyName = KeyName.KEY_UNDEFINED;
		protected boolean shiftDown;
		protected boolean ctrlDown;
		protected boolean metaDown;
		protected boolean altDown;
		protected boolean altGrtDown;

		public KeyName getKeyName() {
			return keyName;
		}

		public void setKeyName(KeyName keyName) {
			this.keyName = keyName;
		}

		public boolean isShiftDown() {
			return shiftDown;
		}

		public void setShiftDown(boolean shiftDown) {
			this.shiftDown = shiftDown;
		}

		public boolean isCtrlDown() {
			return ctrlDown;
		}

		public void setCtrlDown(boolean ctrlDown) {
			this.ctrlDown = ctrlDown;
		}

		public boolean isMetaDown() {
			return metaDown;
		}

		public void setMetaDown(boolean metaDown) {
			this.metaDown = metaDown;
		}

		public boolean isAltDown() {
			return altDown;
		}

		public void setAltDown(boolean alttDown) {
			this.altDown = alttDown;
		}

		public boolean isAltGrtDown() {
			return altGrtDown;
		}

		public void setAltGrtDown(boolean altGrtDown) {
			this.altGrtDown = altGrtDown;
		}

		@Override
		public List<KeyStroke> getKeyStrokes() {
			return Collections.singletonList(KeyStroke.getKeyStroke(
					getKeyCode(keyName), getModifiers()));
		}

		protected int getModifiers() {
			int result = 0;
			if (isShiftDown()) {
				result |= InputEvent.SHIFT_DOWN_MASK;
			}
			if (isCtrlDown()) {
				result |= InputEvent.CTRL_DOWN_MASK;
			}
			if (isMetaDown()) {
				result |= InputEvent.META_DOWN_MASK;
			}
			if (isAltDown()) {
				result |= InputEvent.ALT_DOWN_MASK;
			}
			if (isAltGrtDown()) {
				result |= InputEvent.ALT_GRAPH_DOWN_MASK;
			}
			return result;
		}

		protected static int getKeyCode(KeyName keyName) {
			String codeFieldName = "VK_"
					+ keyName.name().substring("KEY_".length());
			try {
				Field codeField = KeyEvent.class.getField(codeFieldName);
				return codeField.getInt(null);
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public String toString() {
			String result = keyName.toString();
			if (metaDown) {
				result = "Meta + " + result;
			}
			if (shiftDown) {
				result = "Shift + " + result;
			}
			if (altGrtDown) {
				result = "AltGr + " + result;
			}
			if (altDown) {
				result = "Alt + " + result;
			}
			if (ctrlDown) {
				result = "Ctrl + " + result;
			}
			return result;
		}

	}

}
