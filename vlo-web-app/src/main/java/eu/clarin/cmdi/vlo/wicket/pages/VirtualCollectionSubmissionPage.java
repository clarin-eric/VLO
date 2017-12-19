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
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
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
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import eu.clarin.cmdi.vlo.FieldKey;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionSubmissionPage extends VloBasePage<QueryFacetsSelection> {

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FieldNameService fieldNameService;
    
    private final IDataProvider<SolrDocument> documentProvider;

    public VirtualCollectionSubmissionPage(IModel<QueryFacetsSelection> model, IDataProvider<SolrDocument> documentProvider) {
        super(model);
        this.documentProvider = documentProvider;

        // add a label with the number of URI's for the description
        add(new Label("itemCount", new PropertyModel<>(documentProvider, "size")));

        // the form is a container, not a wicket Form because it submits to an
        // external service
        final WebMarkupContainer form = new WebMarkupContainer("vcrForm");
        // <form action="...">
        form.add(new AttributeModifier("action", Model.of(vloConfig.getVcrSubmitEndpoint())));
        // collection name input
        form.add(createCollectionNameField("collectionName"));
        // hidden URI fields 
        form.add(createURIs("metadataUris"));
        // keyword list with remove options
        form.add(addKeywords(model, "keywords"));

        add(form);
    }

    private WebMarkupContainer createCollectionNameField(String id) {
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
        final WebMarkupContainer collectionName = new WebMarkupContainer(id);
        collectionName.add(new AttributeModifier("value", nameModel));
        return collectionName;
    }

    private DataView<SolrDocument> createURIs(String id) {
        return new DataView<SolrDocument>(id, documentProvider) {

            @Override
            protected void populateItem(Item<SolrDocument> item) {
                final WebMarkupContainer mdUri = new WebMarkupContainer("metadataUri");
                final IModel<String> linkModel = new SolrFieldStringModel(item.getModel(), fieldNameService.getFieldName(FieldKey.SELF_LINK));
                if (linkModel.getObject() == null) {
                    mdUri.add(new AttributeModifier("value", new SolrFieldStringModel(item.getModel(), fieldNameService.getFieldName(FieldKey.COMPLETE_METADATA))));
                } else {
                    mdUri.add(new AttributeModifier("value", linkModel));
                }
                item.add(mdUri);
            }
        };
    }

    private WebMarkupContainer addKeywords(IModel<QueryFacetsSelection> model, String id) {
        //create initial keywords list
        final ArrayList<String> keywordsList = new ArrayList<>();
        if (model.getObject().getQuery() != null) {
            keywordsList.add(model.getObject().getQuery());
        }
        for (FacetSelection selection : model.getObject().getSelection().values()) {
            for (String value : selection.getValues()) {
                keywordsList.add(value);
            }
        }

        // create updatable keywords container
        final WebMarkupContainer keywords = new WebMarkupContainer(id) {

            @Override
            protected void onConfigure() {
                // hide keywords section if none are set
                setVisible(!keywordsList.isEmpty());
            }

        };
        keywords.setOutputMarkupId(true);

        // create keywords list
        final IModel<List<String>> keywordsModel = new ListModel<>(keywordsList);
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

        return keywords;
    }

}
