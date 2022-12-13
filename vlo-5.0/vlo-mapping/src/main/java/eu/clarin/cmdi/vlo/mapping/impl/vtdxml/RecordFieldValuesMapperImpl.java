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
import eu.clarin.cmdi.vlo.mapping.ContextFieldValueMapper;
import eu.clarin.cmdi.vlo.mapping.ContextFieldValueMapperImpl;
import eu.clarin.cmdi.vlo.mapping.RecordFactory;
import eu.clarin.cmdi.vlo.mapping.RecordReader;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.definition.MappingDefinition;
import eu.clarin.cmdi.vlo.mapping.definition.MappingDefinitionProviderImpl;
import eu.clarin.cmdi.vlo.mapping.definition.VloMappingRulesException;
import java.io.IOException;
import eu.clarin.cmdi.vlo.mapping.definition.MappingDefinitionProvider;
import eu.clarin.cmdi.vlo.mapping.processing.FieldValuesProcessor;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class RecordFieldValuesMapperImpl extends BaseRecordFieldValuesMapper {

    public RecordFieldValuesMapperImpl(VloMappingConfiguration mappingConfig) throws VloMappingRulesException, IOException {
        this(new RecordReaderImpl(mappingConfig), new MappingDefinitionProviderImpl(mappingConfig), mappingConfig);
    }

    public RecordFieldValuesMapperImpl(RecordReader recordReader, MappingDefinitionProvider definitionProvider, VloMappingConfiguration mappingConfig) throws VloMappingRulesException {
        this(recordReader, definitionProvider.getDefinition(), mappingConfig);
    }

    public RecordFieldValuesMapperImpl(RecordReader recordReader, MappingDefinition mappingDefinition, VloMappingConfiguration mappingConfig) throws VloMappingRulesException {
        this(new CachingRecordFactory(recordReader),
                new ContextFieldValueMapperImpl(mappingDefinition.getRules(), mappingConfig),
                mappingDefinition.getFieldValuesProcessor());
    }

    public RecordFieldValuesMapperImpl(RecordFactory recordFactory, ContextFieldValueMapper fieldValueMapper, FieldValuesProcessor fieldValuesRootProcessor) throws VloMappingRulesException {
        super(new ContextFactoryImpl(recordFactory), fieldValueMapper, fieldValuesRootProcessor);
    }
}
