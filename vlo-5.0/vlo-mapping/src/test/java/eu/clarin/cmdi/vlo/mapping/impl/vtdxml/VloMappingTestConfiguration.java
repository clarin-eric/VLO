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

import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
class VloMappingTestConfiguration extends VloMappingConfiguration {
    
    public VloMappingTestConfiguration() {
        setProfileSchemaUrl("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/{PROFILE_ID}/xsd");
        setVocabularyRegistryUrl("http://clavas.clarin.eu/clavas/public/api/find-concepts");
    }
    
}
