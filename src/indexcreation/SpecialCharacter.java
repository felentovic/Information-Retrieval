package indexcreation;

import java.util.Arrays;
import java.util.HashSet;

public class SpecialCharacter {

    public static final String[] SET_VALUES = new String[]{"{", "}", "|", "~", "'",
            "_", "-", "^", "]", "\\", "[", "?", "!", ".", "@", "<", ">", "=",
            ";", ":", ",", "+", "-", "*", "(", ")", "&", "%", "$", "#", "\"", " ", "/"};
    private static HashSet<String> specialCharacters = new HashSet<>(Arrays.asList(SET_VALUES));

    public static boolean isSpecialChar(String character) {
        return specialCharacters.contains(character);
    }
}
