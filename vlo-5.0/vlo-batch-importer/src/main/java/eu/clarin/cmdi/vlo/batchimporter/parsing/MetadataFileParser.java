/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.batchimporter.parsing;

import com.google.common.collect.ImmutableList;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.batchimporter.InputProcessingException;
import eu.clarin.cmdi.vlo.batchimporter.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.MappingInput;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MetadataFileParser {

    public MappingInput parseFile(MetadataFile inputFile) throws InputProcessingException {
        log.info("Parsing input file {}", inputFile);
        final MappingInput.MappingInputBuilder builder = MappingInput.builder();
        try {
            builder.dataRoot(inputFile.getDataRoot());
            builder.sourcePath(inputFile.getLocation().toString());
            xmlParse(inputFile, builder);
            log.debug("Builder after parsing: {}", builder);
        } catch (IOException | VTDException ex) {
            throw new InputProcessingException(String.format("Error while trying to parse input file %s", inputFile), ex);
        }
        return builder.build();
    }

    private void xmlParse(MetadataFile inputFile, MappingInput.MappingInputBuilder builder) throws IOException, VTDException {
        // prepare parser
        final VTDGen vg = new VTDGen();
        vg.parseFile(inputFile.getLocation().toString(), true);
        final VTDNav nav = vg.getNav();

        // Profile ID
        final String profileId = SchemaParsingUtil.extractProfileId(nav);
        builder.profileId(profileId);

        nav.toElement(VTDNav.ROOT);

        // Self link
        final AutoPilot mdSelfLink = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(mdSelfLink, null);
        mdSelfLink.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink");
        final String mdSelfLinkString = mdSelfLink.evalXPathToString();
        builder.selflink(mdSelfLinkString);

        // resources
        final List<MappingInput.Resource> resources = parseResources(nav);
        builder.resources(resources);
    }

    private ImmutableList<MappingInput.Resource> parseResources(final VTDNav nav) throws NavException, XPathParseException, XPathEvalException {
        final ImmutableList.Builder<MappingInput.Resource> resources = ImmutableList.<MappingInput.Resource>builder();

        AutoPilot resourceProxy = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceProxy, null);
        resourceProxy.selectXPath("/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy");

        AutoPilot resourceRef = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceRef, null);
        resourceRef.selectXPath("cmd:ResourceRef");

        AutoPilot resourceType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceType, null);
        resourceType.selectXPath("cmd:ResourceType");

        AutoPilot resourceMimeType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceMimeType, null);
        resourceMimeType.selectXPath("cmd:ResourceType/@mimetype");

        while (resourceProxy.evalXPath() != -1) {
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            String mimeType = resourceMimeType.evalXPathToString();

            if (!ref.equals("") && !type.equals("")) {
                // note that the mime type could be empty
                resources.add(new MappingInput.Resource(ref, type, mimeType));
            }

//            // TODO: resource hierarchy information 
//            if (resourceStructureGraph != null && type.toLowerCase().equals("metadata")) {
//                resourceStructureGraph.addEdge(ref, mdSelfLinkString);
//            }
        }
        return resources.build();
    }
}
