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
import clarin.cmdi.vlo.statistics.model.VloReportMarshaller;
import java.io.File;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class XmlReportWriter implements VloReportHandler {

    private final static Logger logger = LoggerFactory.getLogger(XmlReportWriter.class);
    private final File xmlOutputFile;
    private final VloReportMarshaller marshaller;

    public XmlReportWriter(File xmlOutputFile) throws JAXBException {
        this.xmlOutputFile = xmlOutputFile;
        this.marshaller = new VloReportMarshaller();
    }

    @Override
    public void handleReport(VloReport report) {
        logger.info("Writing report to {}", xmlOutputFile);

        try {
            marshaller.marshall(report, new StreamResult(xmlOutputFile));
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

}
