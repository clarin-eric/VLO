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
package eu.clarin.cmdi.vlo.mapping;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.mapping.model.CmdRecord;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates {@link VloRecord} objects from a stream source
 *
 * @author twagoo
 */
@Slf4j
public class VloRecordFactory {

    private final RecordFieldValuesMapper fieldValuesMapper;
    private final CmdRecordFactory cmdRecordFactory;
    private IdNormalizer idNormalizer;

    /**
     * Value mapper to use to populate the fields of a record
     *
     * @param cmdRecordFactory
     * @param fieldValuesMapper
     */
    public VloRecordFactory(CmdRecordFactory cmdRecordFactory, RecordFieldValuesMapper fieldValuesMapper) {
        this(cmdRecordFactory, fieldValuesMapper, new IdNormalizer());
    }

    public VloRecordFactory(CmdRecordFactory cmdRecordFactory, RecordFieldValuesMapper fieldValuesMapper, IdNormalizer idNormalizer) {
        this.cmdRecordFactory = cmdRecordFactory;
        this.fieldValuesMapper = fieldValuesMapper;
        this.idNormalizer = idNormalizer;
    }

    public VloRecord mapToRecord(StreamSource source, String dataRoot, String sourcePath) throws IOException, VloMappingException {
        log.debug("Mapping {} to record", source.getSystemId());

        final Map<String, Collection<ValueLanguagePair>> fieldValues = fieldValuesMapper.mapRecordToFields(source);
        log.trace("Field values obtained for source {}", source.getSystemId());

        final CmdRecord cmdRecord = cmdRecordFactory.getRecord(source);

        return constructRecord(cmdRecord, fieldValues, dataRoot, sourcePath);
    }

    private VloRecord constructRecord(final CmdRecord cmdRecord, final Map<String, Collection<ValueLanguagePair>> fieldValues, String dataRoot, String sourcePath) {
        // resturce field values map
        final Map<String, List<Object>> recordFields = Maps.transformValues(fieldValues,
                values -> values.stream()
                        .map(vlp -> (Object) vlp.getValue()) //TODO: preserve language information
                        .toList());

        // build record
        final VloRecord record = new VloRecord();
        record.setDataRoot(dataRoot);
        record.setSourcePath(sourcePath);
        record.setFields(recordFields);

        final String docId = idNormalizer.normalizeIdString(getId(cmdRecord, sourcePath));
        log.debug("Id for record {} determined to be {}", sourcePath, docId);
        record.setId(docId);

        //TODO
        //record.setResources(
        return record;
    }

    private String getId(final CmdRecord cmdRecord, String sourcePath) {
        final CmdRecord.Header recordHeader = cmdRecord.getHeader();

        final String selfLink = recordHeader.getSelfLink();
        if (Strings.isNullOrEmpty(selfLink)) {
            return sourcePath;
        } else {
            return selfLink;
        }
    }

}