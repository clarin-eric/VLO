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

import eu.clarin.cmdi.vlo.wicket.model.RatingLevel;
import eu.clarin.cmdi.vlo.VloWebSession;
import java.io.Serializable;
import javax.servlet.http.Cookie;
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
import org.apache.wicket.util.cookies.CookieDefaults;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel for eliciting user satisfaction ratings. Has logic for delayed display
 * (relative to session initiation) and persisted dismissal (within and across
 * sessions). Designed to work with and without javascript.
 *
 * @see RatingLevel
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RatingPanel extends Panel {

    public static final Logger logger = LoggerFactory.getLogger(RatingPanel.class);

    public final static Duration TIME_BEFORE_RATING_ASKED = Duration.seconds(5); //TODO: make configurable via VloConfig

    public final static String PANEL_DISMISSED_ATTRIBUTE = "VLO_RATING_PANEL_DISMISSED";
    public final static String PANEL_DISMISSED_COOKIE = "VLO_RATING_PANEL_DISMISSED";
    /**
     * Maximum of age of cookie that keeps panel from appearing on dismissal
     * without submitting a rating
     */
    public final static Duration COOKIE_MAX_AGE_DISMISS = Duration.days(7);
    /**
     * Maximum of age of cookie that keeps panel from appearing after submitting
     * a rating
     */
    public final static Duration COOKIE_MAX_AGE_SUBMIT = Duration.days(30);

    private final IModel<RatingLevel> selectedRatingModel = new Model<>();
    private final IModel<String> commentModel = new Model<>();

    public RatingPanel(String id) {
        super(id);

        final WebMarkupContainer ratingPanel = new WebMarkupContainer("user-rating-panel");
        //The panel should only be shown after a certain amount of time has 
        //passed in the session and if not dismissed before
        ratingPanel.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                component.setVisible(!isDismissed() && preRatingTimeHasLapsed());
            }

        });

        add(ratingPanel
                // link to dismiss entire panel persistently
                .add(createDismissButton("dismiss"))
                // add links for rating levels
                .add(createRatingLinks("user-rating-link"))
                // form to submit (shown after rating selected) and optionally add motivation text
                .add(createCommentSubmitForm("user-rating-form"))
                .add(createCurrentSelectionLabel("user-rating-selection")));

        // we need to be able to refresh this via Ajax
        setOutputMarkupId(true);
    }

    private Component createDismissButton(String id) {
        return (new AjaxFallbackLink(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dismiss(COOKIE_MAX_AGE_DISMISS);
                if (target != null) {
                    target.add(RatingPanel.this);
                }
            }
        }).add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                // hide once a selection has been made
                component.setVisible(selectedRatingModel.getObject() == null);
            }

        });
    }

    private RepeatingView createRatingLinks(String id) {
        final RepeatingView ratingLinks = new RepeatingView(id);
        for (RatingLevel ratingLevel : RatingLevel.values()) {
            ratingLinks.add(newRatingLink(ratingLinks.newChildId(), ratingLevel));
        }
        return ratingLinks;
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

    private Component createCurrentSelectionLabel(String id) {
        return new Label(id, new PropertyModel<String>(selectedRatingModel, "description"))
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        //hide until a selection has been made
                        component.setVisible(selectedRatingModel.getObject() != null);
                    }

                });
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

        form.add(new AjaxFallbackLink("user-rating-form-cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                selectedRatingModel.setObject(null);
                commentModel.setObject(null);
                if (target != null) {
                    target.add(RatingPanel.this);
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
            dismiss(COOKIE_MAX_AGE_SUBMIT);
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
        final Serializable dismissedSessionAttribute = (session == null) ? null : session.getAttribute(PANEL_DISMISSED_ATTRIBUTE);

        if (dismissedSessionAttribute != null) {
            //dismissed state was stored in session
            return Boolean.TRUE.equals(dismissedSessionAttribute);
        } else {
            //has a cookie been set?
            return isDismissedCookie();
        }
    }

    /**
     * dismisses the panel for the time being
     */
    private void dismiss(Duration maxCookieAge) {
        //set session param
        Session.get().setAttribute(PANEL_DISMISSED_ATTRIBUTE, Boolean.TRUE);
        //set cookie
        setDismissedCookie(true, maxCookieAge);
    }

    /**
     *
     * @return whether enough time has passed in the session to show this panel
     */
    private boolean preRatingTimeHasLapsed() {
        final VloWebSession session = VloWebSession.get();
        return (session != null && Time.now().after(session.getInitTime().add(TIME_BEFORE_RATING_ASKED)));
    }

    private boolean isDismissedCookie() {
        return Boolean.valueOf(getCookie(PANEL_DISMISSED_COOKIE));
    }

    private void setDismissedCookie(boolean value, Duration maxAge) {
        setCookie(PANEL_DISMISSED_COOKIE, Boolean.toString(value), maxAge);
    }

    private String getCookie(String key) {
        final Cookie cookie = new CookieUtils().getCookie(key);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    private void setCookie(String key, String value, Duration maxAge) {
        final CookieDefaults cookieDefaults = new CookieDefaults();
        //maximum age of the cookie to be set in seconds
        cookieDefaults.setMaxAge((int) (maxAge.getMilliseconds() / 1000));
        //save cookie
        new CookieUtils(cookieDefaults).save(key, value);
    }

}
