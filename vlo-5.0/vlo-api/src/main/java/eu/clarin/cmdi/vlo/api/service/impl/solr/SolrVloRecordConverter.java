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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.solr.common.SolrDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 *
 * @author twagoo
 */
@Component
@Profile("solr")
public class SolrVloRecordConverter implements Converter<SolrDocument, VloRecord> {

    @Override
    public VloRecord convert(SolrDocument solrDoc) {
        final VloRecord record = new VloRecord();
        record.setId(Objects.toString(solrDoc.getFieldValue("id")));
        record.setFields(createFieldValuesMap(solrDoc));
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

}
