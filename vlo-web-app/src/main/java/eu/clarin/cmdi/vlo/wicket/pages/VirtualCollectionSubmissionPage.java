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
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.SolrDocumentProvider;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionSubmissionPage extends VloBasePage<QueryFacetsSelection> {

    @SpringBean
    private VloConfig vloConfig;

    public VirtualCollectionSubmissionPage(IModel<QueryFacetsSelection> model) {
        super(model);

        final WebMarkupContainer form = new WebMarkupContainer("vcrForm");
        form.add(new AttributeModifier("action", Model.of(vloConfig.getVcrSubmitEndpoint())));
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

        final WebMarkupContainer keywords = new WebMarkupContainer("keywords");
        keywords.setOutputMarkupId(true);
        form.add(keywords);

        final ArrayList<String> keywordsList = new ArrayList<String>();
        if (model.getObject().getQuery() != null) {
            keywordsList.add(model.getObject().getQuery());
        }
        for (FacetSelection selection : model.getObject().getSelection().values()) {
            for (String value : selection.getValues()) {
                keywordsList.add(value);
            }
        }

        final IModel<List<String>> keywordsModel = new ListModel<String>(keywordsList);
        keywords.add(new ListView<String>("keyword", keywordsModel) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                // add hidden field for keyword
                final WebMarkupContainer keywordField = new WebMarkupContainer("keywordField");
                keywordField.add(new AttributeModifier("value", item.getModel()));
                item.add(keywordField);

                // add label for keyword
                item.add(new Label("keywordValue", item.getModel()));

                // add remove link for keyword
                item.add(new AjaxLink("remove") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        keywordsModel.getObject().remove(item.getModelObject());
                        target.add(keywords);
                    }
                });
            }
        });

        add(new Label("itemCount", new PropertyModel<Long>(provider, "size")));
    }

}
