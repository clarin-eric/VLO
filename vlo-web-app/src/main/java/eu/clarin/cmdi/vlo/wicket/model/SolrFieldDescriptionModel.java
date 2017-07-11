/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.VloWicketApplication;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class SolrFieldDescriptionModel extends AbstractReadOnlyModel<String> {

    private final static Logger logger = LoggerFactory.getLogger(SolrFieldDescriptionModel.class);
    private final IModel<String> facetNameModel;

    public SolrFieldDescriptionModel(IModel<String> facetNameModel) {
        this.facetNameModel = facetNameModel;
    }

    @Override
    public String getObject() {
        return VloWicketApplication.get().getFacetDescriptionService().getDescription(facetNameModel.getObject());
    }

    @Override
    public void detach() {
        facetNameModel.detach();
    }

    @Override
    public String toString() {
        return String.format("[%s: '%s']", facetNameModel.getObject(), getObject());
    }

}
