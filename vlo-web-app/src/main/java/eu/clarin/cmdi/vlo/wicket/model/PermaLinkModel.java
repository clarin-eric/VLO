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
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Page;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Model that calls the {@link PermaLinkModel} registered in the active
 * {@link VloWicketApplication} on the specified page combined with the facet
 * selection and document available through the wrapped models (both optional)
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class PermaLinkModel extends AbstractReadOnlyModel<String> {

    private final IModel<QueryFacetsSelection> selectionmodel;
    private final IModel<SolrDocument> documentModel;
    private final Class<? extends Page> pageClass;

    public PermaLinkModel(Class<? extends Page> pageClass, IModel<QueryFacetsSelection> selectionmodel) {
        this(pageClass, selectionmodel, null);
    }

    public PermaLinkModel(Class<? extends Page> pageClass, IModel<QueryFacetsSelection> selectionmodel, IModel<SolrDocument> documentModel) {
        this.selectionmodel = selectionmodel;
        this.documentModel = documentModel;
        this.pageClass = pageClass;
    }

    @Override
    public String getObject() {
        final QueryFacetsSelection selection = (selectionmodel == null) ? null : selectionmodel.getObject();
        final SolrDocument document = (documentModel == null) ? null : documentModel.getObject();
        return VloWicketApplication.get().getPermalinkService()
                .getUrlString(pageClass, selection, document);
    }

    @Override
    public void detach() {
        if (selectionmodel != null) {
            selectionmodel.detach();
        }
        if (documentModel != null) {
            documentModel.detach();
        }
    }

}
