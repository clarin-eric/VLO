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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.ResourceInfo;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel.JsonLdObject;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoObjectModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;
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
        
        URI landingPageURI = null;
        if (context.hasFieldValue(FieldKey.LANDINGPAGE)) {
            ResourceInfoObjectModel resourceInfoObjectModel = new ResourceInfoObjectModel(new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.LANDINGPAGE)));
            ResourceInfo resourceInfo = resourceInfoObjectModel.getObject();
            if (resourceInfo != null) {
                try {
                    landingPageURI = new URI(resourceInfo.getUrl());
                } catch (URISyntaxException ex) {
                    logger.debug("Landing page reference is not a valid URI: {}", ex.getMessage());
                }
            }
        }

        //identifier
        if (landingPageURI != null) {
            dataSet.setIdentifier(ImmutableList.of(landingPageURI.toString()));
        } else {
            if (context.hasFieldValue(FieldKey.SELF_LINK)) {
                context.setStringValues(FieldKey.SELF_LINK, dataSet::setIdentifier);
            } else {
                context.setStringValues(FieldKey.ID, dataSet::setIdentifier);
            }
        }
        
        //main entity of page
        if (landingPageURI != null) {
            dataSet.setMainEntityOfPage(landingPageURI);
        }

        //Simple properties
        
        //TODO: sameAs
        //TODO: creator
        //TODO: temporal
        //TODO: spatial
        //TODO: license
        
        //Complex properties
        //TODO: hasPart
        //TODO: distribution
        
        return dataSet;
    }
    
    private static URI fieldValuesToURI(Collection<Object> values) {
        if (values.isEmpty()) {
            return null;
        } else {
            try {
                return new URI(values.iterator().next().toString());
            } catch (URISyntaxException ex) {
                logger.debug("Not a valid URI in {}", values);
                return null;
            }
        }
    }
    
    private static class ConstructionContext {
        
        private final FieldNameService fieldNameService;
        private final IModel<SolrDocument> documentModel;
        
        public ConstructionContext(FieldNameService fieldNameService, IModel<SolrDocument> documentModel) {
            this.fieldNameService = fieldNameService;
            this.documentModel = documentModel;
            
        }
        
        protected boolean hasFieldValue(FieldKey key) {
            return documentModel.getObject().containsKey(fieldNameService.getFieldName(key));
        }
        
        protected <T> void setStringValue(FieldKey key, Consumer<String> setter, boolean forceSingleValue) {
            final SolrFieldStringModel valueModel = new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(key), forceSingleValue);
            final String value = valueModel.getObject();
            if (value != null) {
                setter.accept(value);
            }
        }
        
        protected <T> void setStringValues(FieldKey key, Consumer<Collection<String>> setter) {
            final Collection<Object> values = documentModel.getObject().getFieldValues(fieldNameService.getFieldName(key));
            setter.accept(Collections2.transform(values, Objects::toString));
        }
        
        protected <T> void setValue(FieldKey key, Consumer<T> setter, Function<Collection<Object>, T> valueMapper) {
            final Collection<Object> values = documentModel.getObject().getFieldValues(fieldNameService.getFieldName(key));
            if (values != null && !values.isEmpty()) {
                setter.accept(valueMapper.apply(values));
            }
        }
        
    }
    
    private static class DataSet extends JsonLdObject {
        
        private String url;
        
        private String name;
        
        private String description;
        
        private Collection<String> identifier;
        
        private URI sameAs;
        
        private URI mainEntityOfPage;
        
        private URI license;
        
        private Collection<String> keywords;

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
        
        public Collection<String> getIdentifier() {
            return identifier;
        }
        
        public void setIdentifier(Collection<String> identifier) {
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
        
        public Collection<String> getKeywords() {
            return keywords;
        }
        
        public void setKeywords(Collection<String> keywords) {
            this.keywords = keywords;
        }
        
    }
    
}
