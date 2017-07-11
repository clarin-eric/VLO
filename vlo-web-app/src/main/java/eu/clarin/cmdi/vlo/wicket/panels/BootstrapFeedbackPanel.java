/*
 * Copyright (C) 2016 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.panels;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class BootstrapFeedbackPanel extends FeedbackPanel {

    private final String DEBUG_CSS_QUALIFIER = "alert-info";
    private final String INFO_CSS_QUALIFIER = "alert-info";
    private final String SUCCESS_CSS_QUALIFIER = "alert-info";
    private final String WARNING_CSS_QUALIFIER = "alert-warning";
    private final String ERROR_CSS_QUALIFIER = "alert-danger";
    private final String FATAL_CSS_QUALIFIER = "alert-danger";

    public BootstrapFeedbackPanel(String id) {
        super(id);
    }

    @Override
    protected String getCSSClass(FeedbackMessage message) {
        return "alert " + getQualifier(message.getLevel());
    }

    private String getQualifier(int level) {
        switch (level) {
            case FeedbackMessage.DEBUG:
                return DEBUG_CSS_QUALIFIER;
            case FeedbackMessage.INFO:
                return INFO_CSS_QUALIFIER;
            case FeedbackMessage.SUCCESS:
                return SUCCESS_CSS_QUALIFIER;
            case FeedbackMessage.WARNING:
                return WARNING_CSS_QUALIFIER;
            case FeedbackMessage.ERROR:
                return ERROR_CSS_QUALIFIER;
            case FeedbackMessage.FATAL:
                return FATAL_CSS_QUALIFIER;
            default:
                return "";
        }
    }

}
