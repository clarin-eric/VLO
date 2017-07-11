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
public class FacetConceptsMarshaller {

    private final JAXBContext jc;

    public FacetConceptsMarshaller() throws JAXBException {
        this.jc = JAXBContext.newInstance(FacetConcepts.class, FacetConcept.class, FacetConceptsObjectFactory.class);
    }

    /**
     * Marshals (serializes) an existing facets definition file to some output location
     *
     * @param concepts concepts to marshal
     * @param result output result for the marshalling, e.g. a
     * {@link StreamResult} for usage with Files, Streams or Writers
     * @throws JAXBException
     */
    public final void marshal(FacetConcepts concepts, Result result) throws JAXBException {
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(concepts, result);
    }

    /**
     * Unmarshals (deserializes) a facets definition file from some source location
     *
     * @param source the source representing the facets definition to
     * unmarshal
     * @return the facets definition as described by the source
     * @throws JAXBException if an error occurs while unmarshalling
     */
    public final FacetConcepts unmarshal(Source source) throws JAXBException {
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (FacetConcepts) unmarshaller.unmarshal(source);
    }
}
