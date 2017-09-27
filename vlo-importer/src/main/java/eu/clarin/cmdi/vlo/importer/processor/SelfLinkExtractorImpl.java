/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.importer.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.CmdConstants;
import static eu.clarin.cmdi.vlo.CmdConstants.CMD_NAMESPACE;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class SelfLinkExtractorImpl implements SelfLinkExtractor {

    private final static Logger LOG = LoggerFactory.getLogger(SelfLinkExtractorImpl.class);

    /**
     * Target path in XML document: /cmd:CMD/cmd:Header/cmd:MdSelfLink/text()
     */
    private final static List<QName> TARGET_PATH = ImmutableList.of(
            new QName(CMD_NAMESPACE, "CMD"),
            new QName(CMD_NAMESPACE, "Header"),
            new QName(CMD_NAMESPACE, "MdSelfLink")
    ).reverse();

    /**
     * Halting path. No self link past Header section...
     */
    private final static List<QName> HALT_PATH = ImmutableList.of(
            new QName(CmdConstants.CMD_NAMESPACE, "CMD"),
            new QName(CmdConstants.CMD_NAMESPACE, "Resources")
    ).reverse();

    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

    @Override
    public String extractMdSelfLink(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            final LinkedList<QName> stack = new LinkedList<>();

            final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(fileInputStream);
            while (reader.hasNext()) {
                reader.next();
                if (reader.isStartElement()) {
                    stack.push(reader.getName());
                    if (isHaltPath(stack)) {
                        LOG.debug("Halt point reached, no self link found in document {}", file);
                        return null;
                    }
                } else if (reader.isEndElement()) {
                    stack.pop();
                } else if (reader.isCharacters()) {
                    if (isTargetPath(stack)) {
                        LOG.trace("Self link found in document", file);
                        return reader.getText();
                    }
                }
            }
        } catch (XMLStreamException ex) {
            LOG.error("Error while parsing XML to find MdSelfLink");
        }
        
        LOG.debug("Document processing completed, failed to find self link in {}", file);
        return null;
    }

    private static boolean isTargetPath(final LinkedList<QName> stack) {
        return Iterables.elementsEqual(TARGET_PATH, stack);
    }

    private static boolean isHaltPath(final LinkedList<QName> stack) {
        return Iterables.elementsEqual(HALT_PATH, stack);
    }
}
