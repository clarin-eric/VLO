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
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.config.DataSetStructuredData;
import eu.clarin.cmdi.vlo.config.DataSetStructuredDataFilter;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel.JsonLdObject;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoObjectModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class RecordStructuredMeatadataHeaderBehavior extends JsonLdHeaderBehavior {

    private final static Logger logger = LoggerFactory.getLogger(RecordStructuredMeatadataHeaderBehavior.class);

    public static final int ARRAY_SIZE_LIMIT = 25;
    public static final String FILTER_WILDCARD = "*";

    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private FieldNameService fieldNameService;

    private final IModel<SolrDocument> documentModel;

    public RecordStructuredMeatadataHeaderBehavior(IModel<SolrDocument> documentModel) {
        super(createJsonModel(documentModel));
        this.documentModel = documentModel;
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

        final VloConfig vloConfig = VloWicketApplication.get().getVloConfig();
        final DataCatalog dataCatalog = new DataCatalog(vloConfig.getHomeUrl());
        dataSet.setIncludedInDataCatalog(dataCatalog);

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

        if (landingPageURI == null) {
            return null;
        }

        //identifier
        dataSet.setIdentifier(ImmutableList.of(landingPageURI.toString()));
        //main entity of page
        dataSet.setMainEntityOfPage(landingPageURI);
        //sameAs
        dataSet.setSameAs(landingPageURI);
        
        // alternative identifiers if no landing page??
//        if (context.hasFieldValue(FieldKey.SELF_LINK)) {
//            context.setStringValues(FieldKey.SELF_LINK, dataSet::setIdentifier);
//        } else {
//            context.setStringValues(FieldKey.ID, dataSet::setIdentifier);
//        }

        //licence
        context.setValue(FieldKey.LICENSE, dataSet::setLicense, values -> {
            return values.stream().findFirst()
                    .flatMap(o -> {
                        try {
                            return Optional.of(new URI(o.toString()));
                        } catch (URISyntaxException ex) {
                            logger.trace("Not an URL: {}", o, ex.getMessage());
                            return Optional.empty();
                        }
                    }
                    )
                    .orElse(null);
        });

        //Simple properties
        //TODO: temporal
        //Complex properties
        //creator
        final Collection<Object> creatorValues = context.getFieldValues(FieldKey.CREATOR);
        if (creatorValues != null) {
            final List<Person> creators = creatorValues.stream()
                    .map(Objects::toString)
                    .map(Person::new)
                    .limit(ARRAY_SIZE_LIMIT)
                    .collect(Collectors.toList());
            dataSet.setCreator(creators);
        }

        //spatial
        final Collection<Object> placeValues = context.getFieldValues(FieldKey.COUNTRY);
        if (placeValues != null) {
            final List<Place> places = placeValues.stream()
                    .map(Objects::toString)
                    .map(Place::new)
                    .limit(ARRAY_SIZE_LIMIT)
                    .collect(Collectors.toList());
            dataSet.setSpatial(places);
        }

        final Collection<Object> hasPartValues = context.getFieldValues(FieldKey.HAS_PART);
        if (hasPartValues != null) {
            final List<CreativeWork> children = hasPartValues.stream()
                    .map(partId -> {
                        final PageParameters params = new PageParameters().add(VloWebAppParameters.DOCUMENT_ID, partId.toString());
                        final String relativeUrl = RequestCycle.get().urlFor(RecordPage.class, params).toString();
                        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(relativeUrl));
                    })
                    .map(CreativeWork::new)
                    .limit(ARRAY_SIZE_LIMIT)
                    .collect(Collectors.toList());
            dataSet.setHasPart(children);

        }

        final Collection<Object> resourceInfos = context.getFieldValues(FieldKey.RESOURCE);
        if (resourceInfos != null) {
            List<DataDownload> resources = resourceInfos.stream()
                    .map(Objects::toString).map(Model::of).map(ResourceInfoObjectModel::new).map(ResourceInfoObjectModel::getObject)
                    .map(resource -> {
                        final DataDownload dataDownload = new DataDownload();
                        dataDownload.setContentUrl(resource.getUrl());
                        dataDownload.setEncodingFormat(resource.getType());
                        //TODO: set name?
                        return dataDownload;
                    })
                    .limit(ARRAY_SIZE_LIMIT)
                    .collect(Collectors.toList());
            dataSet.setDistribution(resources);
        }

        //TODO: distribution
        return dataSet;
    }

    @Override
    public boolean isEnabled(Component component) {
        if (super.isEnabled(component)) {
            return isEnabled(vloConfig.getDataSetStructuredData());
        } else {
            return false;
        }
    }

    private boolean isEnabled(DataSetStructuredData config) {
        if (config == null || !config.isEnabled()) {
            return false;
        } else {
            //check against include/exclude filters
            final List<DataSetStructuredDataFilter> include = config.getInclude();
            final List<DataSetStructuredDataFilter> exclude = config.getExclude();

            if (include == null
                    || include.isEmpty() // if no include filter is present, it implies 'include all'
                    || matchesFilter(include)) { // matching include filter is first condition
                // include check result positive
                return (exclude == null
                        || exclude.isEmpty() // if no exclude filter, it implies 'exclude none'
                        || !matchesFilter(exclude)); // NOT matching exclude filter is second condition
            } else {
                // include check result negative
                return false;
            }
        }
    }

    private boolean matchesFilter(List<DataSetStructuredDataFilter> filter) {
        final SolrDocument document = documentModel.getObject();
        return document != null // null document treated as no match
                && filter.stream()
                        .anyMatch(f -> {
                            final FieldKey fieldKey = FieldKey.valueOf(f.getField());
                            if (fieldKey != null) {
                                final Collection<Object> fieldValues = document.getFieldValues(fieldNameService.getFieldName(fieldKey));
                                if (fieldValues != null) {
                                    final String filterValue = f.getValue();
                                    return FILTER_WILDCARD.equals(filterValue) || fieldValues.stream().anyMatch(fieldValue -> {
                                        return filterValue.equals(fieldValue.toString());
                                    });
                                }
                            }

                            return false;
                        });
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

        protected Collection<Object> getFieldValues(FieldKey key) {
            return documentModel.getObject().getFieldValues(fieldNameService.getFieldName(key));
        }

        protected <T> void setStringValue(FieldKey key, Consumer<String> setter, boolean forceSingleValue) {
            final SolrFieldStringModel valueModel = new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(key), forceSingleValue);
            final String value = valueModel.getObject();
            if (value != null) {
                setter.accept(value);
            }
        }

        protected <T> void setStringValues(FieldKey key, Consumer<Collection<String>> setter) {
            final Collection<Object> values = getFieldValues(key);
            setter.accept(Collections2.transform(values, Objects::toString));
        }

        protected <T> void setValue(FieldKey key, Consumer<T> setter, Function<Collection<Object>, T> valueMapper) {
            final Collection<Object> values = getFieldValues(key);
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

        private Collection<CreativeWork> hasPart;

        private Collection<String> keywords;

        private Collection<Person> creator;

        private Collection<Place> spatial;

        private Collection<DataDownload> distribution;

        private DataCatalog includedInDataCatalog;

        //TODO: distribution [DataDownload]
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

        public DataCatalog getIncludedInDataCatalog() {
            return includedInDataCatalog;
        }

        public void setIncludedInDataCatalog(DataCatalog includedInDataCatalog) {
            this.includedInDataCatalog = includedInDataCatalog;
        }

        public Collection<Person> getCreator() {
            return creator;
        }

        public void setCreator(Collection<Person> creator) {
            this.creator = creator;
        }

        public Collection<Place> getSpatial() {
            return spatial;
        }

        public void setSpatial(Collection<Place> spatial) {
            this.spatial = spatial;
        }

        public Collection<CreativeWork> getHasPart() {
            return hasPart;
        }

        public void setHasPart(Collection<CreativeWork> hasPart) {
            this.hasPart = hasPart;
        }

        public Collection<DataDownload> getDistribution() {
            return distribution;
        }

        public void setDistribution(Collection<DataDownload> distribution) {
            this.distribution = distribution;
        }

    }

    private static class DataCatalog extends JsonLdObject {

        private final String url;

        public DataCatalog(String url) {
            super("DataCatalog");
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

    }

    private static class Person extends JsonLdObject {

        private final String name;

        public Person(String name) {
            super("Person");
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    private static class Place extends JsonLdObject {

        private final String name;

        public Place(String name) {
            super("Place");
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    private static class CreativeWork extends JsonLdObject {

        private final String url;

        public CreativeWork(String url) {
            super("CreativeWork");
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

    }

    private static class DataDownload extends JsonLdObject {

        private String name;
        private String contentUrl;
        private String encodingFormat;

        public DataDownload() {
            super("DataDownload");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContentUrl() {
            return contentUrl;
        }

        public void setContentUrl(String contentUrl) {
            this.contentUrl = contentUrl;
        }

        public String getEncodingFormat() {
            return encodingFormat;
        }

        public void setEncodingFormat(String encodingFormat) {
            this.encodingFormat = encodingFormat;
        }

    }

}
