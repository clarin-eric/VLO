/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import eu.clarin.cmdi.vlo.mapping.BaseRecordFieldValuesMapper;
import eu.clarin.cmdi.vlo.mapping.CachingRecordFactory;
import eu.clarin.cmdi.vlo.mapping.ContextFieldValueMapperImpl;
import eu.clarin.cmdi.vlo.mapping.FieldValuesProcessorImpl;
import eu.clarin.cmdi.vlo.mapping.RecordFactory;
import eu.clarin.cmdi.vlo.mapping.RecordReader;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class RecordFieldValuesMapperImpl extends BaseRecordFieldValuesMapper {

    public RecordFieldValuesMapperImpl(VloMappingConfiguration mappingConfig) {
        this(new RecordReaderImpl(mappingConfig));

    }

    private RecordFieldValuesMapperImpl(RecordReader recordReader) {
        this(new CachingRecordFactory(recordReader));

    }

    private RecordFieldValuesMapperImpl(RecordFactory recordFactory) {
        super(new ContextFactoryImpl(recordFactory), new ContextFieldValueMapperImpl(), new FieldValuesProcessorImpl());
    }
}
