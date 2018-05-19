/*
 * Copyright (C) 2018 CLARIN
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

import eu.clarin.cmdi.vlo.VloWebSession;
import java.io.Serializable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RatingPanel extends Panel {

    public final static Duration TIME_BEFORE_RATING_ASKED = Duration.seconds(5); //TODO: make configurable via VloConfig

    public final static String PANEL_DISMISSED_ATTRIBUTE = "RATING_PANEL_DISMISSED";

    private IModel<String> selectedRatingModel = new Model<String>();

    public RatingPanel(String id) {
        super(id);

        final RepeatingView ratingLinks = new RepeatingView("user-rating-link");
        ratingLinks
                .add(newRatingLink(ratingLinks.newChildId(), "0", "sentiment_very_dissatisfied", "Very dissatisfied"))
                .add(newRatingLink(ratingLinks.newChildId(), "1", "sentiment_dissatisfied", "Dissatisfied"))
                .add(newRatingLink(ratingLinks.newChildId(), "2", "sentiment_neutral", "Neutral"))
                .add(newRatingLink(ratingLinks.newChildId(), "3", "sentiment_satisfied", "Satisfied"))
                .add(newRatingLink(ratingLinks.newChildId(), "4", "sentiment_very_satisfied", "Very satisfied"));

        add(ratingLinks);

        add(new AjaxFallbackLink("dismiss") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dismiss();
                if (target != null) {
                    target.add(RatingPanel.this);
                }
            }
        });

        //TODO: add buttons for submitting a rating
        //TODO: handler to send rating to back end
        //TODO: feedback form
        setOutputMarkupId(true);
    }

    public Component newRatingLink(String id, String value, String iconName, String description) {
        final Link ratingLink = new AjaxFallbackLink(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                submitRating(value);
                if (target != null) {
                    target.add(RatingPanel.this);
                }
            }
        };

        //model for appending class indicating selection IFF equal to selected value
        final IModel<String> selectedClassModel = new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (value.equals((selectedRatingModel.getObject())))  {
                    return "user-rating-selected";
                } else {
                    return null;
                }
            }
        };

        return ratingLink
                .add(new Label("user-rating-link-icon", Model.of(iconName)))
                .add(new AttributeModifier("title", Model.of(description)))
                .add(new AttributeAppender("class", selectedClassModel, " "));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        //This panel should only be shown after a certain amount of time has
        //passed in the session and if not dismissed before
        setVisible(!isDismissed() && preRatingTimeHasLapsed());
    }

    private void submitRating(String value) {
        selectedRatingModel.setObject(value);
    }

    /**
     *
     * @return whether the rating panel has been dismissed in this session or
     * according to a cookie set earlier
     */
    private boolean isDismissed() {
        //Check if dismissed in session
        final Session session = Session.get();
        final Serializable dismissedAttribute = (session == null) ? null : session.getAttribute(PANEL_DISMISSED_ATTRIBUTE);

        if (dismissedAttribute != null) {
            return Boolean.TRUE.equals(dismissedAttribute);
        } else {//TODO: else check cookie
            return false;
        }
    }

    /**
     * dismisses the panel for the time being
     */
    private void dismiss() {
        Session.get().setAttribute(PANEL_DISMISSED_ATTRIBUTE, Boolean.TRUE);
        //TODO: also set cookie (for a month or so)
    }

    /**
     *
     * @return whether enough time has passed in the session to show this panel
     */
    private boolean preRatingTimeHasLapsed() {
        final VloWebSession session = VloWebSession.get();
        return (session != null && Time.now().after(session.getInitTime().add(TIME_BEFORE_RATING_ASKED)));
    }

}
