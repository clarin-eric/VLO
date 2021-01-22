/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.facets;

import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.facets.configuration.ObjectFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Twan Goosen &lt;twan.goosen@mpi.nl&gt;
 */
public class FacetsConfigurationsMarshaller {

    private final JAXBContext jc;

    public FacetsConfigurationsMarshaller() throws JAXBException {
        this.jc = JAXBContext.newInstance(FacetsConfiguration.class, Facet.class, ObjectFactory.class);
    }

    /**
     * Marshals (serializes) an existing facets definition file to some output location
     *
     * @param configuration configuration to marshal
     * @param result output result for the marshalling, e.g. a
     * {@link StreamResult} for usage with Files, Streams or Writers
     * @throws JAXBException
     */
    public final void marshal(FacetsConfiguration configuration, Result result) throws JAXBException {
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(configuration, result);
    }

    /**
     * Unmarshals (deserializes) a facets configuration file from some source location
     *
     * @param source the source representing the facets configuration to
     * unmarshal
     * @return the facets configuration as described by the source
     * @throws JAXBException if an error occurs while unmarshalling
     */
    public final FacetsConfiguration unmarshal(Source source) throws JAXBException {
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (FacetsConfiguration) unmarshaller.unmarshal(source);
    }
}
