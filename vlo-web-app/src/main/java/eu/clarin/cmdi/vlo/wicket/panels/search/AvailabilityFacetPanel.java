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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AvailabilityFacetPanel extends ExpandablePanel<QueryFacetsSelection> {

    public AvailabilityFacetPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, "Availability");
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
