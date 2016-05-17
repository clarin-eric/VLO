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
package eu.clarin.cmdi.vlo.wicket;

import com.google.common.base.Function;
import java.io.Serializable;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Behaviour that adds the "disabled" class to a markup element if a (provided)
 * check indicates that the component is not enabled
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class LinkDisabledClassBehaviour extends AttributeAppender implements Serializable {

    public LinkDisabledClassBehaviour(final Link link) {
        this(new LinkEnabledFunction(link));
    }

    public LinkDisabledClassBehaviour(final Function<Void, Boolean> isEnabled) {
        super("class", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return isEnabled.apply(null) ? null : "disabled";
            }
        }, " ");
    }

    private static class LinkEnabledFunction implements Function<Void, Boolean>, Serializable {

        private final Link link;

        public LinkEnabledFunction(Link link) {
            this.link = link;
        }

        @Override
        public Boolean apply(Void input) {
            return link.isEnabled();
        }
    }
}
