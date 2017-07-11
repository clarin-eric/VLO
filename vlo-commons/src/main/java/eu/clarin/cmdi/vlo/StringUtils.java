package eu.clarin.cmdi.vlo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

public final class StringUtils {

    /**
     * Converts a newlines in to html (&lt;br/&gt;), not using &lt;p&gt; because
     * it renders differently on firefox/safari/chrome/ie etc... Heavily
     * inspired on:
     * <ul>
     * <li>org.apache.wicket.markup.html.basic.MultiLineLabel</li>
     * <li>org.apache.wicket.util.string.Strings#toMultilineMarkup(CharSequence)</li>
     * </ul>
     *
     * @param s
     */
    public static CharSequence toMultiLineHtml(CharSequence s) {
        StringBuilder result = new StringBuilder();
        if (s == null) {
            return result;
        }
        int newlineCount = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n':
                    newlineCount++;
                    break;
                case '\r':
                    break;
                default:
                    if (newlineCount == 1) {
                        result.append("<br/>");
                    } else if (newlineCount > 1) {
                        result.append("<br/><br/>");
                    }
                    result.append(c);
                    newlineCount = 0;
                    break;
            }
        }
        if (newlineCount == 1) {
            result.append("<br/>");
        } else if (newlineCount > 1) {
            result.append("<br/><br/>");
        }
        return result;
    }

    private static final Set<Character> RESERVED_CHARACTERS = ImmutableSet.of(
            '!', '*', '\'', '(', ')', ';', ':', '@', '&',
            '=', '+', '$', ',', '/', '?', '#', '[', ']'
    );

    /**
     * Return normalized String where all reserved characters in URL encoding
     * are replaced by their ASCII code (in underscores)
     *
     * @param idString String that will be normalized
     * @return normalized version of value where all reserved characters in URL
     * encoding are replaced by their ASCII code
     */
    public static String normalizeIdString(String idString) {
        StringBuilder normalizedString = new StringBuilder();
        for (int i = 0; i < idString.length(); i++) {
            Character character = idString.charAt(i);
            if (RESERVED_CHARACTERS.contains(character)) {
                normalizedString.append("_").append((int) character).append("_");
            } else {
                normalizedString.append(character);
            }
        }
        return normalizedString.toString();
    }

    public static String uncapitalizeFirstLetter(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    public static String capitalizeFirstLetter(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static String createStringFromArray(String... values) {
        String res = "";

        for (String str : values) {
            res += str;
        }

        return res;

    }

}
