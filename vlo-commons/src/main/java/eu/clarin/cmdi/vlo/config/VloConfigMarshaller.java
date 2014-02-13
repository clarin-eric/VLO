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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 *
 * @author twagoo
 */
public class VloConfigMarshaller {

    private final JAXBContext jc;

    public VloConfigMarshaller(JAXBContext jc) {
        this.jc = jc;
    }
    
    public VloConfigMarshaller() throws JAXBException {
        this.jc = JAXBContext.newInstance(VloConfig.class);
    }

    public void marshal(VloConfig config, Result result) throws JAXBException {
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(config, result);
    }

    public VloConfig unmarshal(Source source) throws JAXBException {
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (VloConfig) unmarshaller.unmarshal(source);
    }

}
