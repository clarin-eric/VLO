package eu.clarin.cmdi.vlo.importer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IdPostProcessor implements PostProcessor {

    private static final Set<Character> reservedCharacters = new HashSet<Character>(Arrays.asList('!', '*', '\'', '(',
            ')', ';', ':', '@', '&', '=', '+', '$', ',', '/', '?', '#', '[', ']'));

    /**
     * Return normalized String where all reserved characters in URL encoding
     * are replaced by their ASCII code (in underscores)
     *
     * @param value String that will be normalized
     * @return normalized version of value where all reserved
     * characters in URL encoding are replaced by their ASCII code
     */
    @Override
    public String process(String value) {
        StringBuilder normalizedString = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            Character character = value.charAt(i);
            if (reservedCharacters.contains(character)) {
                normalizedString.append("_").append((int) character).append("_");
            } else {
                normalizedString.append(character);
            }
        }

        return normalizedString.toString();
    }
}
