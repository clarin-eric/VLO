/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionSubmissionPage extends VloBasePage<QueryFacetsSelection> {

    private final String vcrSubmitEndpoint = "http://catalog-clarin.esc.rzg.mpg.de/vcr/service/submit";

    public VirtualCollectionSubmissionPage(IModel<QueryFacetsSelection> model) {
        super(model);
        final WebMarkupContainer form = new WebMarkupContainer("vcrForm");
        form.add(new AttributeModifier("action", Model.of(vcrSubmitEndpoint)));
        add(form);

        final IModel<String> nameModel = new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                final String query = getModelObject().getQuery();
                if (query != null) {
                    return "VLO search results: " + query;
                } else {
                    return "VLO search results";
                }
            }
        };

        final WebMarkupContainer collectionName = new WebMarkupContainer("collectionName");
        collectionName.add(new AttributeModifier("value", nameModel));
        form.add(collectionName);
        
        final SolrDocumentProvider provider = new SolrDocumentProvider(getModel());
        form.add(new DataView<SolrDocument>("metadataUris", provider) {

            @Override
            protected void populateItem(Item<SolrDocument> item) {
                final WebMarkupContainer mdUri = new WebMarkupContainer("metadataUri");
                final IModel<String> linkModel = new SolrFieldStringModel(item.getModel(), FacetConstants.FIELD_SELF_LINK);
                if (linkModel.getObject() == null) {
                    mdUri.add(new AttributeModifier("value", new SolrFieldStringModel(item.getModel(), FacetConstants.FIELD_COMPLETE_METADATA)));
                } else {
                    mdUri.add(new AttributeModifier("value", linkModel));
                }
                item.add(mdUri);
            }
        });

        add(new Label("itemCount", new PropertyModel<Long>(provider, "size")));
    }

}
