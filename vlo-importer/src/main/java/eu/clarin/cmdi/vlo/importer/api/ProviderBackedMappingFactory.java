/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo.importer.api;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;

/**
 * Routes the parser's profile-id lookups to the configured
 * {@link FacetsMappingProvider}, resolving the profile id to a schema location
 * first.
 * </p>
 */
class ProviderBackedMappingFactory extends FacetMappingFactory {

    private final VloConfig vloConfig;
    private final FacetsMappingProvider provider;

    ProviderBackedMappingFactory(VloConfig vloConfig, VLOMarshaller marshaller, FacetsMappingProvider provider) {
        super(vloConfig, marshaller);
        this.vloConfig = vloConfig;
        this.provider = provider;
    }

    @Override
    public FacetsMapping getFacetMapping(String profileId, Boolean useLocalXSDCache) {
        return provider.getFacetsMapping(vloConfig.getComponentRegistryProfileSchema(profileId));
    }
}
