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

import eu.clarin.cmdi.vlo.mapping.CachingProfileFactory;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingTestConfiguration;
import eu.clarin.cmdi.vlo.mapping.definition.MappingDefinitionProvider;
import eu.clarin.cmdi.vlo.mapping.definition.MappingDefinitionProviderImpl;
import org.junit.jupiter.api.BeforeEach;

/**
 * Sets up {@link RecordFieldValuesMapperImpl} and required service
 * implementations with an adapted {@link VTDProfileParser} that resolves
 * profiles against the local test resources
 * ({@link TestResourceVTDProfileParser}).
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public abstract class VtdImplIntegrationTest {

    private VloMappingConfiguration mappingConfig;
    private VTDProfileParser profileParser;
    private ProfileReaderImpl profileReader;
    private ConceptLinkPathMapperImpl conceptLinkPathMapper;
    private CachingProfileFactory profileFactory;
    private MappingDefinitionProvider definitionProvider;
    private RecordReaderImpl recordReader;
    private RecordFieldValuesMapperImpl fieldValuesMapper;

    @BeforeEach
    protected void setUpServices() throws Exception {
        mappingConfig = createConfig();
        profileParser = new TestResourceVTDProfileParser(getMappingConfig());
        conceptLinkPathMapper = new ConceptLinkPathMapperImpl(getMappingConfig(), getProfileParser());
        profileReader = new ProfileReaderImpl(getConceptLinkPathMapper());
        profileFactory = new CachingProfileFactory(getProfileReader());
        recordReader = new RecordReaderImpl(getProfileFactory());
        definitionProvider = new MappingDefinitionProviderImpl(getMappingConfig());
        fieldValuesMapper = new RecordFieldValuesMapperImpl(getRecordReader(), getDefinitionProvider(), getMappingConfig());
    }

    protected VloMappingConfiguration createConfig() {
        return new VloMappingTestConfiguration();
    }

    protected RecordFieldValuesMapperImpl getFieldValuesMapper() {
        return fieldValuesMapper;
    }

    protected VloMappingConfiguration getMappingConfig() {
        return mappingConfig;
    }

    protected RecordReaderImpl getRecordReader() {
        return recordReader;
    }

    protected CachingProfileFactory getProfileFactory() {
        return profileFactory;
    }

    protected VTDProfileParser getProfileParser() {
        return profileParser;
    }

    public ProfileReaderImpl getProfileReader() {
        return profileReader;
    }

    public ConceptLinkPathMapperImpl getConceptLinkPathMapper() {
        return conceptLinkPathMapper;
    }

    public MappingDefinitionProvider getDefinitionProvider() {
        return definitionProvider;
    }

}
