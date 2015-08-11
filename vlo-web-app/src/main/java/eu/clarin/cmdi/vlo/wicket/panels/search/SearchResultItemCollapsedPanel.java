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
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.provider.ResouceTypeCountDataProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SearchResultItemCollapsedPanel extends Panel {

    private static final int MAX_DESCRIPTION_LENGTH = 350;
    private static final int LONG_DESCRIPTION_TRUNCATE_POINT = 320;

    @SpringBean
    private ResourceTypeCountingService countingService;
    private final IModel<SearchContext> selectionModel;
    private final IModel<SolrDocument> documentModel;

    public SearchResultItemCollapsedPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        super(id, documentModel);
        this.documentModel = documentModel;
        this.selectionModel = selectionModel;

        add(new SolrFieldLabel("description", documentModel, FacetConstants.FIELD_DESCRIPTION, "", MAX_DESCRIPTION_LENGTH, LONG_DESCRIPTION_TRUNCATE_POINT));

        // get model for resources
        final SolrFieldModel<String> resourcesModel = new SolrFieldModel<>(documentModel, FacetConstants.FIELD_RESOURCE);
        // wrap with a count provider
        final ResouceTypeCountDataProvider countProvider = new ResouceTypeCountDataProvider(resourcesModel, countingService);

        // add a container for the resource type counts (only visible if there are actual resources)
        add(new WebMarkupContainer("resources") {
            {
                // view that shows provided counts
                add(new ResourceCountDataView("resourceCount", countProvider));
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(countProvider.size() > 0);
            }
        });
    }

    @Override
    public void detachModels() {
        super.detachModels();
        // not passed to super
        selectionModel.detach();
    }

    /**
     * Data view for resource type counts coming from a data provider for
     * {@link ResourceTypeCount}
     */
    private class ResourceCountDataView extends DataView<ResourceTypeCount> {

        public ResourceCountDataView(String id, IDataProvider<ResourceTypeCount> dataProvider) {
            super(id, dataProvider);
        }

        @Override
        protected void populateItem(Item<ResourceTypeCount> item) {
            final Link resourceLink = new RecordPageLink("recordLink", documentModel, selectionModel);
            final Label label = new Label("resourceCountLabel", getResourceCountModel(item.getModel()));
            resourceLink.add(label);
            item.add(resourceLink);
        }

        /**
         *
         * @param resourceTypeCountModel
         * @return a string model that conveys the type of resource and number
         * of instances
         */
        private IModel<String> getResourceCountModel(final IModel<ResourceTypeCount> resourceTypeCountModel) {
            // first create a string model that provides the type of resources 
            // in the right number (plural or singular, conveniently supplied by ResourceTypeCount)
            final StringResourceModel resourceTypeModel = StringResourceModelMigration.of("resourcetype.${resourceType}.${number}", resourceTypeCountModel, "?");
            // inject this into the resource string that combines it with count
            return StringResourceModelMigration.of("resources.typecount", this, resourceTypeCountModel, resourceTypeModel);
        }
    }

}
