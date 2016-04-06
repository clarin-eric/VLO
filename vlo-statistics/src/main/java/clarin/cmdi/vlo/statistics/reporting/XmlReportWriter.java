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
package clarin.cmdi.vlo.statistics.reporting;

import clarin.cmdi.vlo.statistics.model.VloReport;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class XmlReportWriter implements VloReportHandler {

    private final static Logger logger = LoggerFactory.getLogger(XmlReportWriter.class);
    private final File xmlOutputFile;

    public XmlReportWriter(File xmlOutputFile) {
        this.xmlOutputFile = xmlOutputFile;
    }

    @Override
    public void handleReport(VloReport report) {
        try {
            // Prepare marshaller
            final JAXBContext jc = JAXBContext.newInstance(VloReport.class);
            final Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Write to target
            logger.info("Writing report to {}", xmlOutputFile);
            marshaller.marshal(report, xmlOutputFile);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

}
