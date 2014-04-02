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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.ResourceTypeCssBehaviour;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SearchResultItemExpandedPanel extends GenericPanel<SolrDocument> {

    @SpringBean(name = "searchResultPropertiesFilter") 
    private FieldFilter propertiesFilter;
    @SpringBean
    ResourceStringConverter resourceStringConverter;

    public SearchResultItemExpandedPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        super(id, documentModel);

        // add untruncated description
        add(new SolrFieldLabel("description", documentModel, FacetConstants.FIELD_DESCRIPTION, "<no description>"));
        add(new RecordPageLink("recordLink", documentModel, selectionModel));

        // table with some basic properties
        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(documentModel, propertiesFilter)));

        final SolrFieldModel<String> resourceModel = new SolrFieldModel<String>(getModel(), FacetConstants.FIELD_RESOURCE);

        // add a container for the resources (only visible if there are actual resources)
        add(new WebMarkupContainer("resources") {
            {
                add(createResourcesList("resource", resourceModel));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(resourceModel.getObject() != null);
            }

        });
    }

    private Component createResourcesList(String id, SolrFieldModel<String> resourceModel) {
        // list of resources in this record
        // TODO: limit number of resources shown here?
        final IModel<List<String>> resourceListModel = new CollectionListModel<String>(resourceModel);
        return new ListView<String>(id, resourceListModel) {

            @Override
            protected void populateItem(ListItem<String> item) {
                // get resource string converted into a ResourceInfo model
                final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());

                // add a link to the resource with the file name as its label
                final ExternalLink resourceLink = new ExternalLink("resourceLink", new PropertyModel(resourceInfoModel, "href"));
                resourceLink.add(new Label("resourceName", new PropertyModel(resourceInfoModel, "fileName")));

                // add a tooltip showing resource type and mime type
                final StringResourceModel tooltipModel
                        = new StringResourceModel("resource.tooltip", SearchResultItemExpandedPanel.this, null,
                                new Object[]{
                                    new StringResourceModel("resourcetype.${resourceType}.singular", resourceInfoModel, "?"),
                                    new PropertyModel(resourceInfoModel, "mimeType")});
                resourceLink.add(new AttributeAppender("title", tooltipModel));

                // sets the css class depending on the resource type
                item.add(new ResourceTypeCssBehaviour(resourceInfoModel));

                // add to list
                item.add(resourceLink);
            }
        };
    }

    @Override
    public void detachModels() {
        super.detachModels();
    }

}
