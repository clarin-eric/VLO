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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RatingPanel extends Panel {

    public final static Duration TIME_BEFORE_RATING_ASKED = Duration.seconds(30); //TODO: make configurable via VloConfig

    public RatingPanel(String id) {
        super(id);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        final VloWebSession session = VloWebSession.get();
        if (session != null) {
            setVisible(Time.now().after(session.getInitTime().add(TIME_BEFORE_RATING_ASKED)));
        } else {
            setVisible(false);
        }
    }

}
