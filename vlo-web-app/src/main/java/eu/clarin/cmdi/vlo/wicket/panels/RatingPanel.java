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
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RatingPanel extends Panel {

    public final static Duration TIME_BEFORE_RATING_ASKED = Duration.seconds(5); //TODO: make configurable via VloConfig

    public final static String PANEL_DISMISSED_ATTRIBUTE = "RATING_PANEL_DISMISSED";

    public RatingPanel(String id) {
        super(id);

        add(new AjaxFallbackLink("dismiss") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dismiss();
                if (target != null) {
                    target.add(RatingPanel.this);
                }
            }
        });

        setOutputMarkupId(true);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        //This panel should only be shown after a certain amount of time has
        //passed in the session and if not dismissed before
        setVisible(!isDismissed() && preRatingTimeHasLapsed());
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
