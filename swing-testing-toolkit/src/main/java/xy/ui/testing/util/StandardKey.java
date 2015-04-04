package xy.ui.testing.util;

import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.util.Arrays;

/**
 * A typable character object.
 * 
 * This element allows an easy access to the keyCode and character of a standard
 * typable character. An object is identified either by the
 * <code>character</code> or the <code>keyCode</code> and <code>shiftDown<code/>
 * and <code>altDown</code> combination.
 * 
 * In order to avoid improper data insertion there is no public constructor
 * available. Instead there are three factory methods for object retrieval: -
 * object by character [access: O(n)] - object by <code>keyCode</code> and
 * <code>shiftDown<code/> and
 *   <code>altDown</code> combination [access: O(log(n))] - object by
 * <code>KeyEvent</code> [access: O(log(n))]
 * 
 * The character array is initialized during the initial class load. Currently
 * the following characters set are available: - standard qwerty key set -
 * Polish diacritic signs
 * 
 * @see SpecialKey
 * 
 * @author mbryla
 */
public class StandardKey implements Comparable<StandardKey> {
	private final Character character;
	private final Integer keyCode;
	private final boolean shiftDown;
	private final boolean altDown;

	private final static StandardKey[] KEYS = {// <editor-fold
												// defaultstate="collapsed"
												// desc="Character Constants">
	new StandardKey('a', VK_A), new StandardKey('b', VK_B),
			new StandardKey('c', VK_C), new StandardKey('d', VK_D),
			new StandardKey('e', VK_E), new StandardKey('f', VK_F),
			new StandardKey('g', VK_G), new StandardKey('h', VK_H),
			new StandardKey('i', VK_I), new StandardKey('j', VK_J),
			new StandardKey('k', VK_K), new StandardKey('l', VK_L),
			new StandardKey('m', VK_M), new StandardKey('n', VK_N),
			new StandardKey('o', VK_O), new StandardKey('p', VK_P),
			new StandardKey('q', VK_Q), new StandardKey('r', VK_R),
			new StandardKey('s', VK_S), new StandardKey('t', VK_T),
			new StandardKey('u', VK_U), new StandardKey('v', VK_V),
			new StandardKey('w', VK_W), new StandardKey('x', VK_X),
			new StandardKey('y', VK_Y), new StandardKey('z', VK_Z),
			new StandardKey('A', VK_A, true), new StandardKey('B', VK_B, true),
			new StandardKey('C', VK_C, true), new StandardKey('D', VK_D, true),
			new StandardKey('E', VK_E, true), new StandardKey('F', VK_F, true),
			new StandardKey('G', VK_G, true), new StandardKey('H', VK_H, true),
			new StandardKey('I', VK_I, true), new StandardKey('J', VK_J, true),
			new StandardKey('K', VK_K, true), new StandardKey('L', VK_L, true),
			new StandardKey('M', VK_M, true), new StandardKey('N', VK_N, true),
			new StandardKey('O', VK_O, true), new StandardKey('P', VK_P, true),
			new StandardKey('Q', VK_Q, true), new StandardKey('R', VK_R, true),
			new StandardKey('S', VK_S, true), new StandardKey('T', VK_T, true),
			new StandardKey('U', VK_U, true), new StandardKey('V', VK_V, true),
			new StandardKey('W', VK_W, true),
			new StandardKey('X', VK_X, true),
			new StandardKey('Y', VK_Y, true),
			new StandardKey('Z', VK_Z, true),
			new StandardKey('0', VK_0),
			new StandardKey('1', VK_1),
			new StandardKey('2', VK_2),
			new StandardKey('3', VK_3),
			new StandardKey('4', VK_4),
			new StandardKey('5', VK_5),
			new StandardKey('6', VK_6),
			new StandardKey('7', VK_7),
			new StandardKey('8', VK_8),
			new StandardKey('9', VK_9),
			new StandardKey('-', VK_MINUS),
			new StandardKey('=', VK_EQUALS),
			new StandardKey('~', VK_BACK_QUOTE, true),
			new StandardKey('!', VK_EXCLAMATION_MARK),
			new StandardKey('@', VK_AT),
			new StandardKey('#', VK_NUMBER_SIGN),
			new StandardKey('$', VK_DOLLAR),
			new StandardKey('%', VK_5, true),
			new StandardKey('^', VK_CIRCUMFLEX),
			new StandardKey('&', VK_AMPERSAND),
			new StandardKey('*', VK_ASTERISK),
			new StandardKey('(', VK_LEFT_PARENTHESIS),
			new StandardKey(')', VK_RIGHT_PARENTHESIS),
			new StandardKey('_', VK_UNDERSCORE),
			new StandardKey('+', VK_PLUS),
			new StandardKey('\t', VK_TAB),
			new StandardKey('\n', VK_ENTER),
			new StandardKey(' ', VK_SPACE),
			new StandardKey('[', VK_OPEN_BRACKET),
			new StandardKey(']', VK_CLOSE_BRACKET),
			new StandardKey('\\', VK_BACK_SLASH),
			new StandardKey('{', VK_OPEN_BRACKET, true),
			new StandardKey('}', VK_CLOSE_BRACKET, true),
			new StandardKey('|', VK_BACK_SLASH, true),
			new StandardKey(';', VK_SEMICOLON),
			new StandardKey('\'', VK_QUOTE),
			new StandardKey(':', VK_COLON),
			new StandardKey('"', VK_QUOTEDBL),
			new StandardKey(',', VK_COMMA),
			new StandardKey('.', VK_PERIOD),
			new StandardKey('/', VK_SLASH),
			new StandardKey('>', VK_GREATER),
			new StandardKey('<', VK_LESS),
			new StandardKey('/', VK_SLASH, true),

			// Polish Diacritic Signs
			new StandardKey('?', VK_A, false, true),
			new StandardKey('?', VK_C, false, true),
			new StandardKey('?', VK_E, false, true),
			new StandardKey('?', VK_L, false, true),
			new StandardKey('?', VK_N, false, true),
			new StandardKey('ó', VK_O, false, true),
			new StandardKey('?', VK_S, false, true),
			new StandardKey('?', VK_X, false, true),
			new StandardKey('?', VK_Z, false, true),
			new StandardKey('?', VK_A, true, true),
			new StandardKey('?', VK_C, true, true),
			new StandardKey('?', VK_E, true, true),
			new StandardKey('?', VK_L, true, true),
			new StandardKey('?', VK_N, true, true),
			new StandardKey('Ó', VK_O, true, true),
			new StandardKey('?', VK_S, true, true),
			new StandardKey('?', VK_X, true, true),
			new StandardKey('?', VK_Z, true, true) };

	static {
		Arrays.sort(KEYS);
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Constructors">
	private StandardKey(Character character, int keyCode, boolean shiftDown,
			boolean altDown) {
		super();

		this.character = character;
		this.keyCode = keyCode;
		this.shiftDown = shiftDown;
		this.altDown = altDown;
	}

	private StandardKey(char character, int keyCode, boolean shiftDown) {
		this(character, keyCode, shiftDown, false);
	}

	private StandardKey(char character, int keyCode) {
		this(character, keyCode, false);
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Getters">
	public Character getCharacter() {
		return character;
	}

	public Integer getKeyCode() {
		return keyCode;
	}

	public boolean isShiftDown() {
		return shiftDown;
	}

	public boolean isAltDown() {
		return altDown;
	}

	// </editor-fold>

	/**
	 * Retrieves the <code>StandardKey</code> object by its character.
	 * 
	 * @param character
	 *            The character of the desirable <code>StandardKey</code>
	 * @return The corresponding <code>StandardKey</code>. If no
	 *         <code>StandardKey</code> exist for this character
	 *         </code>null</code> is returned.
	 */
	public static StandardKey getKeyByCharacter(char character) {
		for (StandardKey sk : KEYS) {
			if (sk.character.equals(character)) {
				return sk;
			}
		}
		return null;
	}

	/**
	 * Retrieves the <code>StandardKey</code> object by its keyCode shiftDown
	 * and altDown combination.
	 * 
	 * @param keyCode
	 *            The key code of the desirable <code>StandardKey</code>
	 * @param shiftDown
	 *            The flag specifying the state of the Shift key.
	 * @param altDown
	 *            The flag specifying the state of the Alt key.
	 * 
	 * @return The corresponding <code>StandardKey</code>. If no
	 *         <code>StandardKey</code> exist for this combination
	 *         </code>null</code> is returned.
	 */
	public static StandardKey getKeyByKeyCode(int keyCode, boolean shiftDown,
			boolean altDown) {
		int index = Arrays.binarySearch(KEYS, new StandardKey(null, keyCode,
				shiftDown, altDown));
		return index >= 0 ? KEYS[index] : null;
	}

	/**
	 * Retrieves the <code>StandardKey</code> object by a <code>KeyEvent</code>.
	 * 
	 * @param evt
	 *            The character of the desirable <code>StandardKey</code>
	 * @return The corresponding <code>StandardKey</code>. If no
	 *         <code>StandardKey</code> exist for this <code>KeyEvent</code>
	 *         </code>null</code> is returned.
	 */
	public static StandardKey getKeyByKeyEvent(KeyEvent evt) {
		return getKeyByKeyCode(evt.getKeyCode(), evt.isShiftDown(),
				evt.isAltDown());
	}

	@Override
	public int compareTo(StandardKey sk) {
		int keyCodeComparison = keyCode.compareTo(sk.keyCode);
		if (keyCodeComparison == 0) {
			int shiftComparison = new Boolean(shiftDown).compareTo(new Boolean(
					sk.shiftDown));
			if (shiftComparison == 0) {
				return new Boolean(altDown).compareTo(new Boolean(sk.altDown));
			} else {
				return shiftComparison;
			}
		} else {
			return keyCodeComparison;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof StandardKey) {
			StandardKey sk = (StandardKey) o;

			return character.equals(sk.character) && keyCode.equals(sk.keyCode)
					&& shiftDown == sk.shiftDown && altDown == sk.altDown;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 7 * keyCode + 91 * character.charValue();
	}

	@Override
	public String toString() {
		return "'" + character + "' -> " + keyCode + ", shift: " + shiftDown
				+ ", alt: " + altDown;
	}
}