/*
 * Copyright (C) 2015 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class HighlightSearchTermScriptFactory implements Serializable {

    public static final String HIGHLIGHT_FUNCTION = "$('%s').highlight(%s, {className:'%s', wordsOnly: %s})";

    public static final Collection<String> DEFAULT_EXCLUDE_WORDS = ImmutableSet.of("and", "or", "not", "to");

    public static final String DEFAULT_CSS_CLASS = "searchword";

    public String createScript(String componentSelector, final String words) {
        return createScript(componentSelector, words, DEFAULT_CSS_CLASS);
    }

    public String createScript(String componentSelector, final String words, String cssClass) {
        return String.format(HIGHLIGHT_FUNCTION,
                componentSelector,
                makeWordListArray(words),
                cssClass,
                matchWordsOnly(words)
        );
    }

    protected boolean matchWordsOnly(String query) {
        // string with asterixes or question marks should match by character
        return !query.matches(".*[\\*\\?].*");
    }

    /**
     *
     * @param wordList string of whitespace separated words
     * @return a string representing a sanitised javascript array of words
     */
    private CharSequence makeWordListArray(String wordList) {
        final StringBuilder sb = new StringBuilder("[");
        final String[] words = wordList.split("\\s");
        for (int i = 0; i < words.length; i++) {
            final String word = sanitise(words[i]); //remove white space and quotes at beginning or end
            // is on exclude list?
            if (!getExcludeWords().contains(word.toLowerCase())) {
                // wrap in quotes
                sb.append("'").append(word).append("'");
                if (i + 1 < words.length) {
                    // prepare to append next
                    sb.append(",");
                }
            }
        }
        return sb.append("]");
    }

    private String sanitise(String word) {
        //remove everything up to first colon and strip off quotation marks and white space
        return word.replaceAll(
                //match beginning 
                "^("
                //case with colon (also strip quotes + optional whitespace after quotes)
                + "[^:\"']+:(['\"])?"
                //or case without colon (strip quotes and white space)
                + "|['\"]+"
                + ")"
                //match end
                + "|("
                //quotes
                + "['\"]+"
                //or boosting values
                + "|['\"]?\\^.*"
                + ")$"
                //also remove wildcard characters
                + "|[\\?\\*]",
                //replace with empty string
                "");
    }

    /**
     *
     * @return Words not to highlight
     */
    protected Collection<String> getExcludeWords() {
        return DEFAULT_EXCLUDE_WORDS;
    }
}
