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
package eu.clarin.cmdi.vlo.mapping.rules;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class RulesFactoryImpl implements RulesFactory {

    private final Supplier<Source> definitionSource;
    private final Supplier<MappingDefinitionMarshaller> definitionMarshallerSupplier;

    public RulesFactoryImpl(VloMappingConfiguration config) throws VloMappingRulesException {
        this(getConfigSourceSupplier(config));
    }

    public RulesFactoryImpl(Source mappingDefinitionsSource) {
        this(Suppliers.ofInstance(mappingDefinitionsSource));
    }

    public RulesFactoryImpl(Supplier<Source> mappingDefinitionsSource) {
        this.definitionSource = mappingDefinitionsSource;
        this.definitionMarshallerSupplier = Suppliers.memoize(RulesFactoryImpl::newMarshaller);
    }

    @Override
    public Iterable<MappingRule> getRules() throws VloMappingRulesException {
        final MappingDefinitionMarshaller marshaller = definitionMarshallerSupplier.get();
        try {
            final MappingDefinition definition = marshaller.unmarshal(definitionSource.get());
            return Iterables.transform(definition.getRules(), r -> r);
        } catch (JAXBException ex) {
            throw new VloMappingRulesException("Error while trying to unmarshal mapping definition from source: " + definitionSource, ex);
        }
    }

    private static Supplier<Source> getConfigSourceSupplier(VloMappingConfiguration config) throws VloMappingRulesException {
        final String configuredUri = config.getMappingDefinitionUri();
        try {
            final URI uri = new URI(configuredUri);
            return () -> {
                try {
                    if ("file".equalsIgnoreCase(uri.getScheme())) {
                        log.debug("Loading mapping definition: File based stream source for from configured location: {}", uri);
                        return new StreamSource(new File(uri));
                    } else if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                        log.debug("Loading mapping definition: InputStream based stream source from configured location: {}", uri);
                        final InputStream is = uri.toURL().openStream();
                        return new StreamSource(is, uri.toString());
                    } else {
                        throw new VloMappingRulesException("Can't process scheme: " + uri.getScheme());
                    }
                } catch (VloMappingRulesException | IOException ex) {
                    throw new RuntimeException("Could not access mapping definition at configured location: " + configuredUri, ex);
                }
            };
        } catch (URISyntaxException ex) {
            throw new VloMappingRulesException("Could not access mapping definition at configured location: " + configuredUri, ex);
        }
    }

    private static MappingDefinitionMarshaller newMarshaller() {
        try {
            return new MappingDefinitionMarshaller();
        } catch (JAXBException ex) {
            throw new RuntimeException("Failed to instantiate mapping definition marshaller", ex);
        }
    }

}
