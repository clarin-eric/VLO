package eu.clarin.cmdi.vlo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class StringUtils {

    private static final Set<Integer> RESERVED_CHARACTERS = ImmutableSet.of(
            '!', '*', '\'', '(', ')', ';', ':', '@', '&',
            '=', '+', '$', ',', '/', '?', '#', '[', ']', ' '
    ).stream().map(c -> (int) c).collect(Collectors.toUnmodifiableSet());

    /**
     * Return normalized String where all reserved characters in URL encoding
     * are replaced by their ASCII code (in underscores)
     *
     * @param idString String that will be normalized
     * @return normalized version of value where all reserved characters in URL
     * encoding are replaced by their ASCII code
     */
    public static String normalizeIdString(String idString) {
        final StringBuilder normalizedString = new StringBuilder();
        idString.trim()
                .chars()
                .forEach(charInt -> {
                    if (RESERVED_CHARACTERS.contains(charInt)) {
                        normalizedString.append("_").append(charInt).append("_");
                    } else {
                        normalizedString.append((char) charInt);
                    }
                });
        return normalizedString.toString();
    }

}
