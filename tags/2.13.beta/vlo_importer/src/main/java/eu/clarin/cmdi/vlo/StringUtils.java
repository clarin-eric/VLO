package eu.clarin.cmdi.vlo;

public final class StringUtils {

    /**
     * Converts a newlines in to html (&lt;br/&gt;), not using &lt;p&gt; because it renders differently on firefox/safari/chrome/ie etc...
     * Heavily inspired on: 
     * {@link org.apache.wicket.markup.html.basic.MultiLineLabel} 
     * {@link org.apache.wicket.util.string.Strings#toMultilineMarkup(CharSequence)} 
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

}
