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
package eu.clarin.cmdi.vlo.api.processing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.api.parsing.SchemaParsingUtil;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import eu.clarin.cmdi.vlo.exception.InputProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Component
@Profile("default")
@Slf4j
public class MetadataFileProcessorImpl implements MetadataFileProcessor {

    @Override
    public VloRecord processMappingRequest(VloRecordMappingRequest request) throws InputProcessingException {
        log.info("Parsing input from request {}", request);
        final VloRecord.VloRecordBuilder builder = VloRecord.builder();

        try {
            builder.dataRoot(request.getDataRoot());
            builder.sourcePath(request.getFile());
            xmlParse(request.getXmlContent(), builder);
            log.trace("Mapping input builder state after parsing: {}", builder);
        } catch (IOException | VTDException ex) {
            throw new InputProcessingException(String.format("Error while trying to parse input file %s", request.getFile()), ex);
        }

        log.debug("Completed parsing input from request {}", request);
        return builder.build();
    }

    private void xmlParse(byte[] xmlContent, VloRecord.VloRecordBuilder builder) throws IOException, VTDException {
        // prepare parser
        final VTDGen vg = new VTDGen();
        vg.setDoc(xmlContent);
        vg.parse(true);
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

        //TODO: other header fields?
        // resources
        final List<VloRecord.Resource> resources = parseResources(nav);
        builder.resources(resources);

        // component section
        final Map<String, List<String>> pathValuesMap = buildPathsMap(nav, profileId);
        builder.pathValuesMap(pathValuesMap);
    }

    private ImmutableList<VloRecord.Resource> parseResources(final VTDNav nav) throws NavException, XPathParseException, XPathEvalException {
        final ImmutableList.Builder<VloRecord.Resource> resources = ImmutableList.<VloRecord.Resource>builder();

        final AutoPilot resourceProxy = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceProxy, null);
        resourceProxy.selectXPath("/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy");

        final AutoPilot idAp = new AutoPilot(nav);
        idAp.selectXPath("@id");
//        final String id;
//        final int idIdx = nav.getAttrVal("id");
//        if (idIdx == -1) {
//            id = null;
//        } else {
//            id = nav.toNormalizedString(idIdx);
//        }

        final AutoPilot resourceRef = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceRef, null);
        resourceRef.selectXPath("cmd:ResourceRef");

        final AutoPilot resourceType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceType, null);
        resourceType.selectXPath("cmd:ResourceType");

        final AutoPilot resourceMimeType = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(resourceMimeType, null);
        resourceMimeType.selectXPath("cmd:ResourceType/@mimetype");

        while (resourceProxy.evalXPath() != -1) {
            String id = idAp.evalXPathToString();
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            String mimeType = resourceMimeType.evalXPathToString();

            if (!ref.equals("") && !type.equals("")) {
                // note that the mime type could be empty
                resources.add(new VloRecord.Resource(id, ref, type, mimeType));
            }

//            // TODO: resource hierarchy information 
//            if (resourceStructureGraph != null && type.toLowerCase().equals("metadata")) {
//                resourceStructureGraph.addEdge(ref, mdSelfLinkString);
//            }
        }
        return resources.build();
    }

    private Map<String, List<String>> buildPathsMap(VTDNav nav, String profileId) throws VTDException {
        final Map<String, ImmutableList.Builder<String>> builderMap = Maps.newHashMap();

        nav.toElement(VTDNav.ROOT);
        // traverse recursively from root
        traverseElement(nav, builderMap, new StringBuilder());

        // build all value lists
        return builderMap.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        e -> e.getKey(),
                        e -> e.getValue().build()));
    }

    private void traverseElement(VTDNav nav, final Map<String, ImmutableList.Builder<String>> builderMap, CharSequence parentPath) throws NavException {
        final int currentIndex = nav.getCurrentIndex();
        final String elementName = nav.toNormalizedString(currentIndex);
        //TODO: extract and normalise namespace
        final StringBuilder path = new StringBuilder(parentPath).append("/").append(elementName);

        final int textIndex = nav.getText();
        if (textIndex != -1) {
            final String text = nav.toNormalizedString(textIndex);
            log.trace("Current path: {}='{}'", path, text);
            listBuilderForPath(builderMap, path).add(text);
        } else {
            log.trace("Current path: {}", path);
        }

        //TODO: attributes
        if (nav.toElement(VTDNav.FIRST_CHILD)) {
            do {
                traverseElement(nav, builderMap, path);
            } while (nav.toElement(VTDNav.NEXT_SIBLING));

            nav.toElement(VTDNav.PARENT);
        }

    }

    private ImmutableList.Builder<String> listBuilderForPath(final Map<String, ImmutableList.Builder<String>> builderMap, final CharSequence path) {
        return builderMap.computeIfAbsent(path.toString(), p -> ImmutableList.<String>builder());
    }
}
