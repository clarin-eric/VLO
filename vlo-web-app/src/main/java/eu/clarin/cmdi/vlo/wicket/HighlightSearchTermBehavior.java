/*
 * Copyright (C) 2014 CLARIN
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
import eu.clarin.cmdi.vlo.JavaScriptResources;
import java.util.Collection;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.string.StringValue;

/**
 * A behavior that adds JavaScript, executed on DOM ready, to highlight
 * occurrences of the current searchword (retrieved from the 'q' query parameter
 * by the client) in the page
 *
 * @author twagoo
 */
public class HighlightSearchTermBehavior extends Behavior {

    private static final String HIGHLIGHT_FUNCTION = "$('#%s').highlight(%s, {className:'%s'})";
    private static final Collection<String> DEFAULT_EXCLUDE_WORDS = ImmutableSet.of("and", "or", "not", "to");

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        // include highlight script
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getHighlightJS()));

        final StringValue words = getWordList(component);
        if (!words.isEmpty()) {
            // after load, highlight 
            response.render(OnDomReadyHeaderItem.forScript(String.format(HIGHLIGHT_FUNCTION,
                    component.getMarkupId(), makeWordListArray(words.toString()),
                    getSearchWordClass()
            )));
        }
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
        return word.replaceAll("^[\\s'\"]+|[\\s'\"]+$", "");
    }

    /**
     * 
     * @return CSS class to mark matches with
     */
    protected String getSearchWordClass() {
        return "searchword";
    }

    protected StringValue getWordList(Component component) {
        Request request = component.getPage().getRequestCycle().getRequest();
        return request.getQueryParameters().getParameterValue(getQueryParam());
    }

    protected String getQueryParam() {
        return "q";
    }

    /**
     *
     * @return Words not to highlight
     */
    protected Collection<String> getExcludeWords() {
        return DEFAULT_EXCLUDE_WORDS;
    }

}
