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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.wicket.provider.ResouceTypeCountDataProvider;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import java.util.Locale;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author twagoo
 */
public class SearchResultItemPanel extends Panel {

    private final static ResourceTypeCountConverter resourceTypeCountConverter = new ResourceTypeCountConverter();

    @SpringBean
    private ResourceTypeCountingService countingService;
    private final IModel<SearchContext> selectionModel;
    private final IModel<SolrDocument> documentModel;

    public SearchResultItemPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        super(id, documentModel);
        this.documentModel = documentModel;
        this.selectionModel = selectionModel;

        final Link recordLink = new RecordPageLink("recordLink", documentModel, selectionModel);
        recordLink.add(new SolrFieldLabel("title", documentModel, FacetConstants.FIELD_NAME));
        add(recordLink);

        add(new SolrFieldLabel("description", documentModel, FacetConstants.FIELD_DESCRIPTION, "<no description>"));

        // get model for resources
        final SolrFieldModel<String> resourcesModel = new SolrFieldModel<String>(documentModel, FacetConstants.FIELD_RESOURCE);
        // wrap with a count provider
        final ResouceTypeCountDataProvider countProvider = new ResouceTypeCountDataProvider(resourcesModel, countingService);
        // view that shows provided counts 
        // TODO: hide if no resources
        add(new ResourceCountDataView("resourceCount", countProvider));
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
            final Label label = new Label("resourceCountLabel", item.getModel()) {

                @Override
                public <C> IConverter<C> getConverter(Class<C> type) {
                    if (type == ResourceTypeCount.class) {
                        return (IConverter<C>) resourceTypeCountConverter;
                    } else {
                        return super.getConverter(type);
                    }
                }

            };

            resourceLink.add(label);
            item.add(resourceLink);
        }
    }

    /**
     * One-way converter for resource type counts that generates string
     * representations like "1 video file", "2 video files" or "3 text
     * documents"
     */
    private static class ResourceTypeCountConverter implements IConverter<ResourceTypeCount> {

        @Override
        public ResourceTypeCount convertToObject(String value, Locale locale) throws ConversionException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String convertToString(ResourceTypeCount value, Locale locale) {
            final String resourceTypeString;
            if (value.getCount() == 1) {
                resourceTypeString = getSingularResourceTypeString(value);
            } else {
                resourceTypeString = getPluralResourceTypeString(value);
            }
            return String.format("%d %s", value.getCount(), resourceTypeString);
        }

        private String getSingularResourceTypeString(ResourceTypeCount value) {
            //TODO: read from resource bundle
            switch (value.getResourceType()) {
                case ANNOTATION:
                    return "annotation file";
                case AUDIO:
                    return "audio file";
                case VIDEO:
                    return "video file";
                case IMAGE:
                    return "image";
                case TEXT:
                    return "text document";
                case OTHER:
                    return "other";
                default:
                    return "unknown";
            }
        }

        private String getPluralResourceTypeString(ResourceTypeCount value) {
            //TODO: read from resource bundle
            switch (value.getResourceType()) {
                case ANNOTATION:
                    return "annotation files";
                case AUDIO:
                    return "audio files";
                case VIDEO:
                    return "video files";
                case IMAGE:
                    return "images";
                case TEXT:
                    return "text documents";
                case OTHER:
                    return "other";
                default:
                    return "unknown";
            }
        }

    }
}
