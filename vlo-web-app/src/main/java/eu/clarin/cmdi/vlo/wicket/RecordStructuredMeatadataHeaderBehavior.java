/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel.JsonLdObject;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RecordStructuredMeatadataHeaderBehavior extends JsonLdHeaderBehavior {

    private final static Logger logger = LoggerFactory.getLogger(RecordStructuredMeatadataHeaderBehavior.class);

    public RecordStructuredMeatadataHeaderBehavior(IModel<SolrDocument> documentModel) {
        super(createJsonModel(documentModel));
    }

    @Override
    public boolean isEnabled(Component component) {
        // TODO disable in certain cases??
        return super.isEnabled(component);
    }

    private static IModel<String> createJsonModel(IModel<SolrDocument> documentModel) {
        final LoadableDetachableModel<JsonLdObject> model = new LoadableDetachableModel<>() {
            @Override
            protected JsonLdObject load() {
                if (documentModel.getObject() == null) {
                    return null;
                } else {
                    return createDataSetForDocument(documentModel);
                }
            }
        };

        return new JsonLdModel(model);
    }

    private static DataSet createDataSetForDocument(IModel<SolrDocument> documentModel) {
        final FieldNameService fieldNameService = VloWicketApplication.get().getFieldNameService();

        final DataSet dataSet = new DataSet();
        dataSet.setUrl(VloWicketApplication.get().getPermalinkService().getUrlString(RecordPage.class, null, documentModel.getObject()));

        final ConstructionContext context = new ConstructionContext(fieldNameService, documentModel);
        context.setStringValue(FieldKey.NAME, dataSet::setName, true);
        context.setStringValue(FieldKey.DESCRIPTION, dataSet::setDescription, false);

        //TODO: set remaining properties
        return dataSet;
    }

    private static class ConstructionContext {

        private final FieldNameService fieldNameService;
        private final IModel<SolrDocument> documentModel;

        public ConstructionContext(FieldNameService fieldNameService, IModel<SolrDocument> documentModel) {
            this.fieldNameService = fieldNameService;
            this.documentModel = documentModel;
        }

        protected <T> void setStringValue(FieldKey key, Consumer<String> setter, boolean forceSingleValue) {
            final SolrFieldStringModel valueModel = new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(key), forceSingleValue);
            final String value = valueModel.getObject();
            if(value != null) {
                setter.accept(value);
            }
        }

        protected <T> void setValue(FieldKey key, Consumer<T> setter, Function<Object, T> valueMapper) {
            final Collection<Object> value = documentModel.getObject().getFieldValues(fieldNameService.getFieldName(key));
            if (value != null && !value.isEmpty()) {
                setter.accept(valueMapper.apply(value.iterator().next()));
            }
        }

    }

    private static class DataSet extends JsonLdObject {

        private String url;

        private String name;

        private String description;

        private List<String> identifier;

        private URI sameAs;

        private URI mainEntityOfPage;

        private URI license;

        private List<String> keywords;

        //TODO: hasPart [CreativeWork]
        //TODO: creator [Person? Organisation?]
        //TODO: distribution [DataDownload]
        //TODO: spatial [Place]        
        //TODO: inludedInDataCatalog [DataCatalog]
        public DataSet() {
            super("https://schema.org", "DataSet");
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getIdentifier() {
            return identifier;
        }

        public void setIdentifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public URI getSameAs() {
            return sameAs;
        }

        public void setSameAs(URI sameAs) {
            this.sameAs = sameAs;
        }

        public URI getMainEntityOfPage() {
            return mainEntityOfPage;
        }

        public void setMainEntityOfPage(URI mainEntityOfPage) {
            this.mainEntityOfPage = mainEntityOfPage;
        }

        public URI getLicense() {
            return license;
        }

        public void setLicense(URI license) {
            this.license = license;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }

    }

}
