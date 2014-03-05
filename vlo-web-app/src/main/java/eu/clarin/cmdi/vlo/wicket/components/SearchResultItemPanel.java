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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.wicket.provider.ResouceTypeCountDataProvider;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
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

    @SpringBean
    private ResourceTypeCountingService countingService;

    public SearchResultItemPanel(String id, IModel<SolrDocument> model) {
        super(id, model);
        add(new SolrFieldLabel("title", model, FacetConstants.FIELD_NAME));
        add(new SolrFieldLabel("description", model, FacetConstants.FIELD_DESCRIPTION, "<no description>"));

        // get model for resources
        final SolrFieldModel<String> resourcesModel = new SolrFieldModel<String>(model, FacetConstants.FIELD_RESOURCE);
        final ResouceTypeCountDataProvider countProvider = new ResouceTypeCountDataProvider(resourcesModel, countingService);
        add(new ResourceCountDataView("resourceCount", countProvider));
    }

    private static class SolrFieldLabel extends Label {

        public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName) {
            super(id, new SolrFieldStringModel(documentModel, fieldName));
        }

        public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback) {
            super(id,
                    new NullFallbackModel(
                            new SolrFieldStringModel(documentModel, fieldName), nullFallback));
        }

    }

    private static class ResourceCountDataView extends DataView<ResourceTypeCount> {

        private final static ResourceTypeCountConverter resourceTypeCountConverter
                = new ResourceTypeCountConverter();

        public ResourceCountDataView(String id, IDataProvider<ResourceTypeCount> dataProvider) {
            super(id, dataProvider);
        }

        @Override
        protected void populateItem(Item<ResourceTypeCount> item) {
            final Link resourceLink = new Link("recordLink") {

                @Override
                public void onClick() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
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
