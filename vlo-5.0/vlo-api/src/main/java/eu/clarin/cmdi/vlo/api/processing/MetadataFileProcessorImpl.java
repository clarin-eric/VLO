/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.api.processing;

import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import eu.clarin.cmdi.vlo.exception.InputProcessingException;
import eu.clarin.cmdi.vlo.mapping.RecordFieldValuesMapper;
import eu.clarin.cmdi.vlo.mapping.VloMappingException;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@Profile("default")
@Slf4j
public class MetadataFileProcessorImpl implements MetadataFileProcessor {

    private final RecordFieldValuesMapper fieldValuesMapper;

    public MetadataFileProcessorImpl(RecordFieldValuesMapper fieldValuesMapper) {
        this.fieldValuesMapper = fieldValuesMapper;
    }

    @Override
    public VloRecord processMappingRequest(VloRecordMappingRequest request) throws InputProcessingException {
        log.info("Processing input from request {}", request);
        final VloRecord result = new VloRecord();

        try {
            result.setDataRoot(request.getDataRoot());
            result.setSourcePath(request.getFile());

            final StreamSource source = new StreamSource(request.getFile());
            source.setInputStream(new ByteArrayInputStream(request.getXmlContent()));
            final Map<String, Collection<ValueLanguagePair>> fieldValues = fieldValuesMapper.mapRecordToFields(source);

            populateRecordWithFieldValues(result, fieldValues);

            return result;
        } catch (IOException | VloMappingException ex) {
            throw new InputProcessingException(String.format("Error while trying to parse input file %s", request.getFile()), ex);
        }
    }

    private void populateRecordWithFieldValues(VloRecord result, Map<String, Collection<ValueLanguagePair>> fieldValues) {
        //TODO: selflink
        //TODO: profile ID
        result.setFields(
                Maps.transformValues(fieldValues, values -> values.stream().map(vlp -> (Object) vlp.getValue()).toList()));
    }
}
