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
package eu.clarin.cmdi.vlo.api.configuration;

import eu.clarin.cmdi.vlo.mapping.RecordFieldValuesMapper;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.definition.VloMappingRulesException;
import eu.clarin.cmdi.vlo.mapping.impl.vtdxml.RecordFieldValuesMapperImpl;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 *
 * @author twagoo
 */
@Configuration
@Profile("default")
public class MappingConfiguration {

    @Value("${vlo.api.mapping.definitionUri}")
    private String mappingDefinitionUri;
    @Value("${vlo.api.mapping.vocabularyRegistryUrl}")
    private String vocabularyRegistryUrl; // = "http://clavas.clarin.eu/clavas/public/api/find-concepts";
    @Value("${vlo.api.mapping.profileSchemaUrl}")
    private String profileSchemaUrl; // = "https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/{PROFILE_ID}/xsd";    

    @Bean
    public RecordFieldValuesMapper fieldValuesMapper() throws IOException, VloMappingRulesException {
        return new RecordFieldValuesMapperImpl(mappingConfig());
    }

    @Bean
    public VloMappingConfiguration mappingConfig() {
        return new VloMappingConfiguration(vocabularyRegistryUrl, profileSchemaUrl, mappingDefinitionUri);
    }

}
