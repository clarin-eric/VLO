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
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RatingPanel extends Panel {

    private enum RatingLevel {
        VERY_DISSATISFIED("0", "sentiment_very_dissatisfied", "Very dissatisfied"),
        DISSATISFIED("1", "sentiment_dissatisfied", "Dissatisfied"),
        NEUTRAL("2", "sentiment_neutral", "Neutral"),
        SATISFIED("3", "sentiment_satisfied", "Satisfied"),
        VERY_SATISFIED("4", "sentiment_very_satisfied", "Very satisfied");

        private final String value;
        private final String icon;
        private final String description;

        private RatingLevel(String value, String icon, String description) {
            this.value = value;
            this.icon = icon;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getIcon() {
            return icon;
        }

        public String getDescription() {
            return description;
        }

    }

    public static final Logger logger = LoggerFactory.getLogger(RatingPanel.class);

    public final static Duration TIME_BEFORE_RATING_ASKED = Duration.seconds(5); //TODO: make configurable via VloConfig

    public final static String PANEL_DISMISSED_ATTRIBUTE = "RATING_PANEL_DISMISSED";

    private IModel<RatingLevel> selectedRatingModel = new Model<>();
    private IModel<String> commentModel = new Model<>();

    public RatingPanel(String id) {
        super(id);

        final WebMarkupContainer ratingPanel = new WebMarkupContainer("user-rating-panel");
        ratingPanel.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                //This panel should only be shown after a certain amount of time has
                //passed in the session and if not dismissed before
                component.setVisible(!isDismissed() && preRatingTimeHasLapsed());
            }

        });
        add(ratingPanel);

        // add links for rating levels
        final RepeatingView ratingLinks = new RepeatingView("user-rating-link");
        for (RatingLevel ratingLevel : RatingLevel.values()) {
            ratingLinks.add(newRatingLink(ratingLinks.newChildId(), ratingLevel));
        }
        ratingPanel.add(ratingLinks);

        // form to submit (shown after rating selected) and optionally add motivation text
        ratingPanel.add(createCommentSubmitForm("user-rating-form"));

        ratingPanel.add(new Label("user-rating-selection", new PropertyModel<String>(selectedRatingModel, "description"))
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(selectedRatingModel.getObject() != null);
                    }

                })
        );

        // link to dismiss entire panel persistently
        ratingPanel.add(new AjaxFallbackLink("dismiss") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dismiss();
                if (target != null) {
                    target.add(RatingPanel.this);
                }
            }
        });

        // we need to be able to refresh this via Ajax
        setOutputMarkupId(true);
    }

    /**
     * Creates the links for selecting a user satisfaction rating
     *
     * @param id link id
     * @param value value to store/submit
     * @param iconName name of material design icon to display
     * @param description textual description used for link title
     * @return
     */
    private Component newRatingLink(String id, RatingLevel level) {
        final Link ratingLink = new AjaxFallbackLink(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                selectedRatingModel.setObject(level);
                if (target != null) {
                    target.add(RatingPanel.this);
                }
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                //before sending, set chosen value as selected
                attributes.getAjaxCallListeners()
                        .add(new AjaxCallListener().onBeforeSend("$('#'+attrs.c).addClass('user-rating-selected');"));
            }

        };
        ratingLink.setOutputMarkupId(true);

        //model for appending class indicating selection IFF equal to selected value
        final IModel<String> selectedClassModel = new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (level.equals((selectedRatingModel.getObject()))) {
                    return "user-rating-selected";
                } else {
                    return null;
                }
            }
        };

        return ratingLink
                //label determines icon (see material design icons)
                .add(new Label("user-rating-link-icon", Model.of(level.getIcon())))
                //title attribute provides tooltip with description
                .add(new AttributeModifier("title", Model.of(level.getDescription())))
                //apply selected rating class
                .add(new AttributeAppender("class", selectedClassModel, " "));
    }

    private Form createCommentSubmitForm(String id) {
        final Form form = new Form(id);

        // hide form until rating has been selectedÏ
        form.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                component.setVisible(selectedRatingModel.getObject() != null);
            }

        });

        // text area allowing user to input motivation for rating or other comment
        form.add(new TextArea<>("user-rating-comment-input", commentModel));

        // submit button
        form.add(new AjaxFallbackButton("user-rating-form-submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                //submit logic
                submit();

                if (target != null) {
                    // refresh whole panel
                    target.add(RatingPanel.this);
                    // upon submission, show the 'thanks for your feedback' in place of the submit button
                    target.appendJavaScript(
                            "$('.user-rating-thankyou')"
                            + ".removeClass('hidden').hide().fadeIn()"
                            // also add handler for dismiss link
                            + ".on('click', '.close', function() {"
                            + "    $('.user-rating-thankyou').fadeOut();"
                            + "});");
                }
            }

        });

        return form;
    }

    private void submit() {
        if (selectedRatingModel.getObject() == null) {
            logger.warn("Rating form submitted without rating selected!");
        } else {
            logger.info("User rating submitted: {} - '{}'", selectedRatingModel.getObject(), commentModel.getObject());
            //TODO: handler to send rating to back end
            dismiss();
        }
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
