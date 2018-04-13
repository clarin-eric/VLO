/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Creates an instance of the default configuration packed with the VLO commons
 *
 * @author twagoo
 */
public class DefaultVloConfigFactory extends AbstractXmlVloConfigFactory {

    public static final String DEFAULT_CONFIG_RESOURCE = "/VloConfig.xml";

    /**
     *
     * @return an input stream for the XML file describing the default VLO
     * configuration
     */
    @Override
    protected InputStream getXmlConfigurationInputStream() {
        return getClass().getResourceAsStream(DEFAULT_CONFIG_RESOURCE);
    }

    @Override
    protected URI getLocation() {
        try {
            return getClass().getResource(DEFAULT_CONFIG_RESOURCE).toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Invalid config file URI", ex);
        }
    }

    public static VloConfig configureDefaultMappingLocations(VloConfig config) {
        config.setFacetConceptsFile("/mapping/facetConcepts.xml");
        config.setLicenseAvailabilityMapUrl("/uniform-maps/LicenseAvailabilityMap.xml");
        config.setLicenseURIMapUrl("/uniform-maps/LicenseURIMap.xml");
        config.setOrganisationNamesUrl("/uniform-maps/OrganisationControlledVocabulary.xml");
        config.setLanguageNameVariantsUrl("/uniform-maps/LanguageNameVariantsMap.xml");
        return config;
    }

}
