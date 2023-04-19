/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api.service.impl.solr;

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrDocument;
import org.springframework.boot.json.JsonParseException;
import org.springframework.boot.json.JsonParser;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
@AllArgsConstructor
@Slf4j
@Profile("solr")
public class SolrVloRecordConverter implements Converter<SolrDocument, VloRecord> {

    private static final String SOLR_FIELD_ID = "id";
    private static final String SOLR_FIELD_SELFLINK = "_selfLink";
    private static final String SOLR_FIELD_RESOURCE_REF = "_resourceRef";

    private final JsonParser jsonParser;

    @Override
    public VloRecord convert(SolrDocument solrDoc) {
        final VloRecord record = new VloRecord();
        record.setId(Objects.toString(solrDoc.getFieldValue(SOLR_FIELD_ID)));
        record.setSelflink(Objects.toString(solrDoc.getFieldValue(SOLR_FIELD_SELFLINK)));
        record.setFields(createFieldValuesMap(solrDoc));
        record.setResources(createResourceList(solrDoc));
        return record;
    }

    private Map<String, List<Object>> createFieldValuesMap(SolrDocument solrDoc) {
        // Note: this can NOT be implemented as a transformation of 
        // solrDoc.getFieldValueMap() using Guava's Maps.transformValues(), as
        // the map provided by the former does not support iteration

        return solrDoc
                .getFieldNames()
                .stream()
                //collect as map
                .collect(ImmutableMap.toImmutableMap(
                        // key: field name
                        Functions.identity(),
                        // value: field values from solr doc as a list
                        f -> ImmutableList.copyOf(solrDoc.getFieldValues(f))));
    }

    private List<VloRecord.Resource> createResourceList(SolrDocument solrDoc) {
        return FluentIterable.from(
                Optional.ofNullable(
                        solrDoc.getFieldValues(SOLR_FIELD_RESOURCE_REF))
                        // if the field is not found we get a null -- fall back to empty list
                        .orElseGet(Collections::emptyList))
                // resource refs are encoded in strings; filter out any unexpected garbage
                .filter(String.class)
                // convert to resource object
                .transform(this::resourceRefStringToResource)
                .toList();
    }

    private VloRecord.Resource resourceRefStringToResource(String object) {
        try {
            final Map<String, Object> resourceObject = jsonParser.parseMap(object);

            return new VloRecord.Resource(
                    "", //id
                    Objects.toString(resourceObject.getOrDefault("url", "")),
                    "Resource",
                    Objects.toString(resourceObject.getOrDefault("type", ""))
            );
        } catch (JsonParseException ex) {
            log.warn("Could not parse resource ref string from index: {}", object, ex);
            return null;
        }
    }

}
