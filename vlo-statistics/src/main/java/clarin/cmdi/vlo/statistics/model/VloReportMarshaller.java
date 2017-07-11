/*
 * Copyright (C) 2016 CLARIN
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
package clarin.cmdi.vlo.statistics.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class VloReportMarshaller {

    private final JAXBContext jc;

    public VloReportMarshaller() throws JAXBException {
        jc = JAXBContext.newInstance(VloReport.class);
    }

    public void marshall(VloReport report, StreamResult target) throws JAXBException {
        // Prepare marshaller
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Write to target
        marshaller.marshal(report, target);
    }

    public VloReport unmarshall(StreamSource source) throws JAXBException {
        final Unmarshaller unmarshaller = jc.createUnmarshaller();
        final Object object = unmarshaller.unmarshal(source);
        if (object instanceof VloReport) {
            return (VloReport) object;
        } else {
            throw new JAXBException("Object in source is not a representation of VloReport: " + source.toString());
        }
    }

}
