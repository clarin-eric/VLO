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

import eu.clarin.cmdi.vlo.service.UriResolver;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows an actionable link, which is the resolved PID in case of a
 * resolvable PID. Note that this might take a while to be ready, so you might
 * want to wrap this in a lazy AJAX context
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResolvedPidLink extends GenericPanel<String> {

    @SpringBean
    private UriResolver uriResolver;

    /**
     * Resolved link will be used as label
     *
     * @param id component id
     * @param model unresolved link model
     */
    public ResolvedPidLink(String id, final IModel<String> model) {
        this(id, model, null);
    }

    /**
     * Resolved link will be the link, but a custom label will be applied
     *
     * @param id component id
     * @param model unresolved link model
     * @param label model for custom label to use
     */
    public ResolvedPidLink(String id, final IModel<String> model, IModel<String> label) {
        super(id, model);

        //resolved link model
        final LoadableDetachableModel<String> resolvedLinkModel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return uriResolver.resolve(model.getObject());
            }
        };

        if (label == null) {
            label = resolvedLinkModel;
        }

        add(new ExternalLink("link", resolvedLinkModel, resolvedLinkModel));
    }

}
