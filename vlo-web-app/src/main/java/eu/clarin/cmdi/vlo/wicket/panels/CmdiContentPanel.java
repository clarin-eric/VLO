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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.UrlFromStringModel;
import eu.clarin.cmdi.vlo.wicket.model.XsltModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiContentPanel extends GenericPanel<SolrDocument> {

    public CmdiContentPanel(String id, IModel<SolrDocument> model) {
        super(id, model);

        final IModel<String> locationModel = new SolrFieldStringModel(model, FacetConstants.FIELD_FILENAME);
        final UrlFromStringModel locationUrlModel = new UrlFromStringModel(locationModel);

        add(new Label("content", new XsltModel(locationUrlModel))
                .setEscapeModelStrings(false));

    }

}
