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

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.LazyResourceInfoUpdateBehavior;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.components.SmartLinkFieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SearchResultItemExpandedPanel extends GenericPanel<SolrDocument> {

    private static final int MAX_RESOURCES_TO_SHOW = 10;

    @SpringBean(name = "searchResultPropertiesFilter")
    private FieldFilter propertiesFilter;
    @SpringBean(name = "resourceStringConverter")
    ResourceStringConverter resourceStringConverter;
    @SpringBean(name = "resolvingResourceStringConverter")
    ResourceStringConverter resolvingResourceStringConverter;
    @SpringBean(name = "documentFieldOrder")
    private List<String> fieldOrder;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SearchContext> searchContextModel;

    public SearchResultItemExpandedPanel(String id, final IModel<SolrDocument> documentModel, final IModel<SearchContext> searchContextModel, Ordering<String> availabilityOrdering) {
        super(id, documentModel);
        this.searchContextModel = searchContextModel;

        // add untruncated description
        final NullFallbackModel descriptionModel = new NullFallbackModel(new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.DESCRIPTION)), "");
        add(new SmartLinkFieldValueLabel("description", descriptionModel, Model.of(fieldNameService.getFieldName(FieldKey.DESCRIPTION))));

        // add link to record
        add(new RecordPageLink("recordLink", documentModel, searchContextModel));

        // table with some basic properties
        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(documentModel, propertiesFilter, fieldOrder)) {

            @Override
            protected boolean isShowFacetSelectLinks() {
                // do not show the value selection links
                return false;
            }

        });

        // add a container for the resources (only visible if there are actual resources)
        add(createResourcesView("resources", searchContextModel));
    }

    private WebMarkupContainer createResourcesView(String id, final IModel<SearchContext> selectionModel) {
        final SolrFieldModel<String> resourceModel = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.RESOURCE));
        // create a container for the list view that is only visible if there actually are resources
        final WebMarkupContainer container = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(resourceModel.getObject() != null);
            }

        };

        final PageableListView resourcesView = createResourcesList("resource", resourceModel);
        container.add(resourcesView);

        // create a link to the record page that is only visible when there are more resources than shown
        final RecordPageLink moreLink = new RecordPageLink("more", getModel(), selectionModel, RecordPage.RESOURCES_SECTION) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(resourcesView.getPageCount() > 1);
            }

        };
        // add a record page link that shows the number of resources not shown
        moreLink.add(new Label("moreLabel", StringResourceModelMigration.of("resources.more", new AbstractReadOnlyModel<Integer>() {

            @Override
            public Integer getObject() {
                return resourceModel.getObject().size() - MAX_RESOURCES_TO_SHOW;
            }

        }, "more...")));
        container.add(moreLink);

        return container;
    }

    private PageableListView createResourcesList(String id, SolrFieldModel<String> resourceModel) {
        // list of resources in this record
        final IModel<List<String>> resourceListModel = new CollectionListModel<>(resourceModel);
        // use a a pageable view so that the number of resources actually shown is limited
        return new PageableListView<String>(id, resourceListModel, MAX_RESOURCES_TO_SHOW) {

            @Override
            protected void populateItem(final ListItem<String> item) {
                // get resource string converted into a ResourceInfo model
                final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());

                final Label resourceName = new Label("resourceName", new PropertyModel(resourceInfoModel, "fileName"));
                // once loaded, make Ajax request to resolve handles and update resource link
                resourceName.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        // update resource link
                        target.add(resourceName);
                    }
                });
                resourceName.setOutputMarkupId(true);
                item.add(new RecordPageLink("resourceLink", SearchResultItemExpandedPanel.this.getModel(), searchContextModel, RecordPage.RESOURCES_SECTION)
                        .add(resourceName));

                item.add(new ExternalLink("downloadLink", new HandleLinkModel(new PropertyModel(resourceInfoModel, "href"))));
                item.add(new ResourceTypeIcon("resourceTypeIcon", new PropertyModel<String>(resourceInfoModel, "resourceType")));
            }
        };
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (searchContextModel != null) {
            searchContextModel.detach();
        }
    }

}
